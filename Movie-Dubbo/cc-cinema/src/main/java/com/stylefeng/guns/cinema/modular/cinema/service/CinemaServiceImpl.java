package com.stylefeng.guns.cinema.modular.cinema.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.service.CinemaService;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.cinema.modular.cinema.dao.*;
import com.stylefeng.guns.cinema.modular.cinema.model.*;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@DubboService(executes = 10)   // dubbo服务最大并发执行数
public class CinemaServiceImpl implements CinemaService {

    @Autowired
    private CcCinemaMapper ccCinemaMapper;
    @Autowired
    private CcAreaDictMapper ccAreaDictMapper;
    @Autowired
    private CcBrandDictMapper ccBrandDictMapper;
    @Autowired
    private CcHallDictMapper ccHallDictMapper;
    @Autowired
    private CcHallFilmInfoMapper ccHallFilmInfoMapper;
    @Autowired
    private CcFieldMapper ccFieldMapper;


    //1、根据CinemaQueryVo，查询影院列表
    @Override
    public Page<CinemaVo> getCinemas(CinemaQueryVo cinemaQueryVo) {
        // 业务实体集合
        List<CinemaVo> cinemas = new ArrayList<>();

        Page<CcCinema> page = new Page<>(cinemaQueryVo.getNowPage(), cinemaQueryVo.getPageSize());
        // 判断是否传入查询条件 -> brandId,distId,hallType 是否==99
        EntityWrapper<CcCinema> entityWrapper = new EntityWrapper<>();
        if (cinemaQueryVo.getBrandId() != 99) {
            entityWrapper.eq("brand_id", cinemaQueryVo.getBrandId());
        }
        if (cinemaQueryVo.getDistrictId() != 99) {
            entityWrapper.eq("area_id", cinemaQueryVo.getDistrictId());
        }
        if (cinemaQueryVo.getHallType() != 99) {  // %#3#%
            entityWrapper.like("hall_ids", "%#+" + cinemaQueryVo.getHallType() + "+#%");
        }

        // 将数据实体转换为业务实体
        List<CcCinema> ccCinemaList = ccCinemaMapper.selectPage(page, entityWrapper);
        for (CcCinema ccCinema : ccCinemaList) {
            CinemaVo cinemaVo = new CinemaVo();
            cinemaVo.setUuid(ccCinema.getUuid() + "");
            cinemaVo.setMinimumPrice(ccCinema.getMinimumPrice() + "");
            cinemaVo.setCinemaName(ccCinema.getCinemaName());
            cinemaVo.setAddress(ccCinema.getCinemaAddress());
            cinemas.add(cinemaVo);
        }

        // 根据条件，判断影院列表总数
        long counts = ccCinemaMapper.selectCount(entityWrapper);

        // 组织返回值对象
        Page<CinemaVo> result = new Page<>();
        result.setRecords(cinemas);
        result.setSize(cinemaQueryVo.getPageSize());
        result.setTotal(counts);

        return result;
    }

    //2、根据条件获取品牌列表，并设置isActive状态
    @Override
    public List<BrandVo> getBrands(int brandId) {
        // 查询所有列表cc, 转换为BrandVo列表
        List<CcBrandDict> ccBrandDictList = ccBrandDictMapper.selectList(null);
        List<BrandVo> brandVoList = ccBrandDictList.stream().map(e -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(e.getUuid() + "");
            brandVo.setBrandName(e.getShowName());
            return brandVo;
        }).collect(Collectors.toList());

        // 判断brandId是否存在, 设置活跃状态
        CcBrandDict ccBrandDict = ccBrandDictMapper.selectById(brandId);
        if (ccBrandDict != null) {
            brandVoList.stream().filter(e -> e.getBrandId().equals(String.valueOf(brandId))).findFirst().ifPresent(e -> e.setActive(true));
        } else {
            brandVoList.stream().filter(e -> "99".equals(e.getBrandId())).findFirst().ifPresent(e -> e.setActive(true));
        }

