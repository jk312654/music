package com.example.music_chenyujie.utils;

import android.content.Context;
import android.util.Log;

import com.example.music_chenyujie.dataModel.BaseResponse;
import com.example.music_chenyujie.dataModel.HomePageInfo;
import com.example.music_chenyujie.dataModel.PageInfo;
import com.example.music_chenyujie.retrofit.MusicApiService;
import com.example.music_chenyujie.retrofit.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MusicRepository {
    public interface DataCallback {
        void onSuccess(List<HomePageInfo> data);
        void onError(String errorMsg);
    }

    public static void loadHomePageData(int current, int size, final Context context, final DataCallback callback) {
        MusicApiService api = RetrofitClient.getService();
        Call<BaseResponse<PageInfo<HomePageInfo>>> call = api.getHomePage(current, size);

        call.enqueue(new Callback<BaseResponse<PageInfo<HomePageInfo>>>() {
            @Override
            public void onResponse(Call<BaseResponse<PageInfo<HomePageInfo>>> call,
                                   Response<BaseResponse<PageInfo<HomePageInfo>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BaseResponse<PageInfo<HomePageInfo>> result = response.body();
                    if (result.code == 200) {
                        List<HomePageInfo> modules = result.data.getRecords();
                        Log.d("MusicRepository", "模块数量: " + modules.size());
                        if (callback != null) {
                            callback.onSuccess(modules);
                        }
                    } else {
                        String msg = "业务失败: " + result.msg;
                        Log.e("MusicRepository", msg);
                        if (callback != null) {
                            callback.onError(msg);
                        }
                    }
                } else {
                    String msg = "请求失败: " + response.message();
                    Log.e("MusicRepository", msg);
                    if (callback != null) {
                        callback.onError(msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseResponse<PageInfo<HomePageInfo>>> call, Throwable t) {
                String msg = "网络错误: " + t.getMessage();
                Log.e("MusicRepository", msg);
                if (callback != null) {
                    callback.onError(msg);
                }
            }
        });
    }
}
