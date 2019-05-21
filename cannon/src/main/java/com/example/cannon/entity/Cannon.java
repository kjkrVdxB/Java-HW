package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.example.cannon.Utils.*;
import static java.lang.Math.toDegrees;

/** A cannon on the field. */
public class Cannon extends GameEntity implements Drawable {
    /** Just some measure that scales the cannon */
    private final static double SIZE = 20;
    /** AKA speed (but will probably mean something different later). Right now in 'pixels' per second. */
    private final static double ENGINE_POWER = 300;
    /** In radians per second. */
    private final static double ANGLE_MOVING_SPEED = 2;
    /** Distance from base to the cannon rotating point. */
    private final static double LAUNCHING_POSITION_HEIGHT = 10;
    private static final int CANNON_DRAWING_LAYER = 1;

    private double angle;
    @NonNull
    private Point2D basePosition;
    private boolean launching;
    private int movingDirection;
    /** As a coefficient. 1 means CCW, -1 means CW, 0 means no movement. */
    private int angleMovingDirection;
    private Weapon weapon = null;
    private long lastLaunch;
    private boolean launched = false;

    public Cannon(double angle, @NonNull Point2D basePosition) {
        this.angle = angle;
        this.basePosition = basePosition;
    }

    public void update() {
        basePosition = new Point2D(basePosition.getX(), getWorld().getTerrain().getHeight(basePosition.getX()));
        double deltaTime = getWorld().getLastUpdateTimeElapsedSeconds();
        basePosition = getWorld().getTerrain().move(basePosition, ENGINE_POWER * deltaTime * movingDirection);
        angle -= ANGLE_MOVING_SPEED * deltaTime * angleMovingDirection;
        launch();
    }

    public void setLaunching(boolean launching) {
        this.launching = launching;
        if (!launching) {
            launched = false;
        }
    }

    public void setMovingDirection(int direction) {
        this.movingDirection = direction;
    }

    public void setAngleMovingDirection(int direction) {
        this.angleMovingDirection = direction;
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        var center = basePosition.add(new Point2D(0, -LAUNCHING_POSITION_HEIGHT));
        graphicsContext.translate(center.getX(), center.getY());
        graphicsContext.rotate(toDegrees(angle));
        graphicsContext.setFill(Color.GRAY);
        graphicsContext.beginPath();
        graphicsContext.moveTo(0, SIZE / 5 * 2);
        graphicsContext.lineTo(SIZE, SIZE / 5);
        graphicsContext.lineTo(SIZE, -SIZE / 5);
        graphicsContext.lineTo(0, -SIZE / 5 * 2);
        graphicsContext.lineTo(-SIZE / 10 * 3, 0);
        graphicsContext.closePath();
        graphicsContext.fill();
        graphicsContext.restore();
        graphicsContext.save();
        graphicsContext.translate(basePosition.getX(), basePosition.getY());
        graphicsContext.setFill(Color.BROWN);
        graphicsContext.beginPath();
        graphicsContext.moveTo(SIZE / 2, 0);
        graphicsContext.lineTo(0, -SIZE / 2);
        graphicsContext.lineTo(-SIZE / 2, 0);
        graphicsContext.closePath();
        graphicsContext.fill();
        graphicsContext.restore();
        graphicsContext.save();
        graphicsContext.setFill(Color.BLACK);
        drawCircle(graphicsContext, basePosition.add(SIZE / 2, 0), SIZE / 5);
        drawCircle(graphicsContext, basePosition.add(-SIZE / 2, 0), SIZE / 5);
        graphicsContext.restore();
    }

    @Override
    public int getDrawingLayer() {
        return CANNON_DRAWING_LAYER;
    }

    public void selectWeapon(@Nullable Weapon weapon) {
        this.weapon = weapon;
    }

    /** Launching all the projectiles missed in the last frame with getRateLimit intervals. */
    private void launch() {
        if (weapon == null || !launching) {
            return;
        }
        if (weapon.getRateLimit() == Double.POSITIVE_INFINITY) {
            if (!launched) {
                launchOne(getWorld().getCurrentTime());
            }
            return;
        }
        long nanoRateLimit = nanoFromSeconds(weapon.getRateLimit());
        long time = launched ? lastLaunch + nanoRateLimit : getWorld().getPreviousTime();
        while (time < getWorld().getCurrentTime()) {
            launchOne(time);
            time += nanoRateLimit;
        }
    }

    /** Launch a missile at the given time. */
    private void launchOne(long timeNano) {
        assert weapon != null;
        var projectile = weapon.getProjectile(basePosition.add(0, -LAUNCHING_POSITION_HEIGHT),
                                              vectorByAngle(angle),
                                              timeNano);
        spawn(projectile);
        lastLaunch = timeNano;
        launched = true;
    }
}
