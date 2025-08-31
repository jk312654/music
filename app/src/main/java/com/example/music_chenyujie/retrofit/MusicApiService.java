package com.example.music_chenyujie.retrofit;

import com.example.music_chenyujie.dataModel.BaseResponse;
import com.example.music_chenyujie.dataModel.HomePageInfo;
import com.example.music_chenyujie.dataModel.PageInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface MusicApiService {
    @GET("music/homePage")
    Call<BaseResponse<PageInfo<HomePageInfo>>> getHomePage(
            @Query("current") int current,
            @Query("size") int size
    );

    @GET
    Call<ResponseBody> getLyric(@Url String url); // 动态 URL
}
