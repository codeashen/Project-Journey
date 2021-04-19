package com.stylefeng.guns.gateway.modular.film.vo;

import com.stylefeng.guns.api.film.vo.CatVo;
import com.stylefeng.guns.api.film.vo.SourceVo;
import com.stylefeng.guns.api.film.vo.YearVo;
import lombok.Data;

import java.util.List;

@Data
public class FilmConditionVo {

    private List<CatVo> catInfo;
    private List<SourceVo> sourceInfo;
    private List<YearVo> yearInfo;

}
