package com.neatherbench.quencher;

import android.content.SearchRecentSuggestionsProvider;
import android.view.inputmethod.EditorInfo;

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.neatherbench.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);

    }
}