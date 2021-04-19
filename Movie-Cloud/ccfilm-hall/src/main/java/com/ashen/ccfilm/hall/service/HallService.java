package com.ashen.ccfilm.hall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.hall.entity.model.vo.HallSavedReqVo;
import com.ashen.ccfilm.hall.entity.model.vo.HallsReqVo;
import com.ashen.ccfilm.hall.entity.model.vo.HallsRespVo;

public interface HallService {
    // 获取全部播放厅接口
    IPage<HallsRespVo> describeHalls(HallsReqVo hallsReqVo) throws CommonServiceException;

    // 保存影厅信息
    void saveHall(HallSavedReqVo hallSavedReqVo) throws CommonServiceException;
}
