package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;

public class Star extends StellarObject {

    static final float STAR_RADIUS = 15;
    static final float STAR_COLLECTION_RADIUS = 35;

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
        setRadius(STAR_RADIUS);
        collected = false;
    }

    public boolean isCollected() { return collected; }

    public void setCollected(boolean status) { collected = status; }

    public boolean testCollection(float x, float y) {

        return distanceSquared(x, y) < (STAR_COLLECTION_RADIUS * STAR_COLLECTION_RADIUS);
    }

    public void draw(Canvas canvas) {

        Paint p = new Paint();

        p.setColor(Color.CYAN);

        if (collected) {
            p.setAlpha(100);
        }

        canvas.drawCircle(getX(), getY(), STAR_RADIUS, p);
    }
}
