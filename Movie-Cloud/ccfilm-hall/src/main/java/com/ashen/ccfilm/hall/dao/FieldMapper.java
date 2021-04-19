package com.ashen.ccfilm.hall.dao;

import com.ashen.ccfilm.hall.entity.model.Field;
import com.ashen.ccfilm.hall.entity.model.vo.HallsReqVo;
import com.ashen.ccfilm.hall.entity.model.vo.HallsRespVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 放映场次表 Mapper 接口
 * </p>
 *
 * @author ashen
 * @since 2020-11-27
 */
public interface FieldMapper extends BaseMapper<Field> {

    IPage<HallsRespVo> describeHalls(Page<HallsReqVo> page, @Param("ew") QueryWrapper queryWrapper);
}
