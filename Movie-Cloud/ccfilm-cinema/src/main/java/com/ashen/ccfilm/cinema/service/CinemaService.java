package com.ashen.ccfilm.cinema.service;

import com.ashen.ccfilm.cinema.entity.vo.CinemaSavedReqVo;
import com.ashen.ccfilm.cinema.entity.vo.DescribeCinemasRespVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ashen.ccfilm.common.exception.CommonServiceException;

public interface CinemaService {

    // 保存影院
    void saveCinema(CinemaSavedReqVo reqVo) throws CommonServiceException;

    // 获取影院列表
    IPage<DescribeCinemasRespVO> describeCinemas(int nowPage, int pageSize) throws CommonServiceException;
}
