package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;

import java.util.Vector;

public class Ship extends GameObject {

    static final float SHIP_RADIUS = 45;
    static final float SELECTION_RADIUS_INNER = 75;
    static final float SELECTION_RADIUS_OUTER = 200;

    private boolean crashed = false;
    private boolean released = false;

    private double angle;

    public PointF releasePoint;

    // Ship attributes
    static final float MAX_SPEED = 12.5f;
    final private float maxSpeedSquared = MAX_SPEED * MAX_SPEED;

    // Ship speed
    private PointF speed = new PointF(0, 0);
    private PointF acceleration = new PointF(0, 0);

    // Ship angular speed and acceleration
    private double aSpeed;
    private double aAcc;

    public float fuel = 0;
    private float thrust = 0;
    static final float MAX_THRUST = 0.35f;
    static final float MIN_THRUST = 0.01f;
    //private final float THRUST_INCREMENT = MAX_THRUST / 25;
    static final float MAX_FUEL = 10000;
    static final float SUN_FUEL_RECHARGE = 0.5f;

    // Breadcrumbs
    private Vector<Breadcrumb> breadcrumbs = new Vector<Breadcrumb>();
    private Vector<Vector<Breadcrumb>> breadcrumbHistory = new Vector<Vector<Breadcrumb>>();
    private boolean useBreadcrumbs = true;
    private final int MAX_BREADCRUMBS = 500;
    private final int BREADCRUMB_HISTORY = 3;

    // Move history
    private Vector<GameIncrement> moveHistory = new Vector<GameIncrement>();
    private GameIncrement nextMove = null;
    private int gameStep = 0;

    // Auto-rotation timer
    private double targetAngle = 0.0f;
    private int rotationTimer = 0;
    private int MAX_ROTATION_TIMER = 200;
    // Limit rotation speed
    final double MAX_ROT_SPEED = 0.025;
    private double rotationSpeed = 0.0;

    /* Painters */
    private Paint pShip;
    private Paint pThrust;
    private Paint pRotThrust;
    private Paint pCrumb;

    void setupPainters() {
        pCrumb = new Paint();
        pCrumb.setStrokeWidth(3);

        pShip = new Paint();
        pShip.setColor(Color.RED);
        pShip.setStyle(Paint.Style.FILL_AND_STROKE);

        pThrust = new Paint();
        pThrust.setARGB(150, 240, 200, 85);
        pThrust.setStyle(Paint.Style.FILL_AND_STROKE);

        pRotThrust = new Paint();
        pRotThrust.setARGB(100, 150, 220, 230);
        pRotThrust.setStyle(Paint.Style.FILL_AND_STROKE);
        pRotThrust.setStrokeWidth(15);

    }

    public Ship() {
        super();

        setupPainters();

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

        while (aTarget > Math.PI) {
            aTarget -= 2 * Math.PI;
        }

        while (aTarget < -Math.PI) {
            aTarget += 2 * Math.PI;
        }

        final double ANGLE_SEG = 15.0f * Math.PI / 180;

        int a = (int) (aTarget / ANGLE_SEG);

        aTarget = (double) a * ANGLE_SEG;

        double delta = aTarget - targetAngle;

        while (delta > Math.PI) {
            delta -= 2 * Math.PI;
        }

        while (delta < -Math.PI) {
            delta += 2 * Math.PI;
        }

        if (Math.abs(delta) > 0.001) {
            targetAngle = aTarget;

            if (!isReleased()) {
                angle = targetAngle;
            }

            nextMove.setAngle(targetAngle);
        }
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

        // Split into segments
        final int LAUNCH_SEG = 10;

        d /= MAX_SPEED;
        d *= LAUNCH_SEG;

        d = ((int) (d + 0.5)) / LAUNCH_SEG;
        d *= MAX_SPEED;

        if (d < 0) {
            d = 0;
        }

        if (d > MAX_SPEED) {
            d = MAX_SPEED;
        }

        setTargetAngle(Math.atan2(y, x));
        angle = Math.atan2(y, x);

        // Add the first movement history
        nextMove.setAngle(angle);
        nextMove.setThrust(d);

        increment();

        speed.x = d * (float) Math.cos(angle);
        speed.y = d * (float) Math.sin(angle);

        released = true;

        // Point in the right direction
        rotationTimer = 0;

        releasePoint = vector;
    }

