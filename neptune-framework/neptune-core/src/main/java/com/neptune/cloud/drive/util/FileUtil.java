package com.neptune.cloud.drive.util;

import com.neptune.cloud.drive.constant.BasicConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Objects;

/**
 * 公用的文件工具类
 */
public class FileUtil {

    /**
     * 获取文件的后缀
     */
    public static String getFileSuffix(String filename) {
        if (StringUtils.isBlank(filename) || filename.lastIndexOf(StringConstant.POINT) == BasicConstant.NEGATIVE_ONE) {
            return StringConstant.EMPTY;
        }
        return filename.substring(filename.lastIndexOf(StringConstant.POINT)).toLowerCase();
    }

    /**
     * 获取文件的类型
     */
    public static String getFileExtName(String filename) {
        if (StringUtils.isBlank(filename) || filename.lastIndexOf(StringConstant.POINT) == BasicConstant.NEGATIVE_ONE) {
            return StringConstant.EMPTY;
        }
        return filename.substring(filename.lastIndexOf(StringConstant.POINT) + BasicConstant.ONE).toLowerCase();
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
     * <p>将输入流写入到输出流中</p>
     */
    public static void inputStream2OutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        int length = 0;
        // 1. 缓冲数组
        byte[] buffer = new byte[1024];
        // 2. 循环读取
        while ((length = inputStream.read(buffer)) != BasicConstant.NEGATIVE_ONE) {
            outputStream.write(buffer, BasicConstant.ZERO, length);
        }
        // 3. 刷新输出流
        outputStream.flush();
        inputStream.close();
        outputStream.close();
    }
    

}
