package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.databinding.ItemPlaylistBinding;
import com.demo.musicvideoplayer.model.CombineFolderModel;
import com.demo.musicvideoplayer.utils.AppConstants;
import java.util.ArrayList;
import java.util.List;

public class PlaylistFolderAdapter extends RecyclerView.Adapter<PlaylistFolderAdapter.ViewHolder> implements Filterable {
    List<CombineFolderModel> MultiList;
    Context context;
    List<CombineFolderModel> filterList;
    List<CombineFolderModel> folderModalList;
    boolean isLongClick = false;
    boolean isSelectedVideo = false;
    OnFolder onFolder;

    public interface OnFolder {
        void onFolderClick(int i, int i2);

        void onFolderLongClick(int i, int i2);
    }

    public PlaylistFolderAdapter(Context context2, List<CombineFolderModel> list, List<CombineFolderModel> list2, OnFolder onFolder2) {
        this.context = context2;
        this.folderModalList = list;
        this.MultiList = list2;
        this.onFolder = onFolder2;
        this.filterList = list;
    }

    public List<CombineFolderModel> getFilterList() {
        return this.filterList;
    }

    public void SetFolderList(List<CombineFolderModel> list) {
        this.folderModalList = list;
        this.filterList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_playlist, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        CombineFolderModel combineFolderModel = this.filterList.get(i);
        viewHolder.binding.txtTitle.setText(this.filterList.get(i).getFolderModal().getFolderName());
        TextView textView = viewHolder.binding.txtTotal;
        textView.setText("" + combineFolderModel.getTotalSongs() + " Songs, " + combineFolderModel.getTotalVideos() + " Videos");
        if (combineFolderModel.getUri() != null) {
            if (TextUtils.isEmpty(combineFolderModel.getAlbum()) || TextUtils.isEmpty(combineFolderModel.getArtist())) {
                Glide.with(this.context).load(combineFolderModel.getUri()).into(viewHolder.binding.img);
            } else {
                Bitmap folderArt = AppConstants.setFolderArt(combineFolderModel.getUri(), this.context);
                if (folderArt != null) {
                    Glide.with(this.context).load(folderArt).into(viewHolder.binding.img);
                }
            }
        }
        if (this.isLongClick) {
            viewHolder.binding.selected.setVisibility(View.VISIBLE);
            viewHolder.binding.imgMore.setVisibility(View.GONE);
            if (this.MultiList.contains(combineFolderModel)) {
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

    public Filter getFilter() {
        return new Filter() {
            @Override
            public FilterResults performFiltering(CharSequence charSequence) {
                String trim = charSequence.toString().trim();
                if (TextUtils.isEmpty(trim)) {
                    PlaylistFolderAdapter playlistFolderAdapter = PlaylistFolderAdapter.this;
                    playlistFolderAdapter.filterList = playlistFolderAdapter.folderModalList;
                } else {
                    ArrayList arrayList = new ArrayList();
                    for (CombineFolderModel next : PlaylistFolderAdapter.this.folderModalList) {
                        if (!(next == null || next.getFolderModal().getFolderName() == null || trim == null || !next.getFolderModal().getFolderName().toLowerCase().contains(trim.toLowerCase()))) {
                            arrayList.add(next);
                        }
                    }
                    PlaylistFolderAdapter.this.filterList = arrayList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = PlaylistFolderAdapter.this.filterList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                PlaylistFolderAdapter.this.notifyDataSetChanged();
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
        ItemPlaylistBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemPlaylistBinding itemPlaylistBinding = (ItemPlaylistBinding) DataBindingUtil.bind(view);
            this.binding = itemPlaylistBinding;
            itemPlaylistBinding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaylistFolderAdapter.this.onFolder.onFolderClick(ViewHolder.this.getAdapterPosition(), 1);
                }
            });
            this.binding.llMain.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PlaylistFolderAdapter.this.onFolder.onFolderLongClick(ViewHolder.this.getAdapterPosition(), 1);
                    return false;
                }
            });
            this.binding.imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlaylistFolderAdapter.this.onFolder.onFolderClick(ViewHolder.this.getAdapterPosition(), 2);
                }
            });
        }
    }
}
