package com.huan.wxshop.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class AuthService {
    private final UserService userService;
    private final VerificationCheckCode verificationCheckCode;
    private final SmsCodeService smsCodeService;

    @Autowired
    public AuthService(UserService userService,
                       VerificationCheckCode verificationCheckCode,
                       SmsCodeService smsCodeService) {
        this.userService = userService;
        this.verificationCheckCode = verificationCheckCode;
        this.smsCodeService = smsCodeService;
    }

    public void sendVerificationCode(String tel) {
        userService.createUserIfNotExist(tel);
        String correctCode = smsCodeService.sendSmsCode(tel);
        verificationCheckCode.addCode(tel, correctCode);
    }
}
