package com.kannada.musicplayer.adapter;

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
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.databinding.ItemAlbumBinding;
import com.kannada.musicplayer.model.AlbumModel;
import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> implements Filterable {
    List<AlbumModel> MultiList;
    List<AlbumModel> albumModelList;
    Context context;
    List<AlbumModel> filterList;
    boolean isAudioSelected = false;
    boolean isLongClick = false;
    OnFolderClick onFolderClick;

    public interface OnFolderClick {
        void OnClick(int i, int i2);

        void OnLongClick(int i, int i2);
    }

    public AlbumAdapter(Context context2, List<AlbumModel> list, List<AlbumModel> list2, OnFolderClick onFolderClick2) {
        this.context = context2;
        this.albumModelList = list;
        this.MultiList = list2;
        this.onFolderClick = onFolderClick2;
        this.filterList = list;
    }

    public List<AlbumModel> getFilterList() {
        return this.filterList;
    }

    public void setAlbumList(List<AlbumModel> list) {
        this.albumModelList = list;
        this.filterList = list;
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
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_album, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        AlbumModel albumModel = this.filterList.get(i);
        viewHolder.binding.txtTitle.setText(albumModel.getAlbum());
        Glide.with(this.context).load(Uri.parse(albumModel.getAlbumArt())).into(viewHolder.binding.img);
        if (this.isLongClick) {
            viewHolder.binding.imgMore.setVisibility(View.GONE);
            viewHolder.binding.selected.setVisibility(View.VISIBLE);
            if (this.MultiList.contains(this.filterList.get(i))) {
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
                    AlbumAdapter albumAdapter = AlbumAdapter.this;
                    albumAdapter.filterList = albumAdapter.albumModelList;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (AlbumModel next : AlbumAdapter.this.albumModelList) {
                        if (!(next == null || next.getAlbum() == null || !next.getAlbum().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    AlbumAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = AlbumAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                AlbumAdapter.this.notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemAlbumBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemAlbumBinding itemAlbumBinding = (ItemAlbumBinding) DataBindingUtil.bind(view);
            this.binding = itemAlbumBinding;
            itemAlbumBinding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlbumAdapter.this.onFolderClick.OnClick(ViewHolder.this.getAdapterPosition(), 1);
                }
            });
            this.binding.llMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlbumAdapter.this.onFolderClick.OnLongClick(ViewHolder.this.getAdapterPosition(), 1);
                    return false;
                }
            });
            this.binding.imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlbumAdapter.this.onFolderClick.OnClick(ViewHolder.this.getAdapterPosition(), 2);
                }
            });
        }
    }
}
