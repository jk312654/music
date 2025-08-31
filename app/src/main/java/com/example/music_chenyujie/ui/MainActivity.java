package com.example.music_chenyujie.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.music_chenyujie.App;
import com.example.music_chenyujie.R;
import com.example.music_chenyujie.adapter.HomeMultiAdapter;
import com.example.music_chenyujie.dataModel.HomePageInfo;
import com.example.music_chenyujie.dataModel.MusicInfo;
import com.example.music_chenyujie.service.MusicPlayerService;
import com.example.music_chenyujie.utils.MusicRepository;
import com.example.music_chenyujie.utils.PlayListDialogHelper;
import com.example.music_chenyujie.viewModel.MusicPlayerViewModel;
import com.example.music_chenyujie.viewModel.MusicPlayerViewModelFactory;

import java.util.ArrayList;
import java.util.List;



// 首页 Activity，展示音乐模块列表。
public class MainActivity extends AppCompatActivity {
    // 首页内容列表相关组件
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HomeMultiAdapter adapter;


    // 分页加载相关参数
    private int currentPage = 1;
    private boolean isLoading = false;  // 避免重复加载


    // 播放服务及 ViewModel（通过服务控制播放逻辑）
    private MusicPlayerService musicService;
    private MusicPlayerViewModel viewModel; // MusicPlayerService的桥接

    // 标志位：首次进入首页才自动播放一个模块的音乐，刷新和加载数据不随机播放。
    private boolean hasAutoPlayed = false;

    // 悬浮小控件相关
    private View miniPlayer;
    private TextView tvTitle, tvArtist;
    private ImageView imgCover;
    private ImageButton btnPlayPauseMini, btnPlaylistMini;
    private SeekBar seekBarMini;


    // 标志：用户是否正在手动拖动进度条
    private boolean userIsSeeking = false;


    /**
     * 提供 MusicService 实例给 Adapter 或 Dialog 调用播放控制
     */
    public MusicPlayerService getMusicService() {
        return musicService;
    }


    /**
     * 与 MusicPlayerService 的绑定回调
     * 初始化 ViewModel 和播放状态观察
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 获取服务实例
            MusicPlayerService.LocalBinder binder = (MusicPlayerService.LocalBinder) service;
            musicService = binder.getService();

            // 通过 App 全局提供 ViewModel（和 MusicPlayerActivity 保持一致）
            viewModel = new ViewModelProvider(App.get(MainActivity.this),
                    new MusicPlayerViewModelFactory(musicService))
                    .get(MusicPlayerViewModel.class);

            // 初始化播放器点击事件
            initMiniPlayerEvents();
            // 注册 Mini 观察者监听播放状态并更新UI
            observeMiniPlayer();
            // 初始化后尝试播放 Service可能已经初始化好了，但是数据还没加载好。原因是Service和数据加载都是异步请求。
            tryAutoPlayFirstModule();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取 Mini 播放器相关视图引用
        miniPlayer = findViewById(R.id.mini_Player);
        tvTitle = miniPlayer.findViewById(R.id.mini_tvTitle);
        tvArtist = miniPlayer.findViewById(R.id.mini_tvArtist);
        imgCover = miniPlayer.findViewById(R.id.mini_imgCover);
        btnPlayPauseMini = miniPlayer.findViewById(R.id.mini_btnPlayPauseMini);
        btnPlaylistMini = miniPlayer.findViewById(R.id.mini_btnPlaylistMini);
        seekBarMini = miniPlayer.findViewById(R.id.mini_seekBarMini);



        // 初始化列表组件
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.recycler_home); // R.id.recycler_home是外层的RecycleView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new HomeMultiAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // 初始加载第一页数据
        loadData(currentPage);

        //  下拉刷新逻辑
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1; // 重置页码
            adapter.clear(); // 清空数据（需实现 clear 方法）
            loadData(currentPage); // 重新加载
        });

        //  上拉加载逻辑
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!rv.canScrollVertically(1) && !isLoading) {
                    // 到达底部且未加载中
                    isLoading = true;
                    // 加载下一页
                    loadData(++currentPage);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 启动并绑定音乐服务（绑定后回调中初始化 ViewModel）
        Intent intent = new Intent(this, MusicPlayerService.class);
        // 保证服务不被系统杀掉
        startService(intent);
        // 绑定服务
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();
         // 解绑服务，只是不监听而已，Service实际上还是在播放
        unbindService(serviceConnection);
    }


    private void loadData(int page) {
        swipeRefreshLayout.setRefreshing(true); // 显示加载动画

        // 加载数据
        MusicRepository.loadHomePageData(page, 20, this, new MusicRepository.DataCallback() {
            @Override
            public void onSuccess(List<HomePageInfo> data) {
                adapter.addData(data); // 更新列表数据

                // 如果是首页首次加载且服务已绑定，尝试自动播放
                if (currentPage == 1 && musicService != null) {
                    runOnUiThread(() -> tryAutoPlayFirstModule());
                }
                //
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false); // 关闭刷新动画
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    /**
     * 首次启动自动播放一个模块的第一首歌
     * 只执行一次（避免刷新触发）
    **/
    private void tryAutoPlayFirstModule() {
        // 确保首页只在启动时执行随机播放
        if (hasAutoPlayed || musicService == null) {
            return;
        }

        // 获取首页数据列表
        List<HomePageInfo> list = adapter.getData();
        if (list == null || list.isEmpty()) {
            return;
        }

        // 随机播放一个模块
        for (HomePageInfo info : list) {
            if (info.getMusicInfoList() != null && !info.getMusicInfoList().isEmpty()) {
                musicService.init(info.getMusicInfoList(), 0, true);
                break;
            }
        }

        // 标记已经播放
        hasAutoPlayed = true;
    }


