package com.ashen.ccfilm.film.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ashen.ccfilm.film.entity.model.Actor;
import com.ashen.ccfilm.film.vo.DescribeActorsRespVo;


/**
 * <p>
 * 演员表 Mapper 接口
 * </p>
 *
 * @author ashen
 * @since 2020-11-27
 */
public interface ActorMapper extends BaseMapper<Actor> {
    
    IPage<DescribeActorsRespVo> describeActors(Page<DescribeActorsRespVo> page);
    
}
