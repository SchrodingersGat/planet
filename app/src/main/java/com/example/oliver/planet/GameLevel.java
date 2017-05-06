package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Canvas;
import java.util.Vector;

public class GameLevel {

    public Vector<Planet> planets;
    public Vector<Star> stars;

    public Ship ship;

    public GameLevel() {
        planets = new Vector<Planet>();
        stars = new Vector<Star>();

        ship = new Ship();
    }

    /*
    Reset the level to default conditions
     */
    public void reset() {

        ship.reset();

        for (int i=0; i<stars.size(); i++) {
            stars.get(i).setCollected(false);
        }

        ship.fuel = Ship.MAX_FUEL;
    }

    public void update() {

        if (ship.isReleased() && !ship.hasCrashed()) {

            // Reset ship acceleration
            ship.resetBeforeUpdate();

            ship.updatePlanets(planets);

            if (!ship.hasCrashed()) {

                // Test each star for hit
                for (int jj = 0; jj < stars.size(); jj++) {
                    Star star = stars.get(jj);

                    if (star.isCollected()) {
                        continue;
                    }

                    if (star.containsPoint(ship.getX(), ship.getY())) {
                        star.setCollected(true);
                    }
                }

                // Update ship position
                ship.move();
            }
        }

    }

    public StellarObject testStellarObjectHit(float x, float y) {

        // Test each planet
        for (int p=0; p<planets.size(); p++) {
            if (planets.get(p).containsPoint(x,y)) {
                return planets.get(p);
            }
        }

        // Test each star
        for (int s=0; s<stars.size(); s++) {
            if (stars.get(s).containsPoint(x,y)) {
                return stars.get(s);
            }
        }

        // No result
        return null;
    }
}
