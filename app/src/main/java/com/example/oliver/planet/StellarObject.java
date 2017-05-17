package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.PointF;

public class StellarObject extends GameObject {

    static final float MIN_RADIUS = 10;
    static final float MAX_RADIUS = 2500;

    protected float radius = 1.0f;

    public StellarObject() {
        super();
    }

    public StellarObject(float x, float y) {
        super(x,y);
        radius = 0.0f;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float r) {

        if (r < MIN_RADIUS) {
            r = MIN_RADIUS;
        }

        if (r > MAX_RADIUS) {
            r = MAX_RADIUS;
        }

        radius = r;
    }

    public boolean containsPoint(float x, float y) {
        return distanceSquared(x,y) < (radius * radius);
    }

    public boolean containsPoint(PointF point) {
        return containsPoint(point.x, point.y);
    }
}
