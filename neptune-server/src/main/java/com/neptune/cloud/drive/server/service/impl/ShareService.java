package com.neptune.cloud.drive.server.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.cache.core.constant.CacheConstant;
import com.neptune.cloud.drive.constant.BasicConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.constant.ShareConstant;
import com.neptune.cloud.drive.server.common.enums.DeleteEnum;
import com.neptune.cloud.drive.server.common.enums.ShareDayType;
import com.neptune.cloud.drive.server.common.enums.ShareStatus;
import com.neptune.cloud.drive.server.config.CloudDriveSeverConfig;
import com.neptune.cloud.drive.server.context.file.CopyUserFileContext;
import com.neptune.cloud.drive.server.context.file.DownloadUserFileContext;
import com.neptune.cloud.drive.server.context.share.*;
import com.neptune.cloud.drive.server.mapper.ShareMapper;
import com.neptune.cloud.drive.server.model.Share;
import com.neptune.cloud.drive.server.model.ShareFile;
import com.neptune.cloud.drive.server.model.User;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.service.IShareFileService;
import com.neptune.cloud.drive.server.service.IShareService;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.server.service.IUserService;
import com.neptune.cloud.drive.server.vo.*;
import com.neptune.cloud.drive.util.IdUtil;
import com.neptune.cloud.drive.util.JwtUtil;
import com.neptune.cloud.drive.util.UUIDUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShareService extends ServiceImpl<ShareMapper, Share> implements IShareService {

    @Autowired
    private IUserService userService;

    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private IShareFileService shareFileService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CloudDriveSeverConfig config;

    /**
     * 创建分享链接
     */
    @Transactional(rollbackFor = BusinessException.class)
    @Override
    public CreateShareLinkVO createShareLink(CreateShareLinkContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 判断用户是否可以分享这些文件
        checkCreateShareLinkContext(context.getUserId(), context.getShareFileIds());
        // 2. 生成分享的文件的链接信息
        return doCreateShareLink(context.getUserId(), context.getShareName(),
                context.getShareType(), context.getShareDayType(), context.getShareFileIds());
    }

    /**
     * 取消分享链接
     */
    @Override
    public void cancelShareLink(CancelShareLinkContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 判断用户是否可以分享这些文件
        checkCancelShareLinkContext(context.getUserId(), context.getShareIds());
        // 2. 取消分享的文件的链接
        doCancelShareLink(context.getUserId(), context.getShareIds());
    }

    /**
     * 查询分享链接
     */
    @Override
    public List<ShareLinkVO> listShareLink(ListShareLinkContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 根据用户 ID 查询分享的链接
        List<Share> shareLinks = list(
                new QueryWrapper<Share>().eq("create_user", context.getUserId()));
        // 2. 判断是否查询成功
        if (CollectionUtils.isEmpty(shareLinks)) {
            return Collections.emptyList();
        }
        // 3. 封装为返回结果
        return shareLinks.stream().map(shareLink -> new ShareLinkVO()
                        .setShareId(shareLink.getShareId())
                        .setShareName(shareLink.getShareName())
                        .setShareType(shareLink.getShareType())
                        .setShareDayType(shareLink.getShareDayType())
                        .setShareExpireTime(shareLink.getShareEndTime())
                        .setShareUrl(shareLink.getShareUrl())
                        .setShareCode(shareLink.getShareCode())
                        .setShareStatus(shareLink.getShareStatus())
                        .setShareCreateTime(shareLink.getCreateTime()))
                .collect(Collectors.toList());
    }

    /**
     * 查询分享链接详情
     */
    @Override
    public ShareLinkInfoVO infoShareLink(GetShareLinkInfoContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询分享链接
        Share shareLink = getById(context.getShareId());
        // 2. 判断是否查询成功: 如果分享链接被取消, 就会查询不到
        if (Objects.isNull(shareLink)) {
            throw new BusinessException(ResponseCode.SHARE_CANCELLED.getCode(), ResponseCode.SHARE_CANCELLED.getMessage());
        }
        // 3. 判断分享链接的状态
        checkShareLinkStatus(shareLink);
        // 4. 查询分享用户的信息
        User user = userService.getById(shareLink.getCreateUser());
        // 5. 判断是否为空
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseCode.SHARE_CANCELLED.getCode(), "用户不存在");
        }
        // 6. 封装返回结果
        return new ShareLinkInfoVO()
                .setShareId(shareLink.getShareId())
                .setShareName(shareLink.getShareName())
                .setShareUser(new ShareUserVO().setUserId(user.getUserId()).setUsername(user.getUsername()));
    }

    /**
     * 校验分享链接的校验码
     */
    @Override
    public String checkShareCode(CheckShareCodeContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询分享链接
        Share shareLink = getById(context.getShareId());
        // 2. 判断是否查询成功: 如果分享链接被取消, 就会查询不到
        if (Objects.isNull(shareLink)) {
            throw new BusinessException(ResponseCode.SHARE_CANCELLED.getCode(), ResponseCode.SHARE_CANCELLED.getMessage());
        }
        // 3. 判断分享链接的状态
        checkShareLinkStatus(shareLink);
        // 4. 校验分享链接的校验码
        return doCheckShareCode(context.getShareCode(), shareLink);
    }

    /**
     * 保存分享文件
     */
    @Override
    public void storeShareFile(StoreShareFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询分享链接
        Share shareLink = getById(context.getShareId());
        // 2. 判断是否查询成功: 如果分享链接被取消, 就会查询不到
        if (Objects.isNull(shareLink)) {
            throw new BusinessException(ResponseCode.SHARE_CANCELLED.getCode(), ResponseCode.SHARE_CANCELLED.getMessage());
        }
        // 3. 判断分享链接的状态
        checkShareLinkStatus(shareLink);
        // 4. 判断分享文件的状态
        checkShareFileStatus(context.getShareId(), context.getShareFileIds());
        // 5. 保存分享文件
        doStoreShareFile(context.getUserId(), context.getShareFileIds(), context.getTargetId());
    }

    /**
     * 下载分享文件
     */
    @Override
    public void downloadShareFile(DownloadShareFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询分享链接
        Share shareLink = getById(context.getShareId());
        // 2. 判断是否查询成功: 如果分享链接被取消, 就会查询不到
        if (Objects.isNull(shareLink)) {
            throw new BusinessException(ResponseCode.SHARE_CANCELLED.getCode(), ResponseCode.SHARE_CANCELLED.getMessage());
        }
        // 3. 判断分享链接的状态
        checkShareLinkStatus(shareLink);
        // 4. 判断分享文件是否已经被删除: 在删除目录时, 仅更新了目录的状态, 没有递归更新子文件的状态, 所以可能这个文件标识为没有删除, 但是目录已经删除了
        checkShareFileStatus(context.getUserId(), Collections.singletonList(context.getShareFileId()));
        // 4. 下载分享文件
        doDownloadShareFile(context.getUserId(), context.getShareFileId(), context.getResponse());
    }

    /**
     * 查询分享的文件信息
     */
    @Override
    public ShareFileInfoVO infoShareFile(GetShareFileInfoContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询分享链接
        Share shareLink = getById(context.getShareId());
        // 2. 判断是否查询成功: 如果分享链接被取消, 就会查询不到
        if (Objects.isNull(shareLink)) {
            throw new BusinessException(ResponseCode.SHARE_CANCELLED.getCode(), ResponseCode.SHARE_CANCELLED.getMessage());
        }
        // 3. 判断分享链接的状态
        checkShareLinkStatus(shareLink);
        // 4. 查询分享用户的信息
        User user = userService.getById(shareLink.getCreateUser());
        // 5. 判断是否为空
        if (Objects.isNull(user)) {
            throw new BusinessException(ResponseCode.SHARE_CANCELLED.getCode(), "用户不存在");
        }
        // 6. 查询分享链接关联的文件
        List<Long> shareFileIds = getShareFiles(context.getShareId());
        // 7. 判断是否查询成功
        if (CollectionUtils.isEmpty(shareFileIds)) {
            return new ShareFileInfoVO();
        }
        // 6. TODO 查询分享的文件
        return null;
    }


    //============================================ private ============================================

    /**
     * 生成分享文件的链接
     */
    private CreateShareLinkVO doCreateShareLink(long userId, String shareName, int shareType, int shareDayType, List<Long> shareFileIds) {
        // 1. 封装分享链接的实体信息
        Share shareLink = assembleShareLink(userId, shareName, shareType, shareDayType);
        // 2. 记录分享链接
        if (!save(shareLink)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "生成分享链接失败");
        }
        // 3. 调用记录分享文件的接口
        shareFileService.createShareFile(new CreateShareFileContext()
                .setUserId(userId).setShareFileIds(shareFileIds).setShareId(shareLink.getShareId()));

        return new CreateShareLinkVO()
                .setShareId(shareLink.getShareId())
                .setShareName(shareLink.getShareName())
                .setShareUrl(shareLink.getShareUrl())
                .setShareCode(shareLink.getShareCode())
                .setShareStatus(shareLink.getShareStatus());
    }

    /**
     * 判断文件是否可以分享
     */
    private void checkCreateShareLinkContext(long userId, List<Long> shareFileIds) {
        // 1. 查询文件实体
        List<UserFile> files = userFileService.listByIds(shareFileIds);
        // 2. 判断是否查询到文件
        if (Objects.isNull(files)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "分享的文件不存在");
        }
        // 3. 判断用户是否可以分享
        if (files.stream()
                .anyMatch(file -> file.getUserId() != userId)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户无权分享该文件");
        }
        // 4. 判断分享文件的状态是否已经被删除
        if (files.stream()
                .anyMatch(file -> file.getDelFlag() == DeleteEnum.YES.getFlag())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "分享的文件已经被删除");
        }
    }

    /**
     * 封装分享链接实体
     */
    private Share assembleShareLink(long userId, String shareName, int shareType, int shareDayType) {
        // 1. 生成 ID
        long shareId = IdUtil.generate();
        // 1. 获取过期时间
        int expireDay = ShareDayType.getExpireDay(shareDayType);
        // 2. 判断是否获取到过期时间
        if (BasicConstant.NEGATIVE_ONE == expireDay) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "分享时间不正确");
        }
        return new Share()
                .setShareId(shareId)
                .setShareName(shareName)
                .setShareType(shareType)
                .setShareDayType(shareDayType)
                .setShareDay(expireDay)
                .setShareEndTime(DateUtil.offsetDay(new Date(), expireDay))
                .setShareUrl(generateShareUrl(shareId))
                .setShareCode(generateShareCode())
                .setShareStatus(ShareStatus.NORMAL.getStatus())
                .setCreateUser(userId)
                .setCreateTime(new Date());

    }

    /**
     * 生成分享链接
     */
    private String generateShareUrl(long shareId) {
        // 1. 获取分享链接前缀
        String shareLinkPrefix = config.getShareLinkPrefix();
        // 2. 判断是否为空
        if (Objects.isNull(shareLinkPrefix)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "分享链接生成失败");
        }
        // 3. 判断分享链接前缀是否合法
        if (shareLinkPrefix.lastIndexOf(StringConstant.SLASH) == BasicConstant.NEGATIVE_ONE) {
            shareLinkPrefix += StringConstant.SLASH;
        }
        // 4. 根据分享 ID 生成链接
        return shareLinkPrefix + shareId;
    }

    /**
     * 生成分享链接的验证码
     */
    private String generateShareCode() {
        return RandomStringUtils.randomAlphanumeric(4);
    }

    /**
     * 判断用户是否可以取消文件的分享
     */
    private void checkCancelShareLinkContext(long userId, List<Long> shareIds) {
        // 1. 查询分享链接
        List<Share> shareLinks = listByIds(shareIds);
        // 2. 判断是否查询成功
        if (Objects.isNull(shareLinks)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "取消分享的链接不存在");
        }
        // 3. 判断用户是否拥有权限
        if (shareLinks.stream()
                .anyMatch(shareLink -> shareLink.getCreateUser() != userId)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户无权取消该文件分享链接");
        }
        // 4. 判断分享链接是否已经过期
        if (shareLinks.stream()
                .anyMatch(shareLink ->
                        shareLink.getShareEndTime().before(new Date())
                                || shareLink.getShareStatus() == ShareStatus.DELETED.getStatus())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "取消分享的链接已经过期");
        }
    }

    /**
     * 取消文件的分享链接
     */
    private void doCancelShareLink(long userId, List<Long> shareIds) {
        // 1. 取消文件的分享链接: 影删除或者软删除应该都可以
        if (!removeByIds(shareIds)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "取消文件分享链接失败");
        }
        // 2. 取消分享的文件
        if (!shareFileService.remove(
                new QueryWrapper<ShareFile>()
                        .eq("create_user", userId)
                        .in("share_id", shareIds))) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "取消文件分享链接失败");
        }
    }

    /**
     * 校验分享链接的状态
     */
    private void checkShareLinkStatus(Share shareLink) {
        // 1. 判断分享链接是否已经被删除
        if (ShareStatus.DELETED.getStatus() == shareLink.getShareStatus()) {
            throw new BusinessException(ResponseCode.SHARE_FILE_MISS.getCode(), ResponseCode.SHARE_FILE_MISS.getMessage());
        }
        // 2. 判断分享链接是否已经过期
        if (shareLink.getShareDayType() != ShareDayType.PERMANENT_VALIDITY.getType()
                && shareLink.getShareEndTime().before(new Date())) {
            throw new BusinessException(ResponseCode.SHARE_EXPIRE.getCode(), ResponseCode.SHARE_EXPIRE.getMessage());
        }
    }

    /**
     * 校验分享码
     */
    private String doCheckShareCode(String shareCode, Share shareLink) {
        // 1. 校验分享链接的校验码
        if (!shareCode.equals(shareLink.getShareCode())) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "分享链接不存在");
        }
        // 2. 生成分享链接的 token: 如果没有 token, 那就是完全无状态的, 每次请求都要单独携带 share id
        String token = JwtUtil.generateToken(UUIDUtil.getUUID(),
                ShareConstant.SHARE_TOKEN_KEY, shareLink.getShareId(), ShareConstant.SHARE_TOKEN_EXPIRE_TIME);
        // 3. 记录在缓存中
        Cache cache = cacheManager.getCache(BasicConstant.CLOUD_DRIVE);
        // 4. 判断缓存是否为空
        if (Objects.isNull(cache)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        cache.put(CacheConstant.SHARE_TOKEN_PREFIX + shareLink.getShareId(), token);

        return token;
    }

    /**
     * 校验分享文件的状态
     */
    private void checkShareFileStatus(long shareId, List<Long> shareFileIds) {
        // 1. 查询分享链接关联的文件
        List<UserFile> files = getUserFiles(shareId);
        // 2. 判断是否查询到分享的文件
        if (CollectionUtils.isEmpty(files)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "没有查询到分享链接关联的文件");
        }
        // 3. 过滤所有处于删除状态的文件
        List<UserFile> availableFiles = files.stream()
                .filter(file -> file.getDelFlag() == DeleteEnum.YES.getFlag())
                .collect(Collectors.toList());
        // 4. 判断需要保存的分享文件是否在查询到的集合内
        Set<Long> fileIdSet = availableFiles.stream().map(UserFile::getFileId).collect(Collectors.toSet());

        if (!fileIdSet.containsAll(shareFileIds)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "存在无法保存的分享文件");
        }

    }

    /**
     * 查询分享文件
     */
    private List<UserFile> getUserFiles(long shareId) {
        // 1. 查询分享链接关联的文件
        List<Long> fileIds = getShareFiles(shareId);
        // 2. 判断是否查询成功
        if (CollectionUtils.isEmpty(fileIds)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "没有查询到分享链接关联的文件");
        }
        // 3. 调用接口递归查询分享链接中目录下的所有子文件
        List<UserFile> files = userFileService.selectUserChildFiles(fileIds);
        // 4. 判断是否查询到分享的文件
        if (CollectionUtils.isEmpty(files)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "没有查询到分享链接关联的文件");
        }
        return files;
    }

    /**
     * 查询分享链接关联的文件
     */
    private List<Long> getShareFiles(long shareId) {
        // 1. 封装查询条件
        QueryWrapper<ShareFile> queryWrapper = new QueryWrapper<ShareFile>()
                .select("file_id")
                .eq("share_id", shareId);
        // 2. 查询分享链接关联的文件
        return shareFileService.listObjs(
                queryWrapper, fileId -> Long.valueOf(String.valueOf(fileId)));
    }

    /**
     * 分享文件保存
     */
    private void doStoreShareFile(long userId, List<Long> shareFileIds, long targetId) {
        userFileService.copyUserFile(new CopyUserFileContext()
                .setUserId(userId)
                .setSourceIds(shareFileIds)
                .setTargetId(targetId)
                .setShare(true));
    }

    /**
     * 分享文件下载
     */
    private void doDownloadShareFile(long userId, long shareFileId, HttpServletResponse response) {
        userFileService.downloadUserFile(new DownloadUserFileContext()
                .setUserId(userId)
                .setFileId(shareFileId)
                .setResponse(response)
                .setShare(true));
    }
}




