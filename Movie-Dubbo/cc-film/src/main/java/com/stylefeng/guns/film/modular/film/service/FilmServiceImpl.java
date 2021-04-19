package com.stylefeng.guns.film.modular.film.service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.stylefeng.guns.api.film.service.FilmService;
import com.stylefeng.guns.api.film.vo.*;
import com.stylefeng.guns.core.util.DateUtil;
import com.stylefeng.guns.film.modular.film.dao.*;
import com.stylefeng.guns.film.modular.film.model.*;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@DubboService
public class FilmServiceImpl implements FilmService {

    @Autowired
    private CcBannerMapper ccBannerMapper;

    @Autowired
    private CcFilmMapper ccFilmMapper;

    @Autowired
    private CcCatDictMapper ccCatDictMapper;

    @Autowired
    private CcYearDictMapper ccYearDictMapper;

    @Autowired
    private CcSourceDictMapper ccSourceDictMapper;

    @Autowired
    private CcFilmInfoMapper ccFilmInfoMapper;

    @Autowired
    private CcActorMapper ccActorMapper;

    @Override
    public List<BannerVo> getBanners() {
        List<BannerVo> result = new ArrayList<>();
        List<CcBanner> ccBanners = ccBannerMapper.selectList(null);

        for (CcBanner ccBanner : ccBanners) {
            BannerVo bannerVo = new BannerVo();
            bannerVo.setBannerId(ccBanner.getUuid() + "");
            bannerVo.setBannerUrl(ccBanner.getBannerUrl());
            bannerVo.setBannerAddress(ccBanner.getBannerAddress());
            result.add(bannerVo);
        }

        return result;
    }

    private List<FilmInfo> getFilmInfos(List<CcFilm> ccFilms) {
        List<FilmInfo> filmInfos = new ArrayList<>();
        for (CcFilm ccFilm : ccFilms) {
            FilmInfo filmInfo = new FilmInfo();
            filmInfo.setScore(ccFilm.getFilmScore());
            filmInfo.setImgAddress(ccFilm.getImgAddress());
            filmInfo.setFilmType(ccFilm.getFilmType());
            filmInfo.setFilmScore(ccFilm.getFilmScore());
            filmInfo.setFilmName(ccFilm.getFilmName());
            filmInfo.setFilmId(ccFilm.getUuid() + "");
            filmInfo.setExpectNum(ccFilm.getFilmPresalenum());
            filmInfo.setBoxNum(ccFilm.getFilmBoxOffice());
            filmInfo.setShowTime(DateUtil.getDay(ccFilm.getFilmime()));

            // 将转换的对象放入结果集
            filmInfos.add(filmInfo);
        }

        return filmInfos;
    }

    @Override
    public FilmVo getHotFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVo filmVo = new FilmVo();
        List<FilmInfo> filmInfos = new ArrayList<>();

