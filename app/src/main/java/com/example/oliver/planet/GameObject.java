package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import java.lang.Math;
import android.graphics.PointF;

public class GameObject {

    protected PointF pos = new PointF(0, 0);

    public GameObject() {
        setPos(0, 0);
    }

    public GameObject(float x, float y) {
        setPos(x, y);
    }

    public GameObject(PointF p) {
        pos = p;
    }

    public PointF getPos() { return pos; }

    public float getX() { return pos.x; }

    public float getY() { return pos.y; }

    public void setPos(float x, float y) {
        pos.set(x,y);
    }

    public float distanceSquared(float x, float y) {
        float dx = x - pos.x;
        float dy = y - pos.y;

        return dx*dx + dy*dy;
    }

    public float distanceSquared(StellarObject object) {
        return distanceSquared(object.getX(), object.getY());
    }

    public float distanceTo(float x, float y) {
        float dSquared = distanceSquared(x, y);

        return (float) Math.sqrt(dSquared);
    }

    public float distanceTo(StellarObject object) {
        return distanceTo(object.getX(), object.getY());
    }

    public double angleTo(float x, float y) {
        float dx = x - pos.x;
        float dy = y - pos.y;

        return Math.atan2(dy, dx);
    }

    public double angleTo(StellarObject object) {
        return angleTo(object.getX(), object.getY());
    }
}
