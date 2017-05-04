package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;

public class Planet extends StellarObject {

    private float radius;

    // Acceleration constant
    private final float G = 12.0f;

    public Planet(float x, float y, float r) {
        super(x, y);
        setRadius(r);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public double getInfluence(float x, float y) {
        return G * radius * radius / distanceSquared(x, y);
    }

    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.GREEN);

        canvas.drawCircle(getX(), getY(), radius, p);
    }


}
