package com.example.cannon.entity;

import javafx.geometry.Point2D;
import org.apache.commons.lang3.Validate;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ExplosionBuilder {
    private Point2D position;
    private long startingTime;
    private double radius;
    private double duration;
    private double damageBase;
    private double damageInterval;

    public ExplosionBuilder setPosition(@NonNull Point2D position) {
        this.position = position;
        return this;
    }

    public ExplosionBuilder setStartingTime(long startingTime) {
        this.startingTime = startingTime;
        return this;
    }

    public ExplosionBuilder setRadius(double radius) {
        this.radius = radius;
        return this;
    }

    public ExplosionBuilder setDuration(double duration) {
        this.duration = duration;
        return this;
    }

    public ExplosionBuilder setDamageBase(double damageBase) {
        this.damageBase = damageBase;
        return this;
    }

    public ExplosionBuilder setDamageInterval(double damageInterval) {
        this.damageInterval = damageInterval;
        return this;
    }

    public Explosion createExplosion() {
        Validate.notNull(position);
        return new Explosion(position, startingTime, radius, duration, damageBase, damageInterval);
    }
}