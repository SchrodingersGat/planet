package com.example.oliver.planet;

/**
 * Created by Oliver on 5/17/2017.
 */

public class Moon extends Planet {

    static final float MIN_ORBIT_RADIUS = 250;
    static final float MAX_ORBIT_RADIUS = 2500;

    static final float MIN_ORBIT_SPEED = 0.0001f;
    static final float MAX_ORBIT_SPEED = 0.0250f;

    // Planet that the moon orbits
    private Planet parentPlanet;
    private float orbitRadius = 250.0f;
    private float orbitSpeed = 0.025f;
    private double orbitAngle = 0;
    private double startingAngle = 0.0f;

    public Moon(Planet planet, float mRadius, double angle, float oRadius, float oSpeed) {
        parentPlanet = planet;

        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }

        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }

        orbitAngle = angle;
        startingAngle = angle;

        if (oRadius < MIN_ORBIT_RADIUS) {
            oRadius = MIN_ORBIT_RADIUS;
        }
        if (oRadius > MAX_ORBIT_RADIUS) {
            oRadius = MAX_ORBIT_RADIUS;
        }

        orbitRadius = oRadius;

        if (Math.abs(oSpeed) < MIN_ORBIT_SPEED) {
            if (oSpeed < 0) {
                oSpeed = -MIN_ORBIT_SPEED;
            }
            else {
                oSpeed = MIN_ORBIT_SPEED;
            }
        }
        else if (Math.abs(oSpeed) > MAX_ORBIT_SPEED) {
            if (oSpeed < 0) {
                oSpeed = -MAX_ORBIT_SPEED;
            }
            else {
                oSpeed = MAX_ORBIT_SPEED;
            }
        }

        orbitSpeed = oSpeed;
        setRadius(mRadius);

        updateOrbit();
    }

    public void resetOrbit() {
        orbitAngle = startingAngle;
        updateOrbit();
    }

    public Planet getParentPlanet() {
        return parentPlanet;
    }

    public float getOrbitRadius(){
        return orbitRadius;
    }

    public float getMinOrbitSpeed() {
        return orbitSpeed;
    }

    public void updateOrbit() {
        if (parentPlanet == null) {
            setPos(0, 0);
            return;
        }

        double x = parentPlanet.getX() + orbitRadius * Math.cos(orbitAngle);
        double y = parentPlanet.getY() + orbitRadius * Math.sin(orbitAngle);

        pos.set((float) x, (float) y);

        orbitAngle += orbitSpeed;

        if (orbitAngle > Math.PI) {
            orbitAngle -= 2 * Math.PI;
        }
        else if (orbitAngle < -Math.PI) {
            orbitAngle += 2 * Math.PI;
        }
    }

}