package com.neptune.cloud.drive.server.controller;

import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.constant.FileConstant;
import com.neptune.cloud.drive.server.context.file.*;
import com.neptune.cloud.drive.server.converter.UserFileConverter;
import com.neptune.cloud.drive.server.request.file.CreateUserDirectoryRequest;
import com.neptune.cloud.drive.server.request.file.DeleteUserFileRequest;
import com.neptune.cloud.drive.server.request.file.RenameUserFileRequest;
import com.neptune.cloud.drive.server.request.file.SecondUploadUserFileRequest;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.server.threadlocal.UserThreadLocal;
import com.neptune.cloud.drive.server.vo.UserFileVO;
import com.neptune.cloud.drive.util.IdUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Validated
@RestController
public class UserFileController {

    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private UserFileConverter userFileConverter;


    /**
     * 创建文件目录
     */
    @ApiOperation(
            value = "创建文件目录",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/file/directory")
    public Response<String> createUserDirectory(@Validated @RequestBody CreateUserDirectoryRequest request) {
        log.info("UserFileController createUserDirectory: 开始创建用户目录, request = {}", request);
        // 1. 请求转换为上下文
        CreateUserDirectoryContext context = userFileConverter.createUserDirectoryRequest2CreateUserDirectoryContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController createUserDirectory: 用户未登录, 不允许创建目录, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 用户 ID 设置到上下文
        context.setUserId(userId);
        // 5. 调用创建用户目录的方法
        long directoryId = userFileService.createUserDirectory(context);
        // 6. 判断是否创建成功
        if (directoryId <= 0) {
            log.error("UserFileController createUserDirectory: 创建用户目录失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 7. 加密用户创建的目录 ID
        try {
            return Response.success(IdUtil.encrypt(directoryId));
        } catch (Exception e) {
            log.error("UserFileController createUserDirectory: 加密用户创建的目录 ID 失败, request = {}, directoryId = {}", request, directoryId);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
    }


    /**
     * 重命名目录或者文件
     */
    @ApiOperation(
            value = "重命名目录或者文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PutMapping("/file")
    public Response<Void> renameUserFile(@Validated @RequestBody RenameUserFileRequest request) {
        log.info("UserFileController renameUserFile: 开始重命名用户目录下的文件, request = {}", request);
        // 1. 请求转换为中间类
        RenameUserFileContext context = userFileConverter.renameUserFileRequest2RenameUserFileContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController renameUserFile: 用户未登录, 不允许重命名文件, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 设置到上下文中
        context.setUserId(userId);
        // 5. 调用重命名文件的逻辑
        userFileService.renameUserFile(context);
        log.info("UserFileController renameUserFile: 开始删除用户目录下的文件列表, request = {}", request);
        return Response.success();
    }

    /**
     * 删除文件
     */
    @ApiOperation(
            value = "删除文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping("/file")
    public Response<Void> deleteUserFile(@Validated @RequestBody DeleteUserFileRequest request) {
        log.info("UserFileController deleteUserFile: 开始删除用户目录下的文件, request = {}", request);
        // 1. 请求转换为中间类
        DeleteUserFileContext context = userFileConverter.deleteUserFileRequest2DeleteUserFileContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController deleteUserFile: 用户未登录, 不允许删除文件, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 解析删除的文件 ID
        List<Long> fileIdList = IdUtil.decryptList(request.getFileIds());
        // 5. 设置到上下文中
        context.setUserId(userId)
                .setFileIdList(fileIdList);
        // 6. 调用删除文件的方法
        userFileService.deleteUserFile(context);
        log.info("UserFileController deleteUserFile: 删除用户目录下的文件结束, request = {}", request);
        return Response.success();
    }

    /**
     * 秒传文件
     */
    @ApiOperation(
            value = "秒传文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/file/second-upload")
    public Response<Void> secondUploadFile(@Validated @RequestBody SecondUploadUserFileRequest request) {
        log.info("UserFileController secondUploadFile: 开始秒传用户文件, request = {}", request);
        // 1. 请求转换为中间类
        SecondUploadUserFileContext context = userFileConverter.secondUploadUserFileRequest2SecondUploadUserFileContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController secondUploadFile: 用户未登录, 不允许秒传文件, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 设置到上下文中
        context.setUserId(userId);
        // 5. 调用秒传文件的方法
        boolean isSecondUpload = userFileService.secondUploadUserFile(context);
        // 6. 判断是否秒传成功
        if (isSecondUpload) {
            log.error("UserFileController secondUploadFile: 秒传的文件不存在, 秒传失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "秒传失败");
        }
        log.info("UserFileController secondUploadFile: 秒传用户文件结束, request = {}", request);
        return Response.success();
    }

    /**
     * 查询目录的文件列表
     */
    @ApiOperation(
            value = "查询文件列表",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/files")
    public Response<List<UserFileVO>> listUserFiles(@NotBlank(message = "目录不能为空") @RequestParam(value = "parentId", required = false) String parentId,
                                                    @RequestParam(value = "fileType", required = false, defaultValue = FileConstant.ALL_TYPE_FILE) String fileType) {
        log.info("UserFileController listUserFiles: 开始查询用户目录下的文件列表, parentId = {}, fileType = {}", parentId, fileType);
        long realParentId = 0;
        try {
            // 1. 解密目录 ID
            realParentId = IdUtil.decrypt(parentId);
        } catch (Exception exception) {
            log.error("UserFileController listUserFiles: 解析目录 ID 出现异常, parentId = {}, fileType = {}", parentId, fileType);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 2. 解析查询的文件类型
        List<Integer> fileTypes = null;
        if (!FileConstant.ALL_TYPE_FILE.equals(fileType)) {
            fileTypes = Arrays.stream(
                    fileType.split(StringConstant.COMMON_SEPARATOR)).map(Integer::valueOf).collect(Collectors.toList());
        }
        // 4. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 5. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController listUserFiles: 用户未登录, 不允许查询目录的文件列表");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 封装到上下文中
        ListUserFileContext context = new ListUserFileContext()
                .setParentId(realParentId).setFileTypes(fileTypes).setUserId(userId);
        // 4. 调用查询目录文件列表的方法
        List<UserFileVO> userFiles = userFileService.listUserFiles(context);
        log.info("UserFileController listUserFiles: 查询用户目录下的文件列表结束, parentId = {}, fileType = {}, size = {}", parentId, fileType, userFiles.size());
        return Response.success(userFiles);
    }

}
