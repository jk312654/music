package com.example.music_chenyujie.adapter;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.music_chenyujie.R;
import com.example.music_chenyujie.dataModel.MusicInfo;

import java.util.List;

public class MusicGridAdapter extends BaseQuickAdapter<MusicInfo, BaseViewHolder> {
    String newCoverUrl;
    // 点击加号后，添加MusicInfo item搭配
    private OnAddClickListener onAddClickListener;

    public interface OnAddClickListener {
        void onAdd(MusicInfo music);
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.onAddClickListener = listener;
    }
    public MusicGridAdapter(List<MusicInfo> data) {
        super(R.layout.item_music_grid, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MusicInfo item) {
        helper.setText(R.id.tv_music_name, item.getMusicName());
        helper.setText(R.id.tv_author, item.getAuthor());
        if (item.getCoverUrl().startsWith("http://")) {
            newCoverUrl = item.getCoverUrl().replace("http://", "https://");
        }
        Log.d("MusicGridAdapter", "加载图片: " + newCoverUrl);
        Glide.with(helper.itemView.getContext())
                .load(newCoverUrl)
                .into((ImageView) helper.getView(R.id.iv_cover));

        // 点击+号：弹出添加
        helper.getView(R.id.tv_add).setOnClickListener(v -> {
            if (onAddClickListener != null) {
                onAddClickListener.onAdd(item);
            }
        });
    }
}
