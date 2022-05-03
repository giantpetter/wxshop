package com.huan.wxshop.entity;

import com.huan.wxshop.generate.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("EI_EXPOSE_REP")
public class LoginResponse {
    private boolean login;
    private User user;

    public LoginResponse() {
    }

    public static LoginResponse notLogin() {
        return new LoginResponse(false, null);
    }

    public static LoginResponse alreadyLogin(User user) {
        return new LoginResponse(true, user);
    }


    private LoginResponse(boolean login, User user) {
        this.login = login;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }
}
