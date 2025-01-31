package com.neptune.cloud.drive.server.converter;

import com.neptune.cloud.drive.server.context.share.CancelShareLinkContext;
import com.neptune.cloud.drive.server.context.share.CheckShareCodeContext;
import com.neptune.cloud.drive.server.context.share.CreateShareLinkContext;
import com.neptune.cloud.drive.server.context.share.StoreShareFileContext;
import com.neptune.cloud.drive.server.request.share.CancelShareLinkRequest;
import com.neptune.cloud.drive.server.request.share.CheckShareCodeRequest;
import com.neptune.cloud.drive.server.request.share.CreateShareLinkRequest;
import com.neptune.cloud.drive.server.request.share.StoreShareFileRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShareLinkConverter {

    /**
     * CreateShareLinkRequest => CreateShareLinkContext
     */
    CreateShareLinkContext createShareLinkRequest2CreateShareLinkContext(CreateShareLinkRequest request);

    /**
     * CancelShareFileRequest => CancelShareFileContext
     */
    CancelShareLinkContext cancelShareFileRequest2CancelShareFileContext(CancelShareLinkRequest request);

    /**
     * CheckShareCodeRequest => CheckShareCodeContext
     */
    @Mapping(target = "shareId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getShareId())")
    CheckShareCodeContext checkShareCodeRequest2CheckShareCodeContext(CheckShareCodeRequest request);

    /**
     * StoreShareFileRequest => StoreShareFileContext
     */
    @Mapping(target = "targetId", expression = "com.neptune.cloud.drive.util.IdUtil.decrypt(request.getTargetId())")
    StoreShareFileContext storeShareFileRequest2StoreShareFileContext(StoreShareFileRequest request);
}
