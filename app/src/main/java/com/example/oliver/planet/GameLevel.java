package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.PointF;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

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

        // All good levels should have a ship
        ship = new Ship();

        // Default position for end-zone
        endZone = new Galaxy(0, 500);
    }

    /* Clear all game objects from the level
     */
    private void clear() {
        planets.clear();
        stars.clear();
        wormholes.clear();
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

    // Remove a planet from the level
    // Also remove any 'moons' of that planet
    public void removePlanet(Planet p) {

        int idx = 0;

        while (idx < planets.size()) {
            if (planets.get(idx) == p) {
                planets.remove(idx);
                continue;
            }

            if (planets.get(idx).getOrbitParent() == p) {
                planets.remove(idx);
                continue;
            }

            idx++;
        }

        for (int i=0; i<planets.size(); i++) {
            if (planets.get(i) == p) {
                planets.remove(i);
                break;
            }
        }
    }

    // Remove a star from the level
    public void removeStar(Star s) {
        for (int i=0; i<stars.size(); i++) {
            if (stars.get(i) == s) {
                stars.remove(i);
                break;
            }
        }
    }

    public Planet testPlanetHit(float x, float y) {
        // Test each planet
        for (int p=0; p<planets.size(); p++) {
            if (planets.get(p).containsPoint(x, y)) {
                return planets.get(p);
            }
        }

        return null;
    }

    public Star testStarHit(float x, float y) {
        // Test each star
        for (int s=0; s<stars.size(); s++) {
            if (stars.get(s).testCollection(x, y)) {
                return stars.get(s);
            }
        }

        return null;
    }

    public Wormhole testWormholeHit(float x, float y) {
        // Test wormholes
        for (int w=0; w<wormholes.size(); w++) {
            if (wormholes.get(w).wormholeA.containsPoint(x, y)) {
                return wormholes.get(w).wormholeA;
            }
            if (wormholes.get(w).wormholeB.containsPoint(x, y)) {
                return wormholes.get(w).wormholeB;
            }
        }

        return null;
    }

    public boolean testEndzoneHit(float x, float y) {
        return endZone.containsPoint(x, y);
    }

    public StellarObject testStellarObjectHit(float x, float y) {

        // Planet hit?
        Planet p = testPlanetHit(x, y);

        if (p != null) {
            return p;
        }

        // Star hit?
        Star s = testStarHit(x, y);

        if (s != null) {
            return s;
        }

        // Wormhole hit?
        Wormhole w = testWormholeHit(x, y);

        if (w != null) {
            return w;
        }

        // Test end-soze
        if (endZone.containsPoint(x, y)) {
            return endZone;
        }

        // No result
        return null;
    }

    /*
    JSON keys
     */
    private final String KEY_PLANET_ARRAY = "Planets";
    private final String KEY_STAR_ARRAY = "Stars";
    private final String KEY_WORMHOLE_ARRAY = "Wormhole";
    private final String KEY_ENDZONE = "Endzone";

    public boolean encodeToJson(JSONObject json) {

        try {
            JSONObject jo;
            JSONArray  ja;

            // Add the planets
            ja = new JSONArray();
            for (int i=0; i<planets.size(); i++) {
                jo = new JSONObject();
                planets.get(i).encodeToJson(jo);

                ja.put(jo);
            }
            json.put(KEY_PLANET_ARRAY, ja);

            // Add the stars
            ja = new JSONArray();
            for (int i=0; i<stars.size(); i++) {
                jo = new JSONObject();
                stars.get(i).encodeToJson(jo);

                ja.put(jo);
            }
            json.put(KEY_STAR_ARRAY, ja);

            // Add the wormholes
            ja = new JSONArray();
            for (int i=0; i<wormholes.size(); i++) {
                jo = new JSONObject();
                wormholes.get(i).encodeToJson(jo);

                ja.put(jo);
            }
            json.put(KEY_WORMHOLE_ARRAY, ja);

            // Add the endpoint location
            jo = new JSONObject();
            endZone.encodeToJson(jo);

            json.put(KEY_ENDZONE, jo);
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }

    public boolean decodeFromJson(JSONObject json) {

        clear();

        try {
            JSONArray ja;
            JSONObject jo;
            /*
            Load planets
             */
            if (json.has(KEY_PLANET_ARRAY) && !json.isNull(KEY_PLANET_ARRAY)) {
                ja = json.getJSONArray(KEY_PLANET_ARRAY);

                for (int i=0; i<ja.length(); i++) {
                    if (ja.isNull(i)) {
                        continue;
                    }

                    jo = ja.getJSONObject(i);

                    Planet p = new Planet();

                    if (p.decodeFromJson(json)) {
                        planets.add(p);
                    }
                }
            }

            /*
            Load stars
             */
            if (json.has(KEY_STAR_ARRAY) && !json.isNull(KEY_STAR_ARRAY)) {
                ja = json.getJSONArray(KEY_STAR_ARRAY);

                for (int i=0; i<ja.length(); i++) {
                    if (ja.isNull(i)) {
                        continue;
                    }

                    jo = ja.getJSONObject(i);

                    Star s = new Star();

                    if (s.decodeFromJson(jo)) {
                        stars.add(s);
                    }
                }
            }
        }
        catch (JSONException e) {
            return false;
        }

        return true;
    }

}
