package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.database.AppDatabase;
import com.demo.musicvideoplayer.database.model.AudioVideoModal;
import com.demo.musicvideoplayer.databinding.ItemAudioVideoListBinding;
import com.demo.musicvideoplayer.utils.AppConstants;
import com.demo.musicvideoplayer.utils.AppPref;
import com.demo.musicvideoplayer.utils.SwipeAndDragHelper;
import java.util.ArrayList;
import java.util.List;

public class PlaylistItemAdapter extends RecyclerView.Adapter<PlaylistItemAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract, Filterable {
    List<AudioVideoModal> MultiList;
    AppDatabase appDatabase;
    List<AudioVideoModal> audioVideoModalList;
    Context context;
    List<AudioVideoModal> filterList;
    boolean isDraggable = false;
    boolean isFromFav;
    boolean isLongClick;
    boolean isPlaying = false;
    boolean isSelectedVideo;
    public ItemTouchHelper itemTouchHelper;
    OnPlayList onPlayList;
    int positionForPlay = -1;

    public interface OnPlayList {
        void onPlayListClick(int i, int i2, View view);

        void onPlayListLongClick(int i, int i2, View view);
    }

    @Override
    public void onViewSwiped(int i) {
    }

    @Override
    public void reallyMoved(int i, int i2) {
    }

    public PlaylistItemAdapter(Context context2, List<AudioVideoModal> list, List<AudioVideoModal> list2, OnPlayList onPlayList2, boolean z) {
        this.context = context2;
        this.audioVideoModalList = list;
        this.MultiList = list2;
        this.onPlayList = onPlayList2;
        this.isFromFav = z;
        this.filterList = list;
        this.appDatabase = AppDatabase.getAppDatabase(context2);
        this.isLongClick = false;
        this.isSelectedVideo = false;
    }

    public List<AudioVideoModal> getFilterList() {
        return this.filterList;
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper2) {
        this.itemTouchHelper = itemTouchHelper2;
    }

