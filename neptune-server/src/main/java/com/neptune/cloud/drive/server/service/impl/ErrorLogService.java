package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.server.mapper.ErrorLogMapper;
import com.neptune.cloud.drive.server.model.ErrorLog;
import com.neptune.cloud.drive.server.service.IErrorLogService;
import org.springframework.stereotype.Service;

@Service
public class ErrorLogService extends ServiceImpl<ErrorLogMapper, ErrorLog> implements IErrorLogService {

}




