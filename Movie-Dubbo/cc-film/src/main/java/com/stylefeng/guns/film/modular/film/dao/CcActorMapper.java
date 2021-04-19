package com.stylefeng.guns.film.modular.film.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.stylefeng.guns.api.film.vo.ActorVo;
import com.stylefeng.guns.film.modular.film.model.CcActor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 演员表 Mapper 接口
 * </p>
 *
 * @author Ashen
 * @since 2020-10-22
 */
public interface CcActorMapper extends BaseMapper<CcActor> {

    /**
     * 获取演员信息
     * @param filmId
     * @return
     */
    List<ActorVo> getActors(@Param("filmId") String filmId);

}
