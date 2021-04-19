package com.ashen.xunwu.service.house.impl;

import com.ashen.xunwu.base.HouseSort;
import com.ashen.xunwu.base.HouseStatusEnum;
import com.ashen.xunwu.base.LoginUserUtil;
import com.ashen.xunwu.base.SubscribeStatusEnum;
import com.ashen.xunwu.entity.*;
import com.ashen.xunwu.repository.*;
import com.ashen.xunwu.service.ServiceMultiResult;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.service.house.IHouseService;
import com.ashen.xunwu.service.search.ISearchService;
import com.ashen.xunwu.web.dto.HouseDTO;
import com.ashen.xunwu.web.dto.HouseDetailDTO;
import com.ashen.xunwu.web.dto.HousePictureDTO;
import com.ashen.xunwu.web.dto.HouseSubscribeDTO;
import com.ashen.xunwu.web.form.*;
import com.google.common.collect.Maps;
import com.ashen.xunwu.entity.*;
import com.ashen.xunwu.repository.*;
import com.ashen.xunwu.service.house.IQiNiuService;
import com.ashen.xunwu.web.form.*;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 房屋服务实现
 */
@Service
public class HouseServiceImpl implements IHouseService {
    
    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private HouseDetailRepository houseDetailRepository;
    @Autowired
    private HousePictureRepository housePictureRepository;
    @Autowired
    private HouseTagRepository houseTagRepository;
    @Autowired
    private SubwayRepository subwayRepository;
    @Autowired
    private SubwayStationRepository subwayStationRepository;
    @Autowired
    private HouseSubscribeRepository subscribeRepository;

    @Autowired
    private ISearchService searchService;
    @Autowired
    private IQiNiuService qiNiuService;
    @Autowired
    private ModelMapper modelMapper;
    
    @Value("${qiniu.cdn.prefix}")
    private String cdnPrefix;

    /**
     * 保存房源信息
     * @param houseForm
     * @return
     */
    @Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        // 填充房屋详细信息
        HouseDetail detail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidationResult = wrapperDetailInfo(detail, houseForm);
        if (subwayValidationResult != null) {
            return subwayValidationResult;
        }

        // 表单对象转换成实体对象
        House house = new House();
        modelMapper.map(houseForm, house);
        
        // 保存房屋信息
        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        house = houseRepository.save(house);
        // 保存房屋详情
        detail.setHouseId(house.getId());
        detail = houseDetailRepository.save(detail);
        // 保存房屋图片
        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        Iterable<HousePicture> housePictures = housePictureRepository.saveAll(pictures);

