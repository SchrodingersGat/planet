package com.example.oliver.planet;

/**
 * Created by Oliver on 5/14/2017.
 */

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

public class Galaxy extends StellarObject {

    static final float GALAXY_RADIUS = 250.0f;

    public Galaxy(float x, float y) {
        super(x,y);
        setRadius(GALAXY_RADIUS);
    }

    void draw(Canvas canvas) {
        Paint gp = new Paint();

        gp.setColor(Color.argb(200, 200, 250, 250));

        gp.setStrokeWidth(2);
        gp.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(pos.x, pos.y, radius, gp);
        canvas.drawCircle(pos.x, pos.y, 0.5f * radius, gp);
        

        canvas.drawLine(pos.x, pos.y - radius, pos.x, pos.y + radius, gp);
        canvas.drawLine(pos.x - radius, pos.y, pos.x + radius, pos.y, gp);
    }

    boolean testShip(Ship s) {

        float r = 0.5f * GALAXY_RADIUS;

        return distanceSquared(s.getX(), s.getY()) < (r * r);
    }
}
