package com.huan.wxshop.dao;

import com.huan.wxshop.generate.User;
import com.huan.wxshop.generate.UserExample;
import com.huan.wxshop.generate.UserMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class UserDao {
    private final UserMapper userMapper;

    @Autowired
    public UserDao(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void insertUser(User user) {
        userMapper.insertSelective(user);
    }


    public User getUserByTel(String tel) {
        UserExample example = new UserExample();
        example.createCriteria().andTelEqualTo(tel);
        return userMapper.selectByExample(example).get(0);
    }
}
