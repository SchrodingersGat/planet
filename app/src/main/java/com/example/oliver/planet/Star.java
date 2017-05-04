package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;

public class Star extends StellarObject {

    private final float starRadius = 25;

    private boolean collected;

    public Star() {
        super();
        init();
    }

    public Star(float x, float y) {
        super(x, y);
        init();
    }

    private void init() {
        setRadius(starRadius);
        collected = false;
    }

    public boolean isCollected() { return collected; }

    public void setCollected(boolean status) { collected = status; }

    public void draw(Canvas canvas) {
        Paint p = new Paint();

        p.setColor(Color.YELLOW);

        if (collected) {
            p.setAlpha(100);
        }

        canvas.drawCircle(getX(), getY(), 10, p);
    }
}
