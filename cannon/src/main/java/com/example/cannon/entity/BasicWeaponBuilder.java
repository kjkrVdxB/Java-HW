package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ThreadLocalRandom;

import static com.example.cannon.Utils.rotate;

public class BasicWeaponBuilder {
    String name = "<unnamed weapon>";
    double bombRadius = 10;
    double bombBoost = 1;
    double bombCollisionDamage = 1;
    double rateLimit = Double.POSITIVE_INFINITY;
    double startingSpeed = 1;
    double angleStandardDeviation = 0;
    ExplosionBuilder explosionBuilder = null;

    public BasicWeaponBuilder() {
    }

    public BasicWeaponBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public BasicWeaponBuilder withBombRadius(double bombRadius) {
        this.bombRadius = bombRadius;
        return this;
    }

    public BasicWeaponBuilder withBombBoost(double bombBoost) {
        this.bombBoost = bombBoost;
        return this;
    }

    public BasicWeaponBuilder withRateLimit(double rateLimit) {
        this.rateLimit = rateLimit;
        return this;
    }

    public BasicWeaponBuilder withBombCollisionDamage(double collisionDamage) {
        this.bombCollisionDamage = collisionDamage;
        return this;
    }

    public BasicWeaponBuilder withStartingSpeed(double startingSpeed) {
        this.startingSpeed = startingSpeed;
        return this;
    }

    public BasicWeaponBuilder angleStandardDeviation(double angleStandardDeviation) {
        this.angleStandardDeviation = angleStandardDeviation;
        return this;
    }

    public BasicWeaponBuilder withExplosionType(ExplosionBuilder explosionType) {
        this.explosionBuilder = explosionType;
        return this;
    }

    @NonNull
    public Point2D updateStartingSpeed(@NonNull Point2D startingSpeed) {
        startingSpeed = rotate(startingSpeed, ThreadLocalRandom.current().nextGaussian() * 0.5 * angleStandardDeviation);
        startingSpeed = startingSpeed.multiply(BasicWeaponBuilder.this.startingSpeed);
        return startingSpeed;
    }

    public Weapon createWeapon() {
        return new Weapon() {
            @Override
            public @NonNull GameEntity getProjectile(@NonNull Point2D startingPosition,
                                                     @NonNull Point2D startingSpeed,
                                                     long launchingTime) {
                startingSpeed = updateStartingSpeed(startingSpeed);
                return new BasicProjectile(startingPosition, startingSpeed, launchingTime, bombRadius, bombBoost, bombCollisionDamage) {
                    @Override
                    protected void explode(@NonNull Point2D position, long timeNano) {
                        if (explosionBuilder != null) {
                            spawn(explosionBuilder
                                          .setPosition(position)
                                          .setStartingTime(timeNano)
                                          .createExplosion());
                        }
                        disappear();
                    }
                };
            }

            @Override
            @NonNull
            public String getName() {
                return name;
            }

            @Override
            public double getRateLimit() {
                return rateLimit;
            }
        };
    }
}
