package com.neptune.cloud.drive.server.controller;

import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.constant.FileConstant;
import com.neptune.cloud.drive.server.common.enums.DeleteEnum;
import com.neptune.cloud.drive.server.context.file.*;
import com.neptune.cloud.drive.server.converter.FileConverter;
import com.neptune.cloud.drive.server.converter.UserFileConverter;
import com.neptune.cloud.drive.server.request.file.*;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.server.threadlocal.UserThreadLocal;
import com.neptune.cloud.drive.server.vo.DirectoryTreeNodeVO;
import com.neptune.cloud.drive.server.vo.SearchFileVO;
import com.neptune.cloud.drive.server.vo.UploadChunkVO;
import com.neptune.cloud.drive.server.vo.UserFileVO;
import com.neptune.cloud.drive.util.IdUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
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

    @Autowired
    private FileConverter fileConverter;


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
    public Response<Void> secondUploadUserFile(@Validated @RequestBody SecondUploadUserFileRequest request) {
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
        userFileService.secondUploadUserFile(context);
        log.info("UserFileController secondUploadFile: 秒传用户文件结束, request = {}", request);
        return Response.success();
    }

    /**
     * 上传文件
     */
    @ApiOperation(
            value = "上传文件",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/file/upload")
    public Response<Void> uploadUserFile(@Validated @RequestBody UploadUserFileRequest request) {
        log.info("UserFileController uploadUserFile: 开始上传用户文件, request = {}", request);
        // 1. 请求转换为上下文
        UploadUserFileContext context = userFileConverter.uploadUserFileRequest2UploadUserFileContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController uploadUserFile: 用户未登录, 不允许上传文件, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 设置到上下文中
        context.setUserId(userId);
        // 5. 调用上传文件的方法
        userFileService.uploadUserFile(context);
        log.info("UserFileController uploadUserFile: 上传用户文件结束, request = {}", request);
        return Response.success();
    }

    /**
     * 分片上传文件
     */
    @ApiOperation(
            value = "上传文件",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/file/chunk-upload")
    public Response<Boolean> uploadUserFileChunk(@Validated @RequestBody UploadUserFileChunkRequest request) {
        log.info("UserFileController uploadUserFileChunk: 开始分片上传用户文件, request = {}", request);
        // 1. 请求转换为上下文
        UploadUserFileChunkContext context = userFileConverter.uploadUserFileChunkRequest2UploadUserFileChunkContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController uploadUserFileChunk: 用户未登录, 不允许上传文件分片, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 设置到上下文中
        context.setUserId(userId);
        // 5. 调用分片上传文件的方法
        boolean merge = userFileService.uploadUserFileChunk(context);
        // 6. 返回是否可以合并的标识符
        log.info("UserFileController uploadUserFileChunk: 分片上传用户文件结束, request = {}, merge = {}", request, merge);
        return Response.success(merge);
    }

    /**
     * 合并已经上传的文件分片
     */
    @ApiOperation(
            value = "合并已经上传的文件分片",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/file/chunk-merge")
    public Response<Void> mergeUploadedUserFileChunk(@Validated @RequestBody MergeUserFileChunkRequest request) {
        log.info("UserFileController mergeUploadedUserFileChunk: 开始合并文件分片, request = {}", request);
        // 1. 请求转换为上下文
        MergeUserFileChunkContext context = userFileConverter.mergeUserFileChunkRequest2MergeUserFileChunkContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController mergeUploadedUserFileChunk: 用户未登录, 不允许合并分片, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 设置到上下文中
        context.setUserId(userId);
        // 5. 调用合并文件分片的方法
        userFileService.mergeUploadedUserFileChunk(context);
        log.info("UserFileController mergeUploadedUserFileChunk: 合并文件分片结束, request = {}", request);
        return Response.success();
    }

    /**
     * 下载文件
     */
    @ApiOperation(
            value = "下载文件",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @GetMapping("/file/download")
    public Response<Void> downloadUserFile(@NotBlank(message = "文件 ID") @RequestParam(value = "fileId", required = false) long fileId,
                                           @RequestParam(value = "response", required = false)HttpServletResponse response) {
        log.info("UserFileController downloadUserFile: 开始下载文件, fileId = {}", fileId);
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController mergeUploadedUserFileChunk: 用户未登录, 不允许下载文件, fileId = {}", fileId);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 请求转换为上下文
        DownloadUserFileContext context = new DownloadUserFileContext()
                .setUserId(userId)
                .setFileId(fileId)
                .setResponse(response);
        // 4. 调用下载文件的方法
        userFileService.downloadUserFile(context);
        log.info("UserFileController downloadUserFile: 下载文件结束, fileId = {}", fileId);
        return Response.success();
    }

    /**
     * 预览文件: 和下载文件不同的仅在于设置的响应头信息不同
     */
    @ApiOperation(
            value = "预览文件",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @GetMapping("/file/preview")
    public Response<Void> previewUserFile(@NotBlank(message = "文件 ID") @RequestParam(value = "fileId", required = false) long fileId,
                                           @RequestParam(value = "response", required = false)HttpServletResponse response) {
        log.info("UserFileController previewUserFile: 开始预览文件, fileId = {}", fileId);
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController previewUserFile: 用户未登录, 不允许预览文件, fileId = {}", fileId);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 请求转换为上下文
        PreviewUserFileContext context = new PreviewUserFileContext()
                .setUserId(userId)
                .setFileId(fileId)
                .setResponse(response);
        // 4. 调用下载文件的方法
        userFileService.previewUserFile(context);
        log.info("UserFileController previewUserFile: 预览文件结束, fileId = {}", fileId);
        return Response.success();
    }

    /**
     * 移动文件: 可以批量移动
     */
    @ApiOperation(
            value = "移动文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/file/transfer")
    public Response<Void> transferUserFile(@Validated @RequestBody TransferUserFileRequest request) {
        log.info("UserFileController transferUserFile: 开始移动用户文件, request = {}", request);
        // 1. 请求转换为上下文
        TransferUserFileContext context = userFileConverter.transferUserFileRequest2TransferUserFileContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController transferUserFile: 用户未登录, 不允许移动用户文件, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 解密源文件 ID 和目标文件 ID
        long targetId = 0;
        List<Long> sourceId = new ArrayList<>();
        try {
            targetId = IdUtil.decrypt(request.getTargetId());
            sourceId = IdUtil.decryptList(request.getSourceIds());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 4. 设置上下文
        context.setUserId(userId)
                .setTargetId(targetId).setSourceIds(sourceId);
        // 5. 调用移动用户文件的方法
        userFileService.transferUserFile(context);
        log.info("UserFileController transferUserFile: 移动用户文件结束, request = {}", request);
        return Response.success();
    }

    /**
     * 拷贝文件: 可以批量拷贝
     */
    @ApiOperation(
            value = "拷贝文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping("/file/copy")
    public Response<Void> copyUserFile(@Validated @RequestBody CopyUserFileRequest request) {
        log.info("UserFileController copyUserFile: 开始拷贝用户文件, request = {}", request);
        // 1. 请求转换为上下文
        CopyUserFileContext context = userFileConverter.copyUserFileRequest2CopyUserFileContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController copyUserFile: 用户未登录, 不允许拷贝用户文件, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 设置上下文
        context.setUserId(userId);
        // 5. 调用移动用户文件的方法
        userFileService.copyUserFile(context);
        log.info("UserFileController copyUserFile: 拷贝用户文件结束, request = {}", request);
        return Response.success();
    }

    /**
     * 搜索文件: (1) 使用模糊查询搜索文件 (2) 文件信息同步到 ElasticSearch, 全文搜索
     */
    @ApiOperation(
            value = "搜索文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/file/search")
    public Response<List<SearchFileVO>> searchUserFile() {

        return Response.success();
    }

    /**
     * 查询目录树: 保存用户文件时需要使用
     */
    @ApiOperation(
            value = "查询用户目录树",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/file/directory/tree")
    public Response<List<DirectoryTreeNodeVO>> listUserDirectoryTree() {
        log.info("UserFileController listUserDirectoryTree: 开始查询用户目录树");
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController listUserDirectoryTree: 用户未登录, 不允许查询用户目录树");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 设置用户 ID 到上下文中
        GetDirectoryTreeContext context =
                new GetDirectoryTreeContext().setUserId(userId);
        // 4. 调用查询用户目录树的方法
        List<DirectoryTreeNodeVO> tree = userFileService.listUserDirectoryTree(context);
        log.info("UserFileController listUserDirectoryTree: 查询用户目录树结束, tree = {}", tree.size());
        return Response.success(tree);
    }

    /**
     * 获取已经上传的文件分片
     */
    @ApiOperation(
            value = "上传文件",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping("/file/chunk-upload")
    public Response<List<UploadChunkVO>> listUploadedUserFileChunk(@Validated GetUserFileChunkRequest request) {
        log.info("UserFileController listUploadedUserFileChunk: 开始查询文件分片, request = {}", request);
        // 1. 请求转换为上下文
        GetUserFileChunkContext context = userFileConverter.getUserFileChunkReqest2GetUserFileChunkContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("UserFileController listUploadedUserFileChunk: 用户未登录, 不允许查询分片, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 设置到上下文中
        context.setUserId(userId);
        // 5. 查询文件分片
        List<UploadChunkVO> chunks = userFileService.listUploadedUserFileChunk(context);
        log.info("UserFileController listUploadedUserFileChunk: 查询文件分片结束, request = {}", request);
        return Response.success(chunks);
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
                .setParentId(realParentId)
                .setFileTypes(fileTypes)
                .setUserId(userId)
                .setDelete(DeleteEnum.NO.getFlag());
        // 4. 调用查询目录文件列表的方法
        List<UserFileVO> userFiles = userFileService.listUserFiles(context);
        log.info("UserFileController listUserFiles: 查询用户目录下的文件列表结束, parentId = {}, fileType = {}, size = {}", parentId, fileType, userFiles.size());
        return Response.success(userFiles);
    }

}
