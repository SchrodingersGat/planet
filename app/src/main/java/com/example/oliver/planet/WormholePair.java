package com.example.oliver.planet;

/**
 * Created by Oliver on 5/14/2017.
 */

import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONObject;

public class WormholePair {

    public Wormhole wormholeA;
    public Wormhole wormholeB;

    public WormholePair(PointF pa, PointF pb) {

        wormholeA = new Wormhole(pa.x, pa.y);
        wormholeB = new Wormhole(pb.x, pb.y);
    }

    public boolean updateShip(Ship s) {

        float dx = 0;
        float dy = 0;

        boolean result = false;

        if (wormholeA.isShipInside(s)) {
            dx = s.getX() - wormholeA.getX();
            dy = s.getY() - wormholeA.getY();

            wormholeB.moveShipTo(s, dx, dy);

            result = true;
        }

        else if (wormholeB.isShipInside(s)) {
            dx = s.getX() - wormholeB.getX();
            dy = s.getY() - wormholeB.getY();

            wormholeA.moveShipTo(s, dx, dy);

            result = true;
        }

        return result;
    }

    public void draw(Canvas canvas) {
        Paint linePaint = new Paint();

        linePaint.setColor(Color.argb(150, 200, 220, 200));

        canvas.drawLine(
                wormholeA.getX(),
                wormholeA.getY(),
                wormholeB.getX(),
                wormholeB.getY(),
                linePaint);

        wormholeA.draw(canvas);
        wormholeB.draw(canvas);

    }

    /*
    JSON keys
     */
    private final String KEY_WORMHOLE_A = "wormholeA";
    private final String KEY_WORMHOLE_B = "wormholeB";

    public boolean encodeToJson(JSONObject json) {

        try {
            JSONObject a = new JSONObject();
            wormholeA.encodeToJson(a);

            json.put(KEY_WORMHOLE_A, a);

            JSONObject b = new JSONObject();
            wormholeB.encodeToJson(b);
            json.put(KEY_WORMHOLE_B, b);
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }

    public boolean decodeFromJson(JSONObject json) {
        try {
            if (json.has(KEY_WORMHOLE_A) && !json.isNull(KEY_WORMHOLE_A)) {
                JSONObject a = json.getJSONObject(KEY_WORMHOLE_A);

                wormholeA.decodeFromJson(a);
            }

            if (json.has(KEY_WORMHOLE_B) && !json.isNull(KEY_WORMHOLE_B)) {
                JSONObject b = json.getJSONObject(KEY_WORMHOLE_B);

                wormholeB.decodeFromJson(b);
            }
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }

}
