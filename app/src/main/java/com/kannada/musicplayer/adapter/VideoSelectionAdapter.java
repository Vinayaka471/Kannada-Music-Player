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
import com.kannada.musicplayer.databinding.ItemAudioVideoSelectionBinding;
import com.kannada.musicplayer.model.VideoModal;
import java.util.ArrayList;
import java.util.List;

public class VideoSelectionAdapter extends RecyclerView.Adapter<VideoSelectionAdapter.ViewHolder> implements Filterable {
    List<VideoModal> InsertionVideoList;
    Context context;
    List<VideoModal> filterList;
    VideoClick videoClick;
    List<VideoModal> videoModelList;

    public interface VideoClick {
        void onVideoClick(int i);
    }

    public VideoSelectionAdapter(Context context2, List<VideoModal> list, List<VideoModal> list2, VideoClick videoClick2) {
        this.context = context2;
        this.videoModelList = list;
        this.InsertionVideoList = list2;
        this.videoClick = videoClick2;
        this.filterList = list;
    }

    public List<VideoModal> getFilterList() {
        return this.filterList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_audio_video_selection, (ViewGroup) null, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        VideoModal videoModal = this.filterList.get(i);
        viewHolder.binding.txtTitle.setText(videoModal.getaName());
        viewHolder.binding.txtDuration.setText(videoModal.timeSet());
        viewHolder.binding.txtSize.setText(videoModal.sizeSet());
        if (this.InsertionVideoList.contains(videoModal)) {
            viewHolder.binding.unSelectedCheckbox.setVisibility(View.GONE);
            viewHolder.binding.selectedCheckbox.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.unSelectedCheckbox.setVisibility(View.VISIBLE);
            viewHolder.binding.selectedCheckbox.setVisibility(View.GONE);
        }
        Glide.with(this.context).load(videoModal.getaPath()).into(viewHolder.binding.img);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence charSequence) {
                String trim = charSequence.toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    VideoSelectionAdapter videoSelectionAdapter = VideoSelectionAdapter.this;
                    videoSelectionAdapter.filterList = videoSelectionAdapter.videoModelList;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (VideoModal next : VideoSelectionAdapter.this.videoModelList) {
                        if (!(next == null || next.getaName() == null || trim == null || !next.getaName().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    VideoSelectionAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = VideoSelectionAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                VideoSelectionAdapter.this.notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getItemCount() {
        return this.filterList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemAudioVideoSelectionBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemAudioVideoSelectionBinding itemAudioVideoSelectionBinding = (ItemAudioVideoSelectionBinding) DataBindingUtil.bind(view);
            this.binding = itemAudioVideoSelectionBinding;
            itemAudioVideoSelectionBinding.rlMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoSelectionAdapter.this.videoClick.onVideoClick(ViewHolder.this.getAdapterPosition());
                }
            });
        }
    }
}
