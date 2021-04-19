package com.ashen.ccfilm.film.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.utils.ToolUtils;
import com.ashen.ccfilm.film.dao.ActorMapper;
import com.ashen.ccfilm.film.dao.FilmActorMapper;
import com.ashen.ccfilm.film.dao.FilmInfoMapper;
import com.ashen.ccfilm.film.dao.FilmMapper;
import com.ashen.ccfilm.film.entity.model.Film;
import com.ashen.ccfilm.film.entity.model.FilmActor;
import com.ashen.ccfilm.film.entity.model.FilmInfo;
import com.ashen.ccfilm.film.service.FilmService;
import com.ashen.ccfilm.film.vo.DescribeActorsRespVo;
import com.ashen.ccfilm.film.vo.DescribeFilmsRespVo;
import com.ashen.ccfilm.film.vo.FilmSavedReqVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {

    @Resource
    private ActorMapper actorMapper;
    @Resource
    private FilmMapper filmMapper;
    @Resource
    private FilmInfoMapper filmInfoMapper;
    @Resource
    private FilmActorMapper filmActorMapper;

    /**
     * 演员查询列表
     */
    @Override
    public IPage<DescribeActorsRespVo> describeActors(int nowPage, int pageSize) throws CommonServiceException {
        // 查询演员列表
        return actorMapper.describeActors(new Page<>(nowPage, pageSize));
    }

    /**
     * 影片列表查询
     */
    @Override
    public IPage<DescribeFilmsRespVo> describeFilms(int nowPage, int pageSize) throws CommonServiceException {
        return filmMapper.describeFilms(new Page<>(nowPage, pageSize));
    }

    /**
     * 根据主键获取电影详情
     */
    @Override
    public DescribeFilmsRespVo describeFilmById(String filmId) throws CommonServiceException {
        return filmMapper.describeFilmById(filmId);
    }

    /**
     * 保存电影信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   // 指定什么异常会造成事务回滚
    public void saveFilm(FilmSavedReqVo reqVo) throws CommonServiceException {
        try {
            // 保存电影主表
            Film film = new Film();
            film.setFilmName(reqVo.getFilmName());
            film.setFilmType(ToolUtils.str2Int(reqVo.getFilmTypeId()));
            film.setImgAddress(reqVo.getMainImgAddress());
            film.setFilmScore(reqVo.getFilmScore());
            film.setFilmPresalenum(ToolUtils.str2Int(reqVo.getPreSaleNum()));
            film.setFilmBoxOffice(ToolUtils.str2Int(reqVo.getBoxOffice()));
            film.setFilmSource(ToolUtils.str2Int(reqVo.getFilmSourceId()));
            film.setFilmCats(reqVo.getFilmCatIds());
            film.setFilmArea(ToolUtils.str2Int(reqVo.getAreaId()));
            film.setFilmDate(ToolUtils.str2Int(reqVo.getDateId()));
            film.setFilmTime(ToolUtils.str2LocalDateTime(reqVo.getFilmTime() + " 00:00:00"));
            film.setFilmStatus(ToolUtils.str2Int(reqVo.getFilmStatus()));
            filmMapper.insert(film);

            // 保存电影子表
            FilmInfo filmInfo = new FilmInfo();
            filmInfo.setFilmId(film.getUuid() + "");
            filmInfo.setFilmEnName(reqVo.getFilmEnName());
            filmInfo.setFilmScore(reqVo.getFilmScore());
            filmInfo.setFilmScoreNum(ToolUtils.str2Int(reqVo.getFilmScorers()));
            filmInfo.setFilmLength(ToolUtils.str2Int(reqVo.getFilmLength()));
            filmInfo.setBiography(reqVo.getBiography());
            filmInfo.setDirectorId(ToolUtils.str2Int(reqVo.getDirectorId()));
            filmInfo.setFilmImgs(reqVo.getFilmImgs());
            filmInfoMapper.insert(filmInfo);

            String[] actorId = reqVo.getActIds().split("#");
            String[] roleNames = reqVo.getRoleNames().split("#");
            if (actorId.length != roleNames.length) {
                throw new CommonServiceException(500, "演员和角色名数量不匹配");
            }

            // 保存演员映射表
            for (int i = 0; i < actorId.length; i++) {
                FilmActor filmActor = new FilmActor();
                filmActor.setFilmId(film.getUuid());
                filmActor.setActorId(ToolUtils.str2Int(actorId[i]));
                filmActor.setRoleName(roleNames[i]);
                filmActorMapper.insert(filmActor);
            }
        } catch (Exception e) {
            throw new CommonServiceException(500, e.getMessage());
        }
    }
}
