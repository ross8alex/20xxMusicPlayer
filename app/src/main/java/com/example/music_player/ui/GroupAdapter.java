package com.example.music_player.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_player.Classes.Group;
import com.example.music_player.Classes.Song;
import com.example.music_player.R;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.interfaces.AlbumClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GroupAdapter extends RecyclerView.Adapter {
    List<Group> items;
    Context context;
    AlbumClickListener albumClickListener;
    AdapterType adapterType;

    public GroupAdapter(List<Group> data, Context activity, AlbumClickListener listener, AdapterType type){
        items = data;
        context = activity;
        albumClickListener = listener;
        adapterType = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_item_layout, parent, false);

        GroupAdapterViewHolder vh = new GroupAdapterViewHolder(itemView, albumClickListener, adapterType);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Group item = items.get(position);

        GroupAdapterViewHolder viewHolder = (GroupAdapterViewHolder)holder;
        viewHolder.PlayButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2c3e50")));
        viewHolder.PlayButton.setImageResource(R.drawable.play_arrow_black_48dp);
        viewHolder.PlayButton.setColorFilter(Color.parseColor("#FFFFFF"));
        viewHolder.GenreName.setText(item.Name);
        viewHolder.NumberOfAlbums.setVisibility(View.GONE);

        List<Song> distinctSongs = item.Songs.stream().filter(distinctByKey(Song::GetArt)).collect(Collectors.toList());
        viewHolder.NumberOfSongs.setText(String.valueOf(item.Songs.size()) + " song" + ((item.Songs.size() != 1) ? "s":""));

        if(distinctSongs.size() > 0){
            Glide.with(context).load(distinctSongs.get(0).GetArt()).into(viewHolder.AlbumImage1);
            if (distinctSongs.size() > 1){
                Glide.with(context).load(distinctSongs.get(1).GetArt()).into(viewHolder.AlbumImage2);
                if (distinctSongs.size() > 2){
                    Glide.with(context).load(distinctSongs.get(2).GetArt()).into(viewHolder.AlbumImage3);
                    if (distinctSongs.size() > 3){
                        Glide.with(context).load(distinctSongs.get(3).GetArt()).into(viewHolder.AlbumImage4);
                    }
                    else{
                        Glide.with(context).load(distinctSongs.get(0).GetArt()).into(viewHolder.AlbumImage4);
                    }
                }
                else{
                    Glide.with(context).load(distinctSongs.get(1).GetArt()).into(viewHolder.AlbumImage3);
                    Glide.with(context).load(distinctSongs.get(0).GetArt()).into(viewHolder.AlbumImage4);
                }
            }
            else{
                Glide.with(context).load(distinctSongs.get(0).GetArt()).into(viewHolder.AlbumImage2);
                Glide.with(context).load(distinctSongs.get(0).GetArt()).into(viewHolder.AlbumImage3);
                Glide.with(context).load(distinctSongs.get(0).GetArt()).into(viewHolder.AlbumImage4);
            }
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class GroupAdapterViewHolder extends RecyclerView.ViewHolder{
    public TextView GenreName;
    public TextView NumberOfAlbums;
    public TextView NumberOfSongs;
    public ImageView AlbumImage1;
    public ImageView AlbumImage2;
    public ImageView AlbumImage3;
    public ImageView AlbumImage4;
    public FloatingActionButton PlayButton;
    public ImageView Overflow;

    public GroupAdapterViewHolder(@NonNull View itemView, AlbumClickListener listener, AdapterType type) {
        super(itemView);

        GenreName = itemView.findViewById(R.id.GenreName);
        NumberOfAlbums = itemView.findViewById(R.id.NumberOfAlbums);
        NumberOfSongs = itemView.findViewById(R.id.NumberOfSongs);
        AlbumImage1 = itemView.findViewById(R.id.AlbumImage1);
        AlbumImage2 = itemView.findViewById(R.id.AlbumImage2);
        AlbumImage3 = itemView.findViewById(R.id.AlbumImage3);
        AlbumImage4 = itemView.findViewById(R.id.AlbumImage4);
        PlayButton = itemView.findViewById(R.id.PlayButton);
        Overflow = itemView.findViewById(R.id.OverflowMenu);

        itemView.setOnClickListener(v -> listener.onItemViewClicked(getAdapterPosition(), type));
        PlayButton.setOnClickListener(v -> listener.onPlayButtonClicked(getAdapterPosition(), type));
        Overflow.setOnClickListener(v -> listener.onOverflowClicked(getAdapterPosition(), itemView, type));
    }
}
