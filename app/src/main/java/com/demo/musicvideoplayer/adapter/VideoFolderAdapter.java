package com.demo.musicvideoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.demo.musicvideoplayer.R;
import com.demo.musicvideoplayer.databinding.ItemVideoFolderBinding;
import com.demo.musicvideoplayer.model.VideoFolderModal;
import java.util.List;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.ViewHolder> {
    Context context;
    FolderClick folderClick;
    int selectedPos = 0;
    List<VideoFolderModal> videoFolderModalList;

    public interface FolderClick {
        void OnVideoClick(VideoFolderModal videoFolderModal);
    }

    public VideoFolderAdapter(Context context2, List<VideoFolderModal> list, FolderClick folderClick2) {
        this.context = context2;
        this.videoFolderModalList = list;
        this.folderClick = folderClick2;
    }

    public void setVideoFolderList(List<VideoFolderModal> list) {
        this.videoFolderModalList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_video_folder, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        VideoFolderModal videoFolderModal = this.videoFolderModalList.get(i);
        viewHolder.binding.txtFolderName.setText(videoFolderModal.getaName());
        if (videoFolderModal.isSelected()) {
            viewHolder.binding.divider.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.divider.setVisibility(View.GONE);
        }
    }

    public void SetSelected(VideoFolderModal videoFolderModal) {
        videoFolderModal.setSelected(true);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.videoFolderModalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemVideoFolderBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemVideoFolderBinding itemVideoFolderBinding = (ItemVideoFolderBinding) DataBindingUtil.bind(view);
            this.binding = itemVideoFolderBinding;
            itemVideoFolderBinding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (VideoFolderAdapter.this.selectedPos < VideoFolderAdapter.this.videoFolderModalList.size() && VideoFolderAdapter.this.selectedPos != -1) {
                        VideoFolderAdapter.this.videoFolderModalList.get(VideoFolderAdapter.this.selectedPos).setSelected(false);
                    }
                    VideoFolderAdapter.this.selectedPos = ViewHolder.this.getAdapterPosition();
                    VideoFolderAdapter.this.folderClick.OnVideoClick(VideoFolderAdapter.this.videoFolderModalList.get(ViewHolder.this.getAdapterPosition()));
                    VideoFolderAdapter.this.videoFolderModalList.get(VideoFolderAdapter.this.selectedPos).setSelected(true);
                    VideoFolderAdapter.this.notifyDataSetChanged();
                }
            });
        }
    }
}
