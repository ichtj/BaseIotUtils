package com.face_chtj.base_iotutils.callback;

import androidx.annotation.IntDef;

import com.face_chtj.base_iotutils.AudioUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({AudioUtils.LEFT, AudioUtils.RIGHT, AudioUtils.ALL})
@Retention(RetentionPolicy.SOURCE)
public @interface IVolumeType {
}