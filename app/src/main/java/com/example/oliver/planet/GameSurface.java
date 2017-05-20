package com.example.oliver.planet;

import android.view.MotionEvent;
import android.view.SurfaceView;
import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Path;
import android.util.Log;

import org.json.JSONObject;

import java.util.Vector;

/**
 * Created by Oliver on 5/3/2017.
 */

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    /*
    Graphic item constants
     */
    private final float BUTTON_SIZE = 75;

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

    private boolean m_paused = false;

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

    /* Painter Objects */
    private Paint pSelectorFill = new Paint();
    private Paint pSelectorOutline = new Paint();
    private Paint pArrow = new Paint();
    private Paint pMenu = new Paint();
    private Paint pFuelBarInterior = new Paint();
    private Paint pFuelBarOutline = new Paint();
    private Paint pFuelBarBlack = new Paint();

    private void setupPainters() {

        // Ship selector painters
        pSelectorFill.setARGB(255, 115, 200, 230);
        pSelectorFill.setStyle(Paint.Style.FILL);

        pSelectorOutline.setARGB(255, 115, 200, 230);
        pSelectorOutline.setStrokeWidth(5);
        pSelectorOutline.setStyle(Paint.Style.STROKE);

        pArrow.setStyle(Paint.Style.FILL);

        pMenu.setARGB(150, 175, 230, 175);
        pMenu.setStyle(Paint.Style.FILL_AND_STROKE);

        pFuelBarInterior.setStyle(Paint.Style.FILL);
        pFuelBarInterior.setARGB(200, 50, 250, 50);

        pFuelBarBlack.setStyle(Paint.Style.FILL);
        pFuelBarBlack.setARGB(150, 0, 0, 0);

        pFuelBarOutline.setStyle(Paint.Style.STROKE);
        pFuelBarOutline.setARGB(250, 50, 250, 50);
        pFuelBarOutline.setStrokeWidth(5);
    }

    public GameSurface(Context context) {
        super(context);

        setupPainters();

        //Log.i("surface", "created");

        this.setFocusable(true);

        this.getHolder().addCallback(this);

        // Create the thread here
        // it is started in 'surfaceCreated()'

        gameThread = new GameThread(this, this.getHolder(), context, new Handler());

        setFocusable(true);

        level = new GameLevel();

        Planet p;

        p = new Planet(-450, -250, 90);
        level.planets.add(p);

        // Create a moon
        Planet moon = new Planet(p, p.getX() - 250, p.getY() - 250, 40);
        level.planets.add(moon);

        moon = new Planet(p, p.getX()+550, p.getY()+550, 40);
        level.planets.add(moon);

        p = new Planet(500, 125, 10);
        level.planets.add(p);

        p = new Planet(800, 500, 90);
        p.setPlanetType(Planet.PlanetType.REPULSAR);
        level.planets.add(p);

        p = new Planet(-4800, -800, 130);
        p.setPlanetType(Planet.PlanetType.REPULSAR);
        //level.planets.add(p);

        p = new Planet(-1200, 750, 150);
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

        if (m_paused) {
            return;
        }

        // Update game timers
        updateTimers();

        // If a touch-event is continuing
        if (shipBeingDragged) {
            updateShipTouch(touchLast);
        }
        else {
            level.ship.turnEngineOff();
        }

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
    Determine if the game menu should be displayed
     */
    private boolean canShowMenu() {

        if (m_paused) {
            return true;
        }

        if (!level.ship.isReleased()) {
            return false;
        }

        if (mapBeingDragged || shipBeingDragged || stellarObjectBeingDragged != null) {
            return false;
        }

        return true;
    }

    private boolean testPositionHit(PointF pos, float x, float y, float r) {

        float dx = pos.x - x;
        float dy = pos.y - y;

        float ds = (dx * dx) + (dy * dy);

        return ds < (r * r);
    }

    private PointF getPauseButtonLocation() {

        return new PointF(
                WIDTH / 2,
                HEIGHT - BUTTON_SIZE
        );
    }

    private PointF getBackButtonLocation() {

        PointF p = getPauseButtonLocation();

        p.offset(-0.3f * SCREEN_MIN, 0);

        return p;
    }

    private PointF getResetButtonLocation() {

        PointF p = getPauseButtonLocation();

        p.offset(0.3f * SCREEN_MIN, 0);

        return p;
    }

    private void drawMenu(Canvas canvas) {

        if (!canShowMenu()) {
            return;
        }

        if (m_paused) {
            canvas.drawColor(Color.argb(100, 0, 0, 0));
        }

        PointF pb = getPauseButtonLocation();

        // Draw pause button

        if (!m_paused) {

            // Draw Pause Button
            canvas.drawRect(
                    pb.x - BUTTON_SIZE / 2,
                    pb.y - BUTTON_SIZE / 2,
                    pb.x - BUTTON_SIZE / 6,
                    pb.y + BUTTON_SIZE / 2,
                    pMenu);

            canvas.drawRect(
                    pb.x + BUTTON_SIZE / 6,
                    pb.y - BUTTON_SIZE / 2,
                    pb.x + BUTTON_SIZE / 2,
                    pb.y + BUTTON_SIZE / 2,
                    pMenu);
        }
        else {

            // Draw Play Button
            Path p = new Path();
            p.moveTo(pb.x - BUTTON_SIZE / 3, pb.y - BUTTON_SIZE / 2);
            p.lineTo(pb.x - BUTTON_SIZE / 3, pb.y + BUTTON_SIZE / 2);
            p.lineTo(pb.x + BUTTON_SIZE / 2, pb.y);
            p.lineTo(pb.x - BUTTON_SIZE / 3, pb.y - BUTTON_SIZE /2 );

            canvas.drawPath(p, pMenu);

            // Draw Reset Button
            PointF rst = getResetButtonLocation();

            canvas.drawCircle(rst.x, rst.y, BUTTON_SIZE / 2, pMenu);
        }
    }

    private void drawFuelBar(Canvas canvas) {

        float BAR_H = 25;
        float BAR_W = 250;
        float BAR_O = 15;

        // Fuel ratio
        float FR =  level.ship.getFuelRatio();

        // Draw the fuel
        canvas.drawRect(BAR_O, BAR_O, BAR_O + BAR_W * FR, BAR_O + BAR_H, pFuelBarInterior);

        // Draw remaining portion of fuel bar as black
        canvas.drawRect(BAR_O + BAR_W * FR, BAR_O, BAR_O + BAR_W, BAR_O + BAR_H, pFuelBarBlack);

        // Draw outline of fuel bar
        canvas.drawRect(BAR_O, BAR_O, BAR_O + BAR_W, BAR_O + BAR_H, pFuelBarOutline);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        arrows.clear();

        canvas.drawColor(Color.BLACK);

        canvas.save();

        // TODO - Move this painter...
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
                arrows.add(new OffScreenArrow(wh.wormholeA.getX(), wh.wormholeA.getY(), Color.WHITE, OffScreenArrow.ArrowShape.CIRCLE));
            }

            if (itemOnScreen(wh.wormholeB, Wormhole.WORMHOLE_RADIUS)) {
                wh.wormholeB.draw(canvas);
            }
            else {
                arrows.add(new OffScreenArrow(wh.wormholeB.getX(), wh.wormholeB.getY(), Color.WHITE, OffScreenArrow.ArrowShape.CIRCLE));
            }
        }

        // Draw each planet
        for (int jj=0; jj<level.planets.size(); jj++) {
            Planet p = level.planets.get(jj);

            p.drawOrbit(canvas);

            if (itemOnScreen(p, p.getTotalRadius())) {
                p.draw(canvas);
            }
            else {
                arrows.add(new OffScreenArrow(
                        p.getX(),
                        p.getY(),
                        p.getSimpleColor(),
                        OffScreenArrow.ArrowShape.CIRCLE));
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
                arrows.add(new OffScreenArrow(
                        s.getX(),
                        s.getY(),
                        Star.STAR_COLOR,
                        OffScreenArrow.ArrowShape.DIAMOND));
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
                    Color.WHITE,
                    OffScreenArrow.ArrowShape.DIAMOND));
        }

        // Draw the ship selector
        drawSelector(canvas);

        // Draw the ship
        if (itemOnScreen(level.ship, Ship.SHIP_RADIUS)) {
            level.ship.draw(canvas);
        }
        else
        {
            arrows.add(new OffScreenArrow(level.ship.getX(), level.ship.getY(), Color.RED, OffScreenArrow.ArrowShape.DIAMOND));
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

        drawMenu(canvas);
    }

    private void drawSelector(Canvas canvas) {

        if (!shipBeingDragged) {
            return;
        }

        // Selector fades in once activated
        if (shipDragTimer > 0) {
            shipDragTimer--;
        }

        float fade = (float) (MAX_SHIP_DRAG_TIMER - shipDragTimer) / MAX_SHIP_DRAG_TIMER;

        PointF shipPos = level.ship.getPos();
        PointF worldPos = getMapCoordsFromScreenPos(touchLast.x, touchLast.y);

        // Draw the filled interior portion
        pSelectorFill.setAlpha((int) (50 * fade));
        canvas.drawCircle(shipPos.x, shipPos.y, Ship.SELECTION_RADIUS_OUTER, pSelectorFill);

        // Draw the outlines
        pSelectorOutline.setAlpha((int) (150 * fade));

        canvas.drawCircle(shipPos.x, shipPos.y, Ship.SELECTION_RADIUS_OUTER, pSelectorOutline);
        canvas.drawCircle(shipPos.x, shipPos.y, Ship.SELECTION_RADIUS_INNER, pSelectorOutline);
    }

    private void drawArrows(Canvas canvas) {

        // Angle to the
        OffScreenArrow arrow;

        double dx, dy;
        double angle;

        float x, y = 0;
        float a = OffScreenArrow.ARROW_SIZE;

        Path path = new Path();

        for (int i=0; i<arrows.size(); i++) {
            arrow = arrows.get(i);

            pArrow.setColor(arrow.color);

            PointF pos = getScreenPosFromMapCoords(arrow.x, arrow.y);

            dx = pos.x - X_MID;
            dy = pos.y - Y_MID;

            angle = Math.atan2(-dy, dx);

            PointF edge = getPointOnEdge(angle);

            x = edge.x;
            y = edge.y;

            switch (arrow.shape) {
                default:
                case CIRCLE:
                    canvas.drawCircle(x, y, a, pArrow);
                    break;
                case DIAMOND:
                    path.reset();
                    path.moveTo(x, y-a);
                    path.lineTo(x-a, y);
                    path.lineTo(x, y+a);
                    path.lineTo(x+a, y);
                    path.lineTo(x, y-a);

                    canvas.drawPath(path, pArrow);
                    break;
            }
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
        boolean retry = true;

        gameThread.setRunning(false);

        while(retry) {
            try {

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
                retry = false;
            }
            catch(InterruptedException e)  {
                e.printStackTrace();
            }
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

                if (!m_paused && level.ship.distanceTo(worldPos.x, worldPos.y) < Ship.SELECTION_RADIUS_OUTER) {
                    startDraggingShip();
                    break;
                }

                // Test for menu hit
                if (canShowMenu()) {

                    // Play / Pause Button
                    if (testPositionHit(getPauseButtonLocation(), x, y, BUTTON_SIZE)) {
                        setPaused(!m_paused);
                    }
                    else if (m_paused && testPositionHit(getBackButtonLocation(), x, y, BUTTON_SIZE)) {
                        //TODO
                    }
                    else if (m_paused && testPositionHit(getResetButtonLocation(), x, y, BUTTON_SIZE)) {
                        setPaused(false);
                        reset();
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (stellarObjectBeingDragged != null) {
                    stellarObjectBeingDragged.setUserPos(worldPos.x, worldPos.y);
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
                else if (shipBeingDragged && !level.ship.isReleased()) {

                    if (dShip > Ship.SELECTION_RADIUS_OUTER) {
                        //Log.i("surface", "releasing ship");
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
                mapBeingDragged = false;
                stopDraggingShip();
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

    public void setPaused(boolean b) {
        m_paused = b;

        if (b) {
            //Log.i("surface", "set pause true");
        } else {
            //Log.i("surface", "set pause false");
        }
    }
}
