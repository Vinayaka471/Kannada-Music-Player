package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.databinding.ItemSongsBinding;
import com.demo.musicvideoplayer.model.AudioModel;
import java.util.ArrayList;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> implements Filterable {
    List<AudioModel> MultiAudioList;
    boolean audioIsPlaying;
    List<AudioModel> audioModelList;
    Context context;
    List<AudioModel> filterList;
    boolean isAudioSelected;
    boolean isLongClick;
    boolean isPlaying;
    OnItemClick onItemClick;
    int positionForPlay;

    public interface OnItemClick {
        void AudioClick(AudioModel audioModel, int i, View view);

        void AudioLongClick(AudioModel audioModel, int i, View view);
    }

    public SongsAdapter(Context context2, List<AudioModel> list, OnItemClick onItemClick2) {
        this.context = context2;
        this.audioModelList = list;
        this.onItemClick = onItemClick2;
        this.positionForPlay = -1;
        this.isPlaying = false;
        this.filterList = list;
        this.audioIsPlaying = false;
    }

    public SongsAdapter(Context context2, List<AudioModel> list, List<AudioModel> list2, OnItemClick onItemClick2) {
        this.context = context2;
        this.audioModelList = list;
        this.MultiAudioList = list2;
        this.onItemClick = onItemClick2;
        this.isLongClick = false;
        this.isAudioSelected = false;
        this.filterList = list;
        this.audioIsPlaying = false;
    }

    public List<AudioModel> getFilterList() {
        return this.filterList;
    }

    public void setSongsList(List<AudioModel> list) {
        this.audioModelList = list;
        this.filterList = list;
        notifyDataSetChanged();
    }

    public void setPlayingPos(int i) {
        this.positionForPlay = i;
    }

    public void setAudioIsPlaying(boolean z) {
        this.audioIsPlaying = z;
    }

    public void setIsPlaying(boolean z) {
        this.isPlaying = z;
        notifyDataSetChanged();
    }

    public void NotifyLongClick(boolean z) {
        this.isLongClick = z;
        notifyDataSetChanged();
    }

    public void SetSelected(boolean z) {
        this.isAudioSelected = z;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_songs, (ViewGroup) null, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AudioModel audioModel = this.filterList.get(i);
        viewHolder.binding.txtAudioName.setText(audioModel.getName());
        viewHolder.binding.txtDuration.setText(audioModel.timeSet());
        viewHolder.binding.txtSize.setText(audioModel.getSize());
        if (audioModel.getPath() != null && !TextUtils.isEmpty(audioModel.getAlbumName())) {
            TextUtils.isEmpty(audioModel.getArtist());
        }
        Glide.with(this.context).load(Uri.parse(audioModel.getAlbumId())).into(viewHolder.binding.img);
        if (this.isPlaying) {
            int i2 = this.positionForPlay;
            if (i2 == -1) {
                viewHolder.binding.txtAudioName.setTextColor(this.context.getResources().getColor(R.color.white));
                viewHolder.binding.imgPlay.setVisibility(View.GONE);
                viewHolder.binding.imgPlayWave.setVisibility(View.GONE);
            } else if (i == i2) {
                viewHolder.binding.txtAudioName.setTextColor(this.context.getResources().getColor(R.color.playSongTxt));
                if (this.audioIsPlaying) {
                    viewHolder.binding.imgPlay.setVisibility(View.VISIBLE);
                    viewHolder.binding.imgPlayWave.setVisibility(View.GONE);
                } else {
                    viewHolder.binding.imgPlayWave.setVisibility(View.VISIBLE);
                    viewHolder.binding.imgPlay.setVisibility(View.GONE);
                }
            } else {
                viewHolder.binding.txtAudioName.setTextColor(this.context.getResources().getColor(R.color.white));
                viewHolder.binding.imgPlay.setVisibility(View.GONE);
                viewHolder.binding.imgPlayWave.setVisibility(View.GONE);
            }
        } else {
            viewHolder.binding.txtAudioName.setTextColor(this.context.getResources().getColor(R.color.white));
            viewHolder.binding.imgPlay.setVisibility(View.GONE);
            viewHolder.binding.imgPlayWave.setVisibility(View.GONE);
        }
        if (this.isLongClick) {
            viewHolder.binding.imgMore.setVisibility(View.GONE);
            viewHolder.binding.selected.setVisibility(View.VISIBLE);
            if (this.MultiAudioList.contains(audioModel)) {
                viewHolder.binding.selected.setImageResource(R.drawable.ic_checked);
            } else {
                viewHolder.binding.selected.setImageResource(R.drawable.ic_unchecked);
            }
        } else {
            viewHolder.binding.imgMore.setVisibility(View.VISIBLE);
            viewHolder.binding.selected.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return this.filterList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence charSequence) {
                String trim = charSequence.toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    SongsAdapter songsAdapter = SongsAdapter.this;
                    songsAdapter.filterList = songsAdapter.audioModelList;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (AudioModel next : SongsAdapter.this.audioModelList) {
                        if (!(next == null || next.getName() == null || !next.getName().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    SongsAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = SongsAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                SongsAdapter.this.notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemSongsBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemSongsBinding itemSongsBinding = (ItemSongsBinding) DataBindingUtil.bind(view);
            this.binding = itemSongsBinding;
            itemSongsBinding.rlMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!SongsAdapter.this.isLongClick) {
                        if (SongsAdapter.this.positionForPlay != -1) {
                            SongsAdapter.this.setPlayingPos(SongsAdapter.this.positionForPlay);
                            SongsAdapter.this.setAudioIsPlaying(true);
                            SongsAdapter.this.setIsPlaying(false);
                            SongsAdapter.this.notifyItemChanged(SongsAdapter.this.positionForPlay);
                        }
                        SongsAdapter.this.positionForPlay = ViewHolder.this.getLayoutPosition();
                        SongsAdapter.this.setPlayingPos(SongsAdapter.this.positionForPlay);
                        SongsAdapter.this.setAudioIsPlaying(true);
                        SongsAdapter.this.setIsPlaying(true);
                        SongsAdapter.this.onItemClick.AudioClick(SongsAdapter.this.filterList.get(SongsAdapter.this.positionForPlay), 1, view);
                        SongsAdapter.this.notifyDataSetChanged();
                        return;
                    }
                    SongsAdapter.this.onItemClick.AudioClick(SongsAdapter.this.filterList.get(ViewHolder.this.getLayoutPosition()), 1, view);
                }
            });
            this.binding.rlMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    SongsAdapter.this.onItemClick.AudioLongClick(SongsAdapter.this.filterList.get(ViewHolder.this.getLayoutPosition()), 1, view);
                    return false;
                }
            });
            this.binding.imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SongsAdapter.this.onItemClick.AudioClick(SongsAdapter.this.filterList.get(ViewHolder.this.getLayoutPosition()), 2, view);
                }
            });
        }
    }
}
