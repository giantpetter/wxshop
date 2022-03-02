package com.huan.wxshop.service;

import org.springframework.stereotype.Component;

@Component
public class MockSmsCodeService implements SmsCodeService {
    @Override
    public String sendSmsCode(String tel) {
        return "000000";
    }
}
