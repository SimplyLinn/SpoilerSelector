package biz.opposite.spoilerselector;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class SpoilerSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
