package com.example.music_chenyujie.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.music_chenyujie.R;
import com.example.music_chenyujie.retrofit.MusicApiService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class LyricFragment extends Fragment {

    private TextView lyricTextView;

    private String pendingUrl = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyric, container, false);
        lyricTextView = view.findViewById(R.id.lyricTextView);

        // 如果外部已经设置了 pendingUrl，则加载
        if (pendingUrl != null) {
            loadLyricFromUrl(pendingUrl);
            pendingUrl = null;
        }

        return view;
    }

    public void loadLyricFromUrl(String url) {
        if (!isAdded() || lyricTextView == null) {
            pendingUrl = url; // 暂存起来，等 onViewCreated 时再调用
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://example.com/") // 实际不使用，只是 Retrofit 要求
                .build();

        MusicApiService apiService = retrofit.create(MusicApiService.class);

        apiService.getLyric(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {

                        String rawLyrics = response.body().string();
                        StringBuilder cleaned = new StringBuilder();
                        for (String line : rawLyrics.split("\n")) {
                            String cleanedLine = line.replaceAll("\\[\\d{1,2}:\\d{2}(\\.\\d{1,3})?\\]", "").trim();
                            if (!cleanedLine.isEmpty()) {
                                cleaned.append(cleanedLine).append("\n");
                            }
                        }
                        String lyrics = cleaned.toString();
                        // 清洗时间戳 处理换行

                        if (isAdded()) {
                            requireActivity().runOnUiThread(() ->{
                                lyricTextView.setText(lyrics);
                            }
                            );
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError();
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                showError();
            }

            private void showError() {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            lyricTextView.setText("歌词加载失败，请检查网络或链接地址"));
                }
            }
        });
    }
}