        // 保存房屋标签
        List<String> tags = houseForm.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.saveAll(houseTags);
        }

        // 构建返回值
        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        houseDTO.setTags(tags);
        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());
        
        return ServiceResult.of(houseDTO);
    }

    /**
     * 后台查询房屋表格
     * @param searchBody
     * @return
     */
    @Override
    public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody) {
        List<HouseDTO> houseDTOS = new ArrayList<>();

        // 构建jpa排序对象
        Sort sort = Sort.by(Sort.Direction.fromString(searchBody.getDirection()), searchBody.getOrderBy());
        // 构建jpa分页对象
        int page = searchBody.getStart() / searchBody.getLength();
        Pageable pageable = PageRequest.of(page, searchBody.getLength(), sort);

        // 构建查询条件
        Specification<House> specification = (root, query, cb) -> {
            // admin_id = 当前登录人，即只查询本人添加的房源
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getLoginUserId());
            // status != 已删除
            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatusEnum.DELETED.getValue()));
            
            if (searchBody.getCity() != null) {             // cityEnName = 参数
                predicate = cb.and(predicate, cb.equal(root.get("cityEnName"), searchBody.getCity()));
            }
            if (searchBody.getStatus() != null) {           // status = 参数
                predicate = cb.and(predicate, cb.equal(root.get("status"), searchBody.getStatus()));
            }
            if (searchBody.getCreateTimeMin() != null) {    // createTime >= 参数
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMin()));
            }
            if (searchBody.getCreateTimeMax() != null) {    // createTime <= 参数
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMax()));
            }
            if (searchBody.getTitle() != null) {            // title like 样式
                predicate = cb.and(predicate, cb.like(root.get("title"), "%" + searchBody.getTitle() + "%"));
            }

            return predicate;
        };

        // 分页条件查询
        Page<House> houses = houseRepository.findAll(specification, pageable);
        
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
        });
        
        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

    /**
     * 查询完整房源信息
     * @param id
     * @return
     */
    @Override
    public ServiceResult<HouseDTO> findCompleteOne(Long id) {
        House house = houseRepository.findById(id).orElse(null);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail detail = houseDetailRepository.findByHouseId(id);
        List<HousePicture> pictures = housePictureRepository.findAllByHouseId(id);

        HouseDetailDTO detailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : pictures) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO);
        }
        
        List<HouseTag> tags = houseTagRepository.findAllByHouseId(id);
        List<String> tagList = new ArrayList<>();
        for (HouseTag tag : tags) {
            tagList.add(tag.getName());
        }

        HouseDTO result = modelMapper.map(house, HouseDTO.class);
        result.setHouseDetail(detailDTO);
        result.setPictures(pictureDTOS);
        result.setTags(tagList);

        if (LoginUserUtil.getLoginUserId() > 0) { // 已登录用户
            HouseSubscribe subscribe = subscribeRepository.findByHouseIdAndUserId(house.getId(), LoginUserUtil.getLoginUserId());
            if (subscribe != null) {
                result.setSubscribeStatus(subscribe.getStatus());
            }
        }

        return ServiceResult.of(result);
    }

    /**
     * 更新房源信息
     * @param houseForm
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<HouseDTO> update(HouseForm houseForm) {
        House house = this.houseRepository.findById(houseForm.getId()).orElse(null);
        if (house == null) {
            return ServiceResult.notFound();
        }
        
        // 更新详情
        HouseDetail detail = this.houseDetailRepository.findByHouseId(house.getId());
        if (detail == null) {
            return ServiceResult.notFound();
        }
        ServiceResult<HouseDTO> wrapperResult = wrapperDetailInfo(detail, houseForm);
        if (wrapperResult != null) {
            return wrapperResult;
        }
        houseDetailRepository.save(detail);

        // 更新图片
        List<HousePicture> pictures = generatePictures(houseForm, houseForm.getId());
        housePictureRepository.saveAll(pictures);

        // 更新基本信息
        if (houseForm.getCover() == null) {
            houseForm.setCover(house.getCover());
        }
        modelMapper.map(houseForm, house);
        house.setLastUpdateTime(new Date());
        houseRepository.save(house);

        // 更新ES文档
        if (house.getStatus() == HouseStatusEnum.PASSES.getValue()) {
            searchService.index(house.getId());
        }

        return ServiceResult.success();
    }

    /**
     * 移除图片
     * @param id
     * @return
     */
    @Override
    public ServiceResult<String> removePhoto(Long id) {
        HousePicture picture = housePictureRepository.findById(id).orElse(null);
        if (picture == null) {
            return ServiceResult.notFound();
        }

        try {
            Response response = this.qiNiuService.delete(picture.getPath());
            if (response.isOK()) {
                housePictureRepository.deleteById(id);
                return ServiceResult.success();
            } else {
                return new ServiceResult<>(false, response.error);
            }
        } catch (QiniuException e) {
            e.printStackTrace();
            return new ServiceResult<>(false, e.getMessage());
        }
    }

    /**
     * 更新封面
     * @param coverId
     * @param targetId
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> updateCover(Long coverId, Long targetId) {
        HousePicture cover = housePictureRepository.findById(coverId).orElse(null);
        if (cover == null) {
            return ServiceResult.notFound();
        }

        houseRepository.updateCover(targetId, cover.getPath());
        return ServiceResult.success();
    }

    /**
     * 加标签
     * @param houseId
     * @param tag
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> addTag(Long houseId, String tag) {
        House house = houseRepository.findById(houseId).orElse(null);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag != null) {
            return new ServiceResult<>(false, "标签已存在");
        }

        houseTagRepository.save(new HouseTag(houseId, tag));
        return ServiceResult.success();
    }

    /**
     * 移除标签
     * @param houseId
     * @param tag
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> removeTag(Long houseId, String tag) {
        House house = houseRepository.findById(houseId).orElse(null);
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseTag houseTag = houseTagRepository.findByNameAndHouseId(tag, houseId);
        if (houseTag == null) {
            return new ServiceResult<>(false, "标签不存在");
        }

        houseTagRepository.deleteById(houseTag.getId());
        return ServiceResult.success();
    }

    /**
     * 更新房源信息状态
     * @param id
     * @param status
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> updateStatus(Long id, int status) {
        House house = houseRepository.findById(id).orElse(null);
        
        if (house == null) {
            return ServiceResult.notFound();
        }
        if (house.getStatus() == status) {
            return new ServiceResult<>(false, "状态没有发生变化");
        }
        if (house.getStatus() == HouseStatusEnum.RENTED.getValue()) {
            return new ServiceResult<>(false, "已出租的房源不允许修改状态");
        }
        if (house.getStatus() == HouseStatusEnum.DELETED.getValue()) {
            return new ServiceResult<>(false, "已删除的资源不允许操作");
        }

        houseRepository.updateStatus(id, status);

        // 上架更新索引 其他情况都要删除索引
        if (status == HouseStatusEnum.PASSES.getValue()) {
            searchService.index(id);
        } else {
            searchService.remove(id);
        }
        
        return ServiceResult.success();
    }
    
    /**
     * 图片对象列表信息填充
     * @param form
     * @param houseId
     * @return
     */
    private List<HousePicture> generatePictures(HouseForm form, Long houseId) {
        List<HousePicture> pictures = new ArrayList<>();
        if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
            return pictures;
        }

        for (PhotoForm photoForm : form.getPhotos()) {
            HousePicture picture = new HousePicture();
            picture.setHouseId(houseId);
            picture.setCdnPrefix(cdnPrefix);
            picture.setPath(photoForm.getPath());
            picture.setWidth(photoForm.getWidth());
            picture.setHeight(photoForm.getHeight());
            pictures.add(picture);
        }
        return pictures;
    }

    /**
     * 填充房源详细信息
     * @param houseDetail
     * @param houseForm
     * @return
     */
    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm) {
        // 查询地铁信息
        Subway subway = subwayRepository.findById(houseForm.getSubwayLineId()).orElse(null);
        if (subway == null) {
            return new ServiceResult<>(false, "Not valid subway line!");
        }
        // 查询地铁站信息
        SubwayStation subwayStation = subwayStationRepository.findById(houseForm.getSubwayStationId()).orElse(null);
        if (subwayStation == null || !subway.getId().equals(subwayStation.getSubwayId())) {
            return new ServiceResult<>(false, "Not valid subway station!");
        }

        // 填充数据
        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());

        return null;
    }

    /**
     * 搜索房源列表
     * @param rentSearch
     * @return
     */
    @Override
    public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
        // 含有关键词和标签的复杂查询走ES
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
            ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
            if (serviceResult.getTotal() == 0) {
                return new ServiceMultiResult<>(0, new ArrayList<>());
            }

            return new ServiceMultiResult<>(serviceResult.getTotal(), wrapperHouseResult(serviceResult.getResult()));
        }
        
        // 简单查询走数据库
        return simpleQuery(rentSearch);
    }

    /**
     * 从数据库查询
     * @param rentSearch
     * @return
     */
    private ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch) {
        // 构建排序分页都对象
        Sort sort = Sort.by(Sort.Direction.fromString(rentSearch.getOrderDirection()), rentSearch.getOrderBy());
        int page = rentSearch.getStart() / rentSearch.getSize();
        Pageable pageable = PageRequest.of(page, rentSearch.getSize(), sort);
        // 构建筛选条件
        Specification<House> specification = (root, query, criteriaBuilder) -> {
            // 已经通过的状态
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatusEnum.PASSES.getValue());
            // 指定城市
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), rentSearch.getCityEnName()));
            // 排序
            if (HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY), -1));
            }
            return predicate;
        };

        // 查询结果
        Page<House> houses = houseRepository.findAll(specification, pageable);

        // 转化并填充数据
        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = Maps.newHashMap();

        List<HouseDTO> result = houses.stream().map(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            // 记录数据
            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
            return houseDTO;
        }).collect(Collectors.toList());

        wrapperHouseList(houseIds, idToHouseMap);

        return new ServiceMultiResult<>(houses.getTotalElements(), result);
    }

    /**
     * 根据houseId 批量查询
     * @param houseIds houseId列表
     * @return
     */
    private List<HouseDTO> wrapperHouseResult(List<Long> houseIds) {
        List<HouseDTO> result = new ArrayList<>();

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        Iterable<House> houses = houseRepository.findAllById(houseIds);
        
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            idToHouseMap.put(house.getId(), houseDTO);
        });

        wrapperHouseList(houseIds, idToHouseMap);

        // 矫正顺序
        for (Long houseId : houseIds) {
            result.add(idToHouseMap.get(houseId));
        }
        return result;
    }

    /**
     * 填充详细信息 及 标签
     * @param houseIds 待查询的房源id列表
     * @param idToHouseMap id-house Map
     */
    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailRepository.findAllByHouseIdIn(houseIds);
        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });

        List<HouseTag> houseTags = houseTagRepository.findAllByHouseIdIn(houseIds);
        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });
    }

    /**
     * 加入预约清单
     * @param houseId
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> addSubscribeOrder(Long houseId) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = subscribeRepository.findByHouseIdAndUserId(houseId, userId);
        if (subscribe != null) {
            return new ServiceResult<>(false, "已加入预约");
        }

        House house = houseRepository.findById(houseId).orElse(null);
        if (house == null) {
            return new ServiceResult<>(false, "查无此房");
        }

        subscribe = new HouseSubscribe();
        Date now = new Date();
        subscribe.setCreateTime(now);
        subscribe.setLastUpdateTime(now);
        subscribe.setUserId(userId);
        subscribe.setHouseId(houseId);
        subscribe.setStatus(SubscribeStatusEnum.IN_ORDER_LIST.getValue());
        subscribe.setAdminId(house.getAdminId());
        subscribeRepository.save(subscribe);
        return ServiceResult.success();
    }

    /**
     * 获取对应状态的预约列表
     * @param status
     * @param start
     * @param size
     * @return
     */
    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(
            SubscribeStatusEnum status, int start, int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        Pageable pageable = PageRequest.of(start / size, size, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<HouseSubscribe> page = subscribeRepository.findAllByUserIdAndStatus(userId, status.getValue(), pageable);

        return this.wrapper(page);
    }

    private ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> wrapper(Page<HouseSubscribe> page) {
        List<Pair<HouseDTO, HouseSubscribeDTO>> result = new ArrayList<>();

        if (page.getSize() < 1) {
            return new ServiceMultiResult<>(page.getTotalElements(), result);
        }

        List<HouseSubscribeDTO> subscribeDTOS = new ArrayList<>();
        List<Long> houseIds = new ArrayList<>();
        page.forEach(houseSubscribe -> {
            subscribeDTOS.add(modelMapper.map(houseSubscribe, HouseSubscribeDTO.class));
            houseIds.add(houseSubscribe.getHouseId());
        });

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        Iterable<House> houses = houseRepository.findAllById(houseIds);
        houses.forEach(house -> {
            idToHouseMap.put(house.getId(), modelMapper.map(house, HouseDTO.class));
        });

        for (HouseSubscribeDTO subscribeDTO : subscribeDTOS) {
            Pair<HouseDTO, HouseSubscribeDTO> pair = Pair.of(idToHouseMap.get(subscribeDTO.getHouseId()), subscribeDTO);
            result.add(pair);
        }

        return new ServiceMultiResult<>(page.getTotalElements(), result);
    }

    /**
     * 预约看房时间
     * @param houseId
     * @param orderTime
     * @param telephone
     * @param desc
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> subscribe(Long houseId, Date orderTime, String telephone, String desc) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = subscribeRepository.findByHouseIdAndUserId(houseId, userId);
        if (subscribe == null) {
            return new ServiceResult<>(false, "无预约记录");
        }

        if (subscribe.getStatus() != SubscribeStatusEnum.IN_ORDER_LIST.getValue()) {
            return new ServiceResult<>(false, "无法预约");
        }

        subscribe.setStatus(SubscribeStatusEnum.IN_ORDER_TIME.getValue());
        subscribe.setLastUpdateTime(new Date());
        subscribe.setTelephone(telephone);
        subscribe.setDesc(desc);
        subscribe.setOrderTime(orderTime);
        subscribeRepository.save(subscribe);
        return ServiceResult.success();
    }

    /**
     * 取消预约
     * @param houseId
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> cancelSubscribe(Long houseId) {
        Long userId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = subscribeRepository.findByHouseIdAndUserId(houseId, userId);
        if (subscribe == null) {
            return new ServiceResult<>(false, "无预约记录");
        }

        subscribeRepository.deleteById(subscribe.getId());
        return ServiceResult.success();
    }

    /**
     * 管理员查询预约信息接口
     * @param start
     * @param size
     * @return
     */
    @Override
    public ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> findSubscribeList(int start, int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        Pageable pageable = PageRequest.of(start / size, size, Sort.by(Sort.Direction.DESC, "orderTime"));

        Page<HouseSubscribe> page = subscribeRepository.findAllByAdminIdAndStatus(userId, SubscribeStatusEnum.IN_ORDER_TIME.getValue(), pageable);

        return this.wrapper(page);
    }

    /**
     * 完成预约
     * @param houseId
     * @return
     */
    @Override
    @Transactional
    public ServiceResult<String> finishSubscribe(Long houseId) {
        Long adminId = LoginUserUtil.getLoginUserId();
        HouseSubscribe subscribe = subscribeRepository.findByHouseIdAndAdminId(houseId, adminId);
        if (subscribe == null) {
            return new ServiceResult<>(false, "无预约记录");
        }

        subscribeRepository.updateStatus(subscribe.getId(), SubscribeStatusEnum.FINISH.getValue());
        houseRepository.updateWatchTimes(houseId);
        return ServiceResult.success();
    }

    /**
     * 全地图查询
     * @param mapSearch
     * @return
     */
    @Override
    public ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> serviceResult = searchService.mapQuery(mapSearch.getCityEnName(), mapSearch.getOrderBy(), mapSearch.getOrderDirection(), mapSearch.getStart(), mapSearch.getSize());

        if (serviceResult.getTotal() == 0) {
            return new ServiceMultiResult<>(0, new ArrayList<>());
        }
        List<HouseDTO> houses = wrapperHouseResult(serviceResult.getResult());
        return new ServiceMultiResult<>(serviceResult.getTotal(), houses);
    }

    /**
     * 全地图查询
     * @param mapSearch
     * @return
     */
    @Override
    public ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch) {
        ServiceMultiResult<Long> serviceResult = searchService.mapQuery(mapSearch);
        if (serviceResult.getTotal() == 0) {
            return new ServiceMultiResult<>(0, new ArrayList<>());
        }

        List<HouseDTO> houses = wrapperHouseResult(serviceResult.getResult());
        return new ServiceMultiResult<>(serviceResult.getTotal(), houses);
    }
}
