package com.neptune.cloud.drive.server.service;

import com.neptune.cloud.drive.server.context.recycle.DeleteRecycleFileContext;
import com.neptune.cloud.drive.server.context.recycle.GetRecycleFileContext;
import com.neptune.cloud.drive.server.context.recycle.RestoreRecycleFileContext;
import com.neptune.cloud.drive.server.vo.UserFileVO;

import java.util.List;

public interface IRecycleFileService {

    List<UserFileVO> listRecycleFile(GetRecycleFileContext context);

    void restoreRecycleFile(RestoreRecycleFileContext context);

    void deleteRecycleFile(DeleteRecycleFileContext context);
}
