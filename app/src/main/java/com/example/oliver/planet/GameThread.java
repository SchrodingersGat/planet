package com.example.oliver.planet;

import android.view.SurfaceHolder;
import android.graphics.Canvas;

/**
 * Created by Oliver on 5/3/2017.
 */

public class GameThread extends Thread {

    private boolean running;
    private GameSurface gameSurface;
    private SurfaceHolder surfaceHolder;

    final long FRAME_TIME_MS = 10;

    public GameThread(GameSurface surface, SurfaceHolder holder) {
        this.gameSurface = surface;
        this.surfaceHolder = holder;
    }

    @Override
    public void run() {
        long now = 0;
        long waitTime = 0;
        long startTime = 0;

        Canvas canvas = null;

        while (running) {

            canvas = null;
            startTime = System.nanoTime();

            try {
                canvas = this.surfaceHolder.lockCanvas();

                synchronized (canvas) {
                    this.gameSurface.update();
                    this.gameSurface.draw(canvas);
                }
            }
            catch (Exception e) {

            }
            finally {
                if (canvas != null) {
                    // Unlock the canvas
                    this.surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            now = System.nanoTime();

            waitTime = (now - startTime) / 1000000;

            waitTime = FRAME_TIME_MS - waitTime;

            // Minimum wait time
            if (waitTime < 5) {
                waitTime = 5;
            }

            try {
                this.sleep(waitTime);
            }
            catch (InterruptedException e) {

            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
