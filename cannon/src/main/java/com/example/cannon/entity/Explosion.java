package com.example.cannon.entity;

import com.example.cannon.game.Game;
import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.example.cannon.Utils.drawCircle;
import static com.example.cannon.Utils.getTimeElapsedSeconds;
import static java.lang.Math.min;

/** An entity for an ongoing explosion. */
public class Explosion extends GameEntity implements Drawable {
    @NonNull
    private final Point2D position;
    @NonNull
    private final Type type;
    private final long startTime;

    public Explosion(@NonNull Type type, @NonNull Point2D position, long startTime) {
        this.type = type;
        this.position = position;
        this.startTime = startTime;
    }

    private double currentRadius() {
        return type.getRadius() * min(getTimeElapsedSeconds(startTime, getWorld().getCurrentTime()) / type.getDuration(), 1);
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.rgb(255, 0, 0, 0.5));
        double currentRadius = currentRadius();
        drawCircle(graphicsContext, position, currentRadius);
        graphicsContext.restore();
    }

    @Override
    public int layer() {
        return 0;
    }

    @Override
    public void update() {
        if (checkTargetCollision()) {
            getWorld().finishGame(Game.FinishReason.TARGET_HIT);
        }
        if (getTimeElapsedSeconds(startTime, getWorld().getCurrentTime()) > type.getDuration()) {
            disappear();
        }
    }

    private boolean checkTargetCollision() {
        var target = getWorld().getTarget();
        return position.distance(target.getPosition()) < currentRadius() + target.getRadius();
    }

    public static class Type {
        private final double radius;
        private final double duration;

        public Type(double radius, double duration) {
            this.radius = radius;
            this.duration = duration;
        }

        public double getRadius() {
            return radius;
        }

        public double getDuration() {
            return duration;
        }
    }
}
