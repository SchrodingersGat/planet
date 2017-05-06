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
    static final double MAX_SPEED = 25;
    final private double maxSpeedSquared = MAX_SPEED * MAX_SPEED;

    final private double maxAngularSpeed = 0.125;
    final private double autoRotateSpeed = 2.05;

    // Ship speed
    private double xSpeed;
    private double ySpeed;

    // Ship acceleration
    private double xAcc;
    private double yAcc;

    // Ship angular speed and acceleration
    private double aSpeed;
    private double aAcc;

    public int fuel = 0;
    public boolean engineOn = false;
    public final float THRUST = 0.5f;

    // Breadcrumbs
    private Vector<PointF> breadcrumbs;
    private boolean useBreadcrumbs = true;
    private int maxBreadcrumbs = 500;

    public Ship() {
        super();

        angle = 0;

        init();
    }

    public void turnEngineOn() {
        engineOn = true;
    }

    public void turnEngineOff() {
        engineOn = false;
    }

    public Ship(float x, float y, float a) {
        super(x,y);
        angle = a;

        init();
    }

    public void setAngle(double angle) {
        if (!released && !crashed) {
            this.angle = angle;
        }
    }

    public boolean hasCrashed() { return crashed; }
    public void setCrashed(boolean state) { crashed = state; }

    public boolean isReleased() { return released; }

    public void release(PointF vector) {

        if (released) { return; }

        reset();

        float x = vector.x;
        float y = vector.y;

        double a = Math.atan2(y, x);

        float d = distanceTo(x, y);

        if (d > SHIP_RADIUS) {
            d -= SHIP_RADIUS;
        }

        d /= 5;

        if (d > MAX_SPEED) {
            d = (float) MAX_SPEED;
        }

        xSpeed = d * (float) Math.cos(a);
        ySpeed = d * (float) Math.sin(a);

        released = true;
    }

    private void init() {

        breadcrumbs = new Vector<PointF>();

        reset();
    }

    public void reset() {
        xSpeed = 0;
        ySpeed = 0;
        aSpeed = 0;

        xAcc = 0;
        yAcc = 0;
        yAcc = 0;

        angle = 0;

        crashed = false;
        released = false;

        breadcrumbs.clear();
    }

    public void resetAcceleration() {
        xAcc = 0;
        yAcc = 0;
    }

    public void resetAngularAcceleration() {
        aAcc = 0;
    }

    public void accelerate(double ax, double ay) {
        xAcc += ax;
        yAcc += ay;
    }

    public void resetBeforeUpdate() {
        resetAcceleration();
        resetAngularAcceleration();
    }

    public void applyPlanetForce(Planet planet) {
        double force = planet.getForce(getX(), getY());

        double a = this.angleTo(planet);

        double ax = force * Math.cos(a);
        double ay = force * Math.sin(a);

        accelerate(ax, ay);
    }

    // Rotate the ship towards the direction it is moving
    public void autoRotate() {
        double aMove = Math.atan2(ySpeed, xSpeed);

        angle = aMove;

        // TODO
    }

    public void move() {

        //TODO - remove this
        engineOn = true;

        // Apply thrust if engine on
        if (engineOn && fuel > 0) {
            fuel--;

            xAcc += THRUST * (float) Math.cos(angle);
            yAcc += THRUST * (float) Math.sin(angle);
        }

        // Apply linear acceleration
        xSpeed += xAcc;
        ySpeed += yAcc;

        double ss = xSpeed * xSpeed + ySpeed * ySpeed;

        // Test if the speed needs to be clipped
        if (ss > maxSpeedSquared) {
            double sAngle = Math.atan2(ySpeed, xSpeed);

            xSpeed = MAX_SPEED * Math.cos(sAngle);
            ySpeed = MAX_SPEED * Math.sin(sAngle);
        }

        setPos(getX() + (float) xSpeed, getY() + (float) ySpeed);

        // Point nose of ship in the direction it is moving
        autoRotate();

        /*
        // Apply angular acceleration
        aSpeed += aAcc;

        // Clip angular speed
        if (Math.abs(aSpeed) > maxAngularSpeed) {

            if (aSpeed < 0) {
                aSpeed = -maxAngularSpeed;
            }
            else
            {
                aSpeed = maxAngularSpeed;
            }
        }

        angle += aSpeed;

        angle = Math.min(-Math.PI, angle);
        angle = Math.max( Math.PI, angle);
        */

        dropBreadcrumb();
    }

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

            if (distanceTo(crumb.x, crumb.y) > 25) {
                breadcrumbs.add(new PointF(xPos, yPos));
            }
        }
    }

    public void drawBreadcrumbs(Canvas canvas) {

        Paint crumbPaint = new Paint();
        crumbPaint.setColor(Color.RED);
        PointF crumb;

        // Draw breadcrumbs
        if (useBreadcrumbs) {
            for (int i=0; i<breadcrumbs.size(); i++) {
                crumb = breadcrumbs.get(i);

                canvas.drawCircle(crumb.x, crumb.y, 3, crumbPaint);
            }
        }
    }

    public void draw(Canvas canvas) {
        canvas.save();

        canvas.translate(getX(), getY());
        canvas.rotate((float) (angle * 180 / Math.PI) + 90);

        Paint p = new Paint();

        Path path = new Path();

        float W = 15;
        float H = 65;

        if (!crashed && released && engineOn && fuel > 0) {
            p.setColor(Color.argb(150, 240, 200, 85));
            path.reset();
            path.moveTo(-W, 5 * W);
            path.lineTo( W, 5 * W);
            path.lineTo( 0, 0);
            path.lineTo(-W, 5 * W);

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
