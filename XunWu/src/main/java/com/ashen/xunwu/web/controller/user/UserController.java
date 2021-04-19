package com.ashen.xunwu.web.controller.user;

import com.ashen.xunwu.base.ApiResponse;
import com.ashen.xunwu.service.ServiceMultiResult;
import com.ashen.xunwu.service.ServiceResult;
import com.ashen.xunwu.service.house.IHouseService;
import com.ashen.xunwu.service.user.ISmsService;
import com.ashen.xunwu.service.user.IUserService;
import com.ashen.xunwu.web.dto.HouseDTO;
import com.ashen.xunwu.web.dto.HouseSubscribeDTO;
import com.ashen.xunwu.base.LoginUserUtil;
import com.ashen.xunwu.base.SubscribeStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
public class UserController {
    
    @Autowired
    private ISmsService smsService;
    @Autowired
    private IUserService userService;
    @Autowired
    private IHouseService houseService;

    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/user/center")
    public String centerPage() {
        return "user/center";
    }

    /**
     * 获取短信验证码
     * @param telephone
     * @return
     */
    @GetMapping(value = "sms/code")
    @ResponseBody
    public ApiResponse<String> smsCode(@RequestParam("telephone") String telephone) {
        if (!LoginUserUtil.checkTelephone(telephone)) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "请输入正确的手机号");
        }
        ServiceResult<String> result = smsService.sendSms(telephone);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess("");
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    /**
     * 修改指定用户信息值
     * @param profile 修改项
     * @param value   修改值
     * @return
     */
    @PostMapping(value = "api/user/info")
    @ResponseBody
    public ApiResponse<String> updateUserInfo(@RequestParam(value = "profile") String profile, 
                                              @RequestParam(value = "value") String value) {
        if (value.isEmpty()) {
            return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
        }

        if ("email".equals(profile) && !LoginUserUtil.checkEmail(value)) {
            return ApiResponse.ofMessage(org.apache.http.HttpStatus.SC_BAD_REQUEST, "不支持的邮箱格式");
        }

        ServiceResult<String> result = userService.modifyUserProfile(profile, value);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess("");
        } else {
            return ApiResponse.ofMessage(org.apache.http.HttpStatus.SC_BAD_REQUEST, result.getMessage());
        }
    }

    /**
     * 加入预约清单
     * @param houseId
     * @return
     */
    @PostMapping(value = "api/user/house/subscribe")
    @ResponseBody
    public ApiResponse<String> subscribeHouse(@RequestParam(value = "house_id") Long houseId) {
        ServiceResult<String> result = houseService.addSubscribeOrder(houseId);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess("");
        } else {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
        }
    }

    /**
     * 获取对应状态的预约列表
     * @param start
     * @param size
     * @param status
     * @return
     */
    @GetMapping(value = "api/user/house/subscribe/list")
    @ResponseBody
    public ApiResponse<List<Pair<HouseDTO, HouseSubscribeDTO>>> subscribeList(
            @RequestParam(value = "start", defaultValue = "0") int start,
            @RequestParam(value = "size", defaultValue = "3") int size,
            @RequestParam(value = "status") int status) {

        ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> result = houseService.querySubscribeList(SubscribeStatusEnum.of(status), start, size);

        if (result.getResultSize() == 0) {
            return ApiResponse.ofSuccess(result.getResult());
        }

        ApiResponse<List<Pair<HouseDTO, HouseSubscribeDTO>>> response = ApiResponse.ofSuccess(result.getResult());
        response.setMore(result.getTotal() > (start + size));
        return response;
    }

    /**
     * 预约看房时间
     * @param houseId
     * @param orderTime
     * @param desc
     * @param telephone
     * @return
     */
    @PostMapping(value = "api/user/house/subscribe/date")
    @ResponseBody
    public ApiResponse<String> subscribeDate(
            @RequestParam(value = "houseId") Long houseId,
            @RequestParam(value = "orderTime") @DateTimeFormat(pattern = "yyyy-MM-dd") Date orderTime,
            @RequestParam(value = "desc", required = false) String desc,
            @RequestParam(value = "telephone") String telephone) {
        if (orderTime == null) {
            return ApiResponse.ofMessage(org.apache.http.HttpStatus.SC_BAD_REQUEST, "请选择预约时间");
        }

        if (!LoginUserUtil.checkTelephone(telephone)) {
            return ApiResponse.ofMessage(org.apache.http.HttpStatus.SC_BAD_REQUEST, "手机格式不正确");
        }

        ServiceResult<String> serviceResult = houseService.subscribe(houseId, orderTime, telephone, desc);
        if (serviceResult.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(org.apache.http.HttpStatus.SC_BAD_REQUEST, serviceResult.getMessage());
        }
    }

    /**
     * 取消预约
     * @param houseId
     * @return
     */
    @DeleteMapping(value = "api/user/house/subscribe")
    @ResponseBody
    public ApiResponse<String> cancelSubscribe(@RequestParam(value = "houseId") Long houseId) {
        ServiceResult<String> serviceResult = houseService.cancelSubscribe(houseId);
        if (serviceResult.isSuccess()) {
            return ApiResponse.ofStatus(ApiResponse.Status.SUCCESS);
        } else {
            return ApiResponse.ofMessage(org.apache.http.HttpStatus.SC_BAD_REQUEST, serviceResult.getMessage());
        }
    }
}
