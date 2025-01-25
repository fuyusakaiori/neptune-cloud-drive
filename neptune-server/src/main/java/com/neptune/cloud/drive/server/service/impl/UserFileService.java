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
import com.neptune.cloud.drive.server.vo.UploadChunkVO;
import com.neptune.cloud.drive.server.vo.UserFileVO;
import com.neptune.cloud.drive.util.FileUtil;
import com.neptune.cloud.drive.util.IdUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public UserFile selectUserRootDir(GetUserRootDirContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 查询用户根目录
        return selectUserRootDir(context.getUserId());
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
        return new StringBuilder(filenameWithoutSuffix)
                .append(StringConstant.LEFT_PAREN)
                .append(count)
                .append(StringConstant.RIGHT_PAREN)
                .append(filenameSuffix)
                .toString();
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
    private UserFile selectUserRootDir(long userId) {
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
        return files.get(BasicConstant.ZERO_INT);
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

}




