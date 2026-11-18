package com.ichtj.basetools;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.ichtj.basetools.base.BaseActivity;
import com.ichtj.basetools.util.AppNavigator;

public class StartPageAty extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppNavigator.open(this);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
