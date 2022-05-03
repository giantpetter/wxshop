package com.huan.wxshop.service;

public interface SmsCodeService {
    /**
     * 向一个指定的手机号码发送验证码
     *
     * @param tel 手机号码
     * @return 返回验证码
     */
    String sendSmsCode(String tel);

}
