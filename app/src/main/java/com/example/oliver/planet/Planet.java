package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;

public class Planet extends StellarObject {

    // Acceleration constant
    private final float G = 12.0f;

    public Planet(float x, float y, float r) {
        super(x, y);
        setRadius(r);
    }

    public double getInfluence(float x, float y) {
        return G * radius * radius / distanceSquared(x, y);
    }

    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.GREEN);

        canvas.drawCircle(xPos, yPos, radius, p);
    }


}
