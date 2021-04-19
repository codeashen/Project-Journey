package com.stylefeng.guns.gateway.modular.film.controller;

import com.stylefeng.guns.api.film.service.FilmAsyncService;
import com.stylefeng.guns.api.film.service.FilmService;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.gateway.common.vo.DataResult;
import com.stylefeng.guns.gateway.modular.film.vo.FilmConditionVo;
import com.stylefeng.guns.gateway.modular.film.vo.FilmIndexVo;
import com.stylefeng.guns.gateway.modular.film.vo.FilmRequestVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/film")
public class FilmController {

    private static final String img_pre = "http://img.meetingshop.cn/";

    @DubboReference(check = false)
    private FilmService filmService;

    /**
     * 启用Dubbo异步调用，整个Service中的方法都可以异步调用，注解不支持方法级别
     */
    @DubboReference(async = true, check = false)
    private FilmAsyncService filmAsyncService;
    
    /**
     * 获取首页信息接口
     * 
     * API网关：功能聚合【API聚合】
     * 好处：
     * 1、六个接口，一次请求，同一时刻节省了五次HTTP请求
     * 2、同一个接口对外暴漏，降低了前后端分离开发的难度和复杂度
     * 坏处：
     * 1、一次获取数据过多，容易出现问题
     *
     * @return
     */
    @RequestMapping(value = "getIndex", method = RequestMethod.GET)
    public DataResult<FilmIndexVo> getIndex() {
        FilmIndexVo filmIndexVo = new FilmIndexVo();
        // 获取banner信息
        filmIndexVo.setBanners(filmService.getBanners());
        // 获取正在热映的电影
        filmIndexVo.setHotFilms(filmService.getHotFilms(true, 8, 1, 1, 99, 99, 99));
        // 即将上映的电影
        filmIndexVo.setSoonFilms(filmService.getSoonFilms(true, 8, 1, 1, 99, 99, 99));
        // 票房排行榜
        filmIndexVo.setBoxRanking(filmService.getBoxRanking());
        // 获取受欢迎的榜单
        filmIndexVo.setExpectRanking(filmService.getExpectRanking());
        // 获取前一百
        filmIndexVo.setTop100(filmService.getTop());

        return DataResult.success(filmIndexVo);
    }


    @RequestMapping(value = "getConditionList", method = RequestMethod.GET)
    public DataResult<FilmConditionVo> getConditionList(
            @RequestParam(name = "catId", required = false, defaultValue = "99") String catId,
            @RequestParam(name = "sourceId", required = false, defaultValue = "99") String sourceId,
            @RequestParam(name = "yearId", required = false, defaultValue = "99") String yearId) {

        FilmConditionVo filmConditionVo = new FilmConditionVo();

        // 类型集合
        List<CatVo> cats = filmService.getCats();
        if (cats.stream().anyMatch(p -> p.getCatId().equals(catId))) {
            cats.stream().filter(e -> e.getCatId().equals(catId)).findFirst().ifPresent(d -> d.setActive(true));
        } else {
            cats.stream().filter(e -> e.getCatId().equals("99")).findFirst().ifPresent(d -> d.setActive(true));
        }

        // 片源集合
        List<SourceVo> sources = filmService.getSources();
        if (sources.stream().anyMatch(p -> p.getSourceId().equals(sourceId))) {
            sources.stream().filter(e -> e.getSourceId().equals(sourceId)).findFirst().ifPresent(d -> d.setActive(true));
        } else {
            sources.stream().filter(e -> e.getSourceId().equals("99")).findFirst().ifPresent(d -> d.setActive(true));
        }

        // 年代集合
        List<YearVo> years = filmService.getYears();
        if (years.stream().anyMatch(p -> p.getYearId().equals(sourceId))) {
            years.stream().filter(e -> e.getYearId().equals(sourceId)).findFirst().ifPresent(d -> d.setActive(true));
        } else {
            years.stream().filter(e -> e.getYearId().equals("99")).findFirst().ifPresent(d -> d.setActive(true));
        }

        filmConditionVo.setCatInfo(cats);
        filmConditionVo.setSourceInfo(sources);
        filmConditionVo.setYearInfo(years);
        return DataResult.success(filmConditionVo);
    }


