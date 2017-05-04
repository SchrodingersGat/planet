package com.example.oliver.planet;

/**
 * Created by Oliver on 5/4/2017.
 */

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Canvas;

public class Ship extends GameObject {

    private float angle;

    // Ship attributes
    final private float maxSpeed = 25;
    final private float maxSpeedSquared = maxSpeed * maxSpeed;

    // Ship speed
    private double xSpeed;
    private double ySpeed;

    // Ship acceleration
    private double xAcc;
    private double yAcc;

    public Ship() {
        super();

        angle = 0;

        reset();
    }

    public Ship(float x, float y, float a) {
        super(x,y);
        angle = a;

        reset();
    }

    public void reset() {
        xSpeed = 0;
        ySpeed = 0;
        xAcc = 0;
        yAcc = 0;
    }

    public void resetAcceleration() {
        xAcc = 0;
        yAcc = 0;
    }

    public void accelerate(double ax, double ay) {
        xAcc += ax;
        yAcc += ay;
    }

    public void applyPlanetForce(Planet planet) {
        double force = planet.getInfluence(getX(), getY());

        double a = this.angleTo(planet);

        double ax = force * Math.cos(a);
        double ay = force * Math.sin(a);

        accelerate(ax, ay);
    }

    public void move() {

        // Apply acceleration
        xSpeed += xAcc;
        ySpeed += yAcc;

        double ss = xSpeed * xSpeed + ySpeed * ySpeed;

        // Test if the speed needs to be clipped
        if (ss > maxSpeedSquared) {
            double sAngle = Math.atan2(ySpeed, xSpeed);

            xSpeed = maxSpeed * Math.cos(sAngle);
            ySpeed = maxSpeed * Math.sin(sAngle);
        }

        setPos(getX() + (float) xSpeed, getY() + (float) ySpeed);
    }

    public void draw(Canvas canvas) {
        canvas.save();

        canvas.translate(getX(), getY());
        canvas.rotate(angle);

        Paint p = new Paint();
        p.setColor(Color.RED);

        Path path = new Path();

        float W = 15;
        float H = 65;

        path.moveTo(-W, W);
        path.lineTo( W, W);
        path.lineTo( 0, W-H);
        path.lineTo(-W, W);

        canvas.drawPath(path, p);

        canvas.restore();
    }
}
