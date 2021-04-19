package com.stylefeng.guns.gateway.modular.cinema.vo;

import com.stylefeng.guns.api.cinema.vo.AreaVo;
import com.stylefeng.guns.api.cinema.vo.BrandVo;
import com.stylefeng.guns.api.cinema.vo.HallTypeVo;
import lombok.Data;

import java.util.List;

@Data
public class CinemaConditionResponseVo {

    private List<BrandVo> brandList;
    private List<AreaVo> areaList;
    private List<HallTypeVo> hallTypeList;

}
