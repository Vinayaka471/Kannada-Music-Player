package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.demo.musicvideoplayer.databinding.ItemAudioVideoSelectionBinding;
import com.demo.musicvideoplayer.model.AudioModel;
import com.demo.musicvideoplayer.utils.AppConstants;
import java.util.ArrayList;
import java.util.List;

public class AudioSelectionAdapter extends RecyclerView.Adapter<AudioSelectionAdapter.ViewHolder> implements Filterable {
    List<AudioModel> InsertionAudioList;
    AudioClick audioClick;
    List<AudioModel> audioModelList;
    Context context;
    List<AudioModel> filterList;

    public interface AudioClick {
        void onAudioClick(int i);
    }

    public AudioSelectionAdapter(Context context2, List<AudioModel> list, List<AudioModel> list2, AudioClick audioClick2) {
        this.context = context2;
        this.audioModelList = list;
        this.InsertionAudioList = list2;
        this.audioClick = audioClick2;
        this.filterList = list;
    }

    public List<AudioModel> getFilterList() {
        return this.filterList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_audio_video_selection, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AudioModel audioModel = this.filterList.get(i);
        viewHolder.binding.txtTitle.setText(audioModel.getName());
        viewHolder.binding.txtDuration.setText(audioModel.timeSet());
        viewHolder.binding.txtSize.setText(audioModel.getSize());
        if (this.InsertionAudioList.contains(audioModel)) {
            viewHolder.binding.unSelectedCheckbox.setVisibility(View.GONE);
            viewHolder.binding.selectedCheckbox.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.unSelectedCheckbox.setVisibility(View.VISIBLE);
            viewHolder.binding.selectedCheckbox.setVisibility(View.GONE);
        }
        if (audioModel.getPath() != null && !TextUtils.isEmpty(audioModel.getAlbumName()) && !TextUtils.isEmpty(audioModel.getArtist())) {
            Bitmap folderArt = AppConstants.setFolderArt(audioModel.getPath(), this.context);
            if (folderArt != null) {
                viewHolder.binding.img.setVisibility(View.VISIBLE);
                Glide.with(this.context).load(folderArt).into(viewHolder.binding.img);
                return;
            }
            viewHolder.binding.img.setVisibility(View.GONE);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence charSequence) {
                String trim = charSequence.toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    AudioSelectionAdapter audioSelectionAdapter = AudioSelectionAdapter.this;
                    audioSelectionAdapter.filterList = audioSelectionAdapter.audioModelList;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (AudioModel next : AudioSelectionAdapter.this.audioModelList) {
                        if (!(next == null || next.getName() == null || trim == null || !next.getName().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    AudioSelectionAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = AudioSelectionAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                AudioSelectionAdapter.this.notifyDataSetChanged();
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
                    AudioSelectionAdapter.this.audioClick.onAudioClick(ViewHolder.this.getAdapterPosition());
                }
            });
        }
    }
}
