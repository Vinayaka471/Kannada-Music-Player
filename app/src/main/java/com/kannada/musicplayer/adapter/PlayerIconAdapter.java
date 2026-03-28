package com.kannada.musicplayer.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.kannada.musicplayer.R;
import com.kannada.musicplayer.databinding.ItemPlayerIconBinding;
import com.kannada.musicplayer.model.IconModel;
import com.kannada.musicplayer.utils.AppConstants;
import java.util.List;

public class PlayerIconAdapter extends RecyclerView.Adapter<PlayerIconAdapter.ViewHolder> {
    Context context;
    List<IconModel> iconModelList;
    OnClick onClick;

    public interface OnClick {
        void onIconClick(int i);
    }

    public PlayerIconAdapter(Context context2, List<IconModel> list, OnClick onClick2) {
        this.context = context2;
        this.iconModelList = list;
        this.onClick = onClick2;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_player_icon, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        IconModel iconModel = this.iconModelList.get(i);
        RequestManager with = Glide.with(this.context);
        with.load(Uri.parse(AppConstants.AssetsPath() + iconModel.getIconName())).into(viewHolder.binding.playerIcon);
        viewHolder.binding.txtTitle.setText(iconModel.getTitle());
    }

    @Override
    public int getItemCount() {
        return this.iconModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemPlayerIconBinding binding;

        public ViewHolder(View view) {
            super(view);
            ItemPlayerIconBinding itemPlayerIconBinding = (ItemPlayerIconBinding) DataBindingUtil.bind(view);
            this.binding = itemPlayerIconBinding;
            itemPlayerIconBinding.llMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PlayerIconAdapter.this.onClick.onIconClick(ViewHolder.this.getAdapterPosition());
                }
            });
        }
    }
}
