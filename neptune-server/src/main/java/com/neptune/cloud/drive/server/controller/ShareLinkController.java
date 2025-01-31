package com.neptune.cloud.drive.server.controller;

import com.neptune.cloud.drive.response.Response;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.annotation.LoginIgnore;
import com.neptune.cloud.drive.server.common.annotation.NeedShareCode;
import com.neptune.cloud.drive.server.context.share.*;
import com.neptune.cloud.drive.server.converter.ShareLinkConverter;
import com.neptune.cloud.drive.server.model.File;
import com.neptune.cloud.drive.server.request.share.CancelShareLinkRequest;
import com.neptune.cloud.drive.server.request.share.CheckShareCodeRequest;
import com.neptune.cloud.drive.server.request.share.CreateShareLinkRequest;
import com.neptune.cloud.drive.server.request.share.StoreShareFileRequest;
import com.neptune.cloud.drive.server.service.IShareService;
import com.neptune.cloud.drive.server.threadlocal.ShareThreadLocal;
import com.neptune.cloud.drive.server.threadlocal.UserThreadLocal;
import com.neptune.cloud.drive.server.vo.*;
import com.neptune.cloud.drive.util.IdUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Api(value = "分享模块")
@Slf4j
@RestController
public class ShareLinkController {

    @Autowired
    private IShareService shareService;

    @Autowired
    private ShareLinkConverter shareLinkConverter;


