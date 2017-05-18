package com.example.oliver.planet;

/**
 * Created by Oliver on 5/6/2017.
 */

public class OffScreenArrow {

    static final float ARROW_SIZE = 15;

    public enum ArrowShape {
        CIRCLE,
        DIAMOND,
    };

    public float x = 0;
    public float y = 0;
    public int color;
    public ArrowShape shape;

    public OffScreenArrow(float x, float y, int color, ArrowShape shape) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.shape = shape;
    }
}
