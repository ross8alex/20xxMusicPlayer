package com.example.music_player.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_player.Classes.Playlist;
import com.example.music_player.R;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.interfaces.AlbumClickListener;
import com.example.music_player.interfaces.BasicClickListener;
import com.example.music_player.interfaces.SettingClickListener;

import java.util.List;

public class AddToPlaylistAdapter extends RecyclerView.Adapter {
    List<Playlist> items;
    BasicClickListener basicClickListener;

    public AddToPlaylistAdapter(List<Playlist> data, BasicClickListener listener){
        items = data;
        basicClickListener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_to_playlist_item_layout, parent, false);

        AddToPlaylistViewHolder vh = new AddToPlaylistViewHolder(itemView, basicClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Playlist item = items.get(position);

        AddToPlaylistViewHolder viewHolder = (AddToPlaylistViewHolder) holder;
        viewHolder.PlaylistName.setText(item.GetName());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class AddToPlaylistViewHolder extends RecyclerView.ViewHolder {
    public TextView PlaylistName;

    public AddToPlaylistViewHolder(@NonNull View itemView, BasicClickListener listener) {
        super(itemView);

        PlaylistName = itemView.findViewById(R.id.PlaylistName);

        itemView.setOnClickListener(v -> listener.onItemViewClicked(getAdapterPosition()));
    }
}
