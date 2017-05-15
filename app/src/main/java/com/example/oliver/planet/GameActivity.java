package com.example.oliver.planet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;

import com.example.oliver.planet.GameSurface;

public class GameActivity extends Activity {

    GameSurface game;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set no title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        game = new GameSurface(this);
        this.setContentView(game);

        //Log.i("activity", "created");
    }

    @Override
    public void onResume() {

        if (game != null) {
            game.setPaused(false);
        }
        super.onResume();

        //Log.i("activity", "resume");
    }

    @Override
    public void onPause() {
        if (game != null) {
            game.setPaused(true);
        }

        super.onPause();

        //Log.i("activity", "pause");
    }

}
