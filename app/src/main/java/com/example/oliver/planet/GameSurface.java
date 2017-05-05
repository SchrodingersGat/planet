package com.example.oliver.planet;

import android.appwidget.AppWidgetHost;
import android.content.ContentResolver;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.util.Vector;

/**
 * Created by Oliver on 5/3/2017.
 */

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    public GameLevel level;

    private StellarObject stellarObjectBeingDragged = null;
    private boolean mapBeingDragged = false;

    private float xMapOffset = 0.0f;
    private float yMapOffset = 0.0f;

    private float WIDTH = 0;
    private float HEIGHT = 0;

    private float X_MID = 0;
    private float Y_MID = 0;

    private double CORNER_ANGLE = 0;

    // Keeping track of touch positions
    private float xTouchLast = 0.0f;
    private float yTouchLast = 0.0f;

    private Vector<OffScreenArrow> arrows = new Vector<OffScreenArrow>();

    public GameSurface(Context context) {
        super(context);

        this.setFocusable(true);

        this.getHolder().addCallback(this);

        level = new GameLevel();

        Planet p;

        p = new Planet(-250, -50, 50);
        level.planets.add(p);

        p = new Planet(100, 25, 15);
        p.setPlanetType(Planet.PlanetType.BLACK_HOLE);
        level.planets.add(p);

        p = new Planet(200, 500, 45);
        p.setPlanetType(Planet.PlanetType.REPULSAR);
        level.planets.add(p);


        p = new Planet(-200, 500, 45);
        p.setPlanetType(Planet.PlanetType.SUN);
        level.planets.add(p);

        Star s;

        s = new Star(0, 0);
        level.stars.add(s);

        s = new Star(250, -50);
        level.stars.add(s);

        level.ship.setPos(25,100);
    }

    void updateScreenSize() {

        WIDTH = getWidth();
        HEIGHT = getHeight();

        X_MID = WIDTH / 2;
        Y_MID = HEIGHT / 2;

        // Calculate angle to the first corner
        CORNER_ANGLE = Math.atan2(HEIGHT/2, WIDTH/2);
    }

    public void update() {

        // Update game mechanics
        level.update();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        arrows.clear();

        canvas.save();

        canvas.translate(WIDTH/2, HEIGHT/2);
        canvas.translate(xMapOffset, yMapOffset);

        canvas.drawColor(Color.BLACK);

        Paint L = new Paint();
        L.setColor(Color.WHITE);

        // Draw each planet
        for (int ii=0; ii<level.planets.size(); ii++) {
            Planet p = level.planets.get(ii);

            if (itemOnScreen(p, p.getRadius())) {
                level.planets.get(ii).draw(canvas);
            }
            else {
                arrows.add(new OffScreenArrow(p.getX(), p.getY(), Color.GREEN));
            }
        }

        // Draw each star
        for (int jj=0; jj<level.stars.size(); jj++) {

            Star s = level.stars.get(jj);

            if (itemOnScreen(s, s.getRadius())) {
                level.stars.get(jj).draw(canvas);
            }
            else
            {
                arrows.add(new OffScreenArrow(s.getX(), s.getY(), Color.YELLOW));
            }
        }

        // Draw the ship
        if (itemOnScreen(level.ship, Ship.SHIP_RADIUS)) {
            level.ship.draw(canvas);
        }
        else
        {
            arrows.add(new OffScreenArrow(level.ship.getX(), level.ship.getY(), Color.RED));
        }

        level.ship.drawBreadcrumbs(canvas);

        // Draw items on top of the screen that do not move with the world
        canvas.restore();

        drawArrows(canvas);
    }

    private void drawArrows(Canvas canvas) {

        // Angle to the
        OffScreenArrow arrow;

        double dx, dy;
        double angle;

        Paint arrowPaint = new Paint();

        float x, y = 0;

        for (int i=0; i<arrows.size(); i++) {
            arrow = arrows.get(i);

            arrowPaint.setColor(arrow.color);

            PointF pos = getScreenPosFromMapCoords(arrow.x, arrow.y);

            dx = pos.x - X_MID;
            dy = pos.y - Y_MID;

            angle = Math.atan2(-dy, dx);

            PointF edge = getPointOnEdge(angle);

            canvas.drawCircle(edge.x, edge.y, 10, arrowPaint);
        }
    }

    /*
    Normalize an angle in the range {-PI, PI} 
     */
    private double normalizeAngle(double angle) {
        while (angle < -Math.PI) {
            angle += 2 * Math.PI;
        }

        while (angle > Math.PI) {
            angle -= 2 * Math.PI;
        }

        return angle;
    }

    /*
    Map the provided angle to a position on the edge of the screen
    - Angle is measured from center of screen
     */
    private PointF getPointOnEdge(double angle) {
        // Work out which quadrant the angle is in

        double x = 0;
        double y = 0;

        angle = normalizeAngle(angle);

        // Right hand side of screen
        if (angle >= -CORNER_ANGLE && angle <= CORNER_ANGLE) {
            x = WIDTH;
            y = Y_MID - X_MID * Math.tan(angle);
        }
        // Left hand side of screen
        else if (angle >= (Math.PI - CORNER_ANGLE) ||
                (angle <= -(Math.PI - CORNER_ANGLE))) {
            x = 0;
            y = Y_MID + X_MID * Math.tan(angle);
        }
        // Top side of screen
        else if (angle >= CORNER_ANGLE && angle <= (Math.PI - CORNER_ANGLE)) {
            x = X_MID + Y_MID * Math.tan(Math.PI/2 - angle);
            y = 0;
        }
        // Bottom side of screen
        else if (angle <= -CORNER_ANGLE && angle >= (-Math.PI + CORNER_ANGLE)) {
            x = X_MID - Y_MID * Math.tan(-Math.PI/2 - angle);
            y = HEIGHT;
        }

        return new PointF((float) x, (float) y);

    }

    /*
    Determine if an item is visible on the screen
    - Object position is provided in MAP coordinates
    - Provide a radius around the item for testing
     */
    private boolean itemOnScreen(GameObject object, float radius) {

        // Top-left corner of screen
        PointF TL = getMapCoordsFromScreenPos(0, 0);

        // Bottom-right corner of screen
        PointF BR = getMapCoordsFromScreenPos(WIDTH, HEIGHT);

        float x = object.getX();
        float y = object.getY();

        return TL.x < (x + radius) &&
               BR.x > (x - radius) &&
               TL.y < (y + radius) &&
               BR.y > (y - radius);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();

        updateScreenSize();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        updateScreenSize();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry= true;
        while(retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
                retry = false;
            }
            catch(InterruptedException e)  {
                e.printStackTrace();
            }
            retry= true;
        }
    }

    public PointF getMapCoordsFromScreenPos(float xScreen, float yScreen) {

        // World coordinates (0, 0) are located at center of screen nominally
        float x = xScreen - WIDTH / 2;
        float y = yScreen - HEIGHT / 2;

        x -= xMapOffset;
        y -= yMapOffset;

        return new PointF(x, y);
    }

    public PointF getScreenPosFromMapCoords(float xMap, float yMap) {

        float x = xMap + xMapOffset;
        float y = yMap + yMapOffset;

        x += WIDTH / 2;
        y += HEIGHT / 2;

        return new PointF(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Get the position of the ship in world coordinates
        float x = event.getX();
        float y = event.getY();

        PointF worldPos = getMapCoordsFromScreenPos(x, y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                /* Test for item being clicked */
                stellarObjectBeingDragged = level.testStellarObjectHit(worldPos.x, worldPos.y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (stellarObjectBeingDragged != null) {
                    stellarObjectBeingDragged.setPos(worldPos.x, worldPos.y);
                }
                else
                {
                    mapBeingDragged = true;
                    xMapOffset += (x - xTouchLast);
                    yMapOffset += (y - yTouchLast);
                }
                break;

            case MotionEvent.ACTION_UP:

                if (stellarObjectBeingDragged == null && !mapBeingDragged) {
                    level.reset();
                    level.ship.setPos(worldPos.x, worldPos.y);
                }

                stellarObjectBeingDragged = null;
                mapBeingDragged = false;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                stellarObjectBeingDragged = null;
                break;
            default:
                break;
        }

        xTouchLast = x;
        yTouchLast = y;

        return true;
    }
}
