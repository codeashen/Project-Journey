package com.ashen.ccfilm.hall.controller;

import com.ashen.ccfilm.hall.entity.model.vo.HallSavedReqVo;
import com.ashen.ccfilm.hall.entity.model.vo.HallsReqVo;
import com.ashen.ccfilm.hall.entity.model.vo.HallsRespVo;
import com.ashen.ccfilm.hall.service.HallService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Maps;
import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.DataResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/hall")
public class HallController {

    @Autowired
    private HallService hallService;

    /**
     * 新增播放厅
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public DataResult<String> saveHall(@RequestBody HallSavedReqVo hallSavedReqVo) throws CommonServiceException {
        hallSavedReqVo.checkParam();
        hallService.saveHall(hallSavedReqVo);
        return DataResult.success();
    }

    /**
     * 获取播放厅列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public DataResult<Map<String, Object>> describeHalls(HallsReqVo hallsReqVo) throws CommonServiceException {
        hallsReqVo.checkParam();
        IPage<HallsRespVo> page = hallService.describeHalls(hallsReqVo);
        Map<String, Object> halls = describePageResult(page, "halls");
        return DataResult.success(halls);
    }

    // 获取分页对象的公共接口
    private Map<String, Object> describePageResult(IPage page, String title) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("totalSize", page.getTotal());
        result.put("totalPages", page.getPages());
        result.put("pageSize", page.getSize());
        result.put("nowPage", page.getCurrent());
        result.put(title, page.getRecords());
        return result;
    }
}
