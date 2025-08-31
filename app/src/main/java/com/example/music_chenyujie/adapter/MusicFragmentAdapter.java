package com.example.music_chenyujie.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.music_chenyujie.ui.CoverFragment;
import com.example.music_chenyujie.ui.LyricFragment;

public class MusicFragmentAdapter extends FragmentStateAdapter {
    private final CoverFragment coverFragment = new CoverFragment();
    private final LyricFragment lyricFragment = new LyricFragment();

    public MusicFragmentAdapter(@NonNull FragmentActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? coverFragment : lyricFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public CoverFragment getCoverFragment() {
        return coverFragment;
    }

    public LyricFragment getLyricFragment() {
        return lyricFragment;
    }
}
