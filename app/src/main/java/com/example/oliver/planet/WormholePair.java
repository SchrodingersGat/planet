package com.example.oliver.planet;

/**
 * Created by Oliver on 5/14/2017.
 */

import android.graphics.PointF;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Color;

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

}
