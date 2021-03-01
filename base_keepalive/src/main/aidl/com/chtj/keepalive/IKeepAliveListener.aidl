// IKeepAliveListener.aidl
package com.chtj.keepalive;

// Declare any non-default types here with import statements

import com.chtj.keepalive.entity.KeepAliveData;

interface IKeepAliveListener {
    /**
         * Demonstrates some basic types that you can use as parameters
         * and return values in AIDL.
         */
        void onError(String errMeg);
        void onSuccess() ;
}