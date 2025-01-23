package com.neptune.cloud.drive.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neptune.cloud.drive.server.model.User;

public interface UserMapper extends BaseMapper<User> {

    String selectUserByUsername(String username);

}




