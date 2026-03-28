package com.demo.musicvideoplayer.fragment;

import com.demo.musicvideoplayer.model.AudioModel;
import java.util.concurrent.Callable;

public final class MusicFragmentObservable1 implements Callable {
    public final MusicFragment musicFragment1;
    public final AudioModel audioModel1;

    public MusicFragmentObservable1(MusicFragment musicFragment, AudioModel audioModel) {
        this.musicFragment1 = musicFragment;
        this.audioModel1 = audioModel;
    }

    @Override
    public Object call() throws Exception {
        return this.musicFragment1.MusicFragmentObservable1call(this.audioModel1);
    }
}
