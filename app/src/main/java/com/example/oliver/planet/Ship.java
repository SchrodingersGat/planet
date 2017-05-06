package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.PointF;
import java.util.Vector;

public class Ship extends GameObject {

    static final float SHIP_RADIUS = 45;

    private boolean crashed = false;
    private boolean released = false;

    private double angle;

    // Ship attributes
    static final float MAX_SPEED = 25;
    final private float maxSpeedSquared = MAX_SPEED * MAX_SPEED;

    final private float maxAngularSpeed = 0.125f;
    final private float autoRotateSpeed = 2.05f;

    // Ship speed
    private PointF speed = new PointF(0, 0);
    private PointF acceleration = new PointF(0, 0);

    // Ship angular speed and acceleration
    private double aSpeed;
    private double aAcc;

    public int fuel = 0;
    public boolean engineOn = false;
    private float thrust = 0.5f;
    static final float MAX_THRUST = 1.25f;
    private final float THRUST_INCREMENT = MAX_THRUST / 25;
    static final int MAX_FUEL = 250;

    // Breadcrumbs
    private Vector<PointF> breadcrumbs = new Vector<PointF>();
    private boolean useBreadcrumbs = true;
    private int maxBreadcrumbs = 500;

    public Ship() {
        super();

        angle = 0;

        reset();
    }

    public double getSpeed() {
        return Math.sqrt(speed.x * speed.x + speed.y * speed.y);
    }

    public boolean isEngineOn() { return engineOn; }

    public void turnEngineOn() {
        engineOn = true;
    }

    public void turnEngineOff() {
        engineOn = false;
    }

    public Ship(float x, float y, float a) {
        super(x,y);
        angle = a;

        reset();
    }

    /**
     * Force-set the angle of the ship
     * e.g. when updating launching angle
     * @param angle - ship angle in radians
     */
    public void setAngle(double angle) {
        if (!released && !crashed) {
            this.angle = angle;
        }
    }

    public boolean hasCrashed() { return crashed; }
    public void setCrashed(boolean state) { crashed = state; }

    public boolean isReleased() { return released; }

    /**
     * Release ship into the world
     * @param vector - Starting speed vector, from ship location
     */
    public void release(PointF vector) {

        if (released) { return; }

        reset();

        float x = vector.x;
        float y = vector.y;

        angle = Math.atan2(y, x);

        float d = distanceTo(x, y);

        if (d > SHIP_RADIUS) {
            d -= SHIP_RADIUS;
        }

        d /= 5;

        if (d > MAX_SPEED) {
            d = (float) MAX_SPEED;
        }

        speed.x = d * (float) Math.cos(angle);
        speed.y = d * (float) Math.sin(angle);

        released = true;
    }


    /**
     * Reset ship to defaults
     */
    public void reset() {
        speed.set(0, 0);
        acceleration.set(0, 0);
        aSpeed = 0;

        angle = 0;

        crashed = false;
        released = false;

        breadcrumbs.clear();
    }

    public void resetAcceleration() {
        acceleration.set(0, 0);
    }

    public void resetAngularAcceleration() {
        aAcc = 0;
    }

    /**
     * Apply acceleration vector
     * @param ax - acceleration in x axis
     * @param ay - acceleration in y axis
     */
    public void accelerate(double ax, double ay) {
        acceleration.x += ax;
        acceleration.y += ay;
    }

    /**
     * Reset acceleration vectors before updating ship mechanics
     */
    public void resetBeforeUpdate() {
        resetAcceleration();
        resetAngularAcceleration();
    }

    public void updatePlanets(Vector<Planet> planets) {

        Planet hitAtmosphere = null;

        double force;
        double af;
        double ax, ay;

        for (int i=0; i<planets.size(); i++) {

            Planet planet = planets.get(i);

            if (planet.containsPoint(xPos, yPos)) {
                setCrashed(true);
                break;
            }

            force = planet.getForce(xPos, yPos);
            af = angleTo(planet);

            ax = force * Math.cos(af);
            ay = force * Math.sin(af);

            accelerate(ax, ay);

            if (planet.pointWithinAtmosphere(xPos, yPos)) {
                hitAtmosphere = planet;
            }
        }

        // Ship is inside atmosphere of a planet
        if (hitAtmosphere != null) {
            //TODO
        }
    }

