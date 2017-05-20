package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.PointF;
import java.util.Vector;

public class GameLevel {

    /*
    Game States
    */
    static final int STATE_INIT = 0;
    static final int STATE_RUNNING = 1;
    static final int STATE_PAUSED = 2;
    static final int STATE_CRASHED = 3;
    static final int STATE_OUT_OF_BOUNDS = 5;
    static final int STATE_WIN = 10;

    static final float LEVEL_BOUNDS = 5000;

    public Vector<WormholePair> wormholes;

    /*
    Vector for storing the various planets. Planets come in multiple flavours:
        Planet (normal)
        Black hole (strong gravity)
        Repulsar (pushy)
        Moon (orbits a planet)
        Sun (planets can orbit this)
     */
    public Vector<Planet> planets;

    /*
    Stars are to be collected by the user
     */
    public Vector<Star> stars;

    public Galaxy endZone;

    public Ship ship;

    public PointF shipStartingPosition = new PointF(0, 0);

    public GameLevel() {
        planets = new Vector<Planet>();
        stars = new Vector<Star>();
        wormholes = new Vector<WormholePair>();

        ship = new Ship();

        endZone = new Galaxy(0, 0);
    }

    /*
    Reset the level to default conditions
     */
    public void reset() {

        ship.reset();
        ship.setPos(shipStartingPosition.x, shipStartingPosition.y);

        for (int i=0; i<stars.size(); i++) {
            stars.get(i).setCollected(false);
        }

        for (int i=0; i<planets.size(); i++) {
            planets.get(i).reset();
        }

        ship.fuel = Ship.MAX_FUEL;
    }

    public void update() {

        Planet p;

        if (ship.isReleased() && !ship.hasCrashed()) {

            // Reset ship acceleration
            ship.resetBeforeUpdate();

            for (int i=0; i<wormholes.size(); i++) {
                wormholes.get(i).updateShip(ship);
            }

            // Accelerate towards each planet
            for (int i=0; i<planets.size(); i++) {
                p = planets.get(i);

                p.updateOrbit();
                ship.applyPlanetForce(p);

                if (p.containsPoint(ship.getPos())) {
                    ship.setCrashed(true);
                }
            }

            // Accelerate towards the end zone
            ship.applyPlanetForce(endZone);

            if (!ship.hasCrashed()) {

                // Test each star for hit
                for (int jj = 0; jj < stars.size(); jj++) {
                    Star star = stars.get(jj);

                    if (star.isCollected()) {
                        continue;
                    }

                    if (star.testCollection(ship.getX(), ship.getY())) {
                        star.setCollected(true);
                    }
                }

                // Update ship position
                ship.move();
            }
        }

        if (!ship.hasCrashed()) {
            ship.autoRotate();
        }

    }

    public StellarObject testStellarObjectHit(float x, float y) {

        // Test each planet
        for (int p=0; p<planets.size(); p++) {
            if (planets.get(p).containsPoint(x, y)) {
                return planets.get(p);
            }
        }

        // Test each star
        for (int s=0; s<stars.size(); s++) {
            if (stars.get(s).containsPoint(x, y)) {
                return stars.get(s);
            }
        }

        // Test wormholes
        for (int w=0; w<wormholes.size(); w++) {
            if (wormholes.get(w).wormholeA.containsPoint(x, y)) {
                return wormholes.get(w).wormholeA;
            }
            if (wormholes.get(w).wormholeB.containsPoint(x, y)) {
                return wormholes.get(w).wormholeB;
            }
        }

        // Test end-soze
        if (endZone.containsPoint(x, y)) {
            return endZone;
        }

        // No result
        return null;
    }


}
