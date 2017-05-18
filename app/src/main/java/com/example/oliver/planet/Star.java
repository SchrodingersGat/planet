package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;

public class Star extends StellarObject {

    static final int STAR_COLOR = Color.rgb(245, 233, 100);

    static final float STAR_RADIUS = 15;
    static final float STAR_COLLECTION_RADIUS = 5;

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

        canvas.drawCircle(getX(), getY(), STAR_RADIUS, pStar);
    }
}
