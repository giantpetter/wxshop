package com.huan.wxshop.service;

import com.huan.wxshop.dao.UserDao;
import com.huan.wxshop.generate.User;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@SuppressFBWarnings({"EI_EXPOSE_REP2", "DLS_DEAD_LOCAL_STORE"})
public class UserService {
    private final UserDao userDao;

    @Autowired
    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User createUserIfNotExist(String tel) {
        User user = new User();
        user.setTel(tel);
        try {
            userDao.insertUser(user);
        } catch (DuplicateKeyException | PersistenceException e2){
            return userDao.getUserByTel(tel);
        }

        return user;
    }

    public Optional<User> getUserByTel(String tel) {
        return Optional.ofNullable(userDao.getUserByTel(tel));
    }
}
