package com.example.music_player.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_player.Classes.Song;
import com.example.music_player.R;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter {
    List<Song> items;
    AllSongsClickListener allSongsClickListener;

    public SongAdapter(List<Song> data, AllSongsClickListener listener){
        items = data;
        allSongsClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item_layout, parent, false);

        SongAdapterViewHolder vh = new SongAdapterViewHolder(itemView, allSongsClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Song item = items.get(position);

        SongAdapterViewHolder viewHolder = (SongAdapterViewHolder)holder;
        viewHolder.SongNumber.setText(String.valueOf(item.SongNumber));
        viewHolder.SongName.setText(item.Title);
        viewHolder.SongLength.setText(item.SongLength);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class SongAdapterViewHolder extends RecyclerView.ViewHolder{
    public TextView SongNumber;
    public TextView SongName;
    public TextView SongLength;
    public ImageView Overflow;

    public SongAdapterViewHolder(@NonNull View itemView, AllSongsClickListener listener) {
        super(itemView);

        SongNumber = itemView.findViewById(R.id.SongNumber);
        SongName = itemView.findViewById(R.id.SongName);
        SongLength = itemView.findViewById(R.id.SongTime);
        Overflow = itemView.findViewById(R.id.OverflowMenu);

        itemView.setOnClickListener(v -> listener.onItemViewClicked(getAdapterPosition(), itemView));
        Overflow.setOnClickListener(v -> listener.onOverflowClicked(getAdapterPosition(), itemView));
    }
}
