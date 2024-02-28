package com.ichtj.basetools.callback;

import androidx.annotation.IntDef;

import com.ichtj.basetools.network.NetMtools;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({NetMtools.MODE_HARD, NetMtools.MODE_SOFT, NetMtools.MODE_AIRPLANE, NetMtools.MODE_REBOOT})
@Retention(RetentionPolicy.SOURCE)
public @interface IResetModel {
}
