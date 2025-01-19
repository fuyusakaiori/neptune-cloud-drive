package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.server.mapper.UserMapper;
import com.neptune.cloud.drive.server.model.User;
import com.neptune.cloud.drive.server.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}




