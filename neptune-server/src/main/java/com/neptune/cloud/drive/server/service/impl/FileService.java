package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.server.context.file.GetRealFileContext;
import com.neptune.cloud.drive.server.mapper.FileMapper;
import com.neptune.cloud.drive.server.model.File;
import com.neptune.cloud.drive.server.service.IFileService;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class FileService extends ServiceImpl<FileMapper, File> implements IFileService {

    /**
     * 根据文件唯一标识符查询真实文件
     */
    @Override
    public List<File> listRealFiles(GetRealFileContext context) {
        // 1. 封装查询条件
        QueryWrapper<File> queryWrapper = new QueryWrapper<File>()
                .eq("create_user", context.getUserId())
                .eq("identifier", context.getIdentifier());
        // 2. 根据唯一标识符查询
        return list(queryWrapper);
    }

}
