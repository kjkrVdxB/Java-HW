package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.example.cannon.Utils.*;
import static java.lang.Math.*;

/** An entity for an ongoing explosion. */
public class Explosion extends GameEntity implements Drawable {
    private double radius;
    private double duration;
    private Point2D position;
    private long startingTime;
    private double damageBaseStrength;
    private double damageInterval;
    private boolean startedDamaging;
    private long lastDamaged;

    public Explosion(@NonNull Point2D position, long startingTime, double radius, double duration, double damageBaseStrength, double damageInterval) {
        this.position = position;
        this.startingTime = startingTime;
        this.radius = radius;
        this.duration = duration;
        this.damageBaseStrength = damageBaseStrength;
        this.damageInterval = damageInterval;
        this.startedDamaging = false;
    }

    private double getProgress(long time) {
        return getTimeElapsedSeconds(startingTime, time) / duration;
    }

    private double getRelativeStrengthAt(long time) {
        double progress = getProgress(time);
        return 1 - sqrt(progress);
    }

    private double getStrengthAt(long time) {
        return damageBaseStrength * getRelativeStrengthAt(time);
    }

    private double getRadiusAt(long time) {
        return radius * getProgress(time);
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.rgb(255, 0, 0, getRelativeStrengthAt(getWorld().getCurrentTime())));
        drawCircle(graphicsContext, position, getRadiusAt(getWorld().getCurrentTime()));
        graphicsContext.restore();
    }

    @Override
    public int drawingLayer() {
        return 0;
    }

    @Override
    public void update() {
        damage();
        if (getTimeElapsedSeconds(startingTime, getWorld().getCurrentTime()) > duration) {
            disappear();
        }
    }

    /** Deal all the damage missed in the current frame with damageInterval intervals */
    private void damage() {
        if (damageInterval == Double.POSITIVE_INFINITY) {
            if (!startedDamaging) {
                damageOnce(getWorld().getCurrentTime());
            }
            return;
        }
        long nanoDamageInterval = nanoFromSeconds(damageInterval);
        long time = startedDamaging ? lastDamaged + nanoDamageInterval : startingTime;
        while (time < getWorld().getCurrentTime() && getProgress(time) < 1) {
            damageOnce(time);
            time += nanoDamageInterval;
        }
    }

    private void damageOnce(long time) {
        damageTarget(time);
        damageTerrain(time);
        startedDamaging = true;
        lastDamaged = time;
    }

    private void damageTerrain(long time) {
        var vertices = getWorld().getTerrain().getVertices();
        var radius = getRadiusAt(time);
        for (int i = 0; i < vertices.size(); ++i) {
            var vertex = vertices.get(i);
            var xDiff = abs(vertex.getX() - position.getX());
            if (xDiff >= radius) {
                continue;
            }
            var fullIntersectionHalfLength = sqrt(radius * radius - xDiff * xDiff);
            var intersectionLength = max(0, min(2 * fullIntersectionHalfLength, position.getY() + fullIntersectionHalfLength - vertex.getY()));
            vertices.set(i, vertex.add(new Point2D(0, intersectionLength * getStrengthAt(time) * 0.03)));
        }
    }

    private void damageTarget(long time) {
        var target = getWorld().getTarget();
        if (target.getPosition().distance(position) < target.getRadius() + getRadiusAt(time)) {
            target.dealDamage(getStrengthAt(time));
        }
    }
}
