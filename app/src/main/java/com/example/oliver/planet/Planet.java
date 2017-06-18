package com.example.oliver.planet;

/**
 * Created by Oliver on 5/3/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.RadialGradient;
import android.graphics.Shader;

import org.json.JSONException;
import org.json.JSONObject;

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
    private final float G = 350.0f;

    /* Painters */
    private Paint pPlanet = new Paint();
    private Paint pAtmosphere = new Paint();
    private Paint pOrbit = new Paint();
    private RadialGradient rgAtmosphere;

    private void setupPainters() {
        pPlanet.setStyle(Paint.Style.FILL_AND_STROKE);

        pOrbit.setStyle(Paint.Style.STROKE);
        pOrbit.setStrokeWidth(2);
        pOrbit.setARGB(150, 150, 150, 150);
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

    public Planet getOrbitParent() {
        return orbit;
    }

    public void orbitPlanet(Planet p) {

        orbit = p;

        if (orbit != null) {
            initializeOrbit();
        }
    }

    private void initializeOrbit() {

        if (orbit == null) {
            return;
        }

        double dx = pos.x - orbit.getX();
        double dy = pos.y - orbit.getY();

        double angle = Math.atan2(dy, dx);

        double distance = orbit.distanceTo(pos.x, pos.y);

        if (distance < MIN_ORBIT_RADIUS) {
            distance = MIN_ORBIT_RADIUS;
        }
        else if (distance > MAX_ORBIT_RADIUS) {
            distance = MAX_ORBIT_RADIUS;
        }

        // Calculate the orbit velocity
        float W = (float) Math.sqrt(G * orbit.getMass() / Math.pow(distance, 2));
        W /= 2500;

        orbitVelocity = W;
        orbitStartingAngle = (float) angle;
        orbitRadius = (float) distance;

        // Finally, reset the position of this planet
        double x = orbit.getX() + distance * Math.cos(angle);
        double y = orbit.getY() + distance * Math.sin(angle);

        pos.x = (float) x;
        pos.y = (float) y;

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

    public void resetOrbit() {

        if (orbit == null) {
            return;
        }

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
    public Planet(Planet parent, float x, float y, float r) {

        setPos(x, y);
        setRadius(r);

        orbitPlanet(parent);
        //initializeOrbit();

        init();

        // Override planet type
        setPlanetType(PlanetType.MOON);
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

    @Override
    public void setPos(float x, float y) {

        super.setPos(x, y);

        if (orbit != null) {
            orbitPlanet(orbit);
        }

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
                Color.argb(100, r, g, b),
                Color.argb(50, r, g, b),
                Color.argb(0, r, g, b)
        };

        float R = radius + atmosphere;

        if (R < 50) {
            R = 50;
        }

        float[] stops = {
                radius / R,
                (R - radius) / R,
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

    public float getMinAllowableRadius() {
        switch (planetType) {
            default:
                return MIN_RADIUS;
            case PLANET:
                return MIN_PLANET_RADIUS;
            case SUN:
                return MIN_SUN_RADIUS;
            case REPULSAR:
                return MIN_REPULSAR_RADIUS;
            case BLACK_HOLE:
                return MIN_BLACKHOLE_RADIUS;
            case MOON:
                return MIN_MOON_RADIUS;
        }
    }

    public float getMaxAllowableRadius() {
        switch (planetType) {
            default:
                return MAX_RADIUS;
            case PLANET:
                return MAX_PLANET_RADIUS;
            case SUN:
                return MAX_SUN_RADIUS;
            case REPULSAR:
                return MAX_REPULSAR_RADIUS;
            case BLACK_HOLE:
                return MAX_BLACKHOLE_RADIUS;
            case MOON:
                return MAX_MOON_RADIUS;
        }
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

    /*
    Return the mass of the planet.
    Mass is used to calculate force exerted on the ship.
    It is also used for calculating orbital periods.
     */
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

    /*
    Get the force that this planet exerts on the given point
     */
    public double getForce(float x, float y) {

        return getMass() / distanceSquared(x, y);
    }

    public void drawOrbit(Canvas canvas) {

        if (orbit == null) {
            return;
        }
        canvas.drawCircle(orbit.getX(), orbit.getY(), orbitRadius, pOrbit);
        canvas.drawLine(pos.x, pos.y, orbit.getX(), orbit.getY(), pOrbit);

    }

    public void draw(Canvas canvas) {

        // Draw atmosphere for SUN type planet
        if (planetType == PlanetType.SUN && atmosphere > 0) {
            canvas.drawCircle(pos.x, pos.y, radius + atmosphere, pAtmosphere);
        }

        canvas.drawCircle(pos.x, pos.y, radius, pPlanet);
    }

    @Override
    public void setRadius(float r) {
        super.setRadius(r);

        updateAtmosphere();
    }

    /*
    JSON keys
     */

    private final String KEY_PLANET_TYPE = "planetType";
    private final String KEY_ATMOSPHERE = "atmosphere";

    @Override
    public boolean encodeToJson(JSONObject json) {
        if (!super.encodeToJson(json)) {
            return false;
        }

        try {
            json.put(KEY_PLANET_TYPE, planetType.toString());

            if (atmosphere > 0) {
                json.put(KEY_ATMOSPHERE, (double) atmosphere);
            }
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }

    @Override
    public boolean decodeFromJson(JSONObject json) {
        if (!super.decodeFromJson(json)) {
            return false;
        }

        try {

            PlanetType pt = PlanetType.PLANET;

            if (json.has(KEY_PLANET_TYPE) && !json.isNull(KEY_PLANET_TYPE)) {
                pt = PlanetType.valueOf(json.getString(KEY_PLANET_TYPE));
            }

            if (json.has(KEY_ATMOSPHERE) && !json.isNull(KEY_ATMOSPHERE)) {
                float a = (float) json.getDouble(KEY_ATMOSPHERE);

                setAtmosphere(a);
            }
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }
}
