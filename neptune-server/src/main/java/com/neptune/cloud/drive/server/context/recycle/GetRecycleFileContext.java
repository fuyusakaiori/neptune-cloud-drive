package com.neptune.cloud.drive.server.context.recycle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class GetRecycleFileContext implements Serializable {

    private static final long serialVersionUID = -3494327853821984926L;

    /**
     * 用户 ID
     */
    private long userId;

}
