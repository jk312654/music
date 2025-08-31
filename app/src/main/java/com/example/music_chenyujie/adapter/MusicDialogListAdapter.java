package com.example.music_chenyujie.adapter;

import android.graphics.Color;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.music_chenyujie.R;
import com.example.music_chenyujie.dataModel.MusicInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;



public class MusicDialogListAdapter extends BaseQuickAdapter<MusicInfo, BaseViewHolder> {

    // 用于设置当前音乐播放列表正在播放的音乐高亮
    private MusicInfo currentMusic;

    // 设置当前播放的音乐（外部调用更新）
    public void setCurrentMusic(MusicInfo currentMusic) {
        this.currentMusic = currentMusic;
    }


    // 事件回调（播放功能）
    public interface OnItemClickListener {
        void onClick(int position);
    }


    // 事件回调（删除功能）
    public interface OnItemDeleteListener {
        void onDelete(int position);
    }


    private final OnItemClickListener clickListener;
    private final OnItemDeleteListener deleteListener;


    /**
     * 构造函数
     * @param data 音乐列表数据
     * @param clickListener 点击播放项的监听器
     * @param deleteListener 点击删除按钮的监听器
     */
    public MusicDialogListAdapter(@Nullable List<MusicInfo> data,
                                  OnItemClickListener clickListener,
                                  OnItemDeleteListener deleteListener) {
        super(R.layout.dialog_item_music_list, data); // 指定 item 布局
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }


    /**
     * 绑定每一项的数据与视图
     * @param holder 当前项的 ViewHolder
     * @param music 当前项的数据
     */
    @Override
    protected void convert(@NotNull BaseViewHolder holder, MusicInfo music) {
        // 设置歌曲名称和歌手名称
        holder.setText(R.id.dialog_item_songName, music.getMusicName())
                .setText(R.id.dialog_item_artistName, music.getAuthor());


        // 获取 View 引用
        TextView songName = holder.getView(R.id.dialog_item_songName);
        TextView artistName = holder.getView(R.id.dialog_item_artistName);
        ImageButton deleteButton = holder.getView(R.id.dialog_item_btnDelete);

        // 判断是否是当前播放的歌曲
        boolean isCurrent = currentMusic != null && currentMusic.equals(music);
        int highlightColor = Color.parseColor("#2196F3");  // 蓝色（你可自定义）
        int normalColor = Color.BLACK;


        // 根据是否是当前播放项，设置文本颜色（高亮处理）
        songName.setTextColor(isCurrent ? highlightColor : normalColor);
        artistName.setTextColor(isCurrent ? highlightColor : normalColor);

        // 设置整个 item 的点击事件，触发播放操作
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (clickListener != null && position != RecyclerView.NO_POSITION) {
                clickListener.onClick(position);
            }
        });




        // 设置删除按钮点击事件
        deleteButton.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (deleteListener != null && position != RecyclerView.NO_POSITION) {
                deleteListener.onDelete(position);
            }
        });
    }



}
