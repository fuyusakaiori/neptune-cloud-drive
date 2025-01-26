package com.neptune.cloud.drive.server.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.constant.BasicConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.constant.FileConstant;
import com.neptune.cloud.drive.server.common.enums.FileType;
import com.neptune.cloud.drive.server.common.enums.DeleteEnum;
import com.neptune.cloud.drive.server.common.enums.DirectoryEnum;
import com.neptune.cloud.drive.server.common.event.DeleteUserFileEvent;
import com.neptune.cloud.drive.server.context.file.*;
import com.neptune.cloud.drive.server.context.user.GetUserRootDirContext;
import com.neptune.cloud.drive.server.converter.UserFileConverter;
import com.neptune.cloud.drive.server.mapper.UserFileMapper;
import com.neptune.cloud.drive.server.model.File;
import com.neptune.cloud.drive.server.model.FileChunk;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.service.IFileChunkService;
import com.neptune.cloud.drive.server.service.IFileService;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.server.vo.DirectoryTreeNodeVO;
import com.neptune.cloud.drive.server.vo.UploadChunkVO;
import com.neptune.cloud.drive.server.vo.UserFileVO;
import com.neptune.cloud.drive.util.FileUtil;
import com.neptune.cloud.drive.util.IdUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserFileService extends ServiceImpl<UserFileMapper, UserFile> implements IUserFileService, ApplicationContextAware {


    @Autowired
    private UserFileConverter userFileConverter;

    @Autowired
    private IFileService fileService;

    @Autowired
    private IFileChunkService fileChunkService;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 创建用户目录
     */
    @Override
    public long createUserDirectory(CreateUserDirectoryContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 创建用户目录
        return createUserFile(
                context.getUserId(),
                context.getParentId(),
                0,
                context.getDirectoryName(),
                StringConstant.EMPTY,
                0,
                DirectoryEnum.YES);
    }

    /**
     * 查询用户根目录
     */
    @Override
    public UserFile selectUserRootDirectory(GetUserRootDirContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询用户根目录
        return selectUserRootDirectory(context.getUserId());
    }

    /**
     * 重命名用户文件
     */
    @Override
    public void renameUserFile(RenameUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 判断重命名文件的 ID 是否有效
        checkRenameUserFile(context.getUserId(), context.getFileId(), context.getNewFilename());
        // 2. 重命名文件
        doRenameUserFile(context.getUserId(), context.getFileId(), context.getNewFilename());
    }

    /**
     * 删除用户文件
     */
    @Override
    public void deleteUserFile(DeleteUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 判断删除文件的 ID 是否有效
        checkDeleteUserFile(context.getUserId(), context.getFileIdList());
        // 2. 逻辑删除文件
        doDeleteUserFile(context.getUserId(), context.getFileIdList());
        // 3. 发布删除文件的事件, 交给存储引擎删除
        afterDeleteUserFile(context.getFileIdList());
    }

    /**
     * 秒传用户文件
     */
    @Override
    public void secondUploadUserFile(SecondUploadUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 根据文件唯一标识符查询真实的文件
        File file = selectUserFileIdentifier(context.getUserId(), context.getIdentifier());
        // 2. 判断是否查询到真实的文件
        if (Objects.isNull(file)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "秒传失败");
        }
        // 3. 如果存在对应的文件, 那么就链接到用户文件中
        long userFileId = createUserFile(
                context.getUserId(),
                context.getParentId(),
                file.getFileId(),
                // 注: 用户文件名可能和真实文件名不同, 不要使用真实文件名
                context.getFilename(),
                file.getFileSizeDesc(),
                FileType.getFileTypeCode(FileUtil.getFileSuffix(context.getFilename())),
                DirectoryEnum.NO);
        // 4. 判断是否链接成功
        if (userFileId <= 0) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "秒传失败");
        }
    }

    /**
     * 上传用户文件
     */
    @Override
    public void uploadUserFile(UploadUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 调用文件接口上传文件
        uploadFile(context.getUserId(), context.getFileName(),
                context.getFileSize(), context.getIdentifier(), context.getFile());
        // 2. 查询上传的文件
        File file = selectUserFileIdentifier(context.getUserId(), context.getIdentifier());
        // 3. 判断是否上传成功
        if (Objects.isNull(file)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "上传文件失败");
        }
        // 4. 用户文件链接到真实文件
        long userFileId = createUserFile(context.getUserId(), context.getParentId(), file.getFileId(), context.getFileName(), file.getFileSizeDesc(),
                FileType.getFileTypeCode(FileUtil.getFileSuffix(context.getFileName())), DirectoryEnum.NO);
        if (userFileId <= 0) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文件链接到用户失败");
        }
    }

    /**
     * 分片上传用户文件
     */
    @Override
    public boolean uploadUserFileChunk(UploadUserFileChunkContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 调用文件接口上传文件分片
        return uploadFileChunk(
                context.getUserId(),
                context.getIdentifier(),
                context.getFileName(),
                context.getChunkSeq(),
                context.getChunkCount(),
                context.getChunkSize(),
                context.getChunk());
    }

    /**
     * 查询文件的分片
     */
    @Override
    public List<UploadChunkVO> listUploadedUserFileChunk(GetUserFileChunkContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询文件的分片
        List<FileChunk> chunks = listFileChunk(context.getUserId(), context.getIdentifier());
        // 2. 转换为查询结果
        return chunks.stream()
                .map(chunk -> new UploadChunkVO().setChunkId(chunk.getChunkId()))
                .collect(Collectors.toList());

    }

    /**
     * 合并文件分片
     */
    @Override
    public void mergeUploadedUserFileChunk(MergeUserFileChunkContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 调用文件接口合并文件
        mergeFileChunk(
                context.getUserId(),
                context.getIdentifier(),
                context.getFileName(),
                context.getFileSize());
        // 2. 根据文件唯一标识符查询真实的文件
        File file = selectUserFileIdentifier(context.getUserId(), context.getIdentifier());
        // 3. 判断是否查询到真实的文件
        if (Objects.isNull(file)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "合并文件分片失败");
        }
        // 4. 链接文件到对应的用户
        long userFileId = createUserFile(
                context.getUserId(),
                context.getParentId(),
                file.getFileId(),
                // 注: 用户文件名可能和真实文件名不同, 不要使用真实文件名
                context.getFileName(),
                file.getFileSizeDesc(),
                FileType.getFileTypeCode(FileUtil.getFileSuffix(context.getFileName())),
                DirectoryEnum.NO);
        if (userFileId <= 0) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "合并文件分片失败");
        }
    }

    /**
     * 下载用户文件
     */
    @Override
    public void downloadUserFile(DownloadUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询下载的文件
        UserFile userFile = getById(context.getFileId());
        // 2. 判断是否查询到需要下载的文件
        if (Objects.isNull(userFile)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "下载的文件不存在");
        }
        // 3. 判断文件是否为目录: 如果需要支持, 那么就查询出所有文件, 然后并发下载
        if (DirectoryEnum.YES.getFlag() == userFile.getFolderFlag()) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "不支持直接下载目录");
        }
        // 4. 调用文件接口下载文件
        doDownloadUserFile(userFile.getRealFileId(), context.getResponse());
    }

    /**
     * 预览文件
     */
    @Override
    public void previewUserFile(PreviewUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询下载的文件
        UserFile userFile = getById(context.getFileId());
        // 2. 判断是否查询到需要下载的文件
        if (Objects.isNull(userFile)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "下载的文件不存在");
        }
        // 3. 判断文件是否为目录: 如果需要支持, 那么就查询出所有文件, 然后并发下载
        if (DirectoryEnum.YES.getFlag() == userFile.getFolderFlag()) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "不支持直接预览目录");
        }
        // 5. 调用文件接口预览文件
        doPreviewUserFile(userFile.getRealFileId(), context.getResponse());
    }

    /**
     * 查询目录树
     * <p>(1) 查询出所有目录到内存中, 然后拼接成目录树</p>
     * <p>(2) 查询根目录的子目录, 递归查询子目录的子目录</p>
     */
    @Override
    public List<DirectoryTreeNodeVO> listUserDirectoryTree(GetDirectoryTreeContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询出用户所有目录
        List<UserFile> directories = selectUserDirectories(context.getUserId());
        // 2. 判断是否存在目录
        if (CollectionUtils.isEmpty(directories)) {
            return Collections.emptyList();
        }
        // 3. 根据目录的映射关系封装成目录树
        return assembleDirectoryTree(directories);
    }

    /**
     * 移动文件: 只需要更新逻辑链接, 不需要真正移动文件
     */
    @Override
    public void transferUserFile(TransferUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 校验是否可以移动文件
        checkTransferUserFile(context.getUserId(), context.getTargetId(), context.getSourceIds());
        // 2. 将文件链接到目标目录
        doTransferUserFile(context.getUserId(), context.getTargetId(), context.getSourceIds());
    }

    /**
     * 拷贝文件: 只需要建立逻辑链接, 不需要真正拷贝文件
     */
    @Override
    public void copyUserFile(CopyUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 校验是否可以移动文件
        checkCopyUserFile(context.getUserId(), context.getTargetId(), context.getSourceIds());
        // 2. 新建文件链接
        doCopyUserFile(context.getUserId(), context.getTargetId(), context.getSourceIds());
    }

    /**
     * 查询用户文件列表
     */
    @Override
    public List<UserFileVO> listUserFiles(ListUserFileContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询用户目录的文件列表
        List<UserFile> userFiles = baseMapper.listUserFiles(context.getUserId(), context.getParentId(), context.getFileTypes());
        // 2. 转换为返回结果
        return userFiles.stream()
                .map(userFile -> userFileConverter.userFile2UserFileVO(userFile))
                .collect(Collectors.toList());
    }

    //============================================= private =============================================
    /**
     * 创建文件/目录
     */
    private long createUserFile(long userId, long parentId, long realId, String filename, String description, int fileType, DirectoryEnum isDirectory) {
        // 1. 封装文件实体
        UserFile file = assembleUserFile(userId, parentId, realId, filename, description, fileType, isDirectory);
        // 2. 判断文件实体是否为空
        if (Objects.isNull(file)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 3. 创建文件
        if (!save(file)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "创建文件或者目录失败");
        }
        return file.getFileId();
    }

    /**
     * 封装文件实体
     */
    private UserFile assembleUserFile(long userId, long parentId, long realId, String filename, String description, int fileType, DirectoryEnum isFolder) {
        // 1. 为重复的文件名添加标识符
        filename = generateUniqueFilename(userId, parentId, filename, isFolder);
        // 2. 封装文件实体信息
        return new UserFile().setUserId(userId)
                .setParentId(parentId)
                .setFileId(IdUtil.generate())
                .setRealFileId(realId)
                .setFilename(filename)
                .setFileSizeDesc(description)
                .setFileType(fileType)
                .setFolderFlag(isFolder.getFlag())
                .setDelFlag(DeleteEnum.NO.getFlag())
                .setCreateUser(userId)
                .setUpdateUser(userId)
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
    }

    /**
     * 生成唯一的文件名
     */
    private String generateUniqueFilename(long userId, long parentId, String filename, DirectoryEnum isFolder) {
        // 1. 获取文件名称和扩展名的分割处
        int filenameSuffixPosition = filename.lastIndexOf(StringConstant.POINT);
        // 2. 获取文件名称和文件扩展名
        String filenameWithoutSuffix = filename;
        String filenameSuffix = StringConstant.EMPTY;
        if (filenameSuffixPosition != -1) {
            filenameWithoutSuffix = filename.substring(0, filenameSuffixPosition);
            filenameSuffix = filename.substring(filenameSuffixPosition);
        }
        // 3. 获取重复的文件数量
        int count = selectUserFilenameCount(userId, parentId, filenameWithoutSuffix, isFolder);
        // 4. 判断是否存在重复的文件
        if (count == 0) {
            return filename;
        }
        // 5. 如果存在重复的文件, 就添加相应的标识符加以区分
        return filenameWithoutSuffix +
                StringConstant.LEFT_PAREN +
                count +
                StringConstant.RIGHT_PAREN +
                filenameSuffix;
    }

    /**
     * 获取用户相同文件的数量
     */
    private int selectUserFilenameCount(long userId, long parentId, String filenameWithoutSuffix, DirectoryEnum isFolder) {
        // 1. 封装查询条件
        QueryWrapper<UserFile> queryWrapper = new QueryWrapper<UserFile>()
                .eq("user_id", userId)
                .eq("parent_id", parentId)
                .eq("folder_flag", isFolder.getFlag())
                .eq("del_flag", DeleteEnum.NO.getFlag())
                .likeLeft("filename", filenameWithoutSuffix);
        // 2. 查询数量
        return count(queryWrapper);
    }

    /**
     * 查询用户根目录
     */
    private UserFile selectUserRootDirectory(long userId) {
        QueryWrapper<UserFile> queryWrapper = new QueryWrapper<UserFile>()
                .eq("user_id", userId)
                .eq("parent_id", FileConstant.ROOT_PARENT_ID)
                .eq("folder_flag", DirectoryEnum.YES.getFlag())
                .eq("del_flag", DeleteEnum.NO.getFlag());
        return getOne(queryWrapper);
    }

    /**
     * 校验重命名的文件是否合法
     */
    private void checkRenameUserFile(long userId, long fileId, String newFilename) {
        // 1. 根据文件 ID 查询文件信息
        UserFile userFile = getById(fileId);
        // 2. 判断是否查询到文案金
        if (Objects.isNull(userFile)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "重命名的文件不存在");
        }
        // 3. 判断查询到的文件 ID 和重命名的文件 ID 是否相同
        if (userFile.getFileId() != fileId) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "重命名的文件不存在");
        }
        // 4. 判断查询到的文件所属用户的 ID 是否和重命名文件所属用户 ID 相同
        if (userFile.getUserId() != userId) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "不允许重命名其他用户的文件");
        }
        // 5. 判断新的文件名字是否和原来的名字相同
        if (userFile.getFilename().equals(newFilename)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "新的文件名字不能和旧的文件名字相同");
        }
        // 6. 判断新的文件名字是否和目录下其他文件名字相同
        QueryWrapper<UserFile> queryWrapper = new QueryWrapper<UserFile>()
                .eq("parent_id", userFile.getParentId())
                .eq("filename", newFilename);
        if (count(queryWrapper) > 0) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "目录下已经存在相同名字的文件, 请更换新的文件名称");
        }
    }

    /**
     * 重命名文件
     */
    private void doRenameUserFile(long userId, long fileId, String newFilename) {
        UpdateWrapper<UserFile> updateWrapper = new UpdateWrapper<UserFile>()
                .eq("user_id", userId)
                .eq("file_id", fileId)
                .set("filename", newFilename)
                .set("update_time", new Date());
        if (!update(updateWrapper)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "重命名文件失败");
        }
    }

    /**
     * 校验删除的文件是否合法
     */
    private void checkDeleteUserFile(long userId, List<Long> fileIdList) {
        // 1. 根据文件 ID 查询文件信息
        List<UserFile> userFiles = listByIds(fileIdList);
        // 2. 判断查询的文件数量是否符合预期
        if (CollectionUtil.isEmpty(userFiles) || userFiles.size() != fileIdList.size()) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "需要删除的文件不合法");
        }
        // 3. 将查询到的文件转换为对应的文件 ID
        Set<Long> fileIdSet = userFiles.stream().map(UserFile::getFileId).collect(Collectors.toSet());
        // 4. 记录查询到的文件 ID 数量
        int oldSize = fileIdSet.size();
        // 5. 将需要删除的文件 ID 放入查询到的文件 ID 集合中
        fileIdSet.addAll(fileIdList);
        // 6. 记录新的文件 ID 数量
        int newSize = fileIdSet.size();
        // 7. 判断查询的文件是否和删除的文件完全一致
        if (oldSize != newSize) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "需要删除的文件不合法");
        }
        // 8. 将查询到的文件转换为对应的用户 ID
        Set<Long> userIdSet = userFiles.stream().map(UserFile::getUserId).collect(Collectors.toSet());
        // 9. 判断用户 ID 是否相同
        if (userIdSet.size() != 1) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "不允许删除其他用户的文件");
        }
        // 10. 判断删除文件所属的用户是否合法
        if (!userIdSet.contains(userId)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "不允许删除其他用户的文件");
        }
    }

    /**
     * 逻辑删除用户文件
     */
    private void doDeleteUserFile(long userId, List<Long> fileIdList) {
        // 1. 封装更新条件
        UpdateWrapper<UserFile> updateWrapper = new UpdateWrapper<UserFile>()
                .eq("user_id", userId)
                .in("file_id", fileIdList)
                .set("del_flag", DeleteEnum.YES.getFlag())
                .set("update_time", new Date());
        // 2. 删除文件
        if (!update(updateWrapper)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "删除文件失败");
        }
    }

    /**
     * 删除用户文件的后置处理
     */
    private void afterDeleteUserFile(List<Long> fileIdList) {
        applicationContext.publishEvent(new DeleteUserFileEvent(this, fileIdList));
    }

    /**
     * 根据文件唯一标识符查询真实文件
     */
    private File selectUserFileIdentifier(long userId, String identifier) {
        // 1. 根据唯一标识符查询真实文件
        // TODO 暂时不清楚为什么会查询到多个文件
        List<File> files = fileService.listFiles(new GetFileContext()
                .setUserId(userId).setIdentifier(identifier));
        // 2. 判断文件集合是否为空
        if (CollectionUtil.isEmpty(files)) {
            return null;
        }
        // 3. 仅使用第一个文件
        return files.get(BasicConstant.ZERO);
    }

    /**
     * 上传文件
     */
    private void uploadFile(long userId, String fileName, long fileSize, String identifier, MultipartFile file) {
        fileService.uploadFile(new UploadFileContext()
                .setUserId(userId)
                .setFileName(fileName)
                .setFileSize(fileSize)
                .setIdentifier(identifier)
                .setFile(file));
    }

    /**
     * 分片上传文件
     */
    private boolean uploadFileChunk(long userId, String identifier, String fileName, long chunkSeq, long chunkCount, long chunkSize, MultipartFile chunk) {
        return fileChunkService.uploadFileChunk(
                new UploadFileChunkContext()
                        .setUserId(userId)
                        .setIdentifier(identifier)
                        .setFileName(fileName)
                        .setChunkSeq(chunkSeq)
                        .setChunkCount(chunkCount)
                        .setChunkSize(chunkSize)
                        .setChunk(chunk));
    }

    /**
     * 查询文件分片
     */
    private List<FileChunk> listFileChunk(long userId, String identifier) {
        QueryWrapper<FileChunk> queryWrapper = new QueryWrapper<FileChunk>()
                .eq("identifier", identifier)
                .eq("create_user", userId)
                // expiration_time > current time
                .gt("expiration_time", new Date());
        return fileChunkService.list(queryWrapper);
    }

    /**
     * 合并文件分片
     */
    private void mergeFileChunk(long userId, String identifier, String fileName, long fileSize) {
        fileService.mergeFileChunk(new MergeFileChunkContext()
                .setUserId(userId)
                .setIdentifier(identifier)
                .setFileName(fileName)
                .setFileSize(fileSize));
    }

    /**
     * 下载文件
     */
    private void doDownloadUserFile(long fileId, HttpServletResponse response) {
        // 1. 调用文件接口下载文件
        fileService.downloadFile(new DownloadFileContext()
                .setFileId(fileId)
                .setResponse(response));
    }

    /**
     * 预览文件
     */
    private void doPreviewUserFile(long fileId, HttpServletResponse response) {
        // 1. 调用文件接口下载文件
        fileService.previewFile(new DownloadFileContext()
                .setFileId(fileId)
                .setResponse(response));
    }

    /**
     * 查询目录
     */
    private List<UserFile> selectUserDirectories(long userId) {
        QueryWrapper<UserFile> queryWrapper = new QueryWrapper<UserFile>()
                .eq("user_id", userId)
                .eq("folder_flag", DirectoryEnum.YES.getFlag())
                .eq("del_flag", DeleteEnum.NO.getFlag());
        return list(queryWrapper);
    }

    /**
     * 封装目录树: 是否可以单纯使用 SQL 完成
     */
    private List<DirectoryTreeNodeVO> assembleDirectoryTree(List<UserFile> directories) {
        // 1. 将目录转换为树的节点
        List<DirectoryTreeNodeVO> directoryNodes = directories.stream()
                .map(directory -> userFileConverter.UserFile2DirectoryTreeNodeVO(directory))
                .collect(Collectors.toList());
        // 2. 再将目录树的节点转换成哈希表
        Map<Long, List<DirectoryTreeNodeVO>> directoryNodeMapping = directoryNodes.stream()
                .collect(Collectors.groupingBy(DirectoryTreeNodeVO::getParentId));
        // 3. 遍历所有目录树节点; 注: 这么做才能够保证节点集合中的对象是相同的
        for (DirectoryTreeNodeVO directoryNode : directoryNodes) {
            // 4. 获取当前节点和子节点的映射关系, 然后将其添加到子节点对应的子节点集合中
            directoryNode.getChildren().addAll(directoryNodeMapping.get(directoryNode.getDirectoryId()));
        }
        // 4. 移除非根节点的节点; 注: 不能够先将非根节点移除
        return directoryNodes.stream()
                .filter(directoryNode -> directoryNode.getParentId() != FileConstant.ROOT_PARENT_ID)
                .collect(Collectors.toList());
    }

    /**
     * 校验是否可以移动文件
     * <p>(1) 目标文件必须是目录</p>
     * <p>(2) 不能将父目录移动到子目录</p>
     */
    private void checkTransferUserFile(long userId, long targetId, List<Long> sourceIds) {
        // 1. 查询目标文件是否为目录
        UserFile userFile = getById(targetId);
        // 2. 判断是否查询成功
        if (Objects.isNull(userFile)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "目标目录不存在");
        }
        // 3. 判断是否为目录
        if (DirectoryEnum.YES.getFlag() != userFile.getFolderFlag()) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "目标文件不是目录");
        }
        // 4. 查询所有需要移动的文件
        List<UserFile> userFiles = listByIds(sourceIds);
        // 5. 判断是否查询成功
        if (CollectionUtil.isEmpty(userFiles)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "需要移动的文件不存在");
        }
        // TODO: 判断是否查询的是用户的文件
        if (userFiles.stream().anyMatch(file -> file.getUserId() != userId)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "禁止移动其他用户的文件");
        }
        // 6. 过滤所有需要移动的目录
        List<UserFile> moveDirectories = userFiles.stream()
                .filter(file -> DirectoryEnum.YES.getFlag() == userFile.getFolderFlag())
                .collect(Collectors.toList());
        // 7. 判断是否需要移动目录: 如果不需要移动目录, 就直接返回
        if (CollectionUtil.isEmpty(moveDirectories)) {
            return;
        }
        // 8. 查询用户的所有目录
        List<UserFile> allDirectories = selectUserDirectories(userId);
        // 9. 判断是否查询成功
        if (CollectionUtil.isEmpty(allDirectories)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "用户不存在任何目录信息");
        }
        // 10. 将用户的所有目录分组
        Map<Long, List<UserFile>> parentMapping = allDirectories.stream()
                .collect(Collectors.groupingBy(UserFile::getParentId));
        // 11. 递归查询所有子目录
        List<UserFile> forbiddenTargetDirectories = new ArrayList<>();
        for (UserFile moveDirectory : moveDirectories) {
            findChildDirectory(moveDirectory, parentMapping, forbiddenTargetDirectories);
        }
        // 12. 判断目标目录是否在子目录中
        if (forbiddenTargetDirectories.stream()
                .anyMatch(childrenDirectory -> childrenDirectory.getFileId() == targetId)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "不允许将目录移动到自己的子目录中");
        }
    }

    /**
     * 递归查询所有需要移动的目录的子目录
     */
    private void findChildDirectory(UserFile moveDirectory, Map<Long, List<UserFile>> parentMapping, List<UserFile> forbiddenTargetDirectories) {
        // 1. 查询需要移动的目录的子目录
        List<UserFile> childrenDirectories = parentMapping.get(moveDirectory.getFileId());
        // 2. 判断是否为空
        if (CollectionUtil.isEmpty(childrenDirectories)) {
            return;
        }
        // 3. 如果不为空, 就添加到子目录集合中
        forbiddenTargetDirectories.addAll(childrenDirectories);
        // 4. 递归查询
        for (UserFile childrenDirectory : childrenDirectories) {
            findChildDirectory(childrenDirectory, parentMapping, forbiddenTargetDirectories);
        }
    }

    /**
     * 移动文件
     */
    private void doTransferUserFile(long userId, long targetId, List<Long> sourceIds) {
        // 1. 查询所有需要移动的文件
        List<UserFile> userFiles = listByIds(sourceIds);
        // 2. 判断是否查询成功
        if (CollectionUtil.isEmpty(userFiles)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "需要移动的文件不存在");
        }
        // 3. 判断是否移动的是用户的文件
        if (userFiles.stream().anyMatch(file -> file.getUserId() != userId)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "禁止移动其他用户的文件");
        }
        // 4. 重新设置文件的目录 ID
        for (UserFile userFile : userFiles) {
            String newFileName = generateUniqueFilename(userId, targetId,
                    userFile.getFilename(), DirectoryEnum.getDirectoryEnum(userFile.getFolderFlag()));
            userFile.setParentId(targetId);
            userFile.setFilename(newFileName);
            userFile.setCreateTime(new Date());
            userFile.setUpdateTime(new Date());
        }
        // 5. 更新移动的文件
        if (!updateBatchById(userFiles)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "移动文件失败");
        }
    }

    /**
     * 校验是否可以拷贝文件
     */
    private void checkCopyUserFile(long userId, long targetId, List<Long> sourceIds) {
        checkTransferUserFile(userId, targetId, sourceIds);
    }

    /**
     * 复制文件
     */
    private void doCopyUserFile(long userId, long targetId, List<Long> sourceIds) {
        // 1. 查询所有需要移动的文件
        List<UserFile> userFiles = listByIds(sourceIds);
        // 2. 判断是否查询成功
        if (CollectionUtil.isEmpty(userFiles)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "需要移动的文件不存在");
        }
        // 3. 判断是否移动的是用户的文件
        if (userFiles.stream().anyMatch(file -> file.getUserId() != userId)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "禁止移动其他用户的文件");
        }
        // 4. 复制文件以及目录下的所有文件的信息
        List<UserFile> copyUserFiles = new ArrayList<>();
        // TODO: 利用并发工具优化查询
        for (UserFile userFile : userFiles) {
            generateCopyUserFile(userId, targetId, userFile, copyUserFiles);
        }
        // 5. 更新移动的文件
        if (!saveBatch(copyUserFiles)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "移动文件失败");
        }
    }

    /**
     * 递归复制文件
     */
    private void generateCopyUserFile(long userId, long targetId, UserFile userFile, List<UserFile> copyUserFiles) {
        long fileId = userFile.getFileId();
        // 1. 封装新的文件实体
        UserFile newUserFile = assembleNewUserFile(userId, targetId, userFile);
        // 2. 添加到集合中
        copyUserFiles.add(newUserFile);
        // 3. 判断该文件是否为目录
        if (newUserFile.getFolderFlag() == DirectoryEnum.YES.getFlag()) {
            // 4. 查询目录的所有子文件
            List<UserFile> childUserFiles = selectChildUserFiles(userId, fileId);
            // 5. 递归调用进行复制
            for (UserFile childUserFile : childUserFiles) {
                generateCopyUserFile(userId, targetId, childUserFile, copyUserFiles);
            }
        }
    }

    /**
     * 复制文件实体
     */
    private UserFile assembleNewUserFile(long userId, long targetId, UserFile userFile) {
        return userFile.setUserId(userId)
                .setFileId(IdUtil.generate())
                .setParentId(targetId)
                .setFilename(generateUniqueFilename(userId, targetId,
                        userFile.getFilename(), DirectoryEnum.getDirectoryEnum(userFile.getFolderFlag())))
                .setCreateTime(new Date())
                .setUpdateTime(new Date());
    }

    /**
     * 查询目录的子文件
     */
    private List<UserFile> selectChildUserFiles(long userId, long fileId) {
        QueryWrapper<UserFile> queryWrapper = new QueryWrapper<UserFile>()
                .eq("user_id", userId)
                .eq("parent_id", fileId)
                .eq("del_flag", DeleteEnum.NO.getFlag());
        return list(queryWrapper);
    }
}




