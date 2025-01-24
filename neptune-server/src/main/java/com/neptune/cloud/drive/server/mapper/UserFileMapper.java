package com.neptune.cloud.drive.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neptune.cloud.drive.server.model.UserFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserFileMapper extends BaseMapper<UserFile> {

    List<UserFile> listUserFiles(@Param("userId") long userId,
                                 @Param("parentId") long parentId,
                                 @Param("fileTypes") List<Integer> fileTypes);
}




