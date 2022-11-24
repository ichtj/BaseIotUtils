package com.wave_chtj.example.network;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({NetMtools.MODE_HARD, NetMtools.MODE_SOFT, NetMtools.MODE_AIRPLANE, NetMtools.MODE_REBOOT})
@Retention(RetentionPolicy.SOURCE)
public @interface IResetModel {
}
