package com.neptune.cloud.drive.server.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateUserFolderContext implements Serializable {

    private static final long serialVersionUID = 7434918941243131244L;

    /**
     * 目录所属的用户
     */
    private long userId;

    /**
     * 目录关联的父目录
     */
    private long parentId;

    /**
     * 目录的名称
     */
    private String folderName;

}
