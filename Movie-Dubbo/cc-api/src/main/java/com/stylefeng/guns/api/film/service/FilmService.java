package com.stylefeng.guns.api.film.service;

import com.stylefeng.guns.api.film.vo.*;

import java.util.List;

public interface FilmService {

    // 获取banners
    List<BannerVo> getBanners();

    // 获取热映影片
    FilmVo getHotFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId);

    // 获取即将上映影片[受欢迎程度做排序]
    FilmVo getSoonFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId);

    // 获取经典影片
    FilmVo getClassicFilms(int nums, int nowPage, int sortId, int sourceId, int yearId, int catId);

    // 获取票房排行榜
    List<FilmInfo> getBoxRanking();

    // 获取人气排行榜
    List<FilmInfo> getExpectRanking();

    // 获取Top100
    List<FilmInfo> getTop();

    // ==== 获取影片条件接口
    // 分类条件
    List<CatVo> getCats();

    // 片源条件
    List<SourceVo> getSources();

    // 获取年代条件
    List<YearVo> getYears();

    // 根据影片ID或者名称获取影片信息
    FilmDetailVo getFilmDetail(int searchType, String searchParam);

    // 获取影片描述信息
    FilmDescVo getFilmDesc(String filmId);

    // 获取图片信息
    ImgVo getImgs(String filmId);

    // 获取导演信息
    ActorVo getDectInfo(String filmId);

    // 获取演员信息
    List<ActorVo> getActors(String filmId);

}
