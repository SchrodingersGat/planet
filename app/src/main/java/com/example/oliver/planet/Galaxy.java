package com.example.oliver.planet;

/**
 * Created by Oliver on 5/14/2017.
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class Galaxy extends Planet {

    static final float GALAXY_RADIUS = 250.0f;
    private final float GALAXY_THRESHOLD = 0.5f * GALAXY_RADIUS;

    private Paint pGalaxy;

    private void setupPainters() {
        pGalaxy = new Paint();
        pGalaxy.setARGB(200, 200, 250, 250);
        pGalaxy.setStrokeWidth(2);
        pGalaxy.setStyle(Paint.Style.STROKE);
    }

    public Galaxy(float x, float y) {
        super(x, y, GALAXY_RADIUS);
        setPlanetType(PlanetType.GALAXY);
        setupPainters();
    }

    @Override
    public void draw(Canvas canvas) {

        canvas.drawCircle(pos.x, pos.y, GALAXY_RADIUS, pGalaxy);
        canvas.drawCircle(pos.x, pos.y, GALAXY_THRESHOLD, pGalaxy);

        canvas.drawLine(pos.x, pos.y - radius, pos.x, pos.y + radius, pGalaxy);
        canvas.drawLine(pos.x - radius, pos.y, pos.x + radius, pos.y, pGalaxy);
    }

    boolean testShip(Ship s) {

        return distanceSquared(s.getX(), s.getY()) < (GALAXY_THRESHOLD * GALAXY_THRESHOLD);
    }

    @Override
    public double getForce(float x, float y) {

        double force = super.getForce(x, y);

        // End-zone galaxy exerts only a weak force
        return force * 0.15;
    }
}
