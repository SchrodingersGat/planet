package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;

public class Star extends StellarObject {

    public Star() {
        super();
    }

    public Star(float x, float y) {
        super(x, y);
    }

    public void draw(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.YELLOW);

        canvas.drawCircle(getX(), getY(), 10, p);
    }
}
