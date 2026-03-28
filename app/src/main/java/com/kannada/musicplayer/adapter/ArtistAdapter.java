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
import com.demo.musicvideoplayer.databinding.ItemArtistBinding;
import com.demo.musicvideoplayer.model.ArtistModel;
import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.MyView> implements Filterable {
    List<ArtistModel> MultiList;
    OnFolderClick click;
    Context context;
    List<ArtistModel> filterList;
    boolean isAudioSelected = false;
    boolean isLongClick = false;
    List<ArtistModel> list;

    public interface OnFolderClick {
        void OnFolderClick(int i, int i2);

        void OnFolderLongClick(int i, int i2);
    }

    public ArtistAdapter(Context context2, List<ArtistModel> list2, List<ArtistModel> list3, OnFolderClick onFolderClick) {
        this.context = context2;
        this.list = list2;
        this.MultiList = list3;
        this.click = onFolderClick;
        this.filterList = list2;
    }

    public List<ArtistModel> getFilterList() {
        return this.filterList;
    }

    public void setArtistList(List<ArtistModel> list2) {
        this.list = list2;
        this.filterList = list2;
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
    public MyView onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new MyView(LayoutInflater.from(this.context).inflate(R.layout.item_artist, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(MyView myView, int i) {
        myView.binding.txtArtistName.setText(this.filterList.get(i).getArtist());
        TextView textView = myView.binding.txtTotalSong;
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
                    ArtistAdapter artistAdapter = ArtistAdapter.this;
                    artistAdapter.filterList = artistAdapter.list;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (ArtistModel next : ArtistAdapter.this.list) {
                        if (!(next == null || next.getArtist() == null || !next.getArtist().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    ArtistAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = ArtistAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ArtistAdapter.this.notifyDataSetChanged();
            }
        };
    }

    class MyView extends RecyclerView.ViewHolder {
        ItemArtistBinding binding;

        public MyView(View view) {
            super(view);
            ItemArtistBinding itemArtistBinding = (ItemArtistBinding) DataBindingUtil.bind(view);
            this.binding = itemArtistBinding;
            itemArtistBinding.rlMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArtistAdapter.this.click.OnFolderClick(MyView.this.getAdapterPosition(), 1);
                }
            });
            this.binding.rlMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ArtistAdapter.this.click.OnFolderLongClick(MyView.this.getAdapterPosition(), 1);
                    return false;
                }
            });
            this.binding.imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArtistAdapter.this.click.OnFolderClick(MyView.this.getAdapterPosition(), 2);
                }
            });
        }
    }
}
