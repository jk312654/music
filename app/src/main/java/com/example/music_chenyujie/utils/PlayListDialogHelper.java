package com.example.music_chenyujie.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_chenyujie.Model.PlayMode;
import com.example.music_chenyujie.R;
import com.example.music_chenyujie.adapter.MusicDialogListAdapter;
import com.example.music_chenyujie.dataModel.MusicInfo;
import com.example.music_chenyujie.service.MusicPlayerService;
import com.example.music_chenyujie.viewModel.MusicPlayerViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;


// 播放列表管理器（单例）
public class PlayListDialogHelper {
    // 单例实例（volatile 保证线程可见性）
    private static volatile PlayListDialogHelper instance;
    // 音乐列表适配器（用于展示播放列表）
    private static MusicDialogListAdapter adapter;
    private List<MusicInfo> musicList;
    private TextView musicCountText;
    private MusicPlayerService musicService;

    // 播放列表弹窗
    private BottomSheetDialog dialog;

    private PlayListDialogHelper() {}


    /**
     * 获取 PlayListDialogHelper 单例
     * 使用双重检查锁优化懒加载性能与线程安全
     */
    public static PlayListDialogHelper getInstance() {
        if (instance == null) {
            synchronized (PlayListDialogHelper.class) {
                if (instance == null) {
                    instance = new PlayListDialogHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 展示播放列表弹窗，绑定 ViewModel 和播放服务
     * @param context 上下文（Activity）
     * @param viewModel 音乐播放器的 ViewModel（用于观察播放状态）
     * @param musicList 当前播放列表
     * @param musicService 播放控制服务
     */
    public void show(Context context,
                            MusicPlayerViewModel viewModel,
                            List<MusicInfo> musicList,
                            MusicPlayerService musicService) {

        this.musicList = musicList;
        this.musicService = musicService;

        dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_music_list, null);
        dialog.setContentView(view);

        ImageView playModeIcon = view.findViewById(R.id.playModeIcon);
        musicCountText = view.findViewById(R.id.musicCountText);
        RecyclerView recyclerView = view.findViewById(R.id.musicListRecyclerView);

        if (context instanceof LifecycleOwner) {
            viewModel.getPlayMode().observe((LifecycleOwner) context, mode -> {
                switch (mode) {
                    case SEQUENTIAL:
                        playModeIcon.setImageResource(R.drawable.ic_sequential);
                        break;
                    case SHUFFLE:
                        playModeIcon.setImageResource(R.drawable.ic_shuffle);
                        break;
                    case REPEAT_SINGLE:
                        playModeIcon.setImageResource(R.drawable.ic_repeat_one);
                        break;
                }
            });
        }

        playModeIcon.setOnClickListener(v -> {
            PlayMode currentMode = viewModel.getPlayMode().getValue();
            PlayMode nextMode;
            switch (currentMode) {
                case SEQUENTIAL:
                    nextMode = PlayMode.SHUFFLE;
                    break;
                case SHUFFLE:
                    nextMode = PlayMode.REPEAT_SINGLE;
                    break;
                default:
                    nextMode = PlayMode.SEQUENTIAL;
                    break;
            }
            viewModel.setPlayMode(nextMode);
        });

        musicCountText.setText("播放列表（" + musicList.size() + "首）");

        // 创建 RecyclerView 适配器
        adapter = new MusicDialogListAdapter(
                musicList,
                // 点击列表中的歌曲进行播放
                position -> {
                    MusicInfo selected = musicList.get(position);
                    if (!selected.equals(viewModel.getCurrentMusic().getValue())) {
                        musicService.init(musicList, position, true); // 切换播放

                    }
                    dialog.dismiss(); // 关闭弹窗
                },
                // 删除播放列表中的某个音乐项
                position -> {
                    // 删除逻辑
                    MusicInfo toRemove = musicList.get(position);
                    boolean isCurrent = toRemove.equals(viewModel.getCurrentMusic().getValue());

                    // 从列表中移除
                    musicList.remove(position);
                    adapter.notifyItemRemoved(position);
                    musicCountText.setText("播放列表（" + musicList.size() + "首）");

                    // 更新 service 播放列表
                    musicService.setPlayList(musicList);

                    if (musicList.isEmpty()) {
                        musicService.stopPlayback(); // 全部删除则停止播放
                        dialog.dismiss();
                        return;
                    }

                    // 若删除的是当前播放项，则播放下一首
                    if (isCurrent) {
                        // currentIndex 的调整
                        int curIndex = musicService.getCurrentIndex();
                        if (curIndex >= position) {
                            curIndex--; // 删除的是当前位置或前面的，要左移 index
                            if (curIndex < 0) {
                                curIndex = musicList.size() - 1; // 防止越界，循环到最后
                            }
                            musicService.setCurrentIndex(curIndex);
                        }

                        musicService.playNext(); // playNext 内部会做 +1，最终等价于跳到删除项的“下一首”
                    }
                }
        );

        // 设置当前播放项（初始高亮）
        adapter.setCurrentMusic(viewModel.getCurrentMusic().getValue());

        // 设置 RecyclerView 布局与适配器
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);


        // 监听当前播放项变化并刷新高亮显示
        if (context instanceof LifecycleOwner) {
            viewModel.getCurrentMusic().observe((LifecycleOwner) context, current -> {
                adapter.setCurrentMusic(current);
                adapter.notifyDataSetChanged();
            });
        }



        dialog.show();
    }


    /**
     * 向播放列表中添加歌曲（不重复）
     * @param music 要添加的音乐
     */
    public void addMusic(MusicInfo music) {
        if (music == null || adapter == null ||  musicService == null) return;

        if (musicList == null) {
            musicList = new ArrayList<>();
        }

        if (musicList.contains(music)) return;

        musicList.add(music);
        adapter.notifyItemInserted(musicList.size() - 1);
        if (musicCountText != null) {
            musicCountText.setText("播放列表（" + musicList.size() + "首）");
        }
        musicService.setPlayList(musicList);
    }



}

