package com.example.oliver.planet;

import android.graphics.PointF;

/**
 * Created by Oliver on 6/16/2017.
 */

public class GameIncrement {

    protected int step = 0;
    protected double angle = 0;
    protected float thrust = 0;

    public boolean hasAngle = false;
    public boolean hasThrust = false;

    public GameIncrement(int i) {
        step = i;
    }

    public int getStep() {
        return step;
    }

    public void setAngle(double a) {
        angle = a;
        hasAngle = true;
    }

    public void setThrust(float t) {
        thrust = t;
        hasThrust = true;
    }

    public double getAngle() {
        return angle;
    }

    public float getThrust() {
        return thrust;
    }
}
