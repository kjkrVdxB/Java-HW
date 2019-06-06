package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ThreadLocalRandom;

import static com.example.cannon.Utils.getTimeElapsedSeconds;
import static com.example.cannon.Utils.vectorByAngle;

class FunkyBombBuilder extends BasicWeaponBuilder {
    public Weapon createWeapon() {
        return new Weapon() {
            @Override
            public @NonNull GameEntity getProjectile(@NonNull Point2D startingPosition,
                                                     @NonNull Point2D startingSpeed,
                                                     long launchingTime) {
                double boomTime = Math.log(1 - ThreadLocalRandom.current().nextDouble()) / (-2);
                startingSpeed = updateStartingSpeed(startingSpeed);
                return new BasicProjectile(startingPosition, startingSpeed, launchingTime, bombRadius, bombBoost, bombCollisionDamage) {
                    @Override
                    public void update() {
                        if (getTimeElapsedSeconds(this.launchingTime, getWorld().getCurrentTime()) > boomTime) {
                            explode(getPositionAtWorldTime(getWorld().getCurrentTime()), getWorld().getCurrentTime());
                        } else {
                            super.update();
                        }
                    }

                    @Override
                    protected void explode(@NonNull Point2D position, long timeNano) {
                        var piecesCount = ThreadLocalRandom.current().nextInt(3, 6);
                        spawn(new ExplosionBuilder()
                                      .setPosition(position)
                                      .setStartingTime(timeNano)
                                      .setRadius(30)
                                      .setDuration(0.5)
                                      .setDamageBase(1)
                                      .setDamageInterval(0.02)
                                      .createExplosion());
                        for (int i = 0; i < piecesCount; ++i) {
                            Point2D direction = vectorByAngle(Math.random() * 2 * Math.PI);
                            spawn(GRENADE_LAUNCHER.getProjectile(position.add(direction.multiply(15)), direction, timeNano));
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