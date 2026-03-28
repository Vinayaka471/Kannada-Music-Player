package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.databinding.ItemPlaylistVideoBinding;
import com.demo.musicvideoplayer.model.VideoModal;
import java.util.List;

public class BottomVideoListAdapter extends RecyclerView.Adapter<BottomVideoListAdapter.ViewHolder> {
    Context context;
    boolean isPlaying;
    List<VideoModal> list;
    int playingPos;
    VideoClick videoClick;

    public interface VideoClick {
        void Click(VideoModal videoModal, int i);
    }

    public BottomVideoListAdapter(Context context2, List<VideoModal> list2, int i, boolean z, VideoClick videoClick2) {
        this.context = context2;
        this.list = list2;
        this.playingPos = i;
        this.isPlaying = z;
        this.videoClick = videoClick2;
    }

    public void setPlayingPos(int i, boolean z) {
        this.playingPos = i;
        this.isPlaying = z;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playlist_video, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        VideoModal videoModal = this.list.get(i);
        Glide.with(this.context).load(this.list.get(i).getaPath()).into(viewHolder.binding.thumbnail);
        viewHolder.binding.txtDuration.setText(videoModal.timeSet());
        viewHolder.binding.txtSize.setText(videoModal.sizeSet());
        if (this.playingPos != i) {
            //viewHolder.binding.cardFolder.setStrokeWidth(0);
        } else if (this.isPlaying) {
            //viewHolder.binding.cardFolder.setStrokeWidth(6);
        } else {
            //viewHolder.binding.cardFolder.setStrokeWidth(0);
        }
    }

    @Override
    public int getItemCount() {
        return this.list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemPlaylistVideoBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemPlaylistVideoBinding itemPlaylistVideoBinding = (ItemPlaylistVideoBinding) DataBindingUtil.bind(view);
            this.binding = itemPlaylistVideoBinding;
            itemPlaylistVideoBinding.cardFolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (BottomVideoListAdapter.this.playingPos != -1) {
                        BottomVideoListAdapter.this.setPlayingPos(BottomVideoListAdapter.this.playingPos, false);
                        BottomVideoListAdapter.this.notifyItemChanged(BottomVideoListAdapter.this.playingPos);
                        BottomVideoListAdapter.this.playingPos = ViewHolder.this.getLayoutPosition();
                    }
                    BottomVideoListAdapter.this.setPlayingPos(BottomVideoListAdapter.this.playingPos, true);
                    BottomVideoListAdapter.this.videoClick.Click(BottomVideoListAdapter.this.list.get(BottomVideoListAdapter.this.playingPos), 1);
                    BottomVideoListAdapter.this.notifyDataSetChanged();
                }
            });
            this.binding.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BottomVideoListAdapter.this.videoClick.Click(BottomVideoListAdapter.this.list.get(ViewHolder.this.getLayoutPosition()), 2);
                }
            });
        }
    }
}
