package com.stylefeng.guns.gateway.modular.cinema.vo;

import com.stylefeng.guns.api.cinema.vo.CinemaInfoVo;
import com.stylefeng.guns.api.cinema.vo.FilmInfoVo;
import com.stylefeng.guns.api.cinema.vo.HallInfoVo;
import lombok.Data;

@Data
public class CinemaFieldResponseVo {

    private CinemaInfoVo cinemaInfo;
    private FilmInfoVo filmInfo;
    private HallInfoVo hallInfo;

}
