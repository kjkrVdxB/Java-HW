package com.example.cannon.entity;

import com.example.cannon.game.Game;
import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.example.cannon.CannonApplication.WIDTH;
import static com.example.cannon.Utils.drawCircle;
import static com.example.cannon.Utils.getTimeElapsedSeconds;
import static com.example.cannon.game.World.GRAVITY;

/** An entity for projectile in flight. */
public class BasicProjectile extends GameEntity implements Drawable {
    @NonNull
    private final Point2D startingPosition;
    @NonNull
    private final Point2D startingVelocity;
    protected final long launchingTime;
    protected final double radius;
    private final double boost;

    public BasicProjectile(@NonNull Point2D startingPosition,
                           @NonNull Point2D startingSpeed,
                           long launchingTime,
                           double radius,
                           double boost) {
        this.launchingTime = launchingTime;
        this.startingPosition = startingPosition;
        this.startingVelocity = startingSpeed;
        this.radius = radius;
        this.boost = boost;
    }

    protected Point2D getPosition() {
        return getPosition(getTimeElapsedSeconds(launchingTime, getWorld().getCurrentTime()));
    }

    /** Get position at the given time in seconds. */
    @NonNull
    protected Point2D getPosition(double time) {
        time *= boost;
        return startingPosition.add(startingVelocity.multiply(time)).add(GRAVITY.multiply(time * time / 2));
    }

    public void update() {
        checkCollisions(getPosition(getTimeElapsedSeconds(launchingTime, getWorld().getCurrentTime()))); // TODO: more updates
    }

    /** Check collisions in the given position. */
    private void checkCollisions(@NonNull Point2D position) {
        if (outOfBounds(position)) {
            disappear();
        } else if (checkCollisionWithTarget(position)) {
            getWorld().finishGame(Game.FinishReason.TARGET_HIT);
        } else if (checkCollisionWithTerrain(position)) {
            explode(position);
        }
    }

    /** Explode in the given position. */
    protected void explode(@NonNull Point2D position) {
        disappear();
    }

    /** Checks that the projectile is to the left of to the right of the screen. */
    private boolean outOfBounds(@NonNull Point2D position) {
        return position.getX() <= 0 || position.getX() >= WIDTH;
    }

    /** Returns true when we collide with the terrain when in given position. */
    private boolean checkCollisionWithTerrain(@NonNull Point2D position) {
        return getWorld().getTerrain().isUnder(position);
    }

    /** Returns  true when we collide with the targin when in given position. */
    private boolean checkCollisionWithTarget(@NonNull Point2D position) {
        var target = getWorld().getTarget();
        return position.distance(target.getPosition()) < radius + target.getRadius();
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.RED);
        var position = getPosition();
        drawCircle(graphicsContext, position, radius);
        graphicsContext.restore();
    }

    @Override
    public int layer() {
        return 1;
    }
}
