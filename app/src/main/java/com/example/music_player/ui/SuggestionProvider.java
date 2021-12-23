package com.example.music_player.ui;

import android.content.SearchRecentSuggestionsProvider;

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.example.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SuggestionProvider(){
        setupSuggestions(AUTHORITY, MODE);
    }
}
