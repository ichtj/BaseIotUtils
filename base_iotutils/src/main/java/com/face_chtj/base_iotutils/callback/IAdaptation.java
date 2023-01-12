package com.face_chtj.base_iotutils.callback;

import androidx.annotation.IntDef;

import com.face_chtj.base_iotutils.BaseIotUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({BaseIotUtils.HEIGHT, BaseIotUtils.WIDTH})
@Retention(RetentionPolicy.SOURCE)
public @interface IAdaptation { }
