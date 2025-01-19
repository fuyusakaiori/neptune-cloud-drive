package com.neptune.cloud.drive.util;

import cn.hutool.core.date.DateUtil;
import com.neptune.cloud.drive.constant.BasicConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;

/**
 * 公用的文件工具类
 */
public class FileUtil {

    /**
     * 创建文件
     */
    public static boolean createFile(File targetFile) throws IOException {
        // 1. 创建父目录
        if (!targetFile.getParentFile().exists()) {
            return targetFile.getParentFile().mkdirs();
        }
        // 2. 创建文件
        return targetFile.createNewFile();
    }

    /**
     * 删除物理文件
     */
    public static void deleteFiles(List<String> filepathList) throws IOException {
        if (CollectionUtils.isEmpty(filepathList)) {
            return;
        }
        for (String filepath : filepathList) {
            org.apache.commons.io.FileUtils.forceDelete(new File(filepath));
        }
    }

    /**
     * 获取文件的后缀
     */
    public static String getFileSuffix(String filename) {
        if (StringUtils.isBlank(filename) || filename.lastIndexOf(StringConstant.POINT) == BasicConstant.NEGATIVE_ONE_INT) {
            return StringConstant.EMPTY;
        }
        return filename.substring(filename.lastIndexOf(StringConstant.POINT)).toLowerCase();
    }

    /**
     * 获取文件的类型
     */
    public static String getFileExtName(String filename) {
        if (StringUtils.isBlank(filename) || filename.lastIndexOf(StringConstant.POINT) == BasicConstant.NEGATIVE_ONE_INT) {
            return StringConstant.EMPTY;
        }
        return filename.substring(filename.lastIndexOf(StringConstant.POINT) + BasicConstant.ONE_INT).toLowerCase();
    }

    /**
     * 将文件的大小转换为对应的名称: 1024 => 1 MB/GB/TB
     */
    public static String fileSize2DisplaySize(Long fileSize) {
        if (Objects.isNull(fileSize)) {
            return StringConstant.EMPTY;
        }
        return org.apache.commons.io.FileUtils.byteCountToDisplaySize(fileSize);
    }

    /**
     * <p>生成默认的文件存储路径</p>
     * <p>当前登录用户的文件目录 + cloud-drive</p>
     */
    public static String generateFilePathPrefix() {
        return new StringBuffer(System.getProperty("user.home"))
                .append(File.separator)
                .append(BasicConstant.CLOUD_DRIVE)
                .toString();
    }
    
    /**
     * <p>生成文件的存储路径</p>
     * <p>基础路径 + 年 + 月 + 日 + 随机的文件名称</p>
     */
    public static String generateStoreFilePath(String basePath, String filename) {
        return new StringBuilder(basePath)
                .append(File.separator)
                .append(DateUtil.thisYear())
                .append(File.separator)
                .append(DateUtil.thisMonth() + 1)
                .append(File.separator)
                .append(DateUtil.thisDayOfMonth())
                .append(File.separator)
                .append(UUIDUtil.getUUID())
                .append(getFileSuffix(filename))
                .toString();

    }

    /**
     * 生成文件分片的存储路径前缀
     */
    public static String generateStoreFileChunkPathPrefix() {
        return new StringBuilder(System.getProperty("user.home"))
                .append(File.separator)
                .append(BasicConstant.CLOUD_DRIVE)
                .append(File.separator)
                .append("chunks")
                .toString();
    }

    /**
     * <p>生成文件分片的存储路径</p>
     * <p>生成规则：基础路径 + 年 + 月 + 日 + 唯一标识 + 随机的文件名称 + __,__ + 文件分片的下标</p>
     */
    public static String generateStoreFileChunkPath(String basePath, String identifier, Integer chunkNumber) {
        return new StringBuffer(basePath)
                .append(File.separator)
                .append(DateUtil.thisYear())
                .append(File.separator)
                .append(DateUtil.thisMonth() + 1)
                .append(File.separator)
                .append(DateUtil.thisDayOfMonth())
                .append(File.separator)
                .append(identifier)
                .append(File.separator)
                .append(UUIDUtil.getUUID())
                .append(StringConstant.COMMON_SEPARATOR)
                .append(chunkNumber)
                .toString();
    }

    /**
     * 追加写文件
     */
    public static void appendWrite(Path target, Path source) throws IOException {
        Files.write(target, Files.readAllBytes(source), StandardOpenOption.APPEND);
    }

    /**
     * <p>将输入流的内容写入到另一个文件</p>
     */
    public static void inputStream2File(InputStream inputStream, File targetFile, Long totalSize) throws IOException {
        // 1. 创建文件
        createFile(targetFile);
        // 2. 初始化随机读写文件
        RandomAccessFile randomAccessFile = new RandomAccessFile(targetFile, "rw");
        // 3. 初始化目标文件 channel
        FileChannel outputChannel = randomAccessFile.getChannel();
        // 4. 初始化读取文件 channel
        ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
        // 5. 零拷贝到另一个文件
        outputChannel.transferFrom(inputChannel, 0L, totalSize);
        // 6. 关闭文件流
        inputChannel.close();
        outputChannel.close();
        randomAccessFile.close();
        inputStream.close();
    }

    /**
     * <p>将文件中的内容写入到输出流中</p>
     */
    public static void file2OutputStream(FileInputStream fileInputStream, OutputStream outputStream, long length) throws IOException {
        // 1. 获取写入文件的 channel
        FileChannel inputChannel = fileInputStream.getChannel();
        // 2. 获取输出流的 channel
        WritableByteChannel outputChannel = Channels.newChannel(outputStream);
        // 3. 从文件零拷贝到输出流
        inputChannel.transferTo(BasicConstant.ZERO_LONG, length, outputChannel);
        // 4. 刷新输出流
        outputStream.flush();
        // 5. 关闭文件流
        fileInputStream.close();
        outputStream.close();
        inputChannel.close();
        outputChannel.close();
    }

    /**
     * <p>将输入流写入到输出流中</p>
     */
    public static void inputStream2OutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        int length = 0;
        // 1. 缓冲数组
        byte[] buffer = new byte[1024];
        // 2. 循环读取
        while ((length = inputStream.read(buffer)) != BasicConstant.NEGATIVE_ONE_INT) {
            outputStream.write(buffer, BasicConstant.ZERO_INT, length);
        }
        // 3. 刷新输出流
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }
    

}
