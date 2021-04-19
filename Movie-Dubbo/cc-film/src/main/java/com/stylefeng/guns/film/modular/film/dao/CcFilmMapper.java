package com.stylefeng.guns.film.modular.film.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.stylefeng.guns.api.film.vo.FilmDetailVo;
import com.stylefeng.guns.film.modular.film.model.CcFilm;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 影片主表 Mapper 接口
 * </p>
 *
 * @author Ashen
 * @since 2020-10-22
 */
public interface CcFilmMapper extends BaseMapper<CcFilm> {

    /**
     * 通过电影名模糊查询详情
     * @param filmName
     * @return
     */
    FilmDetailVo getFilmDetailByName(@Param("filmName") String filmName);

    /**
     * 通过电影id查询详情
     * @param uuid
     * @return
     */
    FilmDetailVo getFilmDetailById(@Param("uuid") String uuid);

}
