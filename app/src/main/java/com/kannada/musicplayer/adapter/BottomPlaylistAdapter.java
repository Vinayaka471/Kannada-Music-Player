package com.kannada.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.databinding.ItemBottomPlaylistBinding;
import com.kannada.musicplayer.model.CombineFolderModel;
import java.util.List;

public class BottomPlaylistAdapter extends RecyclerView.Adapter<BottomPlaylistAdapter.ViewHolder> {
    Context context;
    FolderClick folderClick;
    List<CombineFolderModel> folderModalList;

    public interface FolderClick {
        void onFolderClick(int i);
    }

    public BottomPlaylistAdapter(Context context2, List<CombineFolderModel> list, FolderClick folderClick2) {
        this.context = context2;
        this.folderModalList = list;
        this.folderClick = folderClick2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bottom_playlist, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        CombineFolderModel combineFolderModel = this.folderModalList.get(i);
        viewHolder.binding.txtFolderName.setText(combineFolderModel.getFolderModal().getFolderName());
        TextView textView = viewHolder.binding.txtTotal;
        textView.setText("" + combineFolderModel.getTotalSongs() + " Songs, " + combineFolderModel.getTotalVideos() + " Videos");
    }

    @Override
    public int getItemCount() {
        return this.folderModalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemBottomPlaylistBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemBottomPlaylistBinding itemBottomPlaylistBinding = (ItemBottomPlaylistBinding) DataBindingUtil.bind(view);
            this.binding = itemBottomPlaylistBinding;
            itemBottomPlaylistBinding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BottomPlaylistAdapter.this.folderClick.onFolderClick(ViewHolder.this.getAdapterPosition());
                }
            });
        }
    }
}
