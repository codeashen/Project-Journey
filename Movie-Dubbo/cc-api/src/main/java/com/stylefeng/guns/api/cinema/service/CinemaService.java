package com.stylefeng.guns.api.cinema.service;

import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.cinema.vo.*;

import java.util.List;

public interface CinemaService {
    
    //1、根据CinemaQueryVo，查询影院列表
    Page<CinemaVo> getCinemas(CinemaQueryVo cinemaQueryVo);
    
    //2、根据条件获取品牌列表，并设置isActive状态
    List<BrandVo> getBrands(int brandId);
    
    //3、获取行政区域列表
    List<AreaVo> getAreas(int areaId);
    
    //4、获取影厅类型列表
    List<HallTypeVo> getHallTypes(int hallType);
    
    //5、根据影院编号，获取影院信息
    CinemaInfoVo getCinemaInfoById(int cinemaId);
    
    //6、获取所有电影的信息和对应的放映场次信息，根据影院编号
    List<FilmInfoVo> getFilmInfoByCinemaId(int cinemaId);
    
    //7、根据放映场次ID获取放映信息
    HallInfoVo getFilmFieldInfo(int fieldId);
    
    //8、根据放映场次查询播放的电影编号，然后根据电影编号获取对应的电影信息
    FilmInfoVo getFilmInfoByFieldId(int fieldId);

    //9、获取放映场次的价格
    FieldPriceVo getPriceByFieldId(Integer fieldId);
}
