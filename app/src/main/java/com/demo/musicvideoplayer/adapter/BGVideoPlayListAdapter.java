package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.databinding.ItemBgPlayerPlaylistBinding;
import com.demo.musicvideoplayer.utils.AppConstants;
import java.util.List;

public class BGVideoPlayListAdapter extends RecyclerView.Adapter<BGVideoPlayListAdapter.ViewHolder> {
    AudioVideoClick audioVideoClick;
    List<AudioVideoModal> audioVideoModalList;
    Context context;
    boolean isPlaying;
    int playingPos;

    public interface AudioVideoClick {
        void Click(int i, int i2);
    }

    public BGVideoPlayListAdapter(Context context2, List<AudioVideoModal> list, int i, boolean z, AudioVideoClick audioVideoClick2) {
        this.context = context2;
        this.audioVideoModalList = list;
        this.playingPos = i;
        this.isPlaying = z;
        this.audioVideoClick = audioVideoClick2;
    }

    public void setPlayingPos(int i, boolean z) {
        this.playingPos = i;
        this.isPlaying = z;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bg_player_playlist, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AudioVideoModal audioVideoModal = this.audioVideoModalList.get(i);
        viewHolder.binding.txtTitle.setText(audioVideoModal.getName());
        viewHolder.binding.txtDuration.setText(AppConstants.formatTime(audioVideoModal.getDuration()));
        if (audioVideoModal.getUri() != null) {
            if (TextUtils.isEmpty(audioVideoModal.getAlbum()) || TextUtils.isEmpty(audioVideoModal.getArtist())) {
                Glide.with(this.context).load(audioVideoModal.getUri()).into(viewHolder.binding.img);
            } else {
                Bitmap folderArt = AppConstants.setFolderArt(audioVideoModal.getUri(), this.context);
                if (folderArt != null) {
                    Glide.with(this.context).load(folderArt).into(viewHolder.binding.img);
                }
            }
        }
        if (this.playingPos != i) {
            //viewHolder.binding.mainCard.setStrokeWidth(0);
        } else if (this.isPlaying) {
            //viewHolder.binding.mainCard.setStrokeWidth(6);
        } else {
            //viewHolder.binding.mainCard.setStrokeWidth(0);
        }
    }

    @Override
    public int getItemCount() {
        return this.audioVideoModalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemBgPlayerPlaylistBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemBgPlayerPlaylistBinding itemBgPlayerPlaylistBinding = (ItemBgPlayerPlaylistBinding) DataBindingUtil.bind(view);
            this.binding = itemBgPlayerPlaylistBinding;
            itemBgPlayerPlaylistBinding.mainCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (BGVideoPlayListAdapter.this.playingPos != -1) {
                        BGVideoPlayListAdapter.this.setPlayingPos(BGVideoPlayListAdapter.this.playingPos, false);
                        BGVideoPlayListAdapter.this.notifyItemChanged(BGVideoPlayListAdapter.this.playingPos);
                        BGVideoPlayListAdapter.this.playingPos = ViewHolder.this.getLayoutPosition();
                    }
                    BGVideoPlayListAdapter.this.setPlayingPos(BGVideoPlayListAdapter.this.playingPos, true);
                    BGVideoPlayListAdapter.this.audioVideoClick.Click(ViewHolder.this.getLayoutPosition(), 1);
                    BGVideoPlayListAdapter.this.notifyDataSetChanged();
                }
            });
            this.binding.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BGVideoPlayListAdapter.this.audioVideoClick.Click(ViewHolder.this.getLayoutPosition(), 2);
                }
            });
        }
    }
}
