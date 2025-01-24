package com.neptune.cloud.drive.server.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * 文件类型
 */
@Getter
@AllArgsConstructor
public enum FileType {

    NORMAL_FILE(1, "NORMAL_FILE", 1, fileSuffix -> true),

    ARCHIVE_FILE(2, "ARCHIVE_FILE", 2, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".rar", ".zip", ".cab", ".iso", ".jar", ".ace", ".7z", ".tar", ".gz", ".arj", ".lah", ".uue", ".bz2", ".z", ".war");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    EXCEL_FILE(3, "EXCEL", 3, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".xlsx", ".xls");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    WORD_FILE(4, "WORD_FILE", 4, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".docx", ".doc");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    PDF_FILE(5, "PDF_FILE", 5, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".pdf");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    TXT_FILE(6, "TXT_FILE", 6, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".txt");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    IMAGE_FILE(7, "IMAGE_FILE", 7, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".bmp", ".gif", ".png", ".ico", ".eps", ".psd", ".tga", ".tiff", ".jpg", ".jpeg");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    AUDIO_FILE(8, "AUDIO_FILE", 8, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".mp3", ".mkv", ".mpg", ".rm", ".wma");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    VIDEO_FILE(9, "VIDEO_FILE", 9, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".avi", ".3gp", ".mp4", ".flv", ".rmvb", ".mov");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    POWER_POINT_FILE(10, "POWER_POINT_FILE", 10, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".ppt", ".pptx");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    SOURCE_CODE_FILE(11, "SOURCE_CODE_FILE", 11, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".java", ".obj", ".h", ".c", ".html", ".net", ".php", ".css", ".js", ".ftl", ".jsp", ".asp");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    }),

    CSV_FILE(12, "CSV_FILE", 12, fileSuffix -> {
        List<String> matchFileSuffixes = Arrays.asList(".csv");
        return StringUtils.isNotBlank(fileSuffix) && matchFileSuffixes.contains(fileSuffix);
    });

    /**
     * 文件类型标识
     */
    private final Integer code;

    /**
     * 文件类型描述
     */
    private final String description;

    /**
     * 排序字段: 按照降序顺序排序
     */
    private final Integer order;

    /**
     * 文件类型匹配器
     */
    private final Predicate<String> predicate;

    /**
     * 根据文件后缀名获取文件类型
     */
    public static int getFileTypeCode(String fileSuffix) {
        Optional<FileType> optional = Arrays.stream(FileType.values())
                .sorted(Comparator.comparing(FileType::getOrder).reversed())
                .filter(fileType -> fileType.getPredicate().test(fileSuffix))
                .findFirst();
        if (optional.isPresent()) {
            return optional.get().getCode();
        }
        return 0;
    }

}
