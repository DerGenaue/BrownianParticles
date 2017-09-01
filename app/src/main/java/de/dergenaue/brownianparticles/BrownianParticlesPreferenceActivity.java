package de.dergenaue.brownianparticles;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * Created by Daniel on 02.03.2016.
 */
public class BrownianParticlesPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        getPreferenceScreen().findPreference("color").setOnPreferenceChangeListener(colorCheckListener);
        getPreferenceScreen().findPreference("touchColor").setOnPreferenceChangeListener(colorCheckListener);
    }


    /**
     * Checks that a preference is a valid color
     */

    Preference.OnPreferenceChangeListener colorCheckListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(newValue.toString().length() == 0)
                return true;
            try{
                Color.parseColor(newValue.toString());
                return true;
            }catch(Exception e) {
            }
            // If now create a message to the user
            Toast.makeText(BrownianParticlesPreferenceActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return false;
        }
    };
}
