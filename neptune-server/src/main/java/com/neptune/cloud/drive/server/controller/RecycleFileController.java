package com.neptune.cloud.drive.server.controller;

import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.context.recycle.DeleteRecycleFileContext;
import com.neptune.cloud.drive.server.context.recycle.GetRecycleFileContext;
import com.neptune.cloud.drive.server.context.recycle.RestoreRecycleFileContext;
import com.neptune.cloud.drive.server.request.recycle.DeleteRecycleFileRequest;
import com.neptune.cloud.drive.server.request.recycle.RestoreRecycleFileRequest;
import com.neptune.cloud.drive.server.service.IRecycleFileService;
import com.neptune.cloud.drive.server.threadlocal.UserThreadLocal;
import com.neptune.cloud.drive.server.vo.UserFileVO;
import com.neptune.cloud.drive.util.IdUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
public class RecycleFileController {

    @Autowired
    private IRecycleFileService recycleFileService;

    /**
     * 查询文件: 查询回收站中待删除的文件
     */
    @ApiOperation(
            value = "查询回收站中的文件",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/recycle")
    public Response<List<UserFileVO>> listRecycleFile() {
        log.info("UserFileController listRecycleFile: 查询回收站文件开始");
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController listRecycleFile: 用户未登录, 不允许查询回收站文件");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 用户 ID 设置到上下文
        GetRecycleFileContext context = new GetRecycleFileContext().setUserId(userId);
        // 4. 调用查询回收站文件的方法
        List<UserFileVO> files = recycleFileService.listRecycleFile(context);
        log.info("UserFileController listRecycleFile: 查询回收站文件结束");
        return Response.success(files);
    }


    /**
     * 还原文件: 从回收站中还原文件
     */
    @ApiOperation(
            value = "回收站还原文件",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PutMapping("/recycle")
    public Response<Void> restoreRecycleFile(@Validated @RequestBody RestoreRecycleFileRequest request) {
        log.info("UserFileController restoreRecycleFile: 还原回收站文件开始");
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController restoreRecycleFile: 用户未登录, 不允许还原回收站文件");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 解密需要还原的回收站文件
        List<Long> fileIds = IdUtil.decryptList(request.getFileIds());
        // 4. 判断是否解密成功
        if (CollectionUtils.isEmpty(fileIds)) {
            return Response.success();
        }
        // 5. 设置上下文
        RestoreRecycleFileContext context = new RestoreRecycleFileContext()
                .setUserId(userId).setFileIds(fileIds);
        // 6. 调用方法开始还原文件
        recycleFileService.restoreRecycleFile(context);
        log.info("UserFileController restoreRecycleFile: 还原回收站文件结束");
        return Response.success();
    }

    /**
     * 删除文件: 彻底从磁盘中删除文件
     */
    @ApiOperation(
            value = "回收站删除文件",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping("/recycle")
    public Response<Void> deleteRecycleFile(@Validated @RequestBody DeleteRecycleFileRequest request) {
        log.info("UserFileController deleteRecycleFile: 删除回收站文件开始");
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController deleteRecycleFile: 用户未登录, 不允许删除回收站文件");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 解密需要还原的回收站文件
        List<Long> fileIds = IdUtil.decryptList(request.getFileIds());
        // 4. 判断是否解密成功
        if (CollectionUtils.isEmpty(fileIds)) {
            return Response.success();
        }
        // 5. 设置上下文
        DeleteRecycleFileContext context = new DeleteRecycleFileContext()
                .setUserId(userId).setFileIds(fileIds);
        // 6. 调用方法开始还原文件
        recycleFileService.deleteRecycleFile(context);
        log.info("UserFileController deleteRecycleFile: 删除回收站文件结束");
        return Response.success();
    }

}
