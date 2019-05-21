package com.example.cannon.entity;

import com.example.cannon.Utils;
import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.example.cannon.Utils.*;

public class Cannon extends GameEntity implements Drawable {
    private final static double RADIUS = 10;
    private final static double LENGTH = 20;
    private final static double THICKNESS = 7;
    private final static double ENGINE_POWER = 100;
    private final static double ANGLE_MOVING_SPEED = 2;

    private double angle;
    @NonNull
    private Point2D position;
    private boolean launching;
    private int movingDirection;
    private int angleMovingDirection;
    private Weapon weapon = null;
    private long lastLaunch;
    private boolean launched = false;

    public Cannon(double angle, @NonNull Point2D position) {
        this.angle = angle;
        this.position = position;
    }

    public void update() {
        double deltaTime = getWorld().getLastUpdateTimeElapsedSeconds();
        position = getWorld().getTerrain().nextToRight(position, ENGINE_POWER * deltaTime * movingDirection);
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
        graphicsContext.setFill(Color.DARKOLIVEGREEN);
        drawCircle(graphicsContext, position, RADIUS);
        graphicsContext.setStroke(Color.DARKOLIVEGREEN);
        graphicsContext.setLineWidth(THICKNESS);
        var end = position.add(Utils.vectorByAngle(angle).multiply(LENGTH));
        graphicsContext.strokeLine(position.getX(), position.getY(), end.getX(), end.getY());
        graphicsContext.restore();
    }

    @Override
    public int layer() {
        return 4;
    }

    public void selectWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    /** Launching all the projectiles missed in the current frame with getRateLimit intervals */
    private void launch() {
        if (weapon == null || !launching) {
            return;
        }
        if (weapon.getRateLimit() == Double.POSITIVE_INFINITY) {
            if (!launched) {
                launchOne(getWorld().getPreviousTime());
            }
            return;
        }
        long nanoRateLimit = nanoFromSeconds(weapon.getRateLimit());
        long startingTime = launched ? lastLaunch + nanoRateLimit : getWorld().getPreviousTime();
        for (long time = startingTime; time < getWorld().getCurrentTime(); time += nanoRateLimit) {
            launchOne(time);
        }
    }

    private void launchOne(long time) {
        assert weapon != null;
        var projectile = weapon.getProjectile(position, vectorByAngle(angle), time);
        spawn(projectile);
        lastLaunch = time;
        launched = true;
    }
}
