package com.stylefeng.guns.gateway.modular.cinema.vo;

import com.stylefeng.guns.api.cinema.vo.CinemaInfoVo;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVo;
import lombok.Data;

import java.util.List;

@Data
public class CinemaFieldsResponseVo {

    private CinemaInfoVo cinemaInfo;
    private List<FilmInfoVo> filmList;

}
