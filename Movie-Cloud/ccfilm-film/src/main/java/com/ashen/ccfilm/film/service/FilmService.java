package com.ashen.ccfilm.film.service;

import com.ashen.ccfilm.film.vo.DescribeFilmsRespVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.film.vo.DescribeActorsRespVo;
import com.ashen.ccfilm.film.vo.FilmSavedReqVo;


public interface FilmService {
    // 获取演员列表
    IPage<DescribeActorsRespVo> describeActors(int nowPage, int pageSize) throws CommonServiceException;

    // 获取电影列表
    IPage<DescribeFilmsRespVo> describeFilms(int nowPage, int pageSize) throws CommonServiceException;

    // 根据主键获取电影信息
    DescribeFilmsRespVo describeFilmById(String filmId) throws CommonServiceException;

    // 保存电影信息
    void saveFilm(FilmSavedReqVo filmSavedReqVo) throws CommonServiceException;
}
