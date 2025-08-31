package com.example.music_chenyujie.ui;

import static com.example.music_chenyujie.Model.PlayMode.REPEAT_SINGLE;
import static com.example.music_chenyujie.Model.PlayMode.SEQUENTIAL;
import static com.example.music_chenyujie.Model.PlayMode.SHUFFLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.music_chenyujie.App;
import com.example.music_chenyujie.Model.PlayMode;
import com.example.music_chenyujie.R;
import com.example.music_chenyujie.adapter.MusicDialogListAdapter;
import com.example.music_chenyujie.adapter.MusicFragmentAdapter;
import com.example.music_chenyujie.adapter.MusicListAdapter;
import com.example.music_chenyujie.dataModel.MusicInfo;
import com.example.music_chenyujie.service.MusicPlayerService;
import com.example.music_chenyujie.utils.PlayListDialogHelper;
import com.example.music_chenyujie.viewModel.MusicPlayerViewModel;
import com.example.music_chenyujie.viewModel.MusicPlayerViewModelFactory;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.Locale;


// 音乐播放器主界面 Activity，展示播放控制界面和歌词、封面等
public class MusicPlayerActivity extends AppCompatActivity {

    // 播放信息相关 UI 控件
    private TextView songTitle, artistName, currentTime, totalTime;
    private SeekBar seekBar;
    private ImageButton btnPrev, btnPlayPause, btnNext, btnPlayMode, btnPlayList,btnClose;
    private ViewPager2 viewPager;

    // 当前播放列表与 Adapter
    private List<MusicInfo> musicList;
    private MusicFragmentAdapter fragmentAdapter;

    // 用于 UI 更新的主线程定时任务
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateProgressRunnable;

    // 播放器 ViewModel，用于监听服务状态
    private MusicPlayerViewModel viewModel;

    // 后台音乐服务实例
    private MusicPlayerService musicService;

    // 当前播放音乐的索引
    private int currentIndex;



    // ServiceConnection 用于绑定 MusicPlayerService
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取服务实例
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();

            /*Intent intent = getIntent();
            musicList = intent.getParcelableArrayListExtra("music_list");
            currentIndex = intent.getIntExtra("index", 0);
            Log.d("onServiceConnected: ", musicList.get(0).getMusicName());
            Log.d("onServiceConnected: ", String.valueOf(currentIndex));
            musicService.init(musicList, currentIndex, false);*/


            // 从 Service 获取播放列表与当前索引
            musicList = musicService.getCurrentPlayList();
            currentIndex = musicService.getCurrentIndex();

            // viewModel由App类管理，保证全局
            viewModel = new ViewModelProvider(App.get(MusicPlayerActivity.this),
                    new MusicPlayerViewModelFactory(musicService))
                    .get(MusicPlayerViewModel.class);


            // 监听viewmodel中数据的变化,用于更新UI
            observeViewModel();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        // 初始化播放控制盘的布局
        initViews();

        // 初始化fragment
        initViewPager();

