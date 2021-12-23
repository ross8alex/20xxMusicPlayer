package com.example.music_player.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_player.Classes.Setting;
import com.example.music_player.R;
import com.example.music_player.enums.AdapterType;
import com.example.music_player.enums.SettingType;
import com.example.music_player.interfaces.SettingClickListener;

import org.w3c.dom.Text;

import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter {
    List<Setting> items;
    SettingClickListener settingClickListener;
    SettingType settingType;

    public SettingsAdapter(List<Setting> data, SettingClickListener listener, SettingType type){
        items = data;
        settingClickListener = listener;
        settingType = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.settings_item_layout, parent, false);

        SettingsAdapterViewHolder vh = new SettingsAdapterViewHolder(itemView, settingClickListener, settingType);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Setting item = items.get(position);

        SettingsAdapterViewHolder viewHolder = (SettingsAdapterViewHolder)holder;
        viewHolder.PrimarySetting.setText(item.PrimarySetting);

        if(item.SecondarySetting != "" && item.SecondarySetting != null){
            viewHolder.SecondarySetting.setText(item.SecondarySetting);
            viewHolder.SecondarySetting.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.SecondarySetting.setVisibility(View.GONE);
        }

        if(item.SettingValue){
            viewHolder.SettingsSwitch.setChecked(true);
        }
        else{
            viewHolder.SettingsSwitch.setChecked(false);
        }

        if(item.HasSwitch){
            viewHolder.SettingsSwitch.setVisibility(View.VISIBLE);
        }
        else{
            viewHolder.SettingsSwitch.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class SettingsAdapterViewHolder extends RecyclerView.ViewHolder{
    public TextView PrimarySetting;
    public TextView SecondarySetting;
    public Switch SettingsSwitch;

    public SettingsAdapterViewHolder(@NonNull View itemView, SettingClickListener listener, SettingType type) {
        super(itemView);

        PrimarySetting = itemView.findViewById(R.id.PrimarySetting);
        SecondarySetting = itemView.findViewById(R.id.SecondarySetting);
        SettingsSwitch = itemView.findViewById(R.id.SettingsSwitch);

        itemView.setOnClickListener(v -> listener.onItemViewClicked(getAdapterPosition(), type));
        SettingsSwitch.setOnClickListener(v -> listener.onSwitchClicked(getAdapterPosition(), type));
    }
}
