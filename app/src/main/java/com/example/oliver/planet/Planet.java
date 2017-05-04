package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;

public class Planet extends StellarObject {

    public enum  PlanetType {
        PLANET,
        SUN,
        BLACK_HOLE,
        REPULSAR,
    };

    private PlanetType planetType = PlanetType.PLANET;

    // Acceleration constant
    private final float G = 12.0f;

    public Planet(float x, float y, float r) {
        super(x, y);
        setRadius(r);
    }

    public void setPlanetType(PlanetType t) { planetType = t; }

    public PlanetType getPlanetType() { return planetType; }

    public double getForce(float x, float y) {

        // Base force
        double force = G * radius * radius / distanceSquared(x, y);

        // Force modifiers depending on planet type
        switch (planetType) {
            default:
            case PLANET:
            case SUN:
                break;
            case REPULSAR:
                force *= -1;
                break;
            case BLACK_HOLE:
                force *= force;
                break;
        }

        return force;
    }

    public void draw(Canvas canvas) {
        Paint p = new Paint();

        switch (planetType) {
            default:
            case PLANET:
                p.setColor(Color.GREEN);
                break;
            case SUN:
                p.setColor(Color.YELLOW);
                break;
            case REPULSAR:
                p.setColor(Color.WHITE);
                break;
            case BLACK_HOLE:
                p.setColor(Color.BLUE);
                break;
        }

        canvas.drawCircle(xPos, yPos, radius, p);
    }


}
