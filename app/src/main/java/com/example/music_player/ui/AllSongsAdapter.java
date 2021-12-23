package com.example.music_player.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_player.Classes.Song;
import com.example.music_player.R;

import java.util.List;

public class AllSongsAdapter extends RecyclerView.Adapter {

    List<Song> items;
    Context context;
    AllSongsClickListener allSongsClickListener;

    public AllSongsAdapter(List<Song> data, Context activity, AllSongsClickListener listener){
        items = data;
        context = activity;
        allSongsClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_songs_item_layout, parent, false);

        AllSongsAdapterViewHolder vh = new AllSongsAdapterViewHolder(itemView, allSongsClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song item = items.get(position);

        AllSongsAdapterViewHolder viewHolder = (AllSongsAdapterViewHolder)holder;
        viewHolder.SongName.setText(item.Title);
        viewHolder.SongTime.setText(item.SongLength);
        viewHolder.SongArtist.setText(item.Artist);
        Glide.with(context).load(item.Art).into(viewHolder.Art);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class AllSongsAdapterViewHolder extends RecyclerView.ViewHolder{
    public ImageView Art;
    public TextView SongName;
    public TextView SongArtist;
    public TextView SongTime;
    public ImageView OverflowMenu;

    public AllSongsAdapterViewHolder(@NonNull View itemView, AllSongsClickListener listener) {
        super(itemView);

        Art = itemView.findViewById(R.id.Art);
        SongName = itemView.findViewById(R.id.SongName);
        SongArtist = itemView.findViewById(R.id.SongArtist);
        SongTime = itemView.findViewById(R.id.SongTime);
        OverflowMenu = itemView.findViewById(R.id.OverflowMenu);

        itemView.setOnClickListener(v -> listener.onItemViewClicked(getAdapterPosition(), itemView));
        OverflowMenu.setOnClickListener(v -> listener.onOverflowClicked(getAdapterPosition(), itemView));
    }
}
