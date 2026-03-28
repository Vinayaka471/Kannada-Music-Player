package com.kannada.musicplayer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.databinding.ItemVideoGridBinding;
import com.kannada.musicplayer.model.VideoModal;
import java.util.ArrayList;
import java.util.List;

public class VideoGridAdapter extends RecyclerView.Adapter<VideoGridAdapter.ViewHolder> implements Filterable {
    List<VideoModal> MultiSelectedList;
    Context context;
    List<VideoModal> filterList;
    boolean isLongClick = false;
    boolean isSelectedVideo = false;
    VideoClick videoClick;
    List<VideoModal> videoModalList;

    public interface VideoClick {
        void Click(VideoModal videoModal, int i, View view);

        void LongClick(VideoModal videoModal, int i, View view);
    }

    public VideoGridAdapter(Context context2, List<VideoModal> list, List<VideoModal> list2, VideoClick videoClick2) {
        this.context = context2;
        this.videoModalList = list;
        this.MultiSelectedList = list2;
        this.videoClick = videoClick2;
        this.filterList = list;
    }

    public List<VideoModal> getFilterList() {
        return this.filterList;
    }

    public void setVideoFolderList(List<VideoModal> list) {
        this.videoModalList = list;
        this.filterList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_video_grid, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        VideoModal videoModal = this.filterList.get(i);
        Glide.with(this.context).load(this.filterList.get(i).getaPath()).into(viewHolder.binding.videoArt);
        viewHolder.binding.txtVideoName.setText(videoModal.getaName());
        viewHolder.binding.txtDuration.setText(videoModal.timeSet());
        viewHolder.binding.txtVideoSize.setText(videoModal.sizeSet());
        if (this.isLongClick) {
            viewHolder.binding.selected.setVisibility(View.VISIBLE);
            viewHolder.binding.imgMore.setVisibility(View.GONE);
            if (this.MultiSelectedList.contains(videoModal)) {
                viewHolder.binding.selected.setImageResource(R.drawable.ic_checked);
            } else {
                viewHolder.binding.selected.setImageResource(R.drawable.ic_unchecked);
            }
        } else {
            viewHolder.binding.selected.setVisibility(View.GONE);
            viewHolder.binding.imgMore.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return this.filterList.size();
    }

    public void NotifyLongClick(boolean z) {
        this.isLongClick = z;
        notifyDataSetChanged();
    }

    public void SetSelected(boolean z) {
        this.isSelectedVideo = z;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence charSequence) {
                String trim = charSequence.toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    VideoGridAdapter videoGridAdapter = VideoGridAdapter.this;
                    videoGridAdapter.filterList = videoGridAdapter.videoModalList;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (VideoModal next : VideoGridAdapter.this.videoModalList) {
                        if (!(next == null || next.getaName() == null || !next.getaName().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    VideoGridAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = VideoGridAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                VideoGridAdapter.this.notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemVideoGridBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemVideoGridBinding itemVideoGridBinding = (ItemVideoGridBinding) DataBindingUtil.bind(view);
            this.binding = itemVideoGridBinding;
            itemVideoGridBinding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoGridAdapter.this.videoClick.Click(VideoGridAdapter.this.filterList.get(ViewHolder.this.getAdapterPosition()), 1, view);
                }
            });
            this.binding.llMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    VideoGridAdapter.this.videoClick.LongClick(VideoGridAdapter.this.filterList.get(ViewHolder.this.getAdapterPosition()), 1, view);
                    return false;
                }
            });
            this.binding.imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoGridAdapter.this.videoClick.Click(VideoGridAdapter.this.filterList.get(ViewHolder.this.getAdapterPosition()), 2, view);
                }
            });
        }
    }
}
