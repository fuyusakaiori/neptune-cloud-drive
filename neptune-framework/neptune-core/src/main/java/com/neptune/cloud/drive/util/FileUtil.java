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
