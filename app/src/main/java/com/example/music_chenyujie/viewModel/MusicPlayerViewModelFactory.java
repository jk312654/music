package com.example.music_chenyujie.viewModel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.music_chenyujie.service.MusicPlayerService;

public class MusicPlayerViewModelFactory implements ViewModelProvider.Factory {
    private final MusicPlayerService service;

    public MusicPlayerViewModelFactory(MusicPlayerService service) {
        this.service = service;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel.class)) {
            return (T) new MusicPlayerViewModel(service);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

