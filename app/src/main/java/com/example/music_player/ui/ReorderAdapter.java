package com.example.music_player.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_player.Classes.Song;
import com.example.music_player.MainActivity;
import com.example.music_player.R;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureViewHolder;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.example.music_player.Classes.Singleton.getPlayInstance;

public class ReorderAdapter extends GestureAdapter {

    List<Song> items;
    Context context;
    AllSongsClickListener allSongsClickListener;
    String buttonColor;
    boolean playlist;

    public ReorderAdapter(List<Song> data, Context activity, AllSongsClickListener listener, String buttonColor, boolean playlist){
        items = data;
        context = activity;
        allSongsClickListener = listener;
        this.buttonColor = buttonColor;
        this.playlist = playlist;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.reorder_item_layout, parent, false);

        ReorderViewHolder vh = new ReorderViewHolder(itemView, allSongsClickListener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //Song item = items.get(position);
        Song item = (Song)getData().get(position);

        ReorderViewHolder viewHolder = (ReorderViewHolder)holder;
        viewHolder.SongName.setText(item.Title);
        viewHolder.SongTime.setText(item.SongLength);
        viewHolder.SongArtist.setText(item.Artist);
        viewHolder.Reorder.setColorFilter(Color.parseColor(buttonColor));

        if(!playlist) {
            if (position == getPlayInstance().getMusicValues().currentSongIndex) {
                if (getPlayInstance().getMusicValues().mediaPlayer.isPlaying()) {
                    Glide.with(context).load(R.drawable.playback_start).into(viewHolder.Art);
                } else {
                    Glide.with(context).load(R.drawable.playback_stop).into(viewHolder.Art);
                }
                viewHolder.Art.setColorFilter(Color.parseColor(buttonColor));
            } else {
                Glide.with(context).load(item.Art).into(viewHolder.Art);
                viewHolder.Art.setColorFilter(null);
            }
        }
        else{
            Glide.with(context).load(item.Art).into(viewHolder.Art);
            viewHolder.Art.setColorFilter(null);
        }
    }

    @Override
    public int getItemCount() {
        return getData().size();
    }
}

class ReorderViewHolder extends GestureViewHolder {
    public ImageView Reorder;
    public ImageView Art;
    public TextView SongName;
    public TextView SongArtist;
    public TextView SongTime;
    public ImageView OverflowMenu;

    public ReorderViewHolder(@NotNull View itemView, AllSongsClickListener listener) {
        super(itemView);

        Reorder = itemView.findViewById(R.id.Reorder);
        Art = itemView.findViewById(R.id.Art);
        SongName = itemView.findViewById(R.id.SongName);
        SongArtist = itemView.findViewById(R.id.SongArtist);
        SongTime = itemView.findViewById(R.id.SongTime);
        OverflowMenu = itemView.findViewById(R.id.OverflowMenu);

        itemView.setOnClickListener(v -> listener.onItemViewClicked(getAdapterPosition(), itemView));
        OverflowMenu.setOnClickListener(v -> listener.onOverflowClicked(getAdapterPosition(), itemView));
    }

    @Override
    public boolean canDrag() {
        return true;
    }

    @Override
    public boolean canSwipe() {
        return true;
    }
}
