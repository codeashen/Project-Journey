package com.ashen.ccfilm.film.facade;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BasePageVo;
import com.ashen.ccfilm.common.vo.DataResult;
import com.ashen.ccfilm.film.vo.DescribeFilmsRespVo;
import com.ashen.ccfilm.film.vo.FilmSavedReqVo;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 电影模块Facade接口，用于FeignClient
 */
@RequestMapping("/film")
public interface FilmFacade {

    // 获取演员列表
    @RequestMapping(value = "/actors", method = RequestMethod.GET)
    DataResult<Map<String, Object>> describeActors(BasePageVo basePageVo) throws CommonServiceException;

    // 获取电影列表
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    DataResult<Map<String, Object>> describeFilms(HttpServletRequest request, @RequestParam(name = "basePageVo", required = false) BasePageVo basePageVo) throws CommonServiceException;


    // 根据电影主键获取电影信息
    @RequestMapping(value = "/{filmId}", method = RequestMethod.GET)
    DataResult<DescribeFilmsRespVo> describeFilmById(@PathVariable("filmId") String filmId) throws CommonServiceException;

    // 根据电影编号获取电影信息
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    DataResult<String> saveFilmInfo(@RequestBody FilmSavedReqVo filmSavedReqVo) throws CommonServiceException;
}