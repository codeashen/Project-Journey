package com.stylefeng.guns.film.modular.film.service;

import com.stylefeng.guns.api.film.service.FilmAsyncService;
import com.stylefeng.guns.api.film.vo.ActorVo;
import com.stylefeng.guns.api.film.vo.FilmDescVo;
import com.stylefeng.guns.api.film.vo.ImgVo;
import com.stylefeng.guns.film.modular.film.dao.CcActorMapper;
import com.stylefeng.guns.film.modular.film.dao.CcFilmInfoMapper;
import com.stylefeng.guns.film.modular.film.model.CcActor;
import com.stylefeng.guns.film.modular.film.model.CcFilmInfo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 异步调用的影片业务层，此Service中的方法都是供上游服务通过dubbo异步调用的
 * 因为注解不支持方法级别的异步调用配置，所以可以将异步调用的方法都写在专门的Service里
 * @see com.stylefeng.guns.gateway.modular.film.controller.FilmController#films(java.lang.String, int)
 */
@Component
@DubboService
public class FilmAsyncServiceImpl implements FilmAsyncService {
    
    @Autowired
    private CcFilmInfoMapper ccFilmInfoMapper;

    @Autowired
    private CcActorMapper ccActorMapper;

    private CcFilmInfo getFilmInfo(String filmId){
        CcFilmInfo ccFilmInfo = new CcFilmInfo();
        ccFilmInfo.setFilmId(filmId);
        return ccFilmInfoMapper.selectOne(ccFilmInfo);
    }
    
    @Override
    public FilmDescVo getFilmDesc(String filmId) {
        CcFilmInfo filmInfo = getFilmInfo(filmId);

        FilmDescVo filmDescVo = new FilmDescVo();
        filmDescVo.setBiography(filmInfo.getBiography());
        filmDescVo.setFilmId(filmId);
        return filmDescVo;
    }

    @Override
    public ImgVo getImgs(String filmId) {
        CcFilmInfo filmInfo = getFilmInfo(filmId);
        // 图片地址是五个以逗号为分隔的链接URL
        String filmImgStr = filmInfo.getFilmImgs();
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
        CcActor actor = ccActorMapper.selectById(directId);
        
        ActorVo actorVo = new ActorVo();
        actorVo.setImgAddress(actor.getActorImg());
        actorVo.setDirectorName(actor.getActorName());
        return actorVo;
    }

    @Override
    public List<ActorVo> getActors(String filmId) {
        return ccActorMapper.getActors(filmId);
    }
}
