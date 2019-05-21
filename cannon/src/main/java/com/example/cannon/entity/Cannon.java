package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.example.cannon.Utils.nanoFromSeconds;
import static com.example.cannon.Utils.vectorByAngle;
import static java.lang.Math.toDegrees;

public class Cannon extends GameEntity implements Drawable {
    private final static double SIZE = 20;
    private final static double ENGINE_POWER = 300;
    private final static double ANGLE_MOVING_SPEED = 2;
    private final static double LAUNCHING_POSITION_HEIGHT = 10;

    private double angle;
    @NonNull
    private Point2D basePosition;
    private boolean launching;
    private int movingDirection;
    private int angleMovingDirection;
    private Weapon weapon = null;
    private long lastLaunch;
    private boolean launched = false;

    public Cannon(double angle, @NonNull Point2D basePosition) {
        this.angle = angle;
        this.basePosition = basePosition;
    }

    public void update() {
        double deltaTime = getWorld().getLastUpdateTimeElapsedSeconds();
        basePosition = getWorld().getTerrain().nextToRight(basePosition, ENGINE_POWER * deltaTime * movingDirection);
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
    }

    @Override
    public int drawingLayer() {
        return 4;
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

    private void launchOne(long timeNano) {
        assert weapon != null;
        var projectile = weapon.getProjectile(basePosition.add(0, -LAUNCHING_POSITION_HEIGHT
        ), vectorByAngle(angle), timeNano);
        spawn(projectile);
        lastLaunch = timeNano;
        launched = true;
    }
}