    @RequestMapping(value = "getFilms", method = RequestMethod.GET)
    public DataResult<List<FilmInfo>> getFilms(FilmRequestVo filmRequestVo) {

        FilmVo filmVo = null;
        // 根据showType判断影片查询类型
        switch (filmRequestVo.getShowType()) {
            case 2:
                filmVo = filmService.getSoonFilms(
                        false, filmRequestVo.getPageSize(), filmRequestVo.getNowPage(),
                        filmRequestVo.getSortId(), filmRequestVo.getSourceId(), filmRequestVo.getYearId(),
                        filmRequestVo.getCatId());
                break;
            case 3:
                filmVo = filmService.getClassicFilms(
                        filmRequestVo.getPageSize(), filmRequestVo.getNowPage(),
                        filmRequestVo.getSortId(), filmRequestVo.getSourceId(),
                        filmRequestVo.getYearId(), filmRequestVo.getCatId());
                break;
            default:
                filmVo = filmService.getHotFilms(
                        false, filmRequestVo.getPageSize(), filmRequestVo.getNowPage(),
                        filmRequestVo.getSortId(), filmRequestVo.getSourceId(), filmRequestVo.getYearId(),
                        filmRequestVo.getCatId());
                break;
        }

        return DataResult.success(filmVo.getNowPage(), filmVo.getTotalPage(), img_pre, filmVo.getFilmInfo());
    }


    @RequestMapping(value = "films/{searchParam}", method = RequestMethod.GET)
    public DataResult<FilmDetailVo> films(@PathVariable("searchParam") String searchParam,
                                          int searchType) throws ExecutionException, InterruptedException {

        // 根据searchType，判断查询类型
        FilmDetailVo filmDetail = filmService.getFilmDetail(searchType, searchParam);

        if (filmDetail == null) {
            return DataResult.serviceFail("没有可查询的影片");
        } else if (filmDetail.getFilmId() == null || filmDetail.getFilmId().trim().length() == 0) {
            return DataResult.serviceFail("没有可查询的影片");
        }
        
        // FilmDescVo filmDescVo = filmAsyncService.getFilmDesc(filmId);  // 同步调用方式
        
        // 【Dubbo的异步调用】
        // 获取影片描述信息
        filmAsyncService.getFilmDesc(filmDetail.getFilmId());
        Future<FilmDescVo> filmDescVoFuture = RpcContext.getContext().getFuture();
        // 获取图片信息
        filmAsyncService.getImgs(filmDetail.getFilmId());
        Future<ImgVo> imgVoFuture = RpcContext.getContext().getFuture();
        // 获取导演信息
        filmAsyncService.getDectInfo(filmDetail.getFilmId());
        Future<ActorVo> actorVoFuture = RpcContext.getContext().getFuture();
        // 获取演员信息
        filmAsyncService.getActors(filmDetail.getFilmId());
        Future<List<ActorVo>> actorsVoFuture = RpcContext.getContext().getFuture();

        // 组织info对象
        InfoRequstVo infoRequstVo = new InfoRequstVo();
        // 组织Actor属性
        ActorRequestVo actorRequestVo = new ActorRequestVo();
        actorRequestVo.setActors(actorsVoFuture.get());
        actorRequestVo.setDirector(actorVoFuture.get());
        // 组织info对象
        infoRequstVo.setActors(actorRequestVo);
        infoRequstVo.setBiography(filmDescVoFuture.get() != null ? filmDescVoFuture.get().getBiography() : null);
        infoRequstVo.setFilmId(filmDetail.getFilmId());
        infoRequstVo.setImgVo(imgVoFuture.get());
        // 组织成返回值
        filmDetail.setInfo04(infoRequstVo);

        return DataResult.success(img_pre, filmDetail);
    }

}
