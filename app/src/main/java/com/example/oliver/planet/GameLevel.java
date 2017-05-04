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

    public void update() {

        // Reset ship acceleration
        ship.resetAcceleration();

        // Accelerate ship towards each planet
        for (int ii=0; ii<planets.size(); ii++) {
            ship.applyPlanetForce(planets.get(ii));
        }

        // Update ship position
        ship.move();

    }
}
