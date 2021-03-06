package com.huan.wxshop.service;

import com.huan.wxshop.controller.AuthController;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class TelVerificationService {

    private static final Pattern TEL_PATTERN = Pattern.compile("1\\d{10}");

    /**
     * 验证输入的参数是否合法:
     * tel 必须为合法的中国大陆手机号
     *
     * @param param 输入的参数
     * @return true 合法，否则返回 false。
     */
    public boolean verify(AuthController.TelAndCode param) {
        if (param == null) {
            return false;
        } else if (param.getTel() == null) {
            return false;
        } else {
            return TEL_PATTERN.matcher(param.getTel()).matches();
        }
    }
}
