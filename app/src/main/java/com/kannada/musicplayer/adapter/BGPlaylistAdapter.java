package com.kannada.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.database.model.AudioVideoModal;
import com.kannada.musicplayer.databinding.ItemBgPlaylistBinding;
import com.kannada.musicplayer.utils.AppPref;
import com.kannada.musicplayer.utils.SwipeAndDragHelper;
import java.util.List;

public class BGPlaylistAdapter extends RecyclerView.Adapter<BGPlaylistAdapter.ViewHolder> implements SwipeAndDragHelper.ActionCompletionContract {
    List<AudioVideoModal> audioVideoModalList;
    Context context;
    boolean isPlaying = false;
    ItemClick itemClick;
    public ItemTouchHelper itemTouchHelper;
    int positionForPlay = -1;


    public interface ItemClick {
        void onItemClick(int i, int i2);
    }

    @Override
    public void onViewSwiped(int i) {
    }

    @Override
    public void reallyMoved(int i, int i2) {
    }

    public BGPlaylistAdapter(Context context2, List<AudioVideoModal> list, ItemClick itemClick2) {
        this.context = context2;
        this.audioVideoModalList = list;
        this.itemClick = itemClick2;
    }

    public void setList(List<AudioVideoModal> list) {
        this.audioVideoModalList = list;
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
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_bg_playlist, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.binding.txtName.setText(this.audioVideoModalList.get(i).getName());

        viewHolder.binding.imgDragger.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View viewv, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() != 0) {
                    return false;
                }
                itemTouchHelper.startDrag(viewHolder);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.audioVideoModalList.size();
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper2) {
        this.itemTouchHelper = itemTouchHelper2;
    }

    @Override
    public void onViewMoved(int i, int i2) {
        this.audioVideoModalList.get(i).setAudioVideoOrder(i2);
        List<AudioVideoModal> list = this.audioVideoModalList;
        list.set(i, list.get(i));
        setPrefList(this.audioVideoModalList);
        this.audioVideoModalList.get(i2).setAudioVideoOrder(i);
        List<AudioVideoModal> list2 = this.audioVideoModalList;
        list2.set(i2, list2.get(i2));
        setPrefList(this.audioVideoModalList);
        AudioVideoModal audioVideoModal = new AudioVideoModal(this.audioVideoModalList.get(i));
        this.audioVideoModalList.remove(i);
        this.audioVideoModalList.add(i2, audioVideoModal);
        notifyItemMoved(i, i2);
    }

    public void setPrefList(List<AudioVideoModal> list) {
        if (AppPref.getBgAudioList() != null) {
            AppPref.getBgAudioList();
            AppPref.setBgAudioList(list);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemBgPlaylistBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemBgPlaylistBinding itemBgPlaylistBinding = (ItemBgPlaylistBinding) DataBindingUtil.bind(view);
            this.binding = itemBgPlaylistBinding;
            itemBgPlaylistBinding.imgDragger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BGPlaylistAdapter.this.itemClick.onItemClick(ViewHolder.this.getAdapterPosition(), 1);
                }
            });
            this.binding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BGPlaylistAdapter.this.itemClick.onItemClick(ViewHolder.this.getAdapterPosition(), 2);
                }
            });
            this.binding.close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BGPlaylistAdapter.this.itemClick.onItemClick(ViewHolder.this.getAdapterPosition(), 3);
                }
            });
        }
    }
}
