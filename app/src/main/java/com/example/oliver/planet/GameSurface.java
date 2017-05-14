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
import android.util.Log;

import java.util.Vector;

/**
 * Created by Oliver on 5/3/2017.
 */

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private final float EPSILOM = 0.0001f;

    public GameThread gameThread;
    public GameLevel level;

    private StellarObject stellarObjectBeingDragged = null;
    private boolean mapBeingDragged = false;

    private boolean shipBeingDragged = false;
    private int shipDragTimer = 0;
    private final int MAX_SHIP_DRAG_TIMER = 25;

    private PointF mapOffset = new PointF();

    private float WIDTH = 0;
    private float HEIGHT = 0;

    private float X_MID = 0;
    private float Y_MID = 0;

    private float SCREEN_MIN = 0;
    private float SCREEN_MAX = 0;

    private double CORNER_ANGLE = 0;

    // Keeping track of touch positions
    private PointF touchLast = new PointF();

    private Vector<OffScreenArrow> arrows = new Vector<OffScreenArrow>();

    /*
    Various 'timers' to keep track of game states
     */

    // How long to look away from the position of the ship
    private int lookAwayTimer = 0;
    private float lookAwaySpring = 0.0f;
    private final int LOOK_AWAY_TIMER_RESET = 50;

    public GameSurface(Context context) {
        super(context);

        this.setFocusable(true);

        this.getHolder().addCallback(this);

        level = new GameLevel();

        Planet p;

        p = new Planet(-450, -250, 90);
        level.planets.add(p);

        p = new Planet(500, 125, 10);
        p.setPlanetType(Planet.PlanetType.BLACK_HOLE);
        //level.planets.add(p);

        p = new Planet(800, 500, 90);
        p.setPlanetType(Planet.PlanetType.REPULSAR);
        level.planets.add(p);

        p = new Planet(-4800, -800, 130);
        p.setPlanetType(Planet.PlanetType.REPULSAR);
        //level.planets.add(p);

        p = new Planet(-1200, 750, 100);
        p.setPlanetType(Planet.PlanetType.SUN);
        p.setAtmosphere(500);
        level.planets.add(p);

        p = new Planet(500, 1800, 100);
        level.planets.add(p);

        Star s;

        s = new Star(600, 0);
        level.stars.add(s);

        s = new Star(250, -50);
        level.stars.add(s);

        s = new Star(-500, 1000);
        level.stars.add(s);

        level.endZone.setPos(0, 900);

        WormholePair whp = new WormholePair(
                new PointF(-1000, -1000),
                new PointF( 1000,  1000)
        );

        level.wormholes.add(whp);

        reset();
    }

    void reset() {
        level.reset();
        mapOffset.set(0,0);
    }

    void updateScreenSize() {

        WIDTH = getWidth();
        HEIGHT = getHeight();

        X_MID = WIDTH / 2;
        Y_MID = HEIGHT / 2;

        SCREEN_MIN = Math.min(WIDTH, HEIGHT);
        SCREEN_MAX = Math.max(WIDTH, HEIGHT);

        // Calculate angle to the first corner
        CORNER_ANGLE = Math.atan2(HEIGHT/2, WIDTH/2);
    }

    void updateShipTouch(PointF screenPos) {

        PointF worldPos = getMapCoordsFromScreenPos(screenPos.x, screenPos.y);

        float dist = level.ship.distanceTo(worldPos.x, worldPos.y);
        double angle = Math.atan2(
                        level.ship.getY() - worldPos.y,
                        level.ship.getX() - worldPos.x);

        if (shipBeingDragged) {
            if (dist > Ship.SELECTION_RADIUS_INNER) {
                level.ship.setTargetAngle(angle); //0.25 * Math.PI); // angle);
            }
            if (level.ship.isReleased() &&
                !level.ship.hasCrashed() &&
                dist > Ship.SELECTION_RADIUS_OUTER) {

                level.ship.turnEngineOn();
                level.ship.setThrust((dist - Ship.SELECTION_RADIUS_OUTER) / 500);
            }
            else {
                level.ship.turnEngineOff();
            }
        }
    }

    public void update() {

        // Update game timers
        updateTimers();

        // If a touch-event is continuing
        if (shipBeingDragged) {
            updateShipTouch(touchLast);
        }
        else {
            level.ship.turnEngineOff();
        }

        // Update game mechanics
        level.update();

        // Check if ship has left map
        //TODO - Make this more graceful
        if (level.ship.distanceTo(0, 0) > GameLevel.LEVEL_BOUNDS) {
            reset();
        }
        else if (level.ship.hasCrashed()) {
            reset();
        }

        // Check if end zone has been reached
        if (level.endZone.testShip(level.ship)) {
            reset();
        }

    }

    void startDraggingShip() {

        shipBeingDragged = true;
        shipDragTimer = MAX_SHIP_DRAG_TIMER;
    }

    void stopDraggingShip() {
        shipBeingDragged = false;
    }

    private void updateTimers() {

        if (lookAwayTimer > 0) {
            lookAwayTimer--;
        }
        else if (mapBeingDragged || shipBeingDragged) {
            // Don't move screen in these cases
        }
        else if (stellarObjectBeingDragged != null) {
            // Don't move screen if planet is being dragged
        }
        else {
            double d = (mapOffset.x * mapOffset.x) + (mapOffset.y * mapOffset.y);

            if (d > EPSILOM) {

                if (lookAwaySpring < 0.1) {
                    lookAwaySpring += 0.01;
                }

                mapOffset.x *= (1 - lookAwaySpring);
                mapOffset.y *= (1 - lookAwaySpring);
            }
        }
    }

    /*
    Draw a grid overlay on the screen
    The grid is in 'world' coordinates
    (moves with the screen)
     */
    public void drawGrid(Canvas canvas) {

        final float GRID = 100;

        // Calculate grid offset
        PointF loc = getMapCoordsFromScreenPos(0, 0);

        loc.x -= GRID * (int) (loc.x / GRID);
        loc.y -= GRID * (int) (loc.y / GRID);

        int nx = (int) (WIDTH / GRID + 0.5);
        int ny = (int) (HEIGHT / GRID + 0.5);

        Paint gp = new Paint();
        gp.setARGB(50, 100, 200, 225);
        gp.setStrokeWidth(3.5f);

        float x, y;

        for (int i=-1; i<=nx; i++) {
            x = i * GRID - loc.x;
            canvas.drawLine(x, 0, x, HEIGHT, gp);
        }

        for (int j=-1; j<=ny; j++) {
            y = j * GRID - loc.y;
            canvas.drawLine(0, y, WIDTH, y, gp);
        }
    }

    public void drawFuelBar(Canvas canvas) {

        Paint fbp = new Paint();

        float BAR_H = 25;
        float BAR_W = 250;
        float BAR_O = 15;

        // Fuel ratio
        float FR =  level.ship.getFuelRatio();

        fbp.setARGB(150, (int) (255 * (1-FR)), (int) (255 * (FR)), 0);

        fbp.setStyle(Paint.Style.FILL);

        canvas.drawRect(BAR_O, BAR_O, BAR_O + BAR_W * FR, BAR_O + BAR_H, fbp);

        // Draw remaining portion of fuel bar as black
        fbp.setARGB(150, 0, 0, 0);

        canvas.drawRect(BAR_O + BAR_W * FR, BAR_O, BAR_O + BAR_W, BAR_O + BAR_H, fbp);

        fbp.setColor(Color.GREEN);
        fbp.setStrokeWidth(5);
        fbp.setStyle(Paint.Style.STROKE);

        // Draw outline of fuel bar
        canvas.drawRect(BAR_O, BAR_O, BAR_O + BAR_W, BAR_O + BAR_H, fbp);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        arrows.clear();

        canvas.drawColor(Color.BLACK);
        drawGrid(canvas);

        canvas.save();

        Paint L = new Paint();
        L.setColor(Color.RED);

        // Offset by correct position
        canvas.translate(WIDTH/2, HEIGHT/2);
        canvas.translate(mapOffset.x, mapOffset.y);
        canvas.translate(-level.ship.pos.x, -level.ship.pos.y);

        // Draw wormholes
        for (int ii=0; ii<level.wormholes.size(); ii++) {
            WormholePair wh = level.wormholes.get(ii);

            if (itemOnScreen(wh.wormholeA, Wormhole.WORMHOLE_RADIUS)) {
                wh.wormholeA.draw(canvas);
            }
            else {
                arrows.add(new OffScreenArrow(wh.wormholeA.getX(), wh.wormholeA.getY(), Color.WHITE));
            }

            if (itemOnScreen(wh.wormholeB, Wormhole.WORMHOLE_RADIUS)) {
                wh.wormholeB.draw(canvas);
            }
            else {
                arrows.add(new OffScreenArrow(wh.wormholeB.getX(), wh.wormholeB.getY(), Color.WHITE));
            }
        }

        // Draw each planet
        for (int jj=0; jj<level.planets.size(); jj++) {
            Planet p = level.planets.get(jj);

            if (itemOnScreen(p, p.getTotalRadius())) {
                p.draw(canvas);
            }
            else {
                arrows.add(new OffScreenArrow(p.getX(), p.getY(), Color.GREEN));
            }
        }

        // Draw each star
        for (int kk=0; kk<level.stars.size(); kk++) {

            Star s = level.stars.get(kk);

            if (itemOnScreen(s, s.getRadius())) {
                s.draw(canvas);
            }
            else
            {
                arrows.add(new OffScreenArrow(s.getX(), s.getY(), Color.YELLOW));
            }
        }

        // Draw the endpoint
        if (itemOnScreen(level.endZone, Galaxy.GALAXY_RADIUS)) {
            level.endZone.draw(canvas);
        }
        else {
            arrows.add(new OffScreenArrow(
                    level.endZone.getX(),
                    level.endZone.getY(),
                    Color.WHITE));
        }

        // Draw the ship selector
        drawSelector(canvas);

        // Draw the ship
        if (itemOnScreen(level.ship, Ship.SHIP_RADIUS)) {
            level.ship.draw(canvas);
        }
        else
        {
            arrows.add(new OffScreenArrow(level.ship.getX(), level.ship.getY(), Color.RED));
        }

        // Draw the 'release point' of the ship
        if (level.ship.releasePoint != null &&
            !level.ship.isReleased()) {
            PointF rp = level.ship.releasePoint;

            canvas.drawCircle(-rp.x, -rp.y, 20, L);
        }

        level.ship.drawBreadcrumbs(canvas);

        // Draw items on top of the screen that do not move with the world
        canvas.restore();

        drawArrows(canvas);

        drawFuelBar(canvas);
    }

    private void drawSelector(Canvas canvas) {

        Paint sp = new Paint();

        sp.setARGB(255, 115, 200, 230);

        // Selector fades in once activated
        if (shipDragTimer > 0) {
            shipDragTimer--;
        }

        float fade = (float) (MAX_SHIP_DRAG_TIMER - shipDragTimer) / MAX_SHIP_DRAG_TIMER;

        sp.setAlpha((int) (50 * fade));

        PointF shipPos = level.ship.getPos();

        PointF worldPos = getMapCoordsFromScreenPos(touchLast.x, touchLast.y);

        if (shipBeingDragged) {
            canvas.drawCircle(shipPos.x, shipPos.y, Ship.SELECTION_RADIUS_OUTER, sp);
        }

        if (shipDragTimer > 0) {
            shipDragTimer--;
        }

        sp.setAlpha(150);
        sp.setStyle(Paint.Style.STROKE);
        sp.setStrokeWidth(5);

        canvas.drawCircle(shipPos.x, shipPos.y, Ship.SELECTION_RADIUS_INNER, sp);

        sp.setAlpha((int) (150 * fade));

        if (shipBeingDragged) {

            canvas.drawCircle(shipPos.x, shipPos.y, Ship.SELECTION_RADIUS_OUTER, sp);

            float d = level.ship.distanceTo(worldPos.x, worldPos.y);

            if (d > Ship.SELECTION_RADIUS_OUTER) {
                d = Ship.SELECTION_RADIUS_OUTER;
            }

            if (d > Ship.SELECTION_RADIUS_INNER) {

                float ca = (float) Math.cos(-level.ship.getAngle());
                float sa = (float) Math.sin(-level.ship.getAngle());

                /*
                canvas.drawLine(
                        shipPos.x - Ship.SELECTION_RADIUS_INNER * ca,
                        shipPos.y + Ship.SELECTION_RADIUS_INNER * sa,
                        shipPos.x - d * ca,
                        shipPos.y + d * sa,
                        sp);
                */
            }
        }
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

        //Log.i("surface", "created");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        updateScreenSize();

        //Log.i("surface", "changed");
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

        //Log.i("surface", "destroyed");
    }

    public PointF getMapCoordsFromScreenPos(float xScreen, float yScreen) {

        // World coordinates (0, 0) are located at center of screen nominally
        float x = xScreen - WIDTH / 2;
        float y = yScreen - HEIGHT / 2;

        x -= mapOffset.x;
        y -= mapOffset.y;

        x += level.ship.pos.x;
        y += level.ship.pos.y;

        return new PointF(x, y);
    }

    public PointF getScreenPosFromMapCoords(float xMap, float yMap) {

        float x = xMap + mapOffset.x;
        float y = yMap + mapOffset.y;

        x -= level.ship.pos.x;
        y -= level.ship.pos.y;

        x += WIDTH / 2;
        y += HEIGHT / 2;

        return new PointF(x, y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // Get the position of the ship in world coordinates
        float x = event.getX();
        float y = event.getY();

        PointF worldPos = toGrid(getMapCoordsFromScreenPos(x, y), 20);

        float dShip = level.ship.distanceTo(worldPos.x, worldPos.y);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                /* Test for item being clicked */
                stellarObjectBeingDragged = level.testStellarObjectHit(worldPos.x, worldPos.y);

                if (stellarObjectBeingDragged != null) {
                    break;
                }

                if (level.ship.distanceTo(worldPos.x, worldPos.y) < Ship.SELECTION_RADIUS_OUTER) {
                    startDraggingShip();
                    break;
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (stellarObjectBeingDragged != null) {
                    stellarObjectBeingDragged.setPos(worldPos.x, worldPos.y);
                }
                else if (shipBeingDragged) {

                }
                else {
                    mapBeingDragged = true;
                    addMapOffset(x-touchLast.x, y-touchLast.y);
                }

                break;

            case MotionEvent.ACTION_UP:

                if (level.ship.isEngineOn()) {
                    level.ship.turnEngineOff();
                }

                else if (stellarObjectBeingDragged == null &&
                    !mapBeingDragged &&
                    !shipBeingDragged) {
                    reset();
                    resetMapOffset();
                }

                else if (shipBeingDragged && !level.ship.isReleased()) {

                    if (dShip > Ship.SELECTION_RADIUS_OUTER) {
                        level.ship.release(new PointF(
                                level.ship.getX() - worldPos.x,
                                level.ship.getY() - worldPos.y));
                    }
                }

                stellarObjectBeingDragged = null;
                mapBeingDragged = false;
                stopDraggingShip();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                stellarObjectBeingDragged = null;
                break;
            default:
                break;
        }

        touchLast.set(x, y);

        return true;
    }

    private PointF toGrid(float x, float y, float g) {
        x = g * (int) (x / g + 0.5);
        y = g * (int) (y / g + 0.5);

        return new PointF(x, y);
    }

    private PointF toGrid(PointF p, float g) {
        return toGrid(p.x, p.y, g);
    }

    private void addMapOffset(float x, float y) {
        mapOffset.x += x;
        mapOffset.y += y;
        lookAwayTimer = LOOK_AWAY_TIMER_RESET;
        lookAwaySpring = 0.0f;
    }

    private void resetMapOffset() {
        mapOffset.set(0, 0);
    }
}
