package com.example.oliver.planet;

import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;

import java.util.Vector;

/**
 * Created by Oliver on 5/3/2017.
 */

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    public GameLevel level;

    private StellarObject stellarObjectBeingDragged;

    public GameSurface(Context context) {
        super(context);

        this.setFocusable(true);

        this.getHolder().addCallback(this);

        level = new GameLevel();

        Planet p;

        p = new Planet(-250, -50, 50);
        level.planets.add(p);

        p = new Planet(100, 25, 15);
        p.setPlanetType(Planet.PlanetType.BLACK_HOLE);
        level.planets.add(p);

        p = new Planet(200, 500, 45);
        p.setPlanetType(Planet.PlanetType.REPULSAR);
        level.planets.add(p);
        Star s;

        s = new Star(0, 0);
        level.stars.add(s);

        s = new Star(250, -50);
        level.stars.add(s);

        level.ship.setPos(25,100);
    }

    public void update() {

        // Update game mechanics
        level.update();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        float w = (float) getWidth();
        float h = (float) getHeight();

        canvas.translate(w/2, h/2);

        canvas.drawColor(Color.BLACK);

        // Draw each planet
        for (int ii=0; ii<level.planets.size(); ii++) {
            level.planets.get(ii).draw(canvas);
        }

        // Draw each star
        for (int jj=0; jj<level.stars.size(); jj++) {
            level.stars.get(jj).draw(canvas);
        }

        // Draw the ship
        level.ship.draw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Get the position of the ship in world coordinates
        float x = event.getX() - this.getWidth() / 2;
        float y = event.getY() - this.getHeight() / 2;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /* Test for item being clicked */
                stellarObjectBeingDragged = level.testStellarObjectHit(x, y);

                if (stellarObjectBeingDragged == null) {
                    level.reset();
                    level.ship.setPos(x,y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (stellarObjectBeingDragged != null) {
                    stellarObjectBeingDragged.setPos(x, y);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                stellarObjectBeingDragged = null;
                break;
            default:
                break;
        }
        return true;
    }
}
