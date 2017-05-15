package com.example.oliver.planet;

import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.os.Handler;
import android.content.Context;
import android.util.Log;

/**
 * Created by Oliver on 5/3/2017.
 */

public class GameThread extends Thread {

    /*
    Thread state variables
     */
    private boolean m_running = false;
    private final Object m_runLock = new Object();

    /*
    Thread variables
     */
    private GameSurface m_gameSurface;
    private SurfaceHolder m_surfaceHolder;
    private Context m_context;
    private Handler m_handler;

    final private long FRAME_TIME_MS = 10;
    private long m_lastTime;

    public GameThread(
            GameSurface surface,
            SurfaceHolder holder,
            Context context,
            Handler handler) {
        this.m_gameSurface = surface;
        this.m_surfaceHolder = holder;
        this.m_context = context;
        this.m_handler = handler;
    }

    public void setRunning(boolean b) {
        if (b) {
            Log.i("thread", "set running = true");
        } else {
            Log.i("thread", "set running = false");
        }
        synchronized(m_runLock) {
            m_running = b;
        }
    }

    @Override
    public void run() {
        long delta = 0;

        Canvas canvas = null;

        Log.i("thread", "starting");

        m_lastTime = System.currentTimeMillis();

        while (m_running) {

            delta = System.currentTimeMillis() - m_lastTime;

            if (delta < FRAME_TIME_MS) {
                // Delay for 1ms at a time
                try {
                    this.sleep(1);
                }
                catch (InterruptedException e) {

                }

                continue;
            }

            try {
                canvas = this.m_surfaceHolder.lockCanvas();

                synchronized (m_runLock) {

                    synchronized (canvas) {

                        this.m_gameSurface.update();

                        this.m_gameSurface.draw(canvas);
                    }
                }
            }
            catch (Exception e) {

            }
            finally {
                if (canvas != null) {
                    // Unlock the canvas
                    this.m_surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

            m_lastTime = System.currentTimeMillis();
        }

        Log.i("thread", "finished");
    }
}
