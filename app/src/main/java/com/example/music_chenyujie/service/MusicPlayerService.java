package com.example.music_chenyujie.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.music_chenyujie.Model.PlayMode;
import com.example.music_chenyujie.R;
import com.example.music_chenyujie.dataModel.MusicInfo;

import java.io.IOException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


// 音乐播放服务类，继承自 Service
// 管理后台播放逻辑，维护 MediaPlayer 实例及播放状态
public class MusicPlayerService extends Service {

    // 用于外部绑定服务的 Binder 对象
    private final IBinder binder = new LocalBinder();
    // 媒体播放器核心组件
    private MediaPlayer mediaPlayer;

    // 当前播放的音乐列表及索引
    private List<MusicInfo> musicList;
    private int currentIndex = 0;

    // 标志位：播放器是否准备完成
    private boolean isPrepared = false;

    // 当前播放模式：顺序播放 / 单曲循环 / 随机播放
    private PlayMode playMode = PlayMode.SEQUENTIAL;

    // 随机播放用的随机数生成器
    private final Random random = new Random();

    // 用于定时更新播放进度的 Handler（主线程）
    private final Handler handler = new Handler(Looper.getMainLooper());

    // 定时任务
    private Runnable progressRunnable;

    // 播放状态相关的 LiveData（用于 ViewModel 观察）
    private final MutableLiveData<MusicInfo> currentMusic = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> position = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> duration = new MutableLiveData<>(0);
    private final MutableLiveData<PlayMode> livePlayMode = new MutableLiveData<>(playMode);

