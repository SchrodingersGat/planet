package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import java.util.Vector;

public class Planet extends StellarObject {

    // Orbit constants
    static final float MIN_ORBIT_RADIUS = 250;
    static final float MAX_ORBIT_RADIUS = 2500;

    public enum PlanetType {
        PLANET,
        SUN,
        BLACK_HOLE,
        REPULSAR,
        MOON,

        // Special cases
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
    private Paint pPlanet = new Paint();
    private Paint pAtmosphere = new Paint();
    private RadialGradient rgAtmosphere;

    private void setupPainters() {
        pPlanet.setStyle(Paint.Style.FILL_AND_STROKE);

    }

    /*
    Does this planet orbit some other planet?
    Orbit defined by equation:
    W = sqrt(G*M/r^3) where:
        G = gravitational constant
        M = mass of planet orbited by this planet
        r = orbit radius

    The angular velocity W is calculated when the orbit radius is changed
    (Otherwise expensive sqrt operation is needed often)
     */
    private Planet orbit = null;
    private float orbitRadius = MIN_ORBIT_RADIUS;
    private float orbitVelocity = 0.0f;
    private float orbitAngle = 0.0f;
    private float orbitStartingAngle = 0.0f;
    private boolean orbitDir = true;

    public void orbitPlanet(Planet p, float r, float a, boolean dir) {

        if (p == this) {
            p = null;
        }

        orbit = p;
        setOrbitRadius(r);
        setOrbitStartingAngle(a);

        orbitAngle = orbitStartingAngle;

        // Calculate the orbit velocity
        if (orbit != null) {
            orbitVelocity = (float) Math.sqrt(G * orbit.getMass() / Math.pow(orbitRadius, 3));
        }
        else {
            orbitVelocity = 0;
        }

        setOrbitDirection(dir);

        resetOrbit();
    }

    public void setOrbitDirection(boolean dir) {
        orbitDir = dir;
    }

    /*
    Set the orbit radius of this planet
     */
    public void setOrbitRadius(float r) {
        if (r < MIN_ORBIT_RADIUS) {
            r = MIN_ORBIT_RADIUS;
        }
        if (r > MAX_ORBIT_RADIUS) {
            r = MAX_ORBIT_RADIUS;
        }

        orbitRadius = r;
    }

    public void setOrbitStartingAngle(float a) {

        if (a < -Math.PI) {
            a += 2 * Math.PI;
        }
        if (a > Math.PI) {
            a -= 2 * Math.PI;
        }

        orbitStartingAngle = a;
    }

    /*
    Update the orbit of this planet around a parent planet
     */
    public void updateOrbit() {
        if (orbit == null) {
            return;
        }

        double x = orbit.getX() + orbitRadius * Math.cos(orbitAngle);
        double y = orbit.getY() + orbitRadius * Math.sin(orbitAngle);

        pos.set((float) x, (float) y);

        if (orbitDir) {
            orbitAngle += orbitVelocity;
        }
        else {
            orbitAngle -= orbitVelocity;
        }

        if (orbitAngle > Math.PI) {
            orbitAngle -= 2 * Math.PI;
        }
        else if (orbitAngle < -Math.PI) {
            orbitAngle += 2 * Math.PI;
        }
    }

    private void resetOrbit() {
        orbitAngle = orbitStartingAngle;
        updateOrbit();
    }

    /* Default constructor */
    public Planet() {
        pos.set(0, 0);
        setRadius(MIN_RADIUS);

        init();
    }

    /* Orbiting constructor */
    public Planet(Planet parent, float r, float rOrbit, float aOrbit, boolean dOrbit) {
        pos.set(0, 0);
        setRadius(r);

        orbitPlanet(parent, rOrbit, aOrbit, dOrbit);

        init();
    }

    public Planet(float x, float y, float r) {

        setupPainters();

        pos.set(x, y);
        setRadius(r);

        init();
    }

    private void init() {
        setupPainters();
        setPlanetType(PlanetType.PLANET);
        updateAtmosphere();
    }

    /*
    Reset the planet to the level starting conditions
     */
    public void reset() {
        resetOrbit();
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

    public int getSimpleColor() {
        switch (planetType) {
            default:
            case PLANET:
                return Color.GREEN;
            case SUN:
                return Color.rgb(255, 175, 70);
            case REPULSAR:
                return Color.rgb(190, 250, 240);
            case BLACK_HOLE:
                return Color.rgb(115, 175, 255);
            case MOON:
                return Color.rgb(200, 200, 200);
        }
    }

    public void setPlanetType(PlanetType t) {
        planetType = t;

        pPlanet.setColor(getSimpleColor());
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

    public double getMass() {

        double mass = G * radius;

        switch (planetType) {
            default:
            case PLANET:
                break;
            case SUN:
                mass *= 1.5;
                break;
            case REPULSAR:
                mass *= -1;
                break;
            case BLACK_HOLE:
                mass *= radius;
                break;
        }

        return mass;
    }

    public double getForce(float x, float y) {

        return getMass() / distanceSquared(x, y);
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
