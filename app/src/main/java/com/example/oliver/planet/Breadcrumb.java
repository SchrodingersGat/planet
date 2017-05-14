package com.example.oliver.planet;

/**
 * Created by Oliver on 5/11/2017.
 */

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import java.lang.Math;

public class Breadcrumb {

    public PointF position = new PointF(0, 0);
    public PointF vector = new PointF(0, 0);

    static final float BREADCRUMB_DISTANCE = 50.0f;

    public Breadcrumb(PointF pos, double angle, double thrust) {
        position.set(pos.x, pos.y);

        thrust *= BREADCRUMB_DISTANCE / Ship.MAX_THRUST;

        double dx = position.x - thrust * Math.cos(angle);
        double dy = position.y - thrust * Math.sin(angle);

        vector.set((float) dx, (float) dy);
    }

    public void draw(Canvas canvas, Paint paint) {

        canvas.drawLine(position.x, position.y, vector.x, vector.y, paint);
        canvas.drawCircle(position.x, position.y, 5, paint);
    }
}
