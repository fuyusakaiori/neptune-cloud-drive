package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.server.mapper.FileMapper;
import com.neptune.cloud.drive.server.model.File;
import com.neptune.cloud.drive.server.service.FileService;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {
}
