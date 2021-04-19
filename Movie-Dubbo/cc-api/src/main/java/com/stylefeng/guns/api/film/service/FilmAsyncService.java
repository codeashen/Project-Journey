package com.stylefeng.guns.api.film.service;

import com.stylefeng.guns.api.film.vo.*;

import java.util.List;

public interface FilmAsyncService {

    // 获取影片描述信息
    FilmDescVo getFilmDesc(String filmId);

    // 获取图片信息
    ImgVo getImgs(String filmId);

    // 获取导演信息
    ActorVo getDectInfo(String filmId);

    // 获取演员信息
    List<ActorVo> getActors(String filmId);

}
