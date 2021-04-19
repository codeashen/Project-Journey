package com.ashen.ccfilm.user.entity.vo;

import com.ashen.ccfilm.common.exception.CommonServiceException;
import com.ashen.ccfilm.common.vo.BaseRequestVo;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class LoginRequest extends BaseRequestVo {
    
    private String username;
    private String password;

    @Override
    public void checkParam() throws CommonServiceException {
        // TODO 验证数据合法性
        if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
            throw new CommonServiceException(404,"username 或 password不能为空");
        }
    }
}