    /**
     * 初始化 Mini 播放器的点击和滑动事件
     */
    private void initMiniPlayerEvents() {

        // 播放/暂停按钮点击事件
        btnPlayPauseMini.setOnClickListener(v -> {
            viewModel.togglePlayPause();
        });

        // 播放列表按钮点击事件
        btnPlaylistMini.setOnClickListener(v -> {
            // 播放列表
            PlayListDialogHelper.getInstance().show(this, viewModel, musicService.getCurrentPlayList(), musicService);
        });

        // 点击整个 MiniPlayer，跳转至全屏播放器
        miniPlayer.setOnClickListener(v -> {
            Intent intent = new Intent(this, MusicPlayerActivity.class);
            ActivityOptions options = ActivityOptions.makeCustomAnimation(
                    this, R.anim.slide_in_up, R.anim.no_anim);
            startActivity(intent, options.toBundle());
        });


        // 拖动进度条事件监听
        seekBarMini.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) viewModel.seekTo(progress); // 拖动进度
            }

            // 正在拖动
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }
            // 拖动结束
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false;
            }
        });
    }



    /**
     * 监听 Mini 播放器状态变化并更新 UI
     */
    private void observeMiniPlayer() {
        // 当前播放的歌曲
        viewModel.getCurrentMusic().observe(this, music -> {
            tvTitle.setText(music.getMusicName());
            tvArtist.setText(music.getAuthor());
            String url = music.getCoverUrl();

            // 用 https:// 替换 http://
            if(url.startsWith("http://")){
                url = url.replace("http://","https://");
            }
            // 加载封面图（使用 Glide）
            Glide.with(this).load(url).into(imgCover);
        });


        // 播放/暂停按钮状态
        viewModel.getIsPlaying().observe(this, playing -> {
            btnPlayPauseMini.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
        });

        // 音乐总时长（用于 SeekBar 最大值）
        viewModel.getDuration().observe(this, dur -> {
            seekBarMini.setMax(dur);
        });

        // 当前播放位置（更新 SeekBar）
        viewModel.getPosition().observe(this, pos -> {
            if (!userIsSeeking) seekBarMini.setProgress(pos);
        });
    }




}