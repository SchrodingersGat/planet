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

    // Atmosphere radius
    private float atmosphere = 0.0f;
    static final float MAX_ATMOSPHERE = 250.0f;

    // Acceleration constant
    private final float G = 20.0f;

    public Planet(float x, float y, float r) {
        super(x, y);
        setRadius(r);
    }

    public void setAtmosphere(float r) {
        if (r < 0) {
            r = 0;
        }

        if (r > MAX_ATMOSPHERE) {
            r = MAX_ATMOSPHERE;
        }

        atmosphere = r;
    }

    public boolean pointWithinAtmosphere(float x, float y) {
        if (atmosphere <= radius) { return false; }

        float r = radius + atmosphere;

        return distanceSquared(x, y) <= (r * r);
    }

    public float getAtmosphere() { return atmosphere; }

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

        if (atmosphere > 0) {
            p.setAlpha(25);
            canvas.drawCircle(pos.x, pos.y, radius + atmosphere, p);
        }

        p.setAlpha(255);
        canvas.drawCircle(pos.x, pos.y, radius, p);

    }


}
