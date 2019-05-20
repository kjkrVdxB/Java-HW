package com.example.cannon.game;

import com.example.cannon.entity.Projectile;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashSet;
import java.util.Set;

import static com.example.cannon.CannonApplication.HEIGHT;
import static com.example.cannon.CannonApplication.WIDTH;
import static java.lang.System.exit;

/** A class representing a game instance. */
public class Game {
    /** Currently pressed key. For use in input processing */
    @NonNull
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    @NonNull
    private final World world = new World(this);
    @NonNull
    private final GraphicsContext graphicsContext;
    @NonNull
    private final Text currentWeaponText = new Text(10, 30, "");

    public Game(@NonNull Group root) {
        root.getScene().setOnKeyPressed(this::onKeyPressed);
        root.getScene().setOnKeyReleased(this::onKeyReleased);

        var canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        selectWeapon(1);
        currentWeaponText.setFont(Font.font(null, FontWeight.BOLD, 20));
        root.getChildren().add(currentWeaponText);
    }

    public void update(long timeNano) {
        world.update(timeNano);
        world.draw(graphicsContext);
    }

    private <T extends KeyEvent> void onKeyPressed(T event) {
        pressedKeys.add(event.getCode());
        onKeysPressedUpdate();
        event.consume();
    }

    private <T extends KeyEvent> void onKeyReleased(T event) {
        pressedKeys.remove(event.getCode());
        onKeysPressedUpdate();
        event.consume();
    }

    /** Fire events for currently pressed keys. */
    private void onKeysPressedUpdate() {
        if (pressedKeys.contains(KeyCode.LEFT) && !pressedKeys.contains(KeyCode.RIGHT)) {
            world.getCannon().setMovingDirection(-1);
        } else if (pressedKeys.contains(KeyCode.RIGHT) && !pressedKeys.contains(KeyCode.LEFT)) {
            world.getCannon().setMovingDirection(1);
        } else {
            world.getCannon().setMovingDirection(0);
        }
        if (pressedKeys.contains(KeyCode.UP) && !pressedKeys.contains(KeyCode.DOWN)) {
            world.getCannon().setAngleMovingDirection(1);
        } else if (pressedKeys.contains(KeyCode.DOWN) && !pressedKeys.contains(KeyCode.UP)) {
            world.getCannon().setAngleMovingDirection(-1);
        } else {
            world.getCannon().setAngleMovingDirection(0);
        }
        world.getCannon().setLaunching(pressedKeys.contains(KeyCode.SPACE));
        for (var key: pressedKeys) {
            if (key.isDigitKey()) {
                selectWeapon(key.getName().charAt(0) - '0');
            }
        }
    }

    /** Select a weapon for cannon given a number on keyboard. */
    private void selectWeapon(int number) {
        Projectile.WeaponType weaponType;
        switch (number) {
            case 1:
                weaponType = Projectile.WeaponType.Pistol;
                break;
            case 2:
                weaponType = Projectile.WeaponType.Nuke;
                break;
            case 3:
                weaponType = Projectile.WeaponType.MachineGun;
                break;
            case 4:
                weaponType = Projectile.WeaponType.GrenadeLauncher;
                break;
            case 0:
                weaponType = null;
                break;
            default:
                return;
        }
        world.getCannon().selectWeapon(weaponType);
        setCurrentWeaponText(weaponType == null ? "None": weaponType.getName());
    }

    private void setCurrentWeaponText(@NonNull String text) {
        currentWeaponText.setText("Current weapon: " + text);
    }

    void finish() {
        exit(0); // TODO show a dialog or something
    }
}
