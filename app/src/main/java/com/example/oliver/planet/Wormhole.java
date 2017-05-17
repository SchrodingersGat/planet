package com.example.oliver.planet;

/**
 * Created by Oliver on 5/11/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.PointF;

public class Wormhole extends Planet {

    static final float WORMHOLE_RADIUS = 250.0f;
    private final float WORMHOLE_THRESHOLD = 0.5f * WORMHOLE_RADIUS;

    private PointF pointA;
    private PointF pointB;

    // State variable prevents ship oscillating
    private boolean shipInsideWormhole = false;

    private Paint pWormhole;

    void setupPainters() {
        pWormhole = new Paint();
        pWormhole.setStyle(Paint.Style.STROKE);
        pWormhole.setStrokeWidth(2);
        pWormhole.setARGB(200, 200, 250, 150);
    }

    public void reset() {
        shipInsideWormhole = false;
    }

    public Wormhole(float x, float y) {
        super(x, y, WORMHOLE_RADIUS);

        setupPainters();

        reset();
    }

    @Override
    public double getForce(float x, float y) {
        double f = super.getForce(x, y);

        // Weaker force
        return f * 0.1f;
    }

    public void moveShipTo(Ship s, float dx, float dy) {
        shipInsideWormhole = true;

        s.setPos(pos.x + dx, pos.y + dy);
    }


    public boolean isShipInside(Ship s) {

        // If the ship is already 'inside' the wormhole,
        // wait until it has exited the outside radius
        // before resetting the state variable

        float dd = distanceSquared(s.getX(), s.getY());

        if (shipInsideWormhole) {
            if (dd > (WORMHOLE_RADIUS * WORMHOLE_RADIUS)) {
                shipInsideWormhole = false;
            }

            return false;
        }

        if (dd < (WORMHOLE_THRESHOLD * WORMHOLE_THRESHOLD)) {
            shipInsideWormhole = true;
            return true;
        }

        return false;
    }

    @Override
    public void draw(Canvas canvas) {

        // Concentric circles, I guess?
        for (int i=0; i<10; i++) {
            canvas.drawCircle(pos.x, pos.y, WORMHOLE_THRESHOLD + (float) (i + 1) / 10 * (WORMHOLE_RADIUS - WORMHOLE_THRESHOLD), pWormhole);
        }
    }
}
