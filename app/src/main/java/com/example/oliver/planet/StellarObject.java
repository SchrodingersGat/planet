package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.PointF;

import org.json.JSONException;
import org.json.JSONObject;

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

    /*
    JSON Keys
     */
    protected final String KEY_POS_X = "x";
    protected final String KEY_POS_Y = "y";
    protected final String KEY_RADIUS = "radius";

    public boolean encodeToJson(JSONObject json) {

        try {

            json.put(KEY_POS_X, (double) pos.x);
            json.put(KEY_POS_Y, (double)pos.y);
            json.put(KEY_RADIUS, (double)radius);
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }

    public boolean decodeFromJson(JSONObject json) {
        try {
            float x = pos.x;
            float y = pos.y;
            float r = radius;

            if (json.has(KEY_POS_X) && !json.isNull(KEY_POS_X)) {
                x = (float) json.getDouble(KEY_POS_X);
            }

            if (json.has(KEY_POS_Y) && !json.isNull(KEY_POS_Y)) {
                y = (float) json.getDouble(KEY_POS_Y);
            }

            if (json.has(KEY_RADIUS) && !json.isNull(KEY_RADIUS)) {
                r = (float) json.getDouble(KEY_RADIUS);
            }

            setPos(x, y);
            setRadius(r);
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }
}
