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
    static final float SELECTION_RADIUS_INNER = 75;
    static final float SELECTION_RADIUS_OUTER = 200;

    private boolean crashed = false;
    private boolean released = false;

    private double angle;

    // Ship attributes
    static final float MAX_SPEED = 15;
    final private float maxSpeedSquared = MAX_SPEED * MAX_SPEED;

    // Ship speed
    private PointF speed = new PointF(0, 0);
    private PointF acceleration = new PointF(0, 0);

    // Ship angular speed and acceleration
    private double aSpeed;
    private double aAcc;

    public float fuel = 0;
    private float thrust = 0;
    public boolean engineOn = false;
    static final float MAX_THRUST = 0.75f;
    //private final float THRUST_INCREMENT = MAX_THRUST / 25;
    static final float MAX_FUEL = 100;

    // Breadcrumbs
    private Vector<Breadcrumb> breadcrumbs = new Vector<Breadcrumb>();
    private boolean useBreadcrumbs = true;
    private final int MAX_BREADCRUMBS = 500;

    // Auto-rotation timer
    private double targetAngle = 0.0f;
    private int rotationTimer = 0;
    private int MAX_ROTATION_TIMER = 200;
    // Limit rotation speed
    final double MAX_ROT_SPEED = 0.025;
    private double rotationSpeed = 0.0;

    public Ship() {
        super();

        angle = 0;

        reset();
    }

    public float getFuelRatio() {
        float ratio = (float) fuel / MAX_FUEL;

        if (ratio < 0) { ratio = 0; }
        if (ratio > 1) { ratio = 1; }

        return ratio;
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
        thrust = 0;
    }

    public Ship(float x, float y, float a) {
        super(x,y);
        angle = a;

        reset();
    }

    /**
     * Force-set the angle of the ship
     * e.g. when updating launching angle
     * @param aTarget - ship angle in radians
     */
    public void setTargetAngle(double aTarget) {

        rotationTimer = MAX_ROTATION_TIMER;
        targetAngle = aTarget;
    }

    private void rotateTowards(double targetAngle) {

        double dAngle = targetAngle - angle;

        if (dAngle > Math.PI) {
            dAngle -= 2 * Math.PI;
        }

        if (dAngle < -Math.PI) {
            dAngle += 2 * Math.PI;
        }

        if (dAngle > MAX_ROT_SPEED) {
            dAngle = MAX_ROT_SPEED;
        }

        if (dAngle < -MAX_ROT_SPEED) {
            dAngle = -MAX_ROT_SPEED;
        }

        angle += dAngle;

        rotationSpeed = dAngle;
    }

    public double getAngle() { return angle; }

    public boolean hasCrashed() { return crashed; }
    public void setCrashed(boolean state) { crashed = state; }

    public boolean isReleased() { return released; }

    /**
     * Release ship into the world
     * @param vector - Starting speed vector, from ship location
     */
    public void release(PointF vector) {

        if (released) { return; }

        float x = vector.x;
        float y = vector.y;

        float d = distanceTo(x, y);

        if (d < SELECTION_RADIUS_OUTER) {
            return;
        }

        reset();

        d -= SELECTION_RADIUS_OUTER;

        if (d < 0) {
            d = 0;
        }

        //d /= 5;

        if (d > MAX_SPEED) {
            d = (float) MAX_SPEED;
        }

        angle = Math.atan2(y, x);

        speed.x = d * (float) Math.cos(angle);
        speed.y = d * (float) Math.sin(angle);

        released = true;

        // Point in the right direction
        rotationTimer = 0;
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

            if (planet.containsPoint(pos)) {
                setCrashed(true);
                break;
            }

            force = planet.getForce(pos.x, pos.y);
            af = angleTo(planet);

            ax = force * Math.cos(af);
            ay = force * Math.sin(af);

            accelerate(ax, ay);

            if (planet.pointWithinAtmosphere(pos.x, pos.y)) {
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

        if (crashed) {
            rotationSpeed = 0;
            return;
        }

        if (!released) {
            rotationSpeed = 0;
            angle = targetAngle;
            return;
        }

        // Allow a certain amount of time after user has rotated ship
        if (rotationTimer > 0) {
            rotationTimer--;
            rotateTowards(targetAngle);
        }
        else if (released) {
            double aMove = Math.atan2(speed.y, speed.x);
            rotateTowards(aMove);
        }
        else {
            rotationSpeed = 0;
        }
    }

    public void setThrust(float t) {

        if (t < 0) {
            t = 0;
        }

        if (t > MAX_THRUST) {
            t = MAX_THRUST;
        }

        thrust = t;
    }

    /**
    Apply linear thrust if the engine is on
     */
    private void applyThrust() {

        if (!engineOn || fuel <= 0) {
            thrust = 0;
            return;
        }

        float t = thrust;

        if (t < 0) {
            t = 0;
        }

        if (t > MAX_THRUST) {
            t = MAX_THRUST;
        }

        fuel -= t;

        acceleration.x += t * (float) Math.cos(angle);
        acceleration.y += t * (float) Math.sin(angle);
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

        final float BCD = Breadcrumb.BREADCRUMB_DISTANCE * Breadcrumb.BREADCRUMB_DISTANCE;

        if (!useBreadcrumbs) { return; }

        while (breadcrumbs.size() > MAX_BREADCRUMBS) {
            breadcrumbs.remove(0);
        }

        boolean dropCrumb = false;

        if (breadcrumbs.size() == 0) {
            dropCrumb = true;
        }
        else {
            PointF crumbPos = breadcrumbs.lastElement().position;

            if (distanceSquared(crumbPos.x, crumbPos.y) > BCD) {
                dropCrumb = true;
            }
        }

        if (dropCrumb) {

            Breadcrumb b = new Breadcrumb(pos, angle, thrust);

            breadcrumbs.add(b);
        }
    }

    /**
     * Draw the breadcrumbs on the canvas
     * @param canvas
     */
    public void drawBreadcrumbs(Canvas canvas) {

        // Draw breadcrumbs
        if (useBreadcrumbs) {
            for (int i=0; i<breadcrumbs.size(); i++) {

                Breadcrumb b = breadcrumbs.get(i);
                b.draw(canvas);
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

        if (!crashed &&
            released &&
            engineOn &&
            fuel > 0 &&
            thrust > 0) {

            drawThrustJet(canvas);
        }

        if (released && !crashed && Math.abs(rotationSpeed) > 0.001) {
            drawRotationJet(canvas);
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

    private void drawThrustJet(Canvas canvas) {
        Paint thrustPaint = new Paint();

        Path path = new Path();

        thrustPaint.setColor(Color.argb(150, 240, 200, 85));

        float W = 15;
        float H = 65;

        float T = 150 * thrust / MAX_THRUST;

        path.reset();
        path.moveTo(-W, T);
        path.lineTo( W, T);
        path.lineTo( 0, 0);
        path.lineTo(-W, T);

        canvas.drawPath(path, thrustPaint);
    }

    private void drawRotationJet(Canvas canvas) {
        Paint jetPaint = new Paint();

        jetPaint.setColor(Color.argb(100, 150, 220, 230));
        jetPaint.setStrokeWidth(15);

        double r = -1 * rotationSpeed * 0.5 * SELECTION_RADIUS_INNER / MAX_ROT_SPEED;

        final float yOff = -15;
        canvas.drawLine(0, yOff, (float) r, yOff, jetPaint);
    }
}
