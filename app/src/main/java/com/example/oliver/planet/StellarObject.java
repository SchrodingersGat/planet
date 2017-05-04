package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import java.lang.Math;
import android.graphics.Canvas;

public class StellarObject extends GameObject {

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

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean containsPoint(float x, float y) {
        return distanceSquared(x,y) < (radius * radius);
    }
}