    // Update breadcrumb history
    void transferBreadcrumbs() {

        if (breadcrumbs.size() > 0) {
            Vector<Breadcrumb> history = new Vector<Breadcrumb>();

            for (int i=0; i<breadcrumbs.size(); i++) {
                history.add(breadcrumbs.get(i));
            }

            breadcrumbHistory.add(history);

            if (breadcrumbHistory.size() > BREADCRUMB_HISTORY) {
                breadcrumbHistory.remove(0);
            }

            breadcrumbs.clear();
        }

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

        transferBreadcrumbs();

        Log.i("moves", Integer.toString(moveHistory.size()));

        moveHistory.clear();
        gameStep = 0;
        nextMove = new GameIncrement(0);
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

    /*
    Apply the force towards given planet
     */
    public void applyPlanetForce(Planet planet) {

        double force;
        double af, ax, ay;

        force = planet.getForce(pos.x, pos.y);
        af = angleTo(planet);

        ax = force * Math.cos(af);
        ay = force * Math.sin(af);

        accelerate(ax, ay);

        if (planet.getPlanetType() == Planet.PlanetType.SUN) {

            float a = planet.getTotalRadius();

            if (planet.distanceSquared(pos.x, pos.y) < (a * a)) {

                fuel += SUN_FUEL_RECHARGE;

                if (fuel > MAX_FUEL) {
                    fuel = MAX_FUEL;
                }
            }
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

        final int THRUST_STEPS = 5;

        // Limit to set number of thrust increments
        float r = t / MAX_THRUST;
        r *= THRUST_STEPS;

        r = ((int) (r + 0.5));

        t = (float) r / THRUST_STEPS * MAX_THRUST;

        if (t <= MIN_THRUST) {
            t = 0;
        }

        if (t > MAX_THRUST) {
            t = MAX_THRUST;
        }

        // Ignore if the new value is the same
        if (Math.abs(t - thrust) < 0.0001) {
            return;
        }

        thrust = t;

        nextMove.setThrust(thrust);
    }

    private boolean isThrusting() {
        return (thrust > MIN_THRUST) && (fuel > 0);
    }

    /**
    Apply linear thrust if the engine is on
     */
    private void applyThrust() {

        if (!isThrusting()) {
            return;
        }

        float t = thrust;

        if (t <= MIN_THRUST) {
            t = 0;
        }

        if (t > MAX_THRUST) {
            t = MAX_THRUST;
        }

        fuel -= t;

        acceleration.x += t * (float) Math.cos(angle);
        acceleration.y += t * (float) Math.sin(angle);
    }

    private void increment() {
        if (nextMove == null) {

        }
        else if (nextMove.hasThrust || nextMove.hasAngle) {
            moveHistory.add(nextMove);
        }

        gameStep++;

        nextMove = new GameIncrement(gameStep);
    }

    // Break from recorded course
    public void newCourse() {

    }

    public void move() {

        increment();

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

        if (!useBreadcrumbs) {
            return;
        }

        float N = 200.0f / BREADCRUMB_HISTORY;

        int n = breadcrumbHistory.size();

        int alpha = 0;

        // Draw history
        for (int i=0; i<n; i++) {

            alpha = 200 - (int) (N * i);

            drawBreadcrumbList(
                    breadcrumbHistory.get(n-1-i),
                    Color.argb(alpha, 230, 200, 0),
                    canvas);
        }

        drawBreadcrumbList(breadcrumbs, Color.argb(255, 255, 0, 0), canvas);
    }

    public void drawBreadcrumbList(Vector<Breadcrumb> crumbs, int color, Canvas canvas) {

        pCrumb.setColor(color);

        for (int i=0; i<crumbs.size(); i++) {
            crumbs.get(i).draw(canvas, pCrumb);
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

        Path path = new Path();

        float W = 15;
        float H = 65;

        // Thrust size
        float T = 5 * W * thrust / MAX_THRUST;

        if (!crashed &&
            released &&
            isThrusting()) {

            drawThrustJet(canvas);
        }

        if (released && !crashed && Math.abs(rotationSpeed) > 0.001) {
            drawRotationJet(canvas);
        }

        path.reset();
        path.moveTo(-W, W);
        path.lineTo( W, W);
        path.lineTo( 0, W-H);
        path.lineTo(-W, W);

        canvas.drawPath(path, pShip);

        canvas.restore();
    }

    private void drawThrustJet(Canvas canvas) {

        Path path = new Path();

        float W = 15;
        float H = 65;

        float T = 150 * thrust / MAX_THRUST;

        path.reset();
        path.moveTo(-W, T);
        path.lineTo( W, T);
        path.lineTo( 0, 0);
        path.lineTo(-W, T);

        canvas.drawPath(path, pThrust);
    }

    private void drawRotationJet(Canvas canvas) {

        double r = -1 * rotationSpeed * 0.5 * SELECTION_RADIUS_INNER / MAX_ROT_SPEED;

        final float yOff = -15;
        canvas.drawLine(0, yOff, (float) r, yOff, pRotThrust);
    }
}