    /**
     * 创建分享链接
     */
    @ApiOperation(
            value = "创建分享链接",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @PostMapping(value = "/share")
    public Response<CreateShareLinkVO> createShareLink(@Validated @RequestBody CreateShareLinkRequest request) {
        log.info("ShareLinkController createShareLink: 开始创建文件分享的链接, request={}", request);
        // 1. 请求转换为上下文
        CreateShareLinkContext context = shareLinkConverter.createShareLinkRequest2CreateShareLinkContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("ShareLinkController createShareLink: 用户未登录, 不允许创建文件分享的链接, request={}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 解密需要还原的回收站文件
        List<Long> shareFileIds = IdUtil.decryptList(request.getShareFileIds());
        // 4. 判断是否解密成功
        if (CollectionUtils.isEmpty(shareFileIds)) {
            log.error("ShareLinkController createShareLink: 解析分享文件的 ID 失败, request={}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "分享文件失败");
        }
        // 5. 设置上下文
        context.setUserId(userId)
                .setShareFileIds(shareFileIds);
        // 5. 调用方法创建分享链接
        CreateShareLinkVO shareLink = shareService.createShareLink(context);
        // 6. 判断是否生成成功
        if (Objects.isNull(shareLink)) {
            log.error("ShareLinkController createShareLink: 生成文件分享链接失败, request={}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "分享文件失败");
        }
        log.info("ShareLinkController createShareLink: 创建文件分享链接结束, request={}", request);
        return Response.success(shareLink);
    }

    /**
     * 取消分享链接
     */
    @ApiOperation(
            value = "取消分享链接",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @DeleteMapping(value = "/share")
    public Response<Void> cancelShareLink(@Validated @RequestBody CancelShareLinkRequest request) {
        log.info("ShareLinkController cancelShareLink: 开始取消文件分享的链接, request={}", request);
        // 1. 请求转换为上下文
        CancelShareLinkContext context = shareLinkConverter.cancelShareFileRequest2CancelShareFileContext(request);
        // 2. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 3. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("ShareLinkController cancelShareLink: 用户未登录, 不允许取消文件分享链接, request={}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 解密需要还原的回收站文件
        List<Long> shareIds = IdUtil.decryptList(request.getShareIds());
        // 5. 判断是否解密成功
        if (CollectionUtils.isEmpty(shareIds)) {
            log.error("ShareLinkController cancelShareLink: 解析取消的分享链接 ID 失败, request={}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), "取消分享链接失败");
        }
        // 6. 设置上下文
        context.setUserId(userId)
                .setShareIds(shareIds);
        // 7. 调用方法取消分享链接
        shareService.cancelShareLink(context);
        log.info("ShareLinkController cancelShareLink: 取消文件分享链接结束, request={}", request);
        return Response.success();
    }

    /**
     * 查询所有分享链接
     */
    @ApiOperation(
            value = "查询所有分享链接",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @GetMapping(value = "/shares")
    public Response<List<ShareLinkVO>> listShareLink() {
        log.info("ShareLinkController listShareLink: 开始查询用户分享的链接");
        // 1. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 2. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("ShareLinkController listShareLink: 用户未登录, 不允许查询分享的链接");
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 3. 设置到上下文中
        ListShareLinkContext context = new ListShareLinkContext().setUserId(userId);
        // 4. 调用查询用户分享的链接的方法
        List<ShareLinkVO> shareLinks = shareService.listShareLink(context);
        log.info("ShareLinkController listShareLink: 查询用户分享的链接结束");
        return Response.success(shareLinks);
    }

    /**
     * 查询分享链接
     */
    @ApiOperation(
            value = "查询分享链接",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @GetMapping(value = "/share")
    public Response<ShareLinkInfoVO> infoShareLink(@NotBlank(message = "分享链接 ID 不能为空") @RequestParam(value = "shareId", required = false) String shareId) {
        log.info("ShareLinkController infoShareLink: 开始查询分享链接信息, shareId = {}", shareId);
        // 1. 解析分享链接 ID
        long shareLinkId = 0;
        try {
            shareLinkId = IdUtil.decrypt(shareId);
        } catch (Exception e) {
            log.error("ShareLinkController infoShareLink: 解析文件 ID 失败, shareId = {}", shareId);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 2. 设置上下文
        GetShareLinkInfoContext context = new GetShareLinkInfoContext().setShareId(shareLinkId);
        // 3. 调用查询链接详情的方法
        ShareLinkInfoVO shareLinkInfo = shareService.infoShareLink(context);
        log.info("ShareLinkController infoShareLink: 查询分享链接信息结束, shareId = {}", shareId);
        return Response.success(shareLinkInfo);
    }

    /**
     * 校验分享链接的校验码
     */
    @ApiOperation(
            value = "校验分享链接的校验码",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @PostMapping("/share/code/check")
    public Response<String> checkShareCode(@Validated @RequestBody CheckShareCodeRequest request) {
        log.info("ShareLinkController checkShareCode: 开始校验分享链接的校验码, request = {}", request);
        // 1. 请求转换为上下文
        CheckShareCodeContext context = shareLinkConverter.checkShareCodeRequest2CheckShareCodeContext(request);
        // 2. 调用校验分享码的方法
        String token = shareService.checkShareCode(context);
        // 3. 判断是否校验并生成 token 成功
        if (StringUtils.isEmpty(token)) {
            log.error("ShareLinkController checkShareCode: 校验分享链接的校验码失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 4. 返回 token
        log.info("ShareLinkController checkShareCode: 校验分享链接的校验码结束, request = {}", request);
        return Response.success(token);
    }

    /**
     * 保存分享的文件
     */
    @ApiOperation(
            value = "保存分享文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @NeedShareCode
    @PostMapping(value = "/share/file/store")
    public Response<Void> storeShareFile(@Validated @RequestBody StoreShareFileRequest request) {
        log.info("ShareLinkController storeShareFile: 开始保存分享的文件, request = {}", request);
        // 1. 请求转换为上下文
        StoreShareFileContext context = shareLinkConverter.storeShareFileRequest2StoreShareFileContext(request);
        // 2. 获取分享链接的 ID
        Long shareId = ShareThreadLocal.get();
        // 3. 判断分享链接 ID 是否为空
        if (Objects.isNull(shareId)) {
            log.error("ShareLinkController storeShareFile: 用户没有校验分享码, 不允许保存文件, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 3. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 4. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("ShareLinkController storeShareFile: 用户未登录, 不允许保存文件, request = {}", request);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 5. 解析分享文件的 ID
        List<Long> shareFileIds = IdUtil.decryptList(request.getShareFileIds());
        // 6. 判断是否解析成功
        if (CollectionUtils.isEmpty(shareFileIds)) {
            log.error("ShareLinkController storeShareFile: 解析分享文件 ID 失败, request = {}", request);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 7. 设置上下文
        context.setUserId(userId)
                .setShareId(shareId)
                .setShareFileIds(shareFileIds);
        // 8. 调用保存分享文件的方法
        shareService.storeShareFile(context);
        log.info("ShareLinkController storeShareFile: 保存分享的文件结束, request = {}", request);
        return Response.success();
    }

    /**
     * 下载分享的文件
     */
    @ApiOperation(
            value = "下载分享文件",
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @NeedShareCode
    @PostMapping("/share/file/download")
    public Response<Void> downloadShareFile(@NotBlank(message = "下载的文件 ID 不能为空") @RequestParam(value = "shareFileId") String shareFileId,
                                            @NotNull(message = "响应结果不能为空") @RequestParam(value = "response") HttpServletResponse response) {
        log.info("ShareLinkController downloadShareFile: 开始下载分享的文件, shareFileId = {}", shareFileId);
        // 1. 获取分享链接的 ID
        Long shareId = ShareThreadLocal.get();
        // 2. 判断分享链接 ID 是否为空
        if (Objects.isNull(shareId)) {
            log.error("ShareLinkController downloadShareFile: 用户没有校验分享码, 不允许下载文件, shareFileId = {}", shareFileId);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 3. 获取用户 ID
        Long userId = UserThreadLocal.get();
        // 4. 判断用户 ID 是否为空
        if (Objects.isNull(userId)) {
            log.error("ShareLinkController storeShareFile: 用户未登录, 不允许下载文件, shareFileId = {}", shareFileId);
            return Response.fail(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getMessage());
        }
        // 4. 解析分享文件的 ID
        long fileId = 0;
        try {
            fileId = IdUtil.decrypt(shareFileId);
        } catch (Exception exception) {
            log.error("ShareLinkController downloadShareFile: 解析文件 ID 失败, shareFileId = {}", shareFileId);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 5. 设置上下文
        DownloadShareFileContext context = new DownloadShareFileContext()
                .setUserId(userId)
                .setShareId(shareId)
                .setShareFileId(fileId)
                .setResponse(response);
        // 6. 调用保存分享文件的方法
        shareService.downloadShareFile(context);
        log.info("ShareLinkController downloadShareFile: 下载分享的文件结束, shareFileId = {}", shareFileId);
        return Response.success();
    }


    /**
     * 查询分享的详情
     */
    @ApiOperation(
            value = "查询分享文件详情",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @LoginIgnore
    @NeedShareCode
    @GetMapping(value = "/share/file/info")
    public Response<ShareFileInfoVO> infoShareFile() {
        log.info("ShareLinkController infoShareFile: 开始查询分享链接的文件信息");
        // 2. 获取分享链接的 ID
        Long shareId = ShareThreadLocal.get();
        // 3. 判断分享链接 ID 是否为空
        if (Objects.isNull(shareId)) {
            log.error("ShareLinkController infoShareFile: 用户没有校验分享码, 不允许查询分享文件信息");
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 4. 设置上下文
        GetShareFileInfoContext context = new GetShareFileInfoContext().setShareId(shareId);
        // 5. 调用查询分享链接详情的方法
        ShareFileInfoVO shareFileInfo = shareService.infoShareFile(context);
        // 6. 判断是否查询成功
        if (Objects.isNull(shareFileInfo)) {
            log.error("ShareLinkController infoShareFile: 查询分享链接详情失败, shareId = {}", shareId);
            return Response.fail(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        log.info("ShareLinkController infoShareFile: 查询分享链接的文件信息结束");
        return Response.success(shareFileInfo);
    }

    /**
     * 查询下一级的分享文件
     */
    @LoginIgnore
    @NeedShareCode
    @GetMapping(value = "/share/file/list")
    public Response<List<ShareFileVO>> listNextShareFile(@NotBlank(message = "文件的父ID不能为空") @RequestParam(value = "parentId", required = false) String parentId){


        return Response.success();
    }

}
