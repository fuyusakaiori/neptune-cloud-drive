package com.neptune.cloud.drive.serializer;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

/**
 * 日期序列化器
 */
public class Date2StringSerializer extends JsonSerializer<Date> {

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (Objects.isNull(date)) {
            return;
        }
        jsonGenerator.writeString(DateUtil.formatDateTime(date));
    }

}
