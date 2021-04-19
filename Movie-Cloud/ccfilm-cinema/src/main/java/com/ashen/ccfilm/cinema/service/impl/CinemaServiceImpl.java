package com.ashen.ccfilm.cinema.service.impl;

import com.ashen.ccfilm.cinema.dao.CinemaMapper;
import com.ashen.ccfilm.cinema.entity.model.Cinema;
import com.ashen.ccfilm.cinema.entity.vo.CinemaSavedReqVo;
import com.ashen.ccfilm.cinema.entity.vo.DescribeCinemasRespVO;
import com.ashen.ccfilm.cinema.service.CinemaService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class CinemaServiceImpl implements CinemaService {

    @Resource
    private CinemaMapper cinemaMapper;

    @Override
    public void saveCinema(CinemaSavedReqVo reqVo) throws CommonServiceException {
        Cinema cinema = new Cinema();

        // TODO 填写具体参数
        cinema.setCinemaName("");
        cinema.setCinemaPhone("");
        cinema.setBrandId(0);
        cinema.setAreaId(0);
        cinema.setHallIds("");
        cinema.setImgAddress("");
        cinema.setCinemaAddress("");
        cinema.setMinimumPrice(0);

        // TODO 记得把实体对象进行保存
    }

    @Override
    public IPage<DescribeCinemasRespVO> describeCinemas(int nowPage, int pageSize) throws CommonServiceException {
        // 查询实体对象，然后与表现层对象进行交互
        // TODO 提示
        Page<Cinema> page = new Page<>(nowPage, pageSize);
        IPage<Cinema> cinemaIPage = cinemaMapper.selectPage(page, null);

        // cinemaTIPage对象内的分页参数与IPage<DescribeCinemasRespVo>一模一样

        cinemaIPage.getRecords(); // set到IPage<DescribeCinemasRespVo> records方法中
        return null;
    }
}
