package com.example.music_chenyujie.adapter;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.music_chenyujie.R;
import com.example.music_chenyujie.dataModel.HomePageInfo;
import com.example.music_chenyujie.dataModel.MusicInfo;
import com.example.music_chenyujie.service.MusicPlayerService;
import com.example.music_chenyujie.ui.MainActivity;
import com.example.music_chenyujie.ui.MusicPlayerActivity;
import com.example.music_chenyujie.utils.PlayListDialogHelper;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import java.util.ArrayList;
import java.util.List;


// HomeMultiAdapter 是一个多类型适配器，用于根据不同的视图类型展示主页内容
public class HomeMultiAdapter extends BaseMultiItemQuickAdapter<HomePageInfo, BaseViewHolder> {

    /**
     * 构造函数，初始化适配器并设置各个 Item 类型的布局
     * @param data 传入的数据源
     */
    public HomeMultiAdapter(List<HomePageInfo> data) {
        super(data);
        addItemType(1, R.layout.item_home_banner);
        addItemType(2, R.layout.item_home_horizontal);
        addItemType(3, R.layout.item_home_single_column);
        addItemType(4, R.layout.item_home_two_column);
    }

    /**
     * 根据不同的 item 类型绑定视图
     * @param helper ViewHolder 用于绑定视图
     * @param item 当前的 dataItem
     */
    @Override
    protected void convert(BaseViewHolder helper, HomePageInfo item) {

        // 判断是否有歌曲列表，若没有则跳过此项的绑定
        if (item.getMusicInfoList() == null || item.getMusicInfoList().isEmpty()) return;

        // 根据不同的 item 类型处理布局
        switch (helper.getItemViewType()) {
            case 1:
                // banner 轮播图
                Banner banner = helper.getView(R.id.banner);
                List<String> imageUrls = new ArrayList<>();
                for (MusicInfo info : item.getMusicInfoList()) {
                    imageUrls.add(info.getCoverUrl()); // 你在 MusicInfo 中自定义字段
                }
                banner.setAdapter(new BannerImageAdapter<String>(imageUrls) {
                    @Override
                    public void onBindView(BannerImageHolder holder, String data, int position, int size) {
                        if (data.startsWith("http://")) {
                            data = data.replace("http://", "https://");
                        }
                        Glide.with(holder.itemView)
                                .load(data)
                                .into(holder.imageView);
                    }
                });
                // 设置圆点指示器
                banner.setIndicator(new CircleIndicator(helper.itemView.getContext()));
                break;

            case 2:
                // 横滑卡片模块
                RecyclerView rvHorizontal = helper.getView(R.id.rv_horizontal);
                rvHorizontal.setLayoutManager(new LinearLayoutManager(helper.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                rvHorizontal.setAdapter(new MusicCardAdapter(item.getMusicInfoList()));


                MusicCardAdapter cardAdapter = new MusicCardAdapter(item.getMusicInfoList());
                rvHorizontal.setAdapter(cardAdapter);
                // 点击卡片播放音乐
                cardAdapter.setOnItemClickListener((adapter, view, position) -> {
                    /*MusicInfo music = item.musicInfoList.get(position);
                    Toast.makeText(view.getContext(), "音乐名称：" + music.musicName, Toast.LENGTH_SHORT).show();*/
                    Context context = view.getContext();
                    /*Intent intent = new Intent(context,MusicPlayerActivity.class);
                    intent.putParcelableArrayListExtra("music_list", new ArrayList<>(item.getMusicInfoList()));
                    intent.putExtra("index", position);
                    context.startActivity(intent);*/
                    if (context instanceof MainActivity) {
                        MainActivity main = (MainActivity) context;
                        MusicPlayerService service = main.getMusicService(); // 你需要添加 getter
                        if (service != null) {
                            service.init(item.getMusicInfoList(), position, true);
                            context.startActivity(new Intent(context, MusicPlayerActivity.class));
                        }
                    }

                });

                // 点击item中的➕号，将item添加到播放列表
                cardAdapter.setOnAddClickListener(musicInfo -> {
                    Context context = helper.itemView.getContext();
                    PlayListDialogHelper.getInstance().addMusic(musicInfo);
                    Toast.makeText(context, "已添加到播放列表：" + musicInfo.getMusicName(), Toast.LENGTH_SHORT).show();
                });

                break;

            case 3:
                // 一行一列
                RecyclerView rvSingleColumn = helper.getView(R.id.rv_single_column);
                rvSingleColumn.setLayoutManager(new LinearLayoutManager(helper.itemView.getContext()));

                MusicListAdapter listAdapter = new MusicListAdapter(item.getMusicInfoList());
                rvSingleColumn.setAdapter(listAdapter);

                listAdapter.setOnItemClickListener((adapter, view, position) -> {
                    /*MusicInfo music = item.musicInfoList.get(position);
                    Toast.makeText(view.getContext(), "音乐名称：" + music.musicName, Toast.LENGTH_SHORT).show();*/
                    Context context = view.getContext();
                    /*Intent intent = new Intent(context,MusicPlayerActivity.class);
                    intent.putParcelableArrayListExtra("music_list", new ArrayList<>(item.getMusicInfoList()));
                    intent.putExtra("index", position);
                    context.startActivity(intent);*/
                    if (context instanceof MainActivity) {
                        MainActivity main = (MainActivity) context;
                        MusicPlayerService service = main.getMusicService(); // 你需要添加 getter
                        if (service != null) {
                            service.init(item.getMusicInfoList(), position, true);
                            context.startActivity(new Intent(context, MusicPlayerActivity.class));
                        }
                    }
                });

                // 点击item中的➕号，将item添加到播放列表
                listAdapter.setOnAddClickListener(musicInfo -> {
                    Context context = helper.itemView.getContext();
                    PlayListDialogHelper.getInstance().addMusic(musicInfo);
                    Toast.makeText(context, "已添加到播放列表：" + musicInfo.getMusicName(), Toast.LENGTH_SHORT).show();
                });
                break;

            case 4:
                // 一行两列网格
                RecyclerView rvTwoColumn = helper.getView(R.id.rv_two_column);
                rvTwoColumn.setLayoutManager(new GridLayoutManager(helper.itemView.getContext(), 2));

                MusicGridAdapter gridAdapter = new MusicGridAdapter(item.getMusicInfoList());
                rvTwoColumn.setAdapter(gridAdapter);
                // 点击item跳转的功能
                gridAdapter.setOnItemClickListener((adapter, view, position) -> {
                    /*MusicInfo music = item.musicInfoList.get(position);
                    Toast.makeText(view.getContext(), "音乐名称：" + music.musicName, Toast.LENGTH_SHORT).show();*/
                    Context context = view.getContext();
                    /*Intent intent = new Intent(context,MusicPlayerActivity.class);
                    intent.putParcelableArrayListExtra("music_list", new ArrayList<>(item.getMusicInfoList()));
                    intent.putExtra("index", position);
                    context.startActivity(intent);*/
                    if (context instanceof MainActivity) {
                        MainActivity main = (MainActivity) context;
                        MusicPlayerService service = main.getMusicService();
                        if (service != null) {
                            service.init(item.getMusicInfoList(), position, true);
                            context.startActivity(new Intent(context, MusicPlayerActivity.class));
                        }
                    }
                });

                // 点击item中的➕号，将item添加到播放列表
                gridAdapter.setOnAddClickListener(musicInfo -> {
                    Context context = helper.itemView.getContext();
                    PlayListDialogHelper.getInstance().addMusic(musicInfo);
                    Toast.makeText(context, "已添加到播放列表：" + musicInfo.getMusicName(), Toast.LENGTH_SHORT).show();
                });
                break;
        }
    }

    /**
     * 上拉加载数据，添加新数据并更新视图
     * @param newData 新数据
     */
    public void addData(List<HomePageInfo> newData) {
        int start = getData().size();
        getData().addAll(newData);
        notifyItemRangeInserted(start, newData.size()); // 更新数据项
    }

    /**
     * 清除当前的数据并刷新视图
     */
    public void clear() {
        getData().clear();
        notifyDataSetChanged();// 清空数据并刷新
    }

}

