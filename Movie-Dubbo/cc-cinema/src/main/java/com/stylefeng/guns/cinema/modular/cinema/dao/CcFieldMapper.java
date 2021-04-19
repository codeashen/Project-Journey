package com.stylefeng.guns.cinema.modular.cinema.dao;

import com.stylefeng.guns.api.cinema.vo.FilmInfoVo;
import com.stylefeng.guns.api.cinema.vo.HallInfoVo;
import com.stylefeng.guns.cinema.modular.cinema.model.CcField;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 放映场次表 Mapper 接口
 * </p>
 *
 * @author Ashen
 * @since 2020-10-29
 */
public interface CcFieldMapper extends BaseMapper<CcField> {

    List<FilmInfoVo> getFilmInfos(@Param("cinemaId") int cinemaId);

    HallInfoVo getHallInfo(@Param("fieldId") int fieldId);

    FilmInfoVo getFilmInfoById(@Param("fieldId") int fieldId);

}
