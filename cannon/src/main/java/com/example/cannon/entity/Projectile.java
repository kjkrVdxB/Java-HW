package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import com.example.cannon.game.World;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.example.cannon.CannonApplication.WIDTH;
import static com.example.cannon.Utils.drawCircle;
import static com.example.cannon.Utils.getTimeElapsedSeconds;
import static com.example.cannon.game.World.*;

/** An entity for projectile in flight. */
public class Projectile extends GameEntity implements Drawable {
    @NonNull
    private final Point2D start;
    @NonNull
    private final Point2D startingVelocity;
    @NonNull
    private final WeaponType weaponType;
    private final long launchingTime;

    public Projectile(@NonNull World world, @NonNull Point2D start, @NonNull Point2D startingDirection, long launchingTime, @NonNull WeaponType weaponType) {
        super(world);
        this.launchingTime = launchingTime;
        this.start = start;
        this.startingVelocity = startingDirection.multiply(weaponType.getInitialSpeed());
        this.weaponType = weaponType;
    }

    private Point2D getPosition() {
        return getPosition(getTimeElapsedSeconds(launchingTime, getWorld().getCurrentTime()));
    }

    /** Get position at the given time in seconds. */
    @NonNull
    private Point2D getPosition(double time) {
        time *= weaponType.getBoost();
        return start.add(startingVelocity.multiply(time)).add(GRAVITY.multiply(time * time / 2));
    }
    
    public void update() {
        checkCollisions(getPosition(getTimeElapsedSeconds(launchingTime, getWorld().getCurrentTime()))); // TODO: more updates
    }

    /** Check collisions in the given position. */
    private void checkCollisions(@NonNull Point2D position) {
        if (outOfBounds(position)) {
            disappear();
        } else if (checkCollisionWithTarget(position)) {
            getWorld().finishGame();
        } else if (checkCollisionWithTerrain(position)) {
            explode(position);
        }
    }

    /** Explode in the given position. */
    private void explode(@NonNull Point2D position) {
        if (weaponType.getExplosionType() != null) {
            spawn(new Explosion(getWorld(), weaponType.getExplosionType(), position, getWorld().getCurrentTime()));
        }
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
        return position.distance(target.getPosition()) < weaponType.getRadius() + target.getRadius();
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.RED);
        var position = getPosition();
        drawCircle(graphicsContext, position, weaponType.getRadius());
        graphicsContext.restore();
    }

    @Override
    public int layer() {
        return 1;
    }

    public enum WeaponType {
        Pistol("Pistol", 5, 1000, 1, Double.POSITIVE_INFINITY, null),
        Nuke("Nuke", 20, 500, 1, Double.POSITIVE_INFINITY, new Explosion.Type(200, 10)),
        MachineGun("Machine Gun", 5, 1000, 1, 0.01, null),
        GrenadeLauncher("Grenade Launcher", 10, 600, 1.5, 0.5, new Explosion.Type(50, 0.5));

        /** Name to be displayed to the user. */
        @NonNull
        private final String name;
        /** Radius in flight. */
        private final double radius;
        /** Initial speed. Changes the trajectory. */
        private final double initialSpeed;
        /** Does not change the trajectory, but makes the projectile move faster. */
        private final double boost;
        /**
         * Minimum time between firing when the 'fire' button is continuously pressed. The value of
         * Double.POSITIVE_INFINITY means that only one shot per press will be fired.
         */
        private final double rateLimit;
        /**
         * Parameters for the explosion the projectile creates. 'null' means that the projectile does not detonate,
         * for example bullets.
         */
        private final Explosion.@Nullable Type explosionType;

        WeaponType(@NonNull String name, double radius, double initialSpeed, double boost, double rateLimit, Explosion.@Nullable Type explosionType) {
            this.name = name;
            this.radius = radius;
            this.initialSpeed = initialSpeed;
            this.boost = boost;
            this.rateLimit = rateLimit;
            this.explosionType = explosionType;
        }

        public double getInitialSpeed() {
            return initialSpeed;
        }

        public double getRadius() {
            return radius;
        }

        public double getRateLimit() {
            return rateLimit;
        }

        public Explosion.Type getExplosionType() {
            return explosionType;
        }

        public double getBoost() {
            return boost;
        }

        @NonNull
        public String getName() {
            return name;
        }
    }
}
