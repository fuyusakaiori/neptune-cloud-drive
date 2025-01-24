package com.neptune.cloud.drive.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neptune.cloud.drive.server.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper extends BaseMapper<User> {

    String selectUserByUsername(@Param("username") String username);

}




