package com.kannada.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.databinding.ItemHistoryListBinding;
import com.kannada.musicplayer.model.HistoryModel;
import com.kannada.musicplayer.utils.AppConstants;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    Context context;
    List<HistoryModel> historyModelList;
    boolean isPlaying = false;
    OnHistoryClick onHistoryClick;
    int positionForPlay = -1;

    public interface OnHistoryClick {
        void onListClick(int i, int i2, View view);
    }

    public HistoryAdapter(Context context2, List<HistoryModel> list, OnHistoryClick onHistoryClick2) {
        this.context = context2;
        this.historyModelList = list;
        this.onHistoryClick = onHistoryClick2;
    }

    public void setPlayingPos(int i) {
        this.positionForPlay = i;
    }

    public void setIsPlaying(boolean z) {
        this.isPlaying = z;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_history_list, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        HistoryModel historyModel = this.historyModelList.get(i);
        AudioVideoModal audioVideoModal = this.historyModelList.get(i).getAudioVideoModal();
        viewHolder.binding.txtTitle.setText(audioVideoModal.getName());
        viewHolder.binding.txtDuration.setText(AppConstants.formatTime(audioVideoModal.getDuration()));
        Glide.with(this.context).load(Integer.valueOf(R.drawable.music_gif)).into(viewHolder.binding.playImg);
        if (this.isPlaying) {
            int i2 = this.positionForPlay;
            if (i2 == -1) {
                viewHolder.binding.txtTitle.setTextColor(ContextCompat.getColor(this.context, R.color.white));
                viewHolder.binding.playImg.setVisibility(View.GONE);
            } else if (i == i2) {
                viewHolder.binding.txtTitle.setTextColor(ContextCompat.getColor(this.context, R.color.playSongTxt));
                viewHolder.binding.playImg.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.txtTitle.setTextColor(ContextCompat.getColor(this.context, R.color.white));
                viewHolder.binding.playImg.setVisibility(View.GONE);
            }
        } else {
            viewHolder.binding.txtTitle.setTextColor(ContextCompat.getColor(this.context, R.color.white));
            viewHolder.binding.playImg.setVisibility(View.GONE);
        }
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
        if (i == 0) {
            viewHolder.binding.txtDate.setVisibility(View.VISIBLE);
            viewHolder.binding.txtDate.setText(AppConstants.GetRecentDate(historyModel.getDate()));
        } else if (this.historyModelList.get(i - 1).getDate() == historyModel.getDate()) {
            viewHolder.binding.txtDate.setVisibility(View.GONE);
        } else {
            viewHolder.binding.txtDate.setVisibility(View.VISIBLE);
            viewHolder.binding.txtDate.setText(AppConstants.GetRecentDate(historyModel.getDate()));
        }
    }

    @Override
    public int getItemCount() {
        return this.historyModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemHistoryListBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemHistoryListBinding itemHistoryListBinding = (ItemHistoryListBinding) DataBindingUtil.bind(view);
            this.binding = itemHistoryListBinding;
            itemHistoryListBinding.rlMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (HistoryAdapter.this.positionForPlay != -1) {
                        HistoryAdapter.this.setPlayingPos(HistoryAdapter.this.positionForPlay);
                        HistoryAdapter.this.setIsPlaying(false);
                        HistoryAdapter.this.notifyItemChanged(HistoryAdapter.this.positionForPlay);
                    }
                    HistoryAdapter.this.positionForPlay = ViewHolder.this.getLayoutPosition();
                    HistoryAdapter.this.setPlayingPos(HistoryAdapter.this.positionForPlay);
                    HistoryAdapter.this.setIsPlaying(true);
                    HistoryAdapter.this.onHistoryClick.onListClick(HistoryAdapter.this.positionForPlay, 1, view);
                    HistoryAdapter.this.notifyDataSetChanged();
                }
            });
            this.binding.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HistoryAdapter.this.onHistoryClick.onListClick(ViewHolder.this.getLayoutPosition(), 2, view);
                }
            });
        }
    }
}
