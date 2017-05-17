package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class Planet extends StellarObject {

    public enum  PlanetType {
        PLANET,
        SUN,
        BLACK_HOLE,
        REPULSAR,

        // Special cases
        MOON,
        WORMHOLE,
        GALAXY
    };

    private PlanetType planetType = PlanetType.PLANET;

    // Atmosphere radius
    private float atmosphere = 0.0f;
    static final float MAX_ATMOSPHERE = 2500.0f;

    public float getTotalRadius() {
        return radius + atmosphere;
    }

    // Acceleration constant
    private final float G = 750.0f;

    /* Painters */
    private Paint pPlanet;
    private Paint pAtmosphere;
    private RadialGradient rgAtmosphere;

    private void setupPainters() {
        pPlanet = new Paint();
        pPlanet.setStyle(Paint.Style.FILL_AND_STROKE);

        if (pAtmosphere == null) {
            pAtmosphere = new Paint();
        }
    }

    /* Default constructor */
    public Planet() {
        pos.set(0, 0);
        radius = MIN_RADIUS;
    }

    public Planet(float x, float y, float r) {

        setupPainters();

        pos.set(x, y);
        radius = r;

        setPlanetType(PlanetType.PLANET);

        updateAtmosphere();
    }

    private void updateAtmosphere() {

        // Atmosphere only applies for SUN planet type
        final int r = 250;
        final int g = 220;
        final int b = 50;

        int[] colors = {
                Color.argb(150, r, g, b),
                Color.argb(50, r, g, b),
                Color.argb(0, r, g, b)
        };

        float R = radius + atmosphere;

        if (R < 50) {
            R = 50;
        }

        float[] stops = {
                radius / R,
                (R - 50) / R,
                1.0f
        };

        rgAtmosphere = new RadialGradient(
                pos.x,
                pos.y,
                R,
                colors,
                stops,
                Shader.TileMode.CLAMP);

        if (pAtmosphere == null) {
            pAtmosphere = new Paint();
        }

        pAtmosphere.setShader(rgAtmosphere);
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public void setPlanetType(PlanetType t) {
        planetType = t;

        switch (t) {
            default:
            case PLANET:
                pPlanet.setColor(Color.GREEN);
                break;
            case SUN:
                pPlanet.setColor(Color.YELLOW);
                updateAtmosphere();
                break;
            case REPULSAR:
                pPlanet.setColor(Color.WHITE);
                break;
            case BLACK_HOLE:
                pPlanet.setColor(Color.BLUE);
                break;
            //case MOON:
            //    pPlanet.setColor(Color.GRAY);
            //    break;
        }
    }

    public void setAtmosphere(float r) {
        if (r < 0) {
            r = 0;
        }

        if (r > MAX_ATMOSPHERE) {
            r = MAX_ATMOSPHERE;
        }

        atmosphere = r;

        updateAtmosphere();
    }

    public float getAtmosphere() { return atmosphere; }

    public double getForce(float x, float y) {

        // Base force
        double force = G * radius / distanceSquared(x, y);

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
                force *= radius;
                break;
        }

        return force;
    }

    public void draw(Canvas canvas) {

        // Draw atmosphere for SUN type planet
        if (planetType == PlanetType.SUN && atmosphere > 0) {
            canvas.drawCircle(pos.x, pos.y, radius + atmosphere, pAtmosphere);
        }

        canvas.drawCircle(pos.x, pos.y, radius, pPlanet);
    }

    @Override
    public void setPos(float x, float y) {
        super.setPos(x, y);

        updateAtmosphere();
    }

    @Override
    public void setRadius(float r) {
        super.setRadius(r);

        updateAtmosphere();
    }
}
