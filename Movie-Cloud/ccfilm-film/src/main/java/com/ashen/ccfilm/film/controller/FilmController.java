package com.ashen.ccfilm.film.controller;

import com.ashen.ccfilm.film.service.FilmService;
import com.ashen.ccfilm.film.vo.DescribeActorsRespVo;
import com.ashen.ccfilm.film.vo.DescribeFilmsRespVo;
import com.ashen.ccfilm.film.vo.FilmSavedReqVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Maps;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BasePageVo;
import com.ashen.ccfilm.common.vo.DataResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;

/**
 * 影片模块表现层
 */
@Slf4j
@RestController
@RequestMapping("/film")
public class FilmController {

    @Autowired
    private FilmService filmService;

    // 获取演员列表
    @RequestMapping(value = "/actors", method = RequestMethod.GET)
    public DataResult<Map<String, Object>> describeActors(BasePageVo basePageVo) throws CommonServiceException {
        // 检查入参
        basePageVo.checkParam();
        // 调用逻辑层，获取返回参数
        IPage<DescribeActorsRespVo> results = filmService.describeActors(basePageVo.getNowPage(), basePageVo.getPageSize());
        Map<String, Object> actors = descrbePageResult(results, "actors");
        return DataResult.success(actors);
    }

    // 获取电影列表
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public DataResult<Map<String, Object>> describeFilms(HttpServletRequest request, BasePageVo basePageVo) throws CommonServiceException {
        // Header信息都打印一下
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headName = headerNames.nextElement();
            log.error("describeFilms - headName:{}, headValue:{}", headName, request.getHeader(headName));
        }
        // 检查入参
        basePageVo.checkParam();
        // 调用逻辑层，获取返回参数
        IPage<DescribeFilmsRespVo> results = filmService.describeFilms(basePageVo.getNowPage(), basePageVo.getPageSize());
        Map<String, Object> films = descrbePageResult(results, "films");
        return DataResult.success(films);
    }


    // 根据电影主键获取电影信息
    @RequestMapping(value = "/{filmId}", method = RequestMethod.GET)
    public DataResult<DescribeFilmsRespVo> describeFilmById(@PathVariable("filmId") String filmId) throws CommonServiceException {
        DescribeFilmsRespVo describeFilmsRespVo = filmService.describeFilmById(filmId);
        if (describeFilmsRespVo == null) {
            return DataResult.success(null);
        } else {
            return DataResult.success(describeFilmsRespVo);
        }

    }

    // 根据电影编号获取电影信息
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public DataResult<String> saveFilmInfo(@RequestBody FilmSavedReqVo filmSavedReqVo) throws CommonServiceException {
        filmService.saveFilm(filmSavedReqVo);
        return DataResult.success();
    }

    // 获取分页对象的公共接口
    private Map<String, Object> descrbePageResult(IPage page, String title) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("totalSize", page.getTotal());
        result.put("totalPages", page.getPages());
        result.put("pageSize", page.getSize());
        result.put("nowPage", page.getCurrent());
        result.put(title, page.getRecords());
        return result;
    }

}
