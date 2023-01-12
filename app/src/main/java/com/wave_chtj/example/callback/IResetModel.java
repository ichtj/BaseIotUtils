package com.wave_chtj.example.callback;

import androidx.annotation.IntDef;

import com.wave_chtj.example.network.NetMtools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({NetMtools.MODE_HARD, NetMtools.MODE_SOFT, NetMtools.MODE_AIRPLANE, NetMtools.MODE_REBOOT})
@Retention(RetentionPolicy.SOURCE)
public @interface IResetModel {
}
