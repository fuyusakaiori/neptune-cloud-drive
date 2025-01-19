package com.neptune.cloud.drive.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.neptune.cloud.drive.constant.StringConstant;
import com.neptune.cloud.drive.util.IdUtil;

import java.io.IOException;
import java.util.Objects;

/**
 * ID 序列化器
 */
public class IdEncryptSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long id, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        try {
            // 1. 初始化加密后的内容
            String encryption = StringConstant.EMPTY;
            // 2. 如果 ID 不为空, 那么执行加密
            if (!Objects.isNull(id)) {
                encryption = IdUtil.encrypt(id);

            }
            // 3. 序列化
            jsonGenerator.writeString(encryption);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

}
