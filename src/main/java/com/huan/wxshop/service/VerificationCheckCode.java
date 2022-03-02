package com.huan.wxshop.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCheckCode {
    Map<String, String> telNumber2CorrectCode = new ConcurrentHashMap<>();

    public void addCode(String tel, String correctCode) {
        telNumber2CorrectCode.put(tel, correctCode);
    }

    public String getCorrectCode(String tel) {

        return telNumber2CorrectCode.get(tel);
    }
}