    // 自定义 Binder 实现，供 Activity 获取服务实例
    public class LocalBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    // 绑定服务时返回 Binder 对象
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService(); // 启动前台服务，防止被系统杀死
    }



    // 当服务通过 startService 启动时调用
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 异常终止后尝试自动重启
        return START_STICKY;
    }


    /**
     * 初始化播放器并准备播放
     * @param list 播放列表
     * @param index 初始播放索引
     * @param autoPlay 是否立即开始播放
     */
    public void init(List<MusicInfo> list, int index, boolean autoPlay) {
        Log.d("DEBUG", "currentList=" + this.musicList);
        Log.d("DEBUG", "incomingList=" + list);
        Log.d("DEBUG", "isSameMusicList=" + isSameMusicList(this.musicList, list));
        Log.d("DEBUG", "currentIndex=" + currentIndex + ", incomingIndex=" + index);

        // 判断是否为相同播放上下文
        if (isPrepared && isSameMusicList(this.musicList, list)) {
            if (this.currentIndex == index) {
                // 同列表同歌曲，不处理
                return;
            } else {
                // 同列表切歌
                this.currentIndex = index;
                preparePlayer(true);
                return;
            }
        }

        // 不同列表或未初始化，释放原资源并重建
        release();
        // 列表不一样，说明是新的播放上下文，重新加载
        this.musicList = list;
        this.currentIndex = index;
        preparePlayer(autoPlay);
    }

    // 异步准备播放器，并根据参数决定是否自动播放
    private void preparePlayer(boolean autoPlay) {
        // 清理上一个播放器
        release();
        if (musicList == null || musicList.isEmpty()) return;

        MusicInfo music = musicList.get(currentIndex);
        currentMusic.postValue(music); // 子线程通知 ViewModel 当前歌曲的信息，供刷新UI视图

        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(music.getMusicUrl()); // 设置播放 URL
            mediaPlayer.prepareAsync(); // 异步准备

            // 准备完成时回调
            mediaPlayer.setOnPreparedListener(mp -> {
                duration.postValue(mp.getDuration());
                isPrepared = true;
                if (autoPlay) {
                    mp.start();
                    isPlaying.postValue(true);
                    startProgressUpdate(); // 开启进度轮询
                }
            });

            // 播放完成回调，处理播放模式
            mediaPlayer.setOnCompletionListener(mp -> {
                if (playMode == PlayMode.REPEAT_SINGLE) {
                    mp.seekTo(0);
                    mp.start();
                } else {
                    playNext(); // 顺序或随机
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 播放下一首歌，按模式切换索引
    public void playNext() {
        logMusicList(); // 打印播放列表
        switch (playMode) {
            case SEQUENTIAL:
                Log.d("playMode", String.valueOf(playMode));

                currentIndex = (currentIndex + 1) % musicList.size();
                break;
            case SHUFFLE:
                Log.d("playMode", String.valueOf(playMode));
                int next;
                do {
                    next = random.nextInt(musicList.size());
                } while (next == currentIndex && musicList.size() > 1);
                currentIndex = next;
                break;
            case REPEAT_SINGLE:
                Log.d("playMode", String.valueOf(playMode));
                break;
        }
        Log.d("currentIndex", String.valueOf(currentIndex));

        preparePlayer(true);
    }

    // 切换播放 / 暂停
    public void togglePlayPause() {
        if (mediaPlayer == null || !isPrepared) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying.postValue(false);
        } else {
            mediaPlayer.start();
            isPlaying.postValue(true);
            startProgressUpdate();
        }
    }


    // 播放上一首歌
    public void playPrevious() {
        switch (playMode) {
            case SEQUENTIAL:
                currentIndex = (currentIndex - 1 + musicList.size()) % musicList.size();
                break;
            case SHUFFLE:
                int prev;
                do {
                    prev = random.nextInt(musicList.size());
                } while (prev == currentIndex && musicList.size() > 1);
                currentIndex = prev;
                break;
            case REPEAT_SINGLE:
                break;
        }
        preparePlayer(true);
    }


    // 拖动进度条跳转播放
    public void seekTo(int pos) {
        if (mediaPlayer != null && isPrepared) {
            mediaPlayer.seekTo(pos);
        }
    }


    // 设置播放模式
    public void setPlayMode(PlayMode mode) {
        playMode = mode;
        livePlayMode.postValue(mode);
    }

    // 启动定时器，每秒更新一次播放进度
    private void startProgressUpdate() {
        if (progressRunnable != null) handler.removeCallbacks(progressRunnable);
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPrepared && mediaPlayer.isPlaying()) {
                    position.postValue(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(progressRunnable);
    }

    // 释放播放器资源
    public void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // LiveData 供外部 ViewModel 观察
    public LiveData<MusicInfo> getCurrentMusic() { return currentMusic; }
    public LiveData<Boolean> getIsPlaying() { return isPlaying; }
    public LiveData<Integer> getPosition() { return position; }
    public LiveData<Integer> getDuration() { return duration; }
    public LiveData<PlayMode> getPlayMode() { return livePlayMode; }


    // 播放列表要用到的内容
    // 获取当前播放列表和索引
    public List<MusicInfo> getCurrentPlayList() {
        return musicList;
    }
    public int getCurrentIndex() {
        return currentIndex;
    }

    // 设置播放列表和当前索引（用于恢复播放）
    public void setPlayList(List<MusicInfo> newList) {this.musicList = newList;}
    public void setCurrentIndex(int currentIndex) {this.currentIndex = currentIndex;}



    // 播放列表为空，重置播放状态。
    public void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        isPrepared = false;
        isPlaying.postValue(false);
        currentMusic.postValue(null);
        position.postValue(0);
        duration.postValue(0);

        // 清除播放列表
        if (musicList != null) {
            musicList.clear();
        }

        // 停止进度更新
        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
        handler.removeCallbacksAndMessages(null); // 清除定时器
    }

    // 工具方法



    // 判断两个播放列表是否相同
    private boolean isSameMusicList(List<MusicInfo> list1, List<MusicInfo> list2) {
        return list1 != null && list1.equals(list2);
    }



    // 打印当前的播放列表
    private void logMusicList() {
        if (musicList == null || musicList.isEmpty()) {
            Log.d("MusicList", "当前播放列表为空");
            return;
        }

        StringBuilder sb = new StringBuilder("当前播放列表内容：\n");
        for (int i = 0; i < musicList.size(); i++) {
            MusicInfo music = musicList.get(i);
            sb.append(i)
                    .append(". ")
                    .append(music.getMusicName())
                    .append(" - ")
                    .append(music.getAuthor())
                    .append(" (url=")
                    .append(music.getMusicUrl())
                    .append(")\n");
        }
        Log.d("MusicList", sb.toString());
    }

    // 启动前台服务，防止被系统杀死
    private void startForegroundService() {
        String CHANNEL_ID = "music_player_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Playback",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("音乐正在播放")
                .setContentText("返回应用查看更多")
                .setSmallIcon(R.drawable.logo)
                .build();

        startForeground(1, notification);
    }
}
