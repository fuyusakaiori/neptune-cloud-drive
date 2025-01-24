package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.file.GetRealFileContext;
import com.neptune.cloud.drive.server.model.File;

import java.util.List;

public interface IFileService extends IService<File> {

    List<File> listRealFiles(GetRealFileContext context);

}
