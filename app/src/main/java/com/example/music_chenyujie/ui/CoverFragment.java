package com.example.music_chenyujie.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.music_chenyujie.R;

import org.jetbrains.annotations.Nullable;

public class CoverFragment extends Fragment {
    private ImageView ivCover;
    private View rootView;
    private String pendingUrl = null;
    private ObjectAnimator rotateAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_cover, container, false);
        ivCover = rootView.findViewById(R.id.coverImageView);

        setupAnimator();
        // 如果之前 setCoverUrl 被调用过，补加载一次
        if (pendingUrl != null) {
            loadCover(pendingUrl);
            pendingUrl = null;
        }

        return rootView;
    }

    public void setCoverUrl(String url) {
        if (ivCover != null) {
            loadCover(url);
        } else {
            pendingUrl = url; // 等待 onCreateView 加载完
        }
    }

    private void loadCover(String url) {
        if(url.startsWith("http://")){
            url = url.replace("http://","https://");
        }
        Glide.with(requireContext())
                .asBitmap()
                .load(url)
                .circleCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ivCover.setImageBitmap(resource);
                        if (resource != null && !resource.isRecycled()) {
                            extractColorFromBitmap(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // 可选：释放资源或设置占位图
                    }
                });
    }


    private void extractColorFromBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) return;

        Palette.from(bitmap).generate(palette -> {
            int defaultColor = ContextCompat.getColor(requireContext(), android.R.color.black);
            int bgColor = palette.getVibrantColor(
                    palette.getMutedColor(
                            palette.getDominantColor(defaultColor)));
            rootView.setBackgroundColor(bgColor);

            // 设置整个 Activity 背景色
            Activity activity = getActivity();
            if (activity != null) {
                View bgView = activity.findViewById(R.id.backgroundTintView);
                if (bgView != null) {
                    bgView.setBackgroundColor(bgColor);
                }
            }
        });
    }

    private void setupAnimator() {
        rotateAnimator = ObjectAnimator.ofFloat(ivCover, "rotation", 0f, 360f);
        rotateAnimator.setDuration(20_000); // 20秒一圈
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    public void startRotate() {
        if (rotateAnimator != null && !rotateAnimator.isRunning()) {
            rotateAnimator.start();
        }
    }

    /*public void pauseRotate() {
        if (rotateAnimator != null && rotateAnimator.isRunning()) {
            rotateAnimator.pause();
        }
    }

    public void resumeRotate() {
        if (rotateAnimator != null && rotateAnimator.isPaused()) {
            rotateAnimator.resume();
        }
    }*/

    public void stopRotate() {
        if (rotateAnimator != null) {
            rotateAnimator.cancel();
            ivCover.setRotation(0f);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rotateAnimator != null) {
            rotateAnimator.cancel();
        }
    }
}