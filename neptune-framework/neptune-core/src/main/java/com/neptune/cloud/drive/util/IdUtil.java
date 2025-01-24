package com.neptune.cloud.drive.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ArrayUtil;
import com.neptune.cloud.drive.constant.BasicConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局唯一 ID
 */
@Slf4j
public class IdUtil {

    /**
     * 工作 id
     */
    private static long workerId;

    /**
     * 数据中心 id
     */
    private static long dataCenterId;

    /**
     * 序列号
     */
    private static long sequence;

    /**
     * 初始时间戳
     */
    private static final long startTimestamp = 1288834974657L;

    /**
     * 工作 id 长度为 5 位
     */
    private static final long workerIdBits = 5L;

    /**
     * 数据中心 id 长度为 5 位
     */
    private static final long dataCenterIdBits = 5L;

    /**
     * 工作 id 最大值
     */
    private static final long maxWorkerId = ~(-1L << workerIdBits);

    /**
     * 数据中心 id 最大值
     */
    private static final long maxDataCenterId = ~(-1L << dataCenterIdBits);

    /**
     * 序列号长度
     */
    private static final long sequenceBits = 12L;

    /**
     * 序列号最大值
     */
    private static final long sequenceMask = ~(-1L << sequenceBits);

    /**
     * 工作 id 需要左移的位数, 12 位
     */
    private static final long workerIdShift = sequenceBits;

    /**
     * 数据 id 需要左移位数, 12 + 5 = 17 位
     */
    private static final long dataCenterIdShift = sequenceBits + workerIdBits;

    /**
     * 时间戳需要左移位数, 12 + 5 + 5 = 22 位
     */
    private static final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;

    /**
     * 上次时间戳，初始值为负数
     */
    private static long lastTimestamp = -1L;

    static {
        try {
            sequence = 0L;
            workerId = generateMachineId() & maxWorkerId;
            dataCenterId = generateMachineId() & maxDataCenterId;
        } catch (SocketException exception) {
            log.error("IdUtil: 生成机器 ID 失败", exception);
        }
    }

    /**
     * 生成 ID
     */
    public synchronized static long generate() {
        // 1. 比较当前时间戳和记录的上一次时间戳: 如果小于上次时间戳, 那么认为发生时钟回拨
        long timestamp = generateTimestamp();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        // 2. 自增序列号: 如果当前时间戳和上次相同, 那么自增序列号, 否则直接置为 0
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = nextTimestamp(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        // 3. 更新上一次记录的时间戳
        lastTimestamp = timestamp;
        // 4. 组装返回结果
        return ((timestamp - startTimestamp) << timestampLeftShift) |
                (dataCenterId << dataCenterIdShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    /**
     * 加密 ID
     */
    public static String encrypt(long id) throws Exception {
        // 1. 初始化缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        // 2. 将 ID 放入缓冲区
        byteBuffer.putLong(0, id);
        // 3. 缓冲区转换为数组
        byte[] content = byteBuffer.array();
        // 4. 生成加密的内容
        byte[] encryption = AES128Util.encrypt(content);
        // 5. 判断是否加密成功
        if (ArrayUtils.isEmpty(encryption)) {
            return StringConstant.EMPTY;
        }
        return Base64.encode(encryption);
    }

    /**
     * 解密 ID
     */
    public static long decrypt(String content) throws Exception {
        // 1. 判断是否为空
        if (StringUtils.isEmpty(content)) {
            return BasicConstant.NEGATIVE_ONE_INT;
        }
        // 2. base64 解码
        byte[] encryption = Base64.decode(content);
        // 3. 解密
        byte[] decryption = AES128Util.decrypt(encryption);
        // 4. 判断是否为空
        if (ArrayUtil.isEmpty(decryption)) {
            return BasicConstant.NEGATIVE_ONE_INT;
        }
        // 5. 初始化缓冲区
        ByteBuffer byteBuffer = ByteBuffer.wrap(decryption);
        // 6. 从缓冲区中获取 ID
        return byteBuffer.getLong();
    }

    /**
     * 解密多个加密 ID 拼接的字符串
     */
    public static List<Long> decryptList(String content) {
        // 1. 判断 id 是否为空
        if (StringUtils.isBlank(content)) {
            return Collections.emptyList();
        }
        // 2. 切割字符串
        String[] ids = content.split(StringConstant.COMMON_SEPARATOR);
        // 3. 判断是否切割成功
        if (ArrayUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // 4. 对每个 id 解密
        return Arrays.stream(ids).map(id -> {
            try {
                return decrypt(id);
            } catch (Exception exception) {
                throw new BusinessException(ResponseCode.ERROR.getCode(), "解析文件 ID 失败");
            }
        }).collect(Collectors.toList());
    }

    /**
     * 获取机器编号
     */
    private static long generateMachineId() throws SocketException {
        StringBuilder network = new StringBuilder();
        Enumeration<NetworkInterface>  networks = NetworkInterface.getNetworkInterfaces();
        while (networks.hasMoreElements()) {
            network.append(networks.nextElement().toString());
        }
        return network.toString().hashCode();
    }

    /**
     * 获取时间戳，并与上次时间戳比较
     */
    private static long nextTimestamp(long lastTimestamp) {
        long timestamp = generateTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = generateTimestamp();
        }
        return timestamp;
    }

    /**
     * 获取系统时间戳
     */
    private static long generateTimestamp() {
        return System.currentTimeMillis();
    }

}
