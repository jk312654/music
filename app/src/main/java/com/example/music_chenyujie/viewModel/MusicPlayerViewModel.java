package com.example.music_chenyujie.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.music_chenyujie.Model.PlayMode;
import com.example.music_chenyujie.dataModel.MusicInfo;
import com.example.music_chenyujie.service.MusicPlayerService;

import java.util.List;

public class MusicPlayerViewModel extends ViewModel {
    private final MusicPlayerService service;

    public MusicPlayerViewModel(MusicPlayerService srv) {

        this.service = srv;
    }

    public LiveData<MusicInfo> getCurrentMusic() { return service.getCurrentMusic(); }
    public LiveData<Boolean> getIsPlaying() { return service.getIsPlaying(); }
    public LiveData<Integer> getPosition() { return service.getPosition(); }
    public LiveData<Integer> getDuration() { return service.getDuration(); }
    public LiveData<PlayMode> getPlayMode() { return service.getPlayMode(); }



    public void togglePlayPause() { service.togglePlayPause(); }
    public void playNext() { service.playNext(); }
    public void playPrevious() { service.playPrevious(); }
    public void seekTo(int pos) { service.seekTo(pos); }
    public void setPlayMode(PlayMode mode) { service.setPlayMode(mode); }
}
