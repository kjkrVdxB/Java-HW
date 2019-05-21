package com.example.cannon.entity;

/** For entities that can be damaged in some way. Currently only {@code Target}. */
public interface Damageable {
    void dealDamage(double damage);
}
