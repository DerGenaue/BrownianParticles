package de.dergenaue.brownianparticles;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import net.rbgrn.android.glwallpaperservice.*;

/**
 * Created by Daniel on 02.03.2016.
 */
public class BrownianParticlesWallpaperService extends GLWallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new BrownianParticlesWallpaperEngine();
    }

    private class BrownianParticlesWallpaperEngine extends GLEngine implements SharedPreferences.OnSharedPreferenceChangeListener {
        BrownianParticlesRenderer renderer;
        SharedPreferences prefs;

        int color = 0, tColor = 0;
        int touchType;

        public BrownianParticlesWallpaperEngine() {
            super();
            // handle prefs, other initialization

            renderer = new BrownianParticlesRenderer();
            setRenderer(renderer);
            setRenderMode(RENDERMODE_CONTINUOUSLY);


            prefs = PreferenceManager.getDefaultSharedPreferences(BrownianParticlesWallpaperService.this);
            prefs.registerOnSharedPreferenceChangeListener(this);

            onSharedPreferenceChanged(null, null);
        }

        public void onOffsetsChanged(float xOffset, float yOffset,
                                    float xStep, float yStep, int xPixels, int yPixels){
            renderer.onOffsetsChanged(xOffset, yOffset, xStep, yStep, xPixels, yPixels);
        }

        public void onDestroy() {
            super.onDestroy();
            if (renderer != null) {
                renderer.release();
            }
            renderer = null;
            prefs.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            if(touchType == 0)
                return;
            if(touchType == 2 || event.getAction() == MotionEvent.ACTION_DOWN)
                renderer.touchAt(event.getX(), event.getY(), touchType);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            try {
                color = Color.parseColor(prefs.getString("color", "#64ff7d"));
            }catch(Exception e){color = Color.rgb(100, 255, 125);}
            try {
                tColor = Color.parseColor(prefs.getString("touchColor", "#ff647d"));
            }catch(Exception e){tColor = Color.rgb(255, 100, 125);}
            try {
                touchType = Integer.parseInt(prefs.getString("touch", "1"));
            }catch(Exception e){touchType = 1;}

            renderer.setColor(color);
            renderer.setWColor(tColor);
        }
    }

}
