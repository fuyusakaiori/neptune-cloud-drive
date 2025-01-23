package com.neptune.cloud.drive.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.server.common.constant.FileConstant;
import com.neptune.cloud.drive.server.common.enums.FileType;
import com.neptune.cloud.drive.server.common.enums.DeleteEnum;
import com.neptune.cloud.drive.server.common.enums.DirectoryEnum;
import com.neptune.cloud.drive.server.context.CreateUserFolderContext;
import com.neptune.cloud.drive.server.context.GetUserRootDirContext;
import com.neptune.cloud.drive.server.mapper.UserFileMapper;
import com.neptune.cloud.drive.server.model.UserFile;
import com.neptune.cloud.drive.server.service.IUserFileService;
import com.neptune.cloud.drive.util.IdUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Service(value = "userFileService")
public class UserFileService extends ServiceImpl<UserFileMapper, UserFile> implements IUserFileService {

    @Override
    public long createUserRootDir(CreateUserFolderContext context) {
        // 0. 判断上下文是否为空
        if (Objects.isNull(context)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getMessage());
        }
        // 1. 创建用户根目录
        return createUserFile(
                context.getUserId(),
                context.getParentId(),
                0,
                context.getFolderName(),
                StringConstant.EMPTY,
                FileType.EMPTY,
                DirectoryEnum.YES);
    }

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
     * 创建文件/目录
     */
    private long createUserFile(long userId, long parentId, long realId, String filename, String description, FileType fileType, DirectoryEnum isFolder) {
        // 1. 封装文件实体
        UserFile file = assembleUserFile(userId, parentId, realId, filename, description, fileType, isFolder);
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
    private UserFile assembleUserFile(long userId, long parentId, long realId, String filename, String description, FileType fileType, DirectoryEnum isFolder) {
        // 1. 为重复的文件名添加标识符
        filename = generateUniqueFilename(userId, parentId, filename, isFolder);
        // 2. 封装文件实体信息
        return new UserFile().setUserId(userId)
                .setParentId(parentId)
                .setFileId(IdUtil.generate())
                .setRealFileId(realId)
                .setFilename(filename)
                .setFileSizeDesc(description)
                .setFileType(fileType.getType())
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
        int count = getUserFilenameCount(userId, parentId, filenameWithoutSuffix, isFolder);
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

    private int getUserFilenameCount(long userId, long parentId, String filenameWithoutSuffix, DirectoryEnum isFolder) {
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

    private UserFile selectUserRootDir(long userId) {
        QueryWrapper<UserFile> queryWrapper = new QueryWrapper<UserFile>()
                .eq("user_id", userId)
                .eq("parent_id", FileConstant.ROOT_PARENT_ID)
                .eq("folder_flag", DirectoryEnum.YES.getFlag())
                .eq("del_flag", DeleteEnum.NO.getFlag());
        return getOne(queryWrapper);
    }

}




