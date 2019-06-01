package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ThreadLocalRandom;

import static com.example.cannon.Utils.*;

/** An interface representing weapons, that is objects that can generate projectiles. */
public interface Weapon {
    @NonNull
    GameEntity getProjectile(@NonNull Point2D start, @NonNull Point2D startingDirection, long launchingTime);

    @NonNull
    String getName();

    double getRateLimit();

    class BasicWeaponBuilder {
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
        Point2D updateStartingSpeed(@NonNull Point2D startingSpeed) {
            startingSpeed = rotate(startingSpeed, ThreadLocalRandom.current().nextGaussian() * 0.5 * angleStandardDeviation);
            startingSpeed = startingSpeed.multiply(BasicWeaponBuilder.this.startingSpeed);
            return startingSpeed;
        }

        Weapon createWeapon() {
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

    class FunkyBombBuilder extends BasicWeaponBuilder {
        Weapon createWeapon() {
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

    Weapon MACHINE_GUN = new BasicWeaponBuilder()
            .withName("Machine Gun")
            .withBombRadius(2)
            .withStartingSpeed(2000)
            .angleStandardDeviation(0.05)
            .withRateLimit(0.003)
            .withBombBoost(0.5)
            .withBombCollisionDamage(0.1)
            .createWeapon();
    Weapon NUKE = new BasicWeaponBuilder()
            .withName("Nuke")
            .withBombRadius(10)
            .withStartingSpeed(500)
            .angleStandardDeviation(0.1)
            .withExplosionType(new ExplosionBuilder()
                                       .setRadius(200)
                                       .setDuration(10)
                                       .setDamageBase(2)
                                       .setDamageInterval(0.02))
            .withBombCollisionDamage(1)
            .createWeapon();
    Weapon PISTOL = new BasicWeaponBuilder()
            .withName("Pistol")
            .withBombRadius(3)
            .withStartingSpeed(1000)
            .angleStandardDeviation(0.05)
            .withBombCollisionDamage(2)
            .createWeapon();
    Weapon GRENADE_LAUNCHER = new BasicWeaponBuilder()
            .withName("Grenade Launcher")
            .withBombRadius(5)
            .withStartingSpeed(600)
            .angleStandardDeviation(0.1)
            .withBombCollisionDamage(1)
            .withRateLimit(0.5)
            .withExplosionType(new ExplosionBuilder()
                                       .setRadius(30)
                                       .setDuration(0.1)
                                       .setDamageBase(20)
                                       .setDamageInterval(0.02))
            .createWeapon();
    Weapon RIFLE = new BasicWeaponBuilder()
            .withName("Rifle")
            .withBombRadius(3)
            .angleStandardDeviation(0.0001)
            .withStartingSpeed(3000)
            .withBombBoost(0.5)
            .withBombCollisionDamage(30)
            .createWeapon();
    Weapon FUNKY_BOMB = new FunkyBombBuilder()
            .withName("Funky Bomb")
            .withBombRadius(5)
            .withStartingSpeed(600)
            .angleStandardDeviation(0.2)
            .withBombBoost(1.5)
            .withBombCollisionDamage(5)
            .withRateLimit(0.5)
            .createWeapon();
}
