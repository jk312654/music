package com.example.music_chenyujie.dataModel;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import java.util.List;

public class HomePageInfo implements MultiItemEntity {
    private int moduleConfigId;
    private String moduleName;
    private int style; // 1 banner, 2 横滑大卡, 3 一行一列, 4 一行两列
    private List<MusicInfo> musicInfoList;

    // Getter 和 Setter
    public int getModuleConfigId() {
        return moduleConfigId;
    }

    public void setModuleConfigId(int moduleConfigId) {
        this.moduleConfigId = moduleConfigId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public List<MusicInfo> getMusicInfoList() {
        return musicInfoList;
    }

    public void setMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList = musicInfoList;
    }

    @Override
    public int getItemType() {
        return style;
    }
}

