package com.neptune.cloud.drive.storage.engine.s3;

import cn.hutool.core.date.DateUtil;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.neptune.cloud.drive.constant.BasicConstant;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.exception.BusinessException;
import com.neptune.cloud.drive.response.ResponseCode;
import com.neptune.cloud.drive.storage.engine.core.AbstractStorageEngine;
import com.neptune.cloud.drive.storage.engine.core.context.*;
import com.neptune.cloud.drive.storage.engine.s3.config.S3StorageEngineConfig;
import com.neptune.cloud.drive.util.FileUtil;
import com.neptune.cloud.drive.util.UUIDUtil;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class S3StorageEngine extends AbstractStorageEngine {

    private static final String CHUNK_UPLOAD_ID_KEY_PREFIX = "chunk_cache_upload_id";

    private static final String CHUNK_UPLOAD_IDENTIFIER_KEY = "identifier";

    private static final String CHUNK_UPLOAD_ID_KEY = "uploadId";

    private static final String CHUNK_UPLOAD_USER_ID_KEY = "userId";

    private static final String CHUNK_UPLOAD_PART_NUMBER_KEY = "partNumber";

    private static final String CHUNK_UPLOAD_E_TAG_KEY = "eTag";

    private static final String CHUNK_UPLOAD_PART_SIZE_KEY = "partSize";

    private static final String CHUNK_UPLOAD_PART_CRC_KEY = "partCRC";

    @Autowired
    private S3StorageEngineConfig config;

    @Autowired
    private OSSClient client;

    /**
     * 存储文件
     */
    @Override
    protected String doStoreFile(StoreFileContext context) throws IOException {
        // 1. 生成文件的存储路径
        String filePath = generateStoreFilePath(config.getBasePath(), context.getFileName());
        // 2. 调用 s3 接口上传文件
        PutObjectResult result = client.putObject(config.getBucket(), filePath, context.getFile());
        // 3. 判断是否上传成功
        if (Objects.isNull(result)) {
            throw new IOException("上传文件失败");
        }
        return filePath;
    }

    /**
     * 删除文件: 如果需要中断分片的上传, 那么可以取消分片上传事件, 会主动清楚所有分片
     */
    @Override
    protected void doDeleteFile(DeleteFileContext context) throws IOException {
        // 1. 遍历所有需要删除的文件或者文件分片
        for (String filePath : context.getFilePaths()) {
            // 2. 判断是否为分片
            if (!checkChunkPath(filePath)) {
                // 3. 如果不是分片, 那么直接删除就行
                client.deleteObject(config.getBucket(), filePath);
            } else {
                // 4. 解析分片的参数
                Map<String, String> params = analysisChunkPath(filePath);
                // 5. 获取缓存的共用上传 ID
                ChunkUploadContext uploadContext = getCache().get(generateUploadKey(
                        Integer.parseInt(params.get(CHUNK_UPLOAD_USER_ID_KEY)),
                        params.get(CHUNK_UPLOAD_IDENTIFIER_KEY)), ChunkUploadContext.class);
                // 6. 判断是否获取到缓存
                if (Objects.isNull(uploadContext)) {
                    throw new BusinessException(ResponseCode.ERROR.getCode(), "文件分片的共用上传 ID 不存在");
                }
                // 7. 获取分片路径中不含参数的基础路径
                String[] chunkBasePathAndParams = filePath.split(StringConstant.LEFT_SQUARE + StringConstant.QUESTION_MARK + StringConstant.RIGHT_SQUARE);
                // 8. 判断是否切割成功
                if (ArrayUtils.isEmpty(chunkBasePathAndParams)) {
                    throw new BusinessException(ResponseCode.ERROR.getCode(), "切割文件分片路径失败");
                }
                // 7. 如果是分片, 那么需要调用中断分片上传的请求
                try {
                    client.abortMultipartUpload(new AbortMultipartUploadRequest(
                            config.getBucket(), chunkBasePathAndParams[0], uploadContext.getUploadId()));
                } catch (Exception exception) {
                    // TODO 因为取消一个分片就会将其他所有已经上传的分片全部取消, 如果后续还有其他分片需要取消, 就会报错
                }
            }
        }
    }

    /**
     * 存储文件分片
     * <p>1. 文件分片是采用并发上传的, 需要加锁进行并发控制</p>
     * <p>2. 分片上传需要获取统一的 upload id, 需要控制 upload id 仅初始化一次, 并放在缓存中</p>
     */
    @Override
    protected synchronized String doStoreFileChunk(StoreFileChunkContext context) throws IOException {
        // 1. 判断文件分片是否超过限制
        if (context.getChunkCount() > config.getMaxChunkCount()) {
            throw new IOException("文件分片的数量超过上限, 需要重新分片");
        }
        // 2. 生成分片上传使用的 upload id 缓存的 key
        String key = generateUploadKey(context.getUserId(), context.getIdentifier());
        // 3. 从缓存中获取已经上传的 upload id 的 value
        ChunkUploadContext uploadContext = getCache().get(key, ChunkUploadContext.class);
        // 4. 判断是否为第一次初始化
        if (Objects.isNull(uploadContext)) {
            // 5. 初始化分片上传的上下文信息: 缓存信息需要移除, 或者设置对应的过期时间
            uploadContext = initChunkUploadContext(key, context.getFileName());
        }
        // 6. 封装分片上传的请求
        UploadPartRequest request = assembleUploadPartRequest(
                uploadContext, context.getChunkSize(), (int) context.getChunkSeq(), context.getChunk());
        // 7. 执行分片上传
        UploadPartResult uploadResult = client.uploadPart(request);
        // 8. 判断分片是否上传成功
        if (Objects.isNull(uploadResult)) {
            throw new IOException("文件分片上传失败");
        }
        // 9. 封装需要添加在路径中的参数, 后续合并分片会使用
        Map<String, String> params = assembleUploadChunkPath(
                context.getUserId(), context.getIdentifier(), uploadContext, uploadResult);
        // 9. 生成分片对应的路径
        return generateUploadChunkPath(uploadContext.getObjectKey(), params);
    }

    /**
     * 合并文件分片
     */
    @Override
    protected String doMergeFileChunk(MergeFileChunkContext context) throws IOException {
        // 1. 获取分片上传的共用 ID
        ChunkUploadContext uploadContext = getCache().get(
                generateUploadKey(context.getUserId(), context.getIdentifier()), ChunkUploadContext.class);
        // 2. 判断是否成功获取到分片上传的共用 ID
        if (Objects.isNull(uploadContext)) {
            throw new IOException("合并文件分片失败");
        }
        // 3. 遍历所有分片的路径
        List<PartETag> eTags = context.getChunkPaths().stream().map(this::analysisChunkPath).map(
                params -> new PartETag(
                        Integer.parseInt(params.get(CHUNK_UPLOAD_PART_NUMBER_KEY)),
                        params.get(CHUNK_UPLOAD_E_TAG_KEY),
                        Long.parseLong(params.get(CHUNK_UPLOAD_PART_SIZE_KEY)),
                        Long.parseLong(params.get(CHUNK_UPLOAD_PART_CRC_KEY))
                )).collect(Collectors.toList());

        // 4. 执行分片合并的请求
        CompleteMultipartUploadResult mergeResult = client.completeMultipartUpload(
                new CompleteMultipartUploadRequest(
                        config.getBucket(), uploadContext.getObjectKey(), uploadContext.getUploadId(), eTags));
        // 5. 判断是否合并成功
        if (Objects.isNull(mergeResult)) {
            throw new IOException("合并分片失败");
        }
        // 6 清楚合并之后的分片共用 ID
        getCache().evict(uploadContext.getObjectKey());

        return uploadContext.getObjectKey();
    }

    /**
     * 下载文件
     */
    @Override
    protected void doDownloadFile(DownloadFileContext context) throws IOException {
        // 1. 调用 s3 接口读取文件
        OSSObject object = client.getObject(config.getBucket(), context.getFilePath());
        // 2. 判断是否读取成功
        if (Objects.isNull(object)) {
            throw new IOException("下载文件失败");
        }
        // 3. 将 s3 对象转换到字节流中
        int length = 0;
        byte[] buffer = new byte[1024];
        // 循环读取字节流
        while ((length = object.getObjectContent().read(buffer)) != BasicConstant.NEGATIVE_ONE) {
            context.getFile().write(buffer, BasicConstant.ZERO, length);
        }
        // 4. 刷新输出流
        context.getFile().flush();
        context.getFile().close();
        object.close();
    }

    /**
     * 生成文件的存储路径: 基础路径/年/月/日/随机的文件名称.扩展名
     */
    private String generateStoreFilePath(String basePath, String fileName) {
        return basePath +
                StringConstant.SLASH +
                DateUtil.thisYear() +
                StringConstant.SLASH +
                (DateUtil.thisMonth() + 1) +
                StringConstant.SLASH +
                DateUtil.thisDayOfMonth() +
                StringConstant.SLASH +
                UUIDUtil.getUUID() +
                FileUtil.getFileSuffix(fileName);
    }

    /**
     * 生成分片上传 id 缓存的 key
     */
    private String generateUploadKey(long userId, String identifier) {
        return CHUNK_UPLOAD_ID_KEY_PREFIX
                + StringConstant.COLON
                + userId
                + StringConstant.COLON
                + identifier
                + StringConstant.COLON;
    }

    /**
     * 初始化分片上传的缓存的 value
     */
    private ChunkUploadContext initChunkUploadContext(String key, String fileName) {
        // 1. 生成文件的路径
        String filePath = generateStoreFilePath(config.getBasePath(), fileName);
        // 2. 发起文件分片上传的初始化请求
        InitiateMultipartUploadResult result = client.initiateMultipartUpload(
                new InitiateMultipartUploadRequest(config.getBucket(), filePath));
        // 3. 判断请求是否执行成功
        if (Objects.isNull(result)) {
            throw new BusinessException(ResponseCode.ERROR.getCode(), "文件分片上传初始化失败");
        }
        // 4. 封装分片上传的上下文
        ChunkUploadContext context = new ChunkUploadContext(result.getUploadId(), filePath);
        // 5. 放入缓存中
        getCache().put(key, context);

        return context;
    }

    /**
     * 封装分片上传的请求
     */
    private UploadPartRequest assembleUploadPartRequest(ChunkUploadContext context, long chunkSize, int chunkSeq, InputStream chunk) {
        // 1. 初始化分片上传的请求
        UploadPartRequest request = new UploadPartRequest();
        // 2. 设置分片上传请求的参数
        request.setBucketName(config.getBucket());
        request.setUploadId(context.getUploadId());
        request.setKey(context.getObjectKey());
        request.setInputStream(chunk);
        request.setPartSize(chunkSize);
        request.setPartNumber(chunkSeq);
        // 3. 返回请求
        return request;
    }

    /**
     * 封装分片的路径地址
     */
    private Map<String, String> assembleUploadChunkPath(long userId, String identifier, ChunkUploadContext uploadContext, UploadPartResult uploadResult) {
        // 1. 初始化参数哈希表
        Map<String, String> params = new HashMap<>();
        // 2. 设置用户 ID
        params.put(CHUNK_UPLOAD_USER_ID_KEY, String.valueOf(userId));
        // 3. 设置文件唯一标识符
        params.put(CHUNK_UPLOAD_IDENTIFIER_KEY, identifier);
        // 4. 设置分片共用上传 ID
        params.put(CHUNK_UPLOAD_ID_KEY, uploadContext.getUploadId());
        // 5. 设置分片的编号
        params.put(CHUNK_UPLOAD_PART_NUMBER_KEY, String.valueOf(uploadResult.getPartNumber()));
        // 6. 设置分片的大小
        params.put(CHUNK_UPLOAD_PART_SIZE_KEY, String.valueOf(uploadResult.getPartSize()));
        // 7. 设置分片 etag
        params.put(CHUNK_UPLOAD_E_TAG_KEY, uploadResult.getETag());

        return params;
    }

    /**
     * 解析分片的路径地址
     */
    private Map<String, String> analysisChunkPath(String chunkPath) {
        // 1. 初始化参数列表
        Map<String, String> params = new HashMap<>();
        // 2. 判断是否分片路径是否存在参数
        if (!checkChunkPath(chunkPath)) {
            return params;
        }
        // 3. 切割分片的路径: 因为字符串切割默认会使用正则表达式, 所以需要添加 [] 来避免 ? 被识别正则表达式
        String[] chunkBashPathAndParams = chunkPath.split(
                StringConstant.LEFT_SQUARE + StringConstant.QUESTION_MARK + StringConstant.RIGHT_SQUARE);
        // 4. 获取分片的参数
        String[] chunkPathParams = chunkBashPathAndParams[1].split(StringConstant.AND);
        // 5. 判断是否切割成功
        if (ArrayUtils.isEmpty(chunkPathParams)) {
            return params;
        }
        // 6. 遍历所有参数并将其放入哈希表中
        for (String chunkPathParam : chunkPathParams) {
            String[] keyValue = chunkPathParam.split(
                    StringConstant.LEFT_SQUARE + StringConstant.EQUALS + StringConstant.RIGHT_SQUARE);
            // 判断是否切割成功
            if (ArrayUtils.isEmpty(keyValue) || keyValue.length != BasicConstant.TWO) {
                throw new BusinessException(ResponseCode.ERROR.getCode(), "合并的分片存在异常");
            }
            params.put(keyValue[0], keyValue[1]);
        }
        return params;
    }

    /**
     * 生成分片上传的路径
     * <p>baseUrl?paramKey1=paramValue1&paramKey2=paramValue2</p>
     */
    private String generateUploadChunkPath(String objectKey, Map<String, String> params) {
        // 1. 初始化分片文件的路径
        StringBuilder chunkPath = new StringBuilder(objectKey).append(StringConstant.QUESTION_MARK);
        // 2. 遍历所有分片的参数
        List<String> chunkPathParams = new ArrayList<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            chunkPathParams.add(
                    entry.getKey() + StringConstant.EQUALS + entry.getValue());
        }
        // 3. 将所有的参数列表都拼接起来
        return chunkPath.append(String.join(StringConstant.AND, chunkPathParams)).toString();
    }

    /**
     * 判断分片路径是否存在参数
     */
    private boolean checkChunkPath(String chunkPath) {
        return StringUtils.isNotBlank(chunkPath) && chunkPath.contains(StringConstant.QUESTION_MARK);
    }

    /**
     * 分片上传需要使用的上下文信息
     */
    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    private final static class ChunkUploadContext implements Serializable {

        private static final long serialVersionUID = 1613147100084657376L;

        /**
         * 同一文件不同分片使用的公用上传 ID
         */
        private final String uploadId;

        /**
         * 文件分片对应的文件名称
         */
        private final String objectKey;
    }

}
