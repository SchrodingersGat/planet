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

    public Galaxy(float x, float y) {
        super(x, y, GALAXY_RADIUS);
    }

    public void draw(Canvas canvas) {
        Paint gp = new Paint();

        gp.setColor(Color.argb(200, 200, 250, 250));

        gp.setStrokeWidth(2);
        gp.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(pos.x, pos.y, GALAXY_RADIUS, gp);
        canvas.drawCircle(pos.x, pos.y, GALAXY_THRESHOLD, gp);

        canvas.drawLine(pos.x, pos.y - radius, pos.x, pos.y + radius, gp);
        canvas.drawLine(pos.x - radius, pos.y, pos.x + radius, pos.y, gp);
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