    /**
     * Rotate the ship towards its direction heading
     */
    public void autoRotate() {
        double aMove = Math.atan2(speed.y, speed.x);

        // TODO - Perhaps more complex behavoir here...
        angle = aMove;
    }

    /**
    Apply linear thrust if the engine is on
     */
    public void applyThrust() {

        if (!engineOn || fuel == 0) {
            thrust = 0;
            return;
        }

        if (thrust < MAX_THRUST) {
            thrust += THRUST_INCREMENT;
        }

        if (thrust > MAX_THRUST) {
            thrust = MAX_THRUST;
        }

        fuel--;

        acceleration.x += thrust * (float) Math.cos(angle);
        acceleration.y += thrust * (float) Math.sin(angle);
    }

    public void move() {

        applyThrust();

        // Apply linear acceleration
        speed.x += acceleration.x;
        speed.y += acceleration.y;

        double ss = (speed.x * speed.x) + (speed.y * speed.y);

        // Test if the speed needs to be clipped
        if (ss > maxSpeedSquared) {
            double sAngle = Math.atan2(speed.y, speed.x);

            speed.x = MAX_SPEED * (float) Math.cos(sAngle);
            speed.y = MAX_SPEED * (float) Math.sin(sAngle);
        }

        setPos(getX() + speed.x, getY() + (float) speed.y);

        // Point nose of ship in the direction it is moving
        autoRotate();

        dropBreadcrumb();
    }

    /**
     * Leave a single breadcrumb on the map
     */
    public void dropBreadcrumb() {

        if (!useBreadcrumbs) { return; }

        while (breadcrumbs.size() > maxBreadcrumbs) {
            breadcrumbs.remove(0);
        }

        if (breadcrumbs.size() == 0) {
            breadcrumbs.add(new PointF(xPos, yPos));
        }
        else {
            PointF crumb = breadcrumbs.lastElement();

            if (distanceSquared(crumb.x, crumb.y) > 1000) {
                breadcrumbs.add(new PointF(xPos, yPos));
            }
        }
    }

    /**
     * Draw the breadcrumbs on the canvas
     * @param canvas
     */
    public void drawBreadcrumbs(Canvas canvas) {

        Paint crumbPaint = new Paint();
        crumbPaint.setColor(Color.RED);
        crumbPaint.setStrokeWidth(5);
        PointF crumb;

        // Draw breadcrumbs
        if (useBreadcrumbs) {
            for (int i=0; i<breadcrumbs.size(); i++) {
                crumb = breadcrumbs.get(i);

                canvas.drawPoint(crumb.x, crumb.y, crumbPaint);
                //canvas.drawCircle(crumb.x, crumb.y, 3, crumbPaint);
            }
        }
    }

    /**
     * Draw the ship on the supplied canvas
     * @param canvas
     */
    public void draw(Canvas canvas) {
        canvas.save();

        canvas.translate(getX(), getY());
        canvas.rotate((float) (angle * 180 / Math.PI) + 90);

        Paint p = new Paint();

        Path path = new Path();

        float W = 15;
        float H = 65;

        // Thrust size
        float T = 5 * W * thrust / MAX_THRUST;

        if (!crashed && released && engineOn && fuel > 0) {
            p.setColor(Color.argb(150, 240, 200, 85));
            path.reset();
            path.moveTo(-W, T);
            path.lineTo( W, T);
            path.lineTo( 0, 0);
            path.lineTo(-W, T);

            canvas.drawPath(path, p);
        }

        p.setColor(Color.RED);

        path.reset();
        path.moveTo(-W, W);
        path.lineTo( W, W);
        path.lineTo( 0, W-H);
        path.lineTo(-W, W);

        canvas.drawPath(path, p);

        canvas.restore();
    }
}
