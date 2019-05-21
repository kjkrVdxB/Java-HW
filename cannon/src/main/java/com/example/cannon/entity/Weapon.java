package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.ThreadLocalRandom;

import static com.example.cannon.Utils.*;

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
        double rateLimit = Double.POSITIVE_INFINITY;
        double startingSpeed = 1;
        double angleStandardDeviation = 0;
        Explosion.Type explosionType = null;

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

        public BasicWeaponBuilder withStartingSpeed(double startingSpeed) {
            this.startingSpeed = startingSpeed;
            return this;
        }

        public BasicWeaponBuilder angleStandardDeviation(double angleStandardDeviation) {
            this.angleStandardDeviation = angleStandardDeviation;
            return this;
        }

        public BasicWeaponBuilder withExplosionType(Explosion.Type explosionType) {
            this.explosionType = explosionType;
            return this;
        }

        @NonNull
        Point2D updateStartingSpeed(@NonNull Point2D startingSpeed) {
            startingSpeed = rotate(startingSpeed, ThreadLocalRandom.current().nextGaussian() * 0.5 * angleStandardDeviation);
            startingSpeed = startingSpeed.multiply(BasicWeaponBuilder.this.startingSpeed);
            return startingSpeed;
        }

        Weapon build() {
            return new Weapon() {
                @Override
                public @NonNull GameEntity getProjectile(@NonNull Point2D startingPosition,
                                                         @NonNull Point2D startingSpeed,
                                                         long launchingTime) {
                    startingSpeed = updateStartingSpeed(startingSpeed);
                    return new BasicProjectile(startingPosition, startingSpeed, launchingTime, bombRadius, bombBoost) {
                        @Override
                        protected void explode(@NonNull Point2D position) {
                            if (explosionType != null) {
                                spawn(new Explosion(explosionType, position, getWorld().getCurrentTime()));
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
        Weapon build() {
            return new Weapon() {
                @Override
                public @NonNull GameEntity getProjectile(@NonNull Point2D startingPosition,
                                                         @NonNull Point2D startingSpeed,
                                                         long launchingTime) {
                    double boomTime =  Math.log(1 - ThreadLocalRandom.current().nextDouble())/(-2);
                    startingSpeed = updateStartingSpeed(startingSpeed);
                    return new BasicProjectile(startingPosition, startingSpeed, launchingTime, bombRadius, bombBoost) {
                        @Override
                        public void update() {
                            if (getTimeElapsedSeconds(this.launchingTime, getWorld().getCurrentTime()) > boomTime) {
                                explode(getPosition());
                            }
                            super.update();
                        }

                        @Override
                        protected void explode(@NonNull Point2D position) {
                            var piecesCount = ThreadLocalRandom.current().nextInt(3, 6);
                            spawn(new Explosion(new Explosion.Type(60, 0.5), position, getWorld().getCurrentTime()));
                            for (int i = 0; i < piecesCount; ++i) {
                                Point2D direction = vectorByAngle(Math.random() * 2 * Math.PI);
                                spawn(GRENADE_LAUNCHER.getProjectile(position.add(direction.multiply(15)), direction, getWorld().getCurrentTime()));
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
            .build();
    Weapon NUKE = new BasicWeaponBuilder()
            .withName("Nuke")
            .withBombRadius(10)
            .withStartingSpeed(500)
            .angleStandardDeviation(0.1)
            .withExplosionType(new Explosion.Type(200, 10))
            .build();
    Weapon PISTOL = new BasicWeaponBuilder()
            .withName("Pistol")
            .withBombRadius(3)
            .withStartingSpeed(1000)
            .angleStandardDeviation(0.05)
            .build();
    Weapon GRENADE_LAUNCHER = new BasicWeaponBuilder()
            .withName("Grenade Launcher")
            .withBombRadius(5)
            .withStartingSpeed(600)
            .angleStandardDeviation(0.1)
            .withRateLimit(0.5)
            .withExplosionType(new Explosion.Type(30, 0.5))
            .build();
    Weapon RIFLE = new BasicWeaponBuilder()
            .withName("Rifle")
            .withBombRadius(3)
            .angleStandardDeviation(0.0001)
            .withStartingSpeed(3000)
            .build();
    Weapon FUNKY_BOMB = new FunkyBombBuilder()
            .withName("Funky Bomb")
            .withBombRadius(5)
            .withStartingSpeed(600)
            .angleStandardDeviation(0.2)
            .withBombBoost(1.5)
            .withRateLimit(0.5)
            .build();
}
