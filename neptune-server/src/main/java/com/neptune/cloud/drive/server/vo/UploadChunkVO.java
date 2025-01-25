package com.neptune.cloud.drive.server.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UploadChunkVO implements Serializable {

    private static final long serialVersionUID = 194789180033323154L;

    private long chunkId;

}