        return brandVoList;
    }

    //3、获取行政区域列表
    @Override
    public List<AreaVo> getAreas(int areaId) {
        // 查询所有列表, 转换为AreaVo列表
        List<CcAreaDict> ccAreaDictList = ccAreaDictMapper.selectList(null);
        List<AreaVo> areaVoList = ccAreaDictList.stream().map(e -> {
            AreaVo areaVo = new AreaVo();
            areaVo.setAreaName(e.getShowName());
            areaVo.setAreaId(e.getUuid() + "");
            return areaVo;
        }).collect(Collectors.toList());

        // 判断areaId是否存在, 设置活跃状态
        CcAreaDict ccAreaDict = ccAreaDictMapper.selectById(areaId);
        if (ccAreaDict != null) {
            areaVoList.stream().filter(e -> e.getAreaId().equals(String.valueOf(areaId))).findFirst().ifPresent(e -> e.setActive(true));
        } else {
            areaVoList.stream().filter(e -> "99".equals(e.getAreaId())).findFirst().ifPresent(e -> e.setActive(true));
        }

        return areaVoList;
    }

    //4、获取影厅类型列表
    @Override
    public List<HallTypeVo> getHallTypes(int hallType) {
        // 查询所有列表, 转换为AreaVo列表
        List<CcHallDict> ccHallDictList = ccHallDictMapper.selectList(null);
        List<HallTypeVo> hallTypeVoList = ccHallDictList.stream().map(e -> {
            HallTypeVo hallTypeVo = new HallTypeVo();
            hallTypeVo.setHalltypeName(e.getShowName());
            hallTypeVo.setHalltypeId(e.getUuid() + "");
            return hallTypeVo;
        }).collect(Collectors.toList());

        // 判断areaId是否存在, 设置活跃状态
        CcHallDict ccHallDict = ccHallDictMapper.selectById(hallType);
        if (ccHallDict != null) {
            hallTypeVoList.stream().filter(e -> e.getHalltypeId().equals(String.valueOf(hallType))).findFirst().ifPresent(e -> e.setActive(true));
        } else {
            hallTypeVoList.stream().filter(e -> "99".equals(e.getHalltypeId())).findFirst().ifPresent(e -> e.setActive(true));
        }

        return hallTypeVoList;
    }

    //5、根据影院编号，获取影院信息
    @Override
    public CinemaInfoVo getCinemaInfoById(int cinemaId) {
        CcCinema ccCinema = ccCinemaMapper.selectById(cinemaId);
        CinemaInfoVo cinemaInfoVo = new CinemaInfoVo();
        if (ccCinema != null) {
            cinemaInfoVo.setImgUrl(ccCinema.getImgAddress());
            cinemaInfoVo.setCinemaPhone(ccCinema.getCinemaPhone());
            cinemaInfoVo.setCinemaName(ccCinema.getCinemaName());
            cinemaInfoVo.setCinemaId(ccCinema.getUuid() + "");
            cinemaInfoVo.setCinemaId(ccCinema.getCinemaAddress());
        }
        return cinemaInfoVo;
    }

    //6、获取所有电影的信息和对应的放映场次信息，根据影院编号
    @Override
    public List<FilmInfoVo> getFilmInfoByCinemaId(int cinemaId) {
        return ccFieldMapper.getFilmInfos(cinemaId);
    }

    //7、根据放映场次ID获取放映信息
    @Override
    public HallInfoVo getFilmFieldInfo(int fieldId) {
        return ccFieldMapper.getHallInfo(fieldId);
    }

    //8、根据放映场次查询播放的电影编号，然后根据电影编号获取对应的电影信息
    @Override
    public FilmInfoVo getFilmInfoByFieldId(int fieldId) {
        return ccFieldMapper.getFilmInfoById(fieldId);
    }

    //9、获取放映场次的价格
    @Override
    public FieldPriceVo getPriceByFieldId(Integer fieldId) {
        CcField ccField = ccFieldMapper.selectById(fieldId);
        
        FieldPriceVo fieldPriceVo = new FieldPriceVo();
        fieldPriceVo.setFieldId(fieldId);
        fieldPriceVo.setCinemaId(ccField.getCinemaId() + "");
        fieldPriceVo.setFilmPrice(ccField.getPrice() + "");

        return fieldPriceVo;
    }
}
