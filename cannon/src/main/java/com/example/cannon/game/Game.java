package com.example.cannon.game;

import com.example.cannon.entity.Weapon;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Consumer;

import static com.example.cannon.CannonApplication.HEIGHT;
import static com.example.cannon.CannonApplication.WIDTH;

/** A class representing a game instance. */
public class Game extends AnimationTimer {
    @NonNull
    private static final SortedMap<Integer, Weapon> digitToWeapon =
            new TreeMap<>(Map.ofEntries(Map.entry(1, Weapon.PISTOL),
                                        Map.entry(2, Weapon.MACHINE_GUN),
                                        Map.entry(3, Weapon.GRENADE_LAUNCHER),
                                        Map.entry(4, Weapon.NUKE),
                                        Map.entry(5, Weapon.RIFLE),
                                        Map.entry(6, Weapon.FUNKY_BOMB)));
    /** Currently pressed keys. For use in input processing */
    @NonNull
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    @NonNull
    private final World world;
    @NonNull
    private final GraphicsContext graphicsContext;
    @NonNull
    private final Text currentWeaponText = new Text(10, 30, null);
    @NonNull
    private final Group root;
    private final World.@NonNull WorldProvider worldProvider;
    private boolean finished = false;
    @NonNull
    private final Consumer<FinishReason> onFinishCallback;

    public Game(@NonNull Group root, World.@NonNull WorldProvider worldProvider, @NonNull Consumer<FinishReason> onFinishCallback) {
        world = new World(this, worldProvider);
        this.root = root;
        this.worldProvider = worldProvider;
        this.onFinishCallback = onFinishCallback;
        root.getScene().setOnKeyPressed(this::onKeyPressed);
        root.getScene().setOnKeyReleased(this::onKeyReleased);

        var canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        selectWeapon(2 ); // Default weapon
        initOverlay();
    }

    private void initOverlay() {
        currentWeaponText.setFont(Font.font(null, FontWeight.BOLD, 20));
        root.getChildren().add(currentWeaponText);

        var helper = new StringBuilder("Available weapons:\n");
        for (var entry: digitToWeapon.entrySet()) {
            helper.append(entry.getKey()).append(": ").append(entry.getValue().getName()).append('\n');
        }
        helper.append("Any other: Disable");
        var text = new Text(10, 50, helper.toString());
        root.getChildren().add(text);
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
        world.getCannon().setLaunching(pressedKeys.contains(KeyCode.ENTER) || pressedKeys.contains(KeyCode.SPACE));
        for (var key: pressedKeys) {
            if (key.isDigitKey()) {
                selectWeapon(key.getName().charAt(0) - '0');
            }
        }
        if (pressedKeys.contains(KeyCode.Q)) {
            finish(FinishReason.USER_EXIT);
        }
        if (pressedKeys.contains(KeyCode.R)) {
            finish(FinishReason.USER_RESTART);
        }
    }

    /** Select a weapon for cannon given a number on keyboard. */
    private void selectWeapon(int number) {
        var weapon = digitToWeapon.get(number);
        world.getCannon().selectWeapon(weapon);
        setCurrentWeaponText(weapon == null ? "None": weapon.getName());
    }

    private void setCurrentWeaponText(@NonNull String text) {
        currentWeaponText.setText("Current weapon: " + text);
    }

    void finish(@NonNull FinishReason reason) {
        if (finished) {
            return;
        }
        finished = true;
        stop();
        Platform.runLater(() -> onFinishCallback.accept(reason));
    }

    @Override
    public void handle(long now) {
        world.update(now);
        world.draw(graphicsContext);
    }

    public enum FinishReason {
        USER_EXIT,
        USER_RESTART,
        TARGET_DESTROYED
    }
}
