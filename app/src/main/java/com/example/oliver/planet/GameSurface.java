package com.example.oliver.planet;

import android.view.Surface;
import android.view.SurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Vector;

/**
 * Created by Oliver on 5/3/2017.
 */

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;

    int wScreen = 0;
    int hScreen = 0;

    float x = 0;
    float y = 0;

    Vector<Planet> planets;

    public GameSurface(Context context) {
        super(context);

        this.setFocusable(true);

        this.getHolder().addCallback(this);

        Planet p;

        planets = new Vector<Planet>();

        p = new Planet(-50, -50, 50);
        planets.add(p);

        p = new Planet(100, 25, 15);
        planets.add(p);

        p = new Planet(200, 500, 45);
        planets.add(p);
    }

    public void update() {

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        float w = (float) getWidth();
        float h = (float) getHeight();

        canvas.translate(w/2, h/2);

        canvas.drawColor(Color.BLACK);

        for( int i=0; i<planets.size(); i++ )
        {
            planets.get(i).draw(canvas);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        wScreen = w;
        hScreen = h;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry= true;
        while(retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
                retry = false;
            }
            catch(InterruptedException e)  {
                e.printStackTrace();
            }
            retry= true;
        }
    }
}
