package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import java.lang.Math;
import android.graphics.Canvas;

public class StellarObject {

    private float xPos;
    private float yPos;

    public StellarObject() {
        xPos = 0.0f;
        yPos = 0.0f;
    }

    public StellarObject(float x, float y) {
        xPos = x;
        yPos = y;
    }

    public float getX() {
        return xPos;
    }

    public float getY() {
        return yPos;
    }

    public void setX(float x) {
        xPos = x;
    }

    public void setY(float y) {
        yPos = y;
    }

    public void setPos(float x, float y) {
        xPos = x;
        yPos = y;
    }

    public float distanceSquared(float x, float y) {
        float dx = x - xPos;
        float dy = y - yPos;

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
        float dx = x - xPos;
        float dy = y - yPos;

        return Math.atan2(dy, dx);
    }

    public double angleTo(StellarObject object) {
        return angleTo(object.getX(), object.getY());
    }
}
