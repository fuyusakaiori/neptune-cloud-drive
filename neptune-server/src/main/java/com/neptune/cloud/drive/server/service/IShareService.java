package com.neptune.cloud.drive.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neptune.cloud.drive.server.context.share.*;
import com.neptune.cloud.drive.server.model.Share;
import com.neptune.cloud.drive.server.vo.CreateShareLinkVO;
import com.neptune.cloud.drive.server.vo.ShareFileInfoVO;
import com.neptune.cloud.drive.server.vo.ShareLinkInfoVO;
import com.neptune.cloud.drive.server.vo.ShareLinkVO;

import java.util.List;

public interface IShareService extends IService<Share> {

    CreateShareLinkVO createShareLink(CreateShareLinkContext context);

    void cancelShareLink(CancelShareLinkContext context);

    List<ShareLinkVO> listShareLink(ListShareLinkContext context);

    ShareLinkInfoVO infoShareLink(GetShareLinkInfoContext context);

    String checkShareCode(CheckShareCodeContext context);

    void storeShareFile(StoreShareFileContext context);

    void downloadShareFile(DownloadShareFileContext context);

    ShareFileInfoVO infoShareFile(GetShareFileInfoContext context);
}
