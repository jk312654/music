package com.example.music_chenyujie;

import android.app.Application;
import android.content.Context;

import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

import com.tencent.mmkv.MMKV;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;

public class App extends Application implements ViewModelStoreOwner {

    private ViewModelStore mAppViewModelStore = new ViewModelStore();


    @Override
    public ViewModelStore getViewModelStore() {
        return mAppViewModelStore;
    }


    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        MMKV.initialize(this);
        AutoSize.initCompatMultiProcess(this);
        AutoSizeConfig.getInstance()
                .setCustomFragment(true)
                .setDesignWidthInDp(392)
                .setDesignHeightInDp(871)
                .setBaseOnWidth(true)
                .getUnitsManager().setSupportDP(true).setSupportSP(true);
    }
}
