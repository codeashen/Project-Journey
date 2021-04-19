package com.ashen.ccfilm.film.dao;

import com.ashen.ccfilm.film.vo.DescribeFilmsRespVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ashen.ccfilm.film.entity.model.Film;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 影片主表 Mapper 接口
 * </p>
 *
 * @author ashen
 * @since 2020-11-27
 */
public interface FilmMapper extends BaseMapper<Film> {

    IPage<DescribeFilmsRespVo> describeFilms(Page<DescribeFilmsRespVo> page);

    DescribeFilmsRespVo describeFilmById(@Param("filmId") String filmId);

}
