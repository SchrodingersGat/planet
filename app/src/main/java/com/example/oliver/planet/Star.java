package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Path;

public class Star extends StellarObject {

    static final int STAR_COLOR = Color.rgb(245, 233, 100);

    static final float STAR_RADIUS = 25;
    static final float STAR_COLLECTION_RADIUS = 35;

    private boolean collected;

    private Paint pStar = new Paint();

    private void setupPainters() {
        pStar.setColor(STAR_COLOR);
    }

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
        setupPainters();
    }

    public boolean isCollected() { return collected; }

    public void setCollected(boolean status) { collected = status; }

    public boolean testCollection(float x, float y) {

        return distanceSquared(x, y) < (STAR_COLLECTION_RADIUS * STAR_COLLECTION_RADIUS);
    }

    public void draw(Canvas canvas) {

        if (collected) {
            return;
        }

        Path p = new Path();

        float x = getX();
        float y = getY();
        float r = (float) STAR_RADIUS;

        p.moveTo(x, y-r);
        p.lineTo(x-0.5f*r, y+r);
        p.lineTo(x+r, y-0.25f*r);
        p.lineTo(x-r, y-0.25f*r);
        p.lineTo(x+0.5f*r, y+r);
        p.lineTo(x, y-r);

        canvas.drawPath(p, pStar);
    }
}