        // 热映影片的限制条件
        EntityWrapper<CcFilm> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");
        // 判断是否是首页需要的内容
        if (isLimit) {
            // 如果是，则限制条数、限制内容为热映影片
            Page<CcFilm> page = new Page<>(1, nums);
            List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);
            // 组织filmInfos
            filmInfos = getFilmInfos(ccFilms);
            filmVo.setFilmNum(ccFilms.size());
            filmVo.setFilmInfo(filmInfos);
        } else {
            // 如果不是，则是列表页，同样需要限制内容为热映影片
            Page<CcFilm> page = null;
            // 根据sortId的不同，来组织不同的Page对象
            // 1-按热门搜索，2-按时间搜索，3-按评价搜索
            switch (sortId) {
                case 1:
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
                case 2:
                    page = new Page<>(nowPage, nums, "film_time");
                    break;
                case 3:
                    page = new Page<>(nowPage, nums, "film_score");
                    break;
                default:
                    page = new Page<>(nowPage, nums, "film_box_office");
                    break;
            }

            // 如果sourceId,yearId,catId 不为99 ,则表示要按照对应的编号进行查询
            if (sourceId != 99) {
                entityWrapper.eq("film_source", sourceId);
            }
            if (yearId != 99) {
                entityWrapper.eq("film_date", yearId);
            }
            if (catId != 99) {
                // 数据库字段值形式: #2#4#22#
                String catStr = "%#" + catId + "#%";
                entityWrapper.like("film_cats", catStr);
            }

            List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);
            // 组织filmInfos
            filmInfos = getFilmInfos(ccFilms);
            filmVo.setFilmNum(ccFilms.size());
            
            int totalCounts = ccFilmMapper.selectCount(entityWrapper);
            int totalPages = (totalCounts / nums) + (totalCounts % nums == 0 ? 0 : 1);

            filmVo.setFilmInfo(filmInfos);
            filmVo.setTotalPage(totalPages);
            filmVo.setNowPage(nowPage);
        }

        return filmVo;
    }

    @Override
    public FilmVo getSoonFilms(boolean isLimit, int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVo filmVo = new FilmVo();
        List<FilmInfo> filmInfos = new ArrayList<>();

        // 即将上映影片的限制条件
        EntityWrapper<CcFilm> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "2");
        // 判断是否是首页需要的内容
        if (isLimit) {
            // 如果是，则限制条数、限制内容为热映影片
            Page<CcFilm> page = new Page<>(1, nums);
            List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);
            // 组织filmInfos
            filmInfos = getFilmInfos(ccFilms);
            filmVo.setFilmNum(ccFilms.size());
            filmVo.setFilmInfo(filmInfos);
        } else {
            // 如果不是，则是列表页，同样需要限制内容为即将上映影片
            Page<CcFilm> page = null;
            // 根据sortId的不同，来组织不同的Page对象
            // 1-按热门搜索，2-按时间搜索，3-按评价搜索
            switch (sortId) {
                case 1:
                    page = new Page<>(nowPage, nums, "film_preSaleNum");
                    break;
                case 2:
                    page = new Page<>(nowPage, nums, "film_time");
                    break;
                case 3:
                    page = new Page<>(nowPage, nums, "film_preSaleNum");
                    break;
                default:
                    page = new Page<>(nowPage, nums, "film_preSaleNum");
                    break;
            }

            // 如果sourceId,yearId,catId 不为99 ,则表示要按照对应的编号进行查询
            if (sourceId != 99) {
                entityWrapper.eq("film_source", sourceId);
            }
            if (yearId != 99) {
                entityWrapper.eq("film_date", yearId);
            }
            if (catId != 99) {
                // #2#4#22#
                String catStr = "%#" + catId + "#%";
                entityWrapper.like("film_cats", catStr);
            }

            List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);
            // 组织filmInfos
            filmInfos = getFilmInfos(ccFilms);
            filmVo.setFilmNum(ccFilms.size());

            int totalCounts = ccFilmMapper.selectCount(entityWrapper);
            int totalPages = (totalCounts / nums) + (totalCounts % nums == 0 ? 0 : 1);

            filmVo.setFilmInfo(filmInfos);
            filmVo.setTotalPage(totalPages);
            filmVo.setNowPage(nowPage);
        }

        return filmVo;
    }

    @Override
    public FilmVo getClassicFilms(int nums, int nowPage, int sortId, int sourceId, int yearId, int catId) {
        FilmVo filmVo = new FilmVo();
        List<FilmInfo> filmInfos = new ArrayList<>();

        // 即将上映影片的限制条件
        EntityWrapper<CcFilm> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "3");

        // 如果不是，则是列表页，同样需要限制内容为即将上映影片
        Page<CcFilm> page = null;
        // 根据sortId的不同，来组织不同的Page对象
        // 1-按热门搜索，2-按时间搜索，3-按评价搜索
        switch (sortId) {
            case 1:
                page = new Page<>(nowPage, nums, "film_box_office");
                break;
            case 2:
                page = new Page<>(nowPage, nums, "film_time");
                break;
            case 3:
                page = new Page<>(nowPage, nums, "film_score");
                break;
            default:
                page = new Page<>(nowPage, nums, "film_box_office");
                break;
        }

        // 如果sourceId,yearId,catId 不为99 ,则表示要按照对应的编号进行查询
        if (sourceId != 99) {
            entityWrapper.eq("film_source", sourceId);
        }
        if (yearId != 99) {
            entityWrapper.eq("film_date", yearId);
        }
        if (catId != 99) {
            // #2#4#22#
            String catStr = "%#" + catId + "#%";
            entityWrapper.like("film_cats", catStr);
        }

        List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);
        // 组织filmInfos
        filmInfos = getFilmInfos(ccFilms);
        filmVo.setFilmNum(ccFilms.size());

        int totalCounts = ccFilmMapper.selectCount(entityWrapper);
        int totalPages = (totalCounts / nums) + (totalCounts % nums == 0 ? 0 : 1);

        filmVo.setFilmInfo(filmInfos);
        filmVo.setTotalPage(totalPages);
        filmVo.setNowPage(nowPage);

        return filmVo;
    }
    
    @Override
    public List<FilmInfo> getBoxRanking() {
        // 条件 -> 正在上映的，票房前10名
        EntityWrapper<CcFilm> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");

        Page<CcFilm> page = new Page<>(1, 10, "film_box_office");

        List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);

        List<FilmInfo> filmInfos = getFilmInfos(ccFilms);

        return filmInfos;
    }

    @Override
    public List<FilmInfo> getExpectRanking() {
        // 条件 -> 即将上映的，预售前10名
        EntityWrapper<CcFilm> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "2");

        Page<CcFilm> page = new Page<>(1, 10, "film_preSaleNum");

        List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);

        List<FilmInfo> filmInfos = getFilmInfos(ccFilms);

        return filmInfos;

    }

    @Override
    public List<FilmInfo> getTop() {
        // 条件 -> 正在上映的，评分前10名
        EntityWrapper<CcFilm> entityWrapper = new EntityWrapper<>();
        entityWrapper.eq("film_status", "1");

        Page<CcFilm> page = new Page<>(1, 10, "film_score");

        List<CcFilm> ccFilms = ccFilmMapper.selectPage(page, entityWrapper);

        List<FilmInfo> filmInfos = getFilmInfos(ccFilms);

        return filmInfos;
    }

    @Override
    public List<CatVo> getCats() {
        List<CatVo> cats = new ArrayList<>();
        // 查询实体对象 - CcCatDict
        List<CcCatDict> ccCats = ccCatDictMapper.selectList(null);
        // 将实体对象转换为业务对象 - CatVo
        for (CcCatDict ccCatDict : ccCats) {
            CatVo catVo = new CatVo();
            catVo.setCatId(ccCatDict.getUuid() + "");
            catVo.setCatName(ccCatDict.getShowName());

            cats.add(catVo);
        }

        return cats;
    }

    @Override
    public List<SourceVo> getSources() {
        List<SourceVo> sources = new ArrayList<>();
        List<CcSourceDict> ccSourceDicts = ccSourceDictMapper.selectList(null);
        for (CcSourceDict ccSourceDict : ccSourceDicts) {
            SourceVo sourceVo = new SourceVo();

            sourceVo.setSourceId(ccSourceDict.getUuid() + "");
            sourceVo.setSourceName(ccSourceDict.getShowName());

            sources.add(sourceVo);
        }
        return sources;
    }

    @Override
    public List<YearVo> getYears() {
        List<YearVo> years = new ArrayList<>();
        // 查询实体对象 - CcCatDict
        List<CcYearDict> ccYears = ccYearDictMapper.selectList(null);
        // 将实体对象转换为业务对象 - CatVo
        for (CcYearDict ccYearDict : ccYears) {
            YearVo yearVo = new YearVo();
            yearVo.setYearId(ccYearDict.getUuid() + "");
            yearVo.setYearName(ccYearDict.getShowName());

            years.add(yearVo);
        }
        return years;
    }

    @Override
    public FilmDetailVo getFilmDetail(int searchType, String searchParam) {
        FilmDetailVo filmDetailVo = null;
        // searchType 1-按名称  2-按ID的查找
        if (searchType == 1) {
            filmDetailVo = ccFilmMapper.getFilmDetailByName("%" + searchParam + "%");
        } else {
            filmDetailVo = ccFilmMapper.getFilmDetailById(searchParam);
        }

        return filmDetailVo;
    }

    private CcFilmInfo getFilmInfo(String filmId) {

        CcFilmInfo ccFilmInfo = new CcFilmInfo();
        ccFilmInfo.setFilmId(filmId);

        ccFilmInfo = ccFilmInfoMapper.selectOne(ccFilmInfo);

        return ccFilmInfo;
    }

    @Override
    public FilmDescVo getFilmDesc(String filmId) {

        CcFilmInfo ccFilmInfo = getFilmInfo(filmId);

        FilmDescVo filmDescVo = new FilmDescVo();
        filmDescVo.setBiography(ccFilmInfo.getBiography());
        filmDescVo.setFilmId(filmId);

        return filmDescVo;
    }

    @Override
    public ImgVo getImgs(String filmId) {

        CcFilmInfo ccFilmInfo = getFilmInfo(filmId);
        // 图片地址是五个以逗号为分隔的链接URL
        String filmImgStr = ccFilmInfo.getFilmImgs();
        String[] filmImgs = filmImgStr.split(",");

        ImgVo imgVo = new ImgVo();
        imgVo.setMainImg(filmImgs[0]);
        imgVo.setImg01(filmImgs[1]);
        imgVo.setImg02(filmImgs[2]);
        imgVo.setImg03(filmImgs[3]);
        imgVo.setImg04(filmImgs[4]);

        return imgVo;
    }

    @Override
    public ActorVo getDectInfo(String filmId) {

        CcFilmInfo ccFilmInfo = getFilmInfo(filmId);

        // 获取导演编号
        Integer directId = ccFilmInfo.getDirectorId();

        CcActor ccActor = ccActorMapper.selectById(directId);

        ActorVo actorVo = new ActorVo();
        actorVo.setImgAddress(ccActor.getActorImg());
        actorVo.setDirectorName(ccActor.getActorName());

        return actorVo;
    }

    @Override
    public List<ActorVo> getActors(String filmId) {

        List<ActorVo> actors = ccActorMapper.getActors(filmId);

        return actors;
    }
}
