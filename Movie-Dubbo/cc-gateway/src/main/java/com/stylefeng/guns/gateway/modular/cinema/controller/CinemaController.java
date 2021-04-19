package com.stylefeng.guns.gateway.modular.cinema.controller;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.service.CinemaService;
import com.stylefeng.guns.api.cinema.vo.*;
import com.stylefeng.guns.gateway.common.vo.DataResult;
import com.stylefeng.guns.gateway.modular.cinema.vo.CinemaConditionResponseVo;
import com.stylefeng.guns.gateway.modular.cinema.vo.CinemaFieldResponseVo;
import com.stylefeng.guns.gateway.modular.cinema.vo.CinemaFieldsResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cinema")
public class CinemaController {

    private static final String IMG_PRE = "http://img.meetingshop.cn/";

    /**
     * connections: 服务提供者最大连接数
     * cache: 启用dubbo的结果缓存，很少用，都用分布式缓存redis
     */
    @DubboReference(connections = 10, cache = "lru", check = false)
    private CinemaService cinemaService;

    /**
     * 条件查询影院信息
     * @param cinemaQueryVo
     * @return
     */
    @RequestMapping(value = "getCinemas")
    public DataResult<List<CinemaVo>> getCinemas(CinemaQueryVo cinemaQueryVo) {
        try {
            // 按照五个条件进行筛选
            Page<CinemaVo> cinemas = cinemaService.getCinemas(cinemaQueryVo);
            // 判断是否有满足条件的影院
            if (cinemas.getRecords() == null || cinemas.getRecords().size() == 0) {
                return DataResult.success("没有影院可查");
            } else {
                return DataResult.success(cinemas.getCurrent(), (int) cinemas.getPages(), "", cinemas.getRecords());
            }
        } catch (Exception e) {
            // 如果出现异常，应该如何处理
            log.error("获取影院列表异常", e);
            return DataResult.serviceFail("查询影院列表失败");
        }
    }

    /**
     * 获取影院各级分类信息
     * 热点数据，可考虑放缓存
     * @param cinemaQueryVo
     * @return
     */
    @RequestMapping(value = "getCondition")
    public DataResult<CinemaConditionResponseVo> getCondition(CinemaQueryVo cinemaQueryVo) {
        try {
            // 获取三个集合，然后封装成一个对象返回即可
            List<BrandVo> brands = cinemaService.getBrands(cinemaQueryVo.getBrandId());
            List<AreaVo> areas = cinemaService.getAreas(cinemaQueryVo.getDistrictId());
            List<HallTypeVo> hallTypes = cinemaService.getHallTypes(cinemaQueryVo.getHallType());

            CinemaConditionResponseVo cinemaConditionResponseVo = new CinemaConditionResponseVo();
            cinemaConditionResponseVo.setAreaList(areas);
            cinemaConditionResponseVo.setBrandList(brands);
            cinemaConditionResponseVo.setHallTypeList(hallTypes);

            return DataResult.success(cinemaConditionResponseVo);
        } catch (Exception e) {
            log.error("获取条件列表失败", e);
            return DataResult.serviceFail("获取影院查询条件失败");
        }
    }

    /**
     * 获取影片播放场次
     * @param cinemaId 影片id
     * @return
     */
    @RequestMapping(value = "getFields")
    public DataResult<CinemaFieldsResponseVo> getFields(Integer cinemaId) {
        try {
            CinemaInfoVo cinemaInfoById = cinemaService.getCinemaInfoById(cinemaId);
            List<FilmInfoVo> filmInfoByCinemaId = cinemaService.getFilmInfoByCinemaId(cinemaId);

            CinemaFieldsResponseVo cinemaFieldResponseVo = new CinemaFieldsResponseVo();
            cinemaFieldResponseVo.setCinemaInfo(cinemaInfoById);
            cinemaFieldResponseVo.setFilmList(filmInfoByCinemaId);

            return DataResult.success(IMG_PRE, cinemaFieldResponseVo);
        } catch (Exception e) {
            log.error("获取播放场次失败", e);
            return DataResult.serviceFail("获取播放场次失败");
        }
    }

    /**
     * 获取播放场次详细信息
     * @param cinemaId 影片id
     * @param fieldId  场次id
     * @return
     */
    @RequestMapping(value = "getFieldInfo", method = RequestMethod.POST)
    public DataResult<CinemaFieldResponseVo> getFieldInfo(Integer cinemaId, Integer fieldId) {
        try {
            CinemaInfoVo cinemaInfoById = cinemaService.getCinemaInfoById(cinemaId);
            FilmInfoVo filmInfoByFieldId = cinemaService.getFilmInfoByFieldId(fieldId);
            HallInfoVo filmFieldInfo = cinemaService.getFilmFieldInfo(fieldId);

            // 造几个销售的假数据，后续会对接订单接口
            filmFieldInfo.setSoldSeats("1,2,3");

            CinemaFieldResponseVo cinemaFieldResponseVo = new CinemaFieldResponseVo();
            cinemaFieldResponseVo.setCinemaInfo(cinemaInfoById);
            cinemaFieldResponseVo.setFilmInfo(filmInfoByFieldId);
            cinemaFieldResponseVo.setHallInfo(filmFieldInfo);

            return DataResult.success(IMG_PRE, cinemaFieldResponseVo);
        } catch (Exception e) {
            log.error("获取选座信息失败", e);
            return DataResult.serviceFail("获取选座信息失败");
        }
    }
}