        // fragment页面切换监听，切换到歌词页时加载歌词
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 1) {
                    MusicInfo currentMusic = viewModel.getCurrentMusic().getValue();
                    if (currentMusic != null) {
                        fragmentAdapter.getLyricFragment().loadLyricFromUrl(currentMusic.getLyricUrl());
                    }
                }
            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MusicPlayerService.class);
        startService(intent); // 保证 service 不会因 unbind 被杀
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }




    private void initViews(){
        // 获取播放控制盘的布局
        View control_panel = findViewById(R.id.musicControls);
        songTitle = control_panel.findViewById(R.id.songTitle);
        artistName = control_panel.findViewById(R.id.artistName);
        currentTime = control_panel.findViewById(R.id.currentTime);
        totalTime = control_panel.findViewById(R.id.totalTime);
        seekBar = control_panel.findViewById(R.id.songSeekBar);
        btnPrev = control_panel.findViewById(R.id.btnPrevious);
        btnPlayPause = control_panel.findViewById(R.id.btnPlayPause);
        btnNext = control_panel.findViewById(R.id.btnNext);
        btnPlayMode = control_panel.findViewById(R.id.btnPlayMode);
        btnPlayList = control_panel.findViewById(R.id.btnPlaylist);
        btnClose = findViewById(R.id.btnCloseContainer).findViewById(R.id.my_btn_Close);

        // 返回按钮
        btnClose.setOnClickListener(v -> {
            finish();
        });
        // 点击播放前一首歌曲的按钮
        btnPrev.setOnClickListener(v -> {
            viewModel.playPrevious();
        });
        // 点击播放下一首歌曲的按钮
        btnNext.setOnClickListener(v -> {
            viewModel.playNext();
        });


        // 播放/暂停
        btnPlayPause.setOnClickListener(v -> {
            viewModel.togglePlayPause();
        });

        // 打开播放列表弹窗
        btnPlayList.setOnClickListener(v ->
                PlayListDialogHelper.getInstance().show(this, viewModel, musicList, musicService));

        // 切换播放模式（顺序 → 随机 → 单曲）
        btnPlayMode.setOnClickListener(v -> {
            // 切换播放模式
            PlayMode currentMode = viewModel.getPlayMode().getValue();
            PlayMode nextMode;
            switch (currentMode) {
                case SEQUENTIAL:
                    nextMode = SHUFFLE;
                    break;
                case SHUFFLE:
                    nextMode = REPEAT_SINGLE;
                    break;
                default:
                    nextMode = SEQUENTIAL;
                    break;
            }
            viewModel.setPlayMode(nextMode);
        });


        // 拖动进度条跳转
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    viewModel.seekTo(progress);
                }
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    // 初始化fragment （用于切换封面、歌词）
    private void initViewPager() {
        viewPager = findViewById(R.id.viewPager);
        fragmentAdapter = new MusicFragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);
    }



    // 切换歌曲后，更新UI数据
    private void observeViewModel() {
        // 当前歌曲变化
        viewModel.getCurrentMusic().observe(this, music -> {
            songTitle.setText(music.getMusicName());
            artistName.setText(music.getAuthor());

            // 显式重置 UI 状态
            currentTime.setText("00:00");
            seekBar.setProgress(0);
            btnPlayPause.setImageResource(R.drawable.ic_play);
            fragmentAdapter.getCoverFragment().stopRotate(); // 避免封面继续旋转

            // 更新封面与歌词
            fragmentAdapter.getCoverFragment().setCoverUrl(music.getCoverUrl());
            fragmentAdapter.getLyricFragment().loadLyricFromUrl(music.getLyricUrl());
        });

        viewModel.getPlayMode().observe(this, mode -> {
            switch (mode) {
                case SEQUENTIAL:
                    btnPlayMode.setImageResource(R.drawable.ic_sequential);
                    break;
                case SHUFFLE:
                    btnPlayMode.setImageResource(R.drawable.ic_shuffle);
                    break;
                case REPEAT_SINGLE:
                    btnPlayMode.setImageResource(R.drawable.ic_repeat_one);
                    break;
            }
        });

        viewModel.getPosition().observe(this, pos -> {
            seekBar.setProgress(pos);
            currentTime.setText(formatTime(pos));
        });

        viewModel.getDuration().observe(this, dur -> {
            seekBar.setMax(dur);
            totalTime.setText(formatTime(dur));
        });

        viewModel.getIsPlaying().observe(this,play ->{
            btnPlayPause.setImageResource(play? R.drawable.ic_pause : R.drawable.ic_play);
            // 控制封面旋转动画
            if (play) {
                fragmentAdapter.getCoverFragment().startRotate();
            } else {
                fragmentAdapter.getCoverFragment().stopRotate();
            }
        });

    }


    private String formatTime(int ms) {
        int sec = ms / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d", sec / 60, sec % 60);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateProgressRunnable);
    }

}