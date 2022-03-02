package com.huan.wxshop.service;

import com.huan.wxshop.controller.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TelVerificationServiceTest {
    public static AuthController.TelAndCode VALID_PARAMETER = new AuthController.TelAndCode("15084920063", null);
    public static AuthController.TelAndCode EMPTY_TEL = new AuthController.TelAndCode(null, "000000");
    public static AuthController.TelAndCode VALID_PARAMETER_CODE = new AuthController.TelAndCode("15084920063", "000000");

    @Test
    public void returnTrueIfValid() {
        Assertions.assertTrue(new TelVerificationService().verify(VALID_PARAMETER));
    }

    @Test
    public void returnFalseIfNotValid() {
        Assertions.assertFalse(new TelVerificationService().verify(EMPTY_TEL));
        Assertions.assertFalse(new TelVerificationService().verify(null));
    }
}
