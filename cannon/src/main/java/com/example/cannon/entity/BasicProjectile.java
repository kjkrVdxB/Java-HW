package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.example.cannon.CannonApplication.WIDTH;
import static com.example.cannon.Utils.*;
import static com.example.cannon.game.World.GRAVITY;
import static java.lang.Math.max;
import static java.lang.Math.min;

/** An entity for projectile in flight. */
public class BasicProjectile extends GameEntity implements Drawable {
    private static final int CHECK_COLLISIONS_PER_SECOND = 100;
    @NonNull
    private final Point2D startingPosition;
    @NonNull
    private final Point2D startingVelocity;
    final long launchingTime;
    private final double radius;
    private final double boost;
    private final double collisionDamage;

    public BasicProjectile(@NonNull Point2D startingPosition,
                           @NonNull Point2D startingSpeed,
                           long launchingTime,
                           double radius,
                           double boost,
                           double collisionDamage) {
        this.launchingTime = launchingTime;
        this.startingPosition = startingPosition;
        this.startingVelocity = startingSpeed;
        this.radius = radius;
        this.boost = boost;
        this.collisionDamage = collisionDamage;
    }

    protected Point2D getPositionAtWorldTime(long timeNano) {
        return getPosition(getTimeElapsedSeconds(launchingTime, timeNano));
    }

    /** Get position at the given time in seconds since the launch. */
    @NonNull
    private Point2D getPosition(double time) {
        time *= boost;
        return startingPosition.add(startingVelocity.multiply(time)).add(GRAVITY.multiply(time * time / 2));
    }

    public void update() {
        long start = max(getWorld().getPreviousTime(), launchingTime);
        long end = getWorld().getCurrentTime();
        long checkCollisions = max(1, (long) (secondsFromNano(end - start) * CHECK_COLLISIONS_PER_SECOND));
        long step = max(1, (end - start) / checkCollisions);
        for (long time = start; time < end; time += step) {
            checkCollisions(getPositionAtWorldTime(time));
        }
    }

    /** Check collisions in the ephemeralPosition. */
    private void checkCollisions(@NonNull Point2D position) {
        if (outOfBounds(position)) {
            disappear();
        } else if (checkCollisionWithTarget(position)) {
            getWorld().getTarget().dealDamage(collisionDamage);
            explode(position);
        } else if (checkCollisionWithTerrain(position)) {
            explode(position);
        }
    }

    /** Explode in the position. */
    protected void explode(@NonNull Point2D position) {
        disappear();
    }

    /** Checks that the projectile is to the left of to the right of the screen. */
    private boolean outOfBounds(@NonNull Point2D position) {
        return position.getX() + radius <= -WIDTH / 2 || position.getX() - radius >= WIDTH * 1.5;
    }

    /** Returns true when we collide with the terrain when in the given position. */
    private boolean checkCollisionWithTerrain(@NonNull Point2D position) {
        return getWorld().getTerrain().isPointUnder(position);
    }

    /** Returns  true when we collide with the target when in the given position. */
    private boolean checkCollisionWithTarget(@NonNull Point2D position) {
        var target = getWorld().getTarget();
        return position.distance(target.getPosition()) < radius + target.getRadius();
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.RED);
        var position = getPositionAtWorldTime(getWorld().getCurrentTime());
        drawCircle(graphicsContext, position, radius);
        graphicsContext.restore();
    }

    @Override
    public int drawingLayer() {
        return 1;
    }
}
