package com.huan.wxshop.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class ShiroRealm extends AuthorizingRealm {
    private final VerificationCheckCode verificationCheckCode;

    @Autowired
    public ShiroRealm(VerificationCheckCode verificationCheckCode) {
        this.verificationCheckCode = verificationCheckCode;
        this.setCredentialsMatcher((authenticationToken, authenticationInfo) ->
                new String((char[]) authenticationToken.getCredentials())
                        .equals(authenticationInfo.getCredentials()));
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String tel = (String) authenticationToken.getPrincipal();
        String correctCode = verificationCheckCode.getCorrectCode(tel);

        return new SimpleAuthenticationInfo(tel, correctCode, getName());
    }
}
