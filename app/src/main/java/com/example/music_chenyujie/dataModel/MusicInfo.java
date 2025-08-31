package com.example.music_chenyujie.dataModel;


import android.os.Parcel;
import android.os.Parcelable;

public class MusicInfo implements Parcelable {
    private long id;
    private String musicName;
    private String author;
    private String coverUrl; // 图片的http链接
    private String musicUrl; // 音频mp3的http链接
    private String lyricUrl; // 歌词text的http链接

    // 构造方法
    public MusicInfo() {}

    protected MusicInfo(Parcel in) {
        id = in.readLong();
        musicName = in.readString();
        author = in.readString();
        coverUrl = in.readString();
        musicUrl = in.readString();
        lyricUrl = in.readString();
    }

    // Parcelable实现
    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel in) {
            return new MusicInfo(in);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(musicName);
        dest.writeString(author);
        dest.writeString(coverUrl);
        dest.writeString(musicUrl);
        dest.writeString(lyricUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getter 和 Setter 方法
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getMusicUrl() {
        return musicUrl;
    }

    public void setMusicUrl(String musicUrl) {
        this.musicUrl = musicUrl;
    }

    public String getLyricUrl() {
        return lyricUrl;
    }

    public void setLyricUrl(String lyricUrl) {
        this.lyricUrl = lyricUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MusicInfo)) return false;
        MusicInfo other = (MusicInfo) obj;
        return this.getId() == other.getId(); // 如果是 int 类型
    }

    @Override
    public int hashCode() {
        return Integer.hashCode((int) getId());
    }
}

