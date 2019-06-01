package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import org.checkerframework.checker.nullness.qual.NonNull;

/** An interface representing weapons, that is objects that can generate projectiles. */
public interface Weapon {
    @NonNull
    GameEntity getProjectile(@NonNull Point2D start, @NonNull Point2D startingDirection, long launchingTime);

    @NonNull
    String getName();

    double getRateLimit();

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
