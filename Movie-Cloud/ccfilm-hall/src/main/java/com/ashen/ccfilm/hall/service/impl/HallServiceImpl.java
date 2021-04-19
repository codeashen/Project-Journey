package com.ashen.ccfilm.hall.service.impl;

import com.ashen.ccfilm.hall.dao.FieldMapper;
import com.ashen.ccfilm.hall.dao.HallFilmInfoMapper;
import com.ashen.ccfilm.hall.entity.model.Field;
import com.ashen.ccfilm.hall.entity.model.HallFilmInfo;
import com.ashen.ccfilm.hall.entity.model.vo.HallSavedReqVo;
import com.ashen.ccfilm.hall.entity.model.vo.HallsReqVo;
import com.ashen.ccfilm.hall.entity.model.vo.HallsRespVo;
import com.ashen.ccfilm.hall.feign.FilmFeignClient;
import com.ashen.ccfilm.hall.service.HallService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.utils.ToolUtils;
import com.ashen.ccfilm.common.vo.DataResult;
import com.ashen.ccfilm.film.vo.DescribeFilmsRespVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

@Slf4j
@Service
public class HallServiceImpl implements HallService {

    @Resource
    private FieldMapper fieldTMapper;
    @Resource
    private HallFilmInfoMapper hallFilmInfoTMapper;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient eurekaClient;
    
    @Resource
    private FilmFeignClient filmFeignClient;

    /**
     * 查询影厅列表
     */
    @Override
    public IPage<HallsRespVo> describeHalls(HallsReqVo hallsReqVo) throws CommonServiceException {

        Page<HallsReqVo> page = new Page<>(hallsReqVo.getNowPage(), hallsReqVo.getPageSize());

        QueryWrapper<HallsReqVo> queryWrapper = new QueryWrapper<>();
        if (ToolUtils.strIsNotNul(hallsReqVo.getCinemaId())) {
            queryWrapper.eq("cinema_id", hallsReqVo.getCinemaId());
        }

        return fieldTMapper.describeHalls(page, queryWrapper);
    }

    /**
     * 保存影厅信息
     */
    @Override
    public void saveHall(HallSavedReqVo reqVo) throws CommonServiceException {
        // 播放厅的列表数据
        Field field = new Field();
        field.setCinemaId(ToolUtils.str2Int(reqVo.getCinemaId()));
        field.setFilmId(ToolUtils.str2Int(reqVo.getFilmId()));
        field.setBeginTime(reqVo.getBeginTime());
        field.setEndTime(reqVo.getEndTime());
        field.setHallId(ToolUtils.str2Int(reqVo.getHallTypeId()));
        field.setHallName(reqVo.getHallName());
        field.setPrice(ToolUtils.str2Int(reqVo.getFilmPrice()));
        fieldTMapper.insert(field);
        
        // 播放厅对应的影片数据，影片冗余数据
        HallFilmInfo hallFilmInfo = describeFilmInfo(reqVo.getFilmId());
        hallFilmInfoTMapper.insert(hallFilmInfo);
    }

    private HallFilmInfo describeFilmInfo(String filmId) throws CommonServiceException {
        // GET REGISTER
        // ServiceInstance choose = eurekaClient.choose("film-service");
        // // 组织调用参数
        // String hostname = choose.getHost();
        // int port = choose.getPort();
        // String uri = "/film/" + filmId;
        // String url = "http://" + hostname + ":" + port + uri;
        //
        // // 通过restTemplate调用影片服务
        // JSONObject baseResponseVo = restTemplate.getForObject(url, JSONObject.class);

        DataResult<DescribeFilmsRespVo> dataResult = filmFeignClient.describeFilmById(filmId);
        DescribeFilmsRespVo data = dataResult.getData();

        // 解析返回值
        HallFilmInfo hallFilmInfo = new HallFilmInfo();
        if (data != null) {
            // 组织参数
            hallFilmInfo.setFilmId(Integer.valueOf(data.getFilmId()));
            hallFilmInfo.setFilmName(data.getFilmName());
            hallFilmInfo.setFilmLength(data.getFilmLength());
        }

        return hallFilmInfo;
    }
}