    public void setDraggable(boolean z) {
        this.isDraggable = z;
        notifyDataSetChanged();
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
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_audio_video_list, (ViewGroup) null, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AudioVideoModal audioVideoModal = this.filterList.get(i);
        viewHolder.binding.txtTitle.setText(audioVideoModal.getName());
        viewHolder.binding.txtDuration.setText(AppConstants.formatTime(audioVideoModal.getDuration()));
        Glide.with(this.context).load(Integer.valueOf(R.drawable.music_gif)).into(viewHolder.binding.playImg);
        if (audioVideoModal.getUri() != null) {
            if (TextUtils.isEmpty(audioVideoModal.getAlbum()) || TextUtils.isEmpty(audioVideoModal.getArtist())) {
                Glide.with(this.context).load(audioVideoModal.getUri()).into(viewHolder.binding.img);
            } else {
                Bitmap folderArt = AppConstants.setFolderArt(audioVideoModal.getUri(), this.context);
                if (folderArt != null) {
                    viewHolder.binding.img.setVisibility(View.VISIBLE);
                    Glide.with(this.context).load(folderArt).into(viewHolder.binding.img);
                } else {
                    viewHolder.binding.img.setVisibility(View.GONE);
                }
            }
        }
        if (this.isPlaying) {
            int i2 = this.positionForPlay;
            if (i2 == -1) {
                viewHolder.binding.txtTitle.setTextColor(this.context.getResources().getColor(R.color.white));
                viewHolder.binding.playImg.setVisibility(View.GONE);
            } else if (i == i2) {
                viewHolder.binding.txtTitle.setTextColor(this.context.getResources().getColor(R.color.playSongTxt));
                viewHolder.binding.playImg.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.txtTitle.setTextColor(this.context.getResources().getColor(R.color.white));
                viewHolder.binding.playImg.setVisibility(View.GONE);
            }
        } else {
            viewHolder.binding.txtTitle.setTextColor(this.context.getResources().getColor(R.color.white));
            viewHolder.binding.playImg.setVisibility(View.GONE);
        }
        if (this.isDraggable) {
            viewHolder.binding.imgOrder.setVisibility(View.VISIBLE);
            viewHolder.binding.more.setVisibility(View.GONE);
        } else {
            viewHolder.binding.imgOrder.setVisibility(View.GONE);
            viewHolder.binding.more.setVisibility(View.VISIBLE);
        }

        viewHolder.binding.imgOrder.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() != 0) {
                    return false;
                }
                itemTouchHelper.startDrag(viewHolder);
                return false;
            }
        });

        if (this.isLongClick) {
            viewHolder.binding.selected.setVisibility(View.VISIBLE);
            viewHolder.binding.rlMore.setVisibility(View.GONE);
            if (this.MultiList.contains(audioVideoModal)) {
                viewHolder.binding.selected.setImageResource(R.drawable.ic_checked);
            } else {
                viewHolder.binding.selected.setImageResource(R.drawable.ic_unchecked);
            }
        } else {
            viewHolder.binding.selected.setVisibility(View.GONE);
            viewHolder.binding.rlMore.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.filterList.size();
    }

    @Override
    public void onViewMoved(int i, int i2) {
        this.audioVideoModalList.get(i).setAudioVideoOrder(i2);
        List<AudioVideoModal> list = this.audioVideoModalList;
        list.set(i, list.get(i));
        if (!this.isFromFav) {
            this.appDatabase.audioVideoDao().UpdateAudioVideo(this.audioVideoModalList.get(i));
        } else {
            ArrayList<AudioVideoModal> favouriteList = AppPref.getFavouriteList();
            favouriteList.get(i).setAudioVideoOrder(i2);
            favouriteList.set(i, favouriteList.get(i));
            AppPref.setFavouriteList(favouriteList);
        }
        this.audioVideoModalList.get(i2).setAudioVideoOrder(i);
        List<AudioVideoModal> list2 = this.audioVideoModalList;
        list2.set(i2, list2.get(i2));
        if (!this.isFromFav) {
            this.appDatabase.audioVideoDao().UpdateAudioVideo(this.audioVideoModalList.get(i2));
        } else {
            ArrayList<AudioVideoModal> favouriteList2 = AppPref.getFavouriteList();
            favouriteList2.get(i2).setAudioVideoOrder(i);
            favouriteList2.set(i2, favouriteList2.get(i2));
            AppPref.setFavouriteList(favouriteList2);
        }
        AudioVideoModal audioVideoModal = new AudioVideoModal(this.audioVideoModalList.get(i));
        this.audioVideoModalList.remove(i);
        this.audioVideoModalList.add(i2, audioVideoModal);
        notifyItemMoved(i, i2);
        if (this.isFromFav) {
            new AudioVideoModal(AppPref.getFavouriteList().get(i));
            ArrayList<AudioVideoModal> favouriteList3 = AppPref.getFavouriteList();
            favouriteList3.remove(i);
            favouriteList3.add(i2, audioVideoModal);
            AppPref.setFavouriteList(favouriteList3);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence charSequence) {
                String trim = charSequence.toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    PlaylistItemAdapter playlistItemAdapter = PlaylistItemAdapter.this;
                    playlistItemAdapter.filterList = playlistItemAdapter.audioVideoModalList;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (AudioVideoModal next : PlaylistItemAdapter.this.audioVideoModalList) {
                        if (!(next == null || next.getName() == null || !next.getName().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    PlaylistItemAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = PlaylistItemAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                PlaylistItemAdapter.this.notifyDataSetChanged();
            }
        };
    }

    public void NotifyLongClick(boolean z) {
        this.isLongClick = z;
        notifyDataSetChanged();
    }

    public void SetSelected(boolean z) {
        this.isSelectedVideo = z;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemAudioVideoListBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemAudioVideoListBinding itemAudioVideoListBinding = (ItemAudioVideoListBinding) DataBindingUtil.bind(view);
            this.binding = itemAudioVideoListBinding;
            itemAudioVideoListBinding.imgOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaylistItemAdapter.this.onPlayList.onPlayListClick(ViewHolder.this.getAdapterPosition(), 1, view);
                }
            });
            this.binding.rlMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!PlaylistItemAdapter.this.isLongClick) {
                        if (PlaylistItemAdapter.this.positionForPlay != -1) {
                            PlaylistItemAdapter.this.setPlayingPos(PlaylistItemAdapter.this.positionForPlay);
                            PlaylistItemAdapter.this.setIsPlaying(false);
                            PlaylistItemAdapter.this.notifyItemChanged(PlaylistItemAdapter.this.positionForPlay);
                        }
                        PlaylistItemAdapter.this.positionForPlay = ViewHolder.this.getLayoutPosition();
                        PlaylistItemAdapter.this.setPlayingPos(PlaylistItemAdapter.this.positionForPlay);
                        PlaylistItemAdapter.this.setIsPlaying(true);
                        PlaylistItemAdapter.this.onPlayList.onPlayListClick(PlaylistItemAdapter.this.positionForPlay, 2, view);
                        PlaylistItemAdapter.this.notifyDataSetChanged();
                        return;
                    }
                    PlaylistItemAdapter.this.onPlayList.onPlayListClick(ViewHolder.this.getLayoutPosition(), 2, view);
                }
            });
            this.binding.rlMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PlaylistItemAdapter.this.onPlayList.onPlayListLongClick(ViewHolder.this.getLayoutPosition(), 2, view);
                    return false;
                }
            });
            this.binding.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaylistItemAdapter.this.onPlayList.onPlayListClick(ViewHolder.this.getAdapterPosition(), 3, view);
                }
            });
        }
    }
}
