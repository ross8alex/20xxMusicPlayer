package com.example.music_player.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.example.music_player.Classes.Album;
import com.example.music_player.R;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.interfaces.AlbumClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class AlbumAdapter extends RecyclerView.Adapter {
    List<Album> items;
    Context context;
    AlbumClickListener albumClickListener;
    AdapterType adapterType;
    RequestManager requestManager;

    public AlbumAdapter(List<Album> data, RequestManager requestManager, Context context, AlbumClickListener listener, AdapterType type){
        items = data;
        this.context = context;
        this.requestManager = requestManager;
        albumClickListener = listener;
        adapterType = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item_layout, parent, false);
        //itemView = LayoutInflater.from(context).inflate(R.layout.album_item_layout, parent, false);

        AlbumAdapterViewHolder vh = new AlbumAdapterViewHolder(itemView, albumClickListener, adapterType);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Album item = items.get(position);
        AlbumAdapterViewHolder viewHolder = (AlbumAdapterViewHolder)holder;

        viewHolder.PlayButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2c3e50")));
        viewHolder.PlayButton.setImageResource(R.drawable.play_icon);
        viewHolder.PlayButton.setColorFilter(Color.parseColor("#FFFFFF"));
        viewHolder.AlbumName.setText(item.Name);
        viewHolder.AlbumArtist.setText(item.AlbumArtist);
        requestManager.load(item.Art).into(viewHolder.AlbumImage);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        AlbumAdapterViewHolder viewHolder = (AlbumAdapterViewHolder)holder;
        //Glide.with(context).clear(viewHolder.AlbumImage);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class AlbumAdapterViewHolder extends RecyclerView.ViewHolder{
    public ImageView AlbumImage;
    public TextView AlbumName;
    public TextView AlbumArtist;
    public ImageView Overflow;
    public FloatingActionButton PlayButton;

    public AlbumAdapterViewHolder(@NonNull View itemView, AlbumClickListener listener, AdapterType type) {
        super(itemView);

        AlbumImage = itemView.findViewById(R.id.AlbumImage);
        AlbumName = itemView.findViewById(R.id.albumName);
        AlbumArtist = itemView.findViewById(R.id.albumArtist);
        Overflow = itemView.findViewById(R.id.OverflowMenu);
        PlayButton = itemView.findViewById(R.id.playButton);

        itemView.setOnClickListener(v -> listener.onItemViewClicked(getAdapterPosition(), type));
        Overflow.setOnClickListener(v -> listener.onOverflowClicked(getAdapterPosition(), itemView, type));
        PlayButton.setOnClickListener(v -> listener.onPlayButtonClicked(getAdapterPosition(), type));
    }
}
