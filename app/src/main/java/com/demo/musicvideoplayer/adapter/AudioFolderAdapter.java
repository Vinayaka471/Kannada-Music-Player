package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.databinding.ItemAudioFolderBinding;
import com.demo.musicvideoplayer.model.AudioFolderModal;
import java.util.ArrayList;
import java.util.List;

public class AudioFolderAdapter extends RecyclerView.Adapter<AudioFolderAdapter.MyView> implements Filterable {
    List<AudioFolderModal> MultiList;
    OnFolderClick click;
    Context context;
    List<AudioFolderModal> filterList;
    boolean isAudioSelected = false;
    boolean isLongClick = false;
    List<AudioFolderModal> list;

    public interface OnFolderClick {
        void OnFolderClick(int i, int i2, View view);

        void OnFolderLongClick(int i, int i2, View view);
    }

    public AudioFolderAdapter(Context context2, List<AudioFolderModal> list2, List<AudioFolderModal> list3, OnFolderClick onFolderClick) {
        this.context = context2;
        this.list = list2;
        this.MultiList = list3;
        this.click = onFolderClick;
        this.filterList = list2;
    }

    public void NotifyLongClick(boolean z) {
        this.isLongClick = z;
        notifyDataSetChanged();
    }

    public void SetSelected(boolean z) {
        this.isAudioSelected = z;
    }

    public List<AudioFolderModal> getFilterList() {
        return this.filterList;
    }

    public void setFolderModelList(List<AudioFolderModal> list2) {
        this.list = list2;
        this.filterList = list2;
        notifyDataSetChanged();
    }

    @Override
    public MyView onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyView(LayoutInflater.from(this.context).inflate(R.layout.item_audio_folder, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(MyView myView, int i) {
        myView.binding.txtAudioName.setText(this.filterList.get(i).getBucketName());
        TextView textView = myView.binding.txtTotalAudios;
        textView.setText("" + this.filterList.get(i).getCount() + " Music");
        if (this.isLongClick) {
            myView.binding.imgMore.setVisibility(View.GONE);
            myView.binding.selected.setVisibility(View.VISIBLE);
            if (this.MultiList.contains(this.filterList.get(i))) {
                myView.binding.selected.setImageResource(R.drawable.ic_checked);
            } else {
                myView.binding.selected.setImageResource(R.drawable.ic_unchecked);
            }
        } else {
            myView.binding.imgMore.setVisibility(View.VISIBLE);
            myView.binding.selected.setVisibility(View.GONE);
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
                    AudioFolderAdapter audioFolderAdapter = AudioFolderAdapter.this;
                    audioFolderAdapter.filterList = audioFolderAdapter.list;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (AudioFolderModal next : AudioFolderAdapter.this.list) {
                        if (!(next == null || next.getBucketName() == null || !next.getBucketName().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    AudioFolderAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = AudioFolderAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                AudioFolderAdapter.this.notifyDataSetChanged();
            }
        };
    }

    class MyView extends RecyclerView.ViewHolder {
        ItemAudioFolderBinding binding;

        public MyView(View view) {
            super(view);
            ItemAudioFolderBinding itemAudioFolderBinding = (ItemAudioFolderBinding) DataBindingUtil.bind(view);
            this.binding = itemAudioFolderBinding;
            itemAudioFolderBinding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AudioFolderAdapter.this.click.OnFolderClick(MyView.this.getAdapterPosition(), 1, view);
                }
            });
            this.binding.llMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AudioFolderAdapter.this.click.OnFolderLongClick(MyView.this.getAdapterPosition(), 1, view);
                    return false;
                }
            });
            this.binding.imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AudioFolderAdapter.this.click.OnFolderClick(MyView.this.getAdapterPosition(), 2, view);
                }
            });
        }
    }
}
