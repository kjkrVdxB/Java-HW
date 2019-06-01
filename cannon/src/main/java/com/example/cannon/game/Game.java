package com.example.cannon.game;

import com.example.cannon.entity.Cannon;
import com.example.cannon.entity.Weapon;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

    private static final int DEFAULT_WEAPON = 3; // By button number, see above. This is Grenade launcher.

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
    private boolean finished = false;
    @NonNull
    private final Consumer<FinishReason> onFinishCallback;

    public Game(@NonNull Group root, World.@NonNull WorldProvider worldProvider, @NonNull Consumer<FinishReason> onFinishCallback) {
        world = new World(this, worldProvider);
        this.root = root;
        this.onFinishCallback = onFinishCallback;
        root.getScene().setOnKeyPressed(this::onKeyPressed);
        root.getScene().setOnKeyReleased(this::onKeyReleased);

        var canvas = new Canvas(WIDTH, HEIGHT);
        graphicsContext = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        selectWeapon(DEFAULT_WEAPON);
        initOverlay();
    }

    /** Initialize the on-screen help. */
    private void initOverlay() {
        currentWeaponText.setFont(Font.font(null, FontWeight.BOLD, 20));
        root.getChildren().add(currentWeaponText);

        var helpBuilder = new StringBuilder();
        helpBuilder.append("Controls:\n" +
                           "Q: exit\n" +
                           "R: restart\n" +
                           "Arrow Left/Right: move the cannon\n" +
                           "Arrow Up/Down: change cannon angle (up - CCW, down - CW)\n" +
                           "Space/Enter: fire\n\n");
        helpBuilder.append("Available weapons:\n");
        for (var entry: digitToWeapon.entrySet()) {
            helpBuilder.append(entry.getKey()).append(": ").append(entry.getValue().getName()).append('\n');
        }
        helpBuilder.append("Any other: Disable\n");
        var helpText = new Text(10, 50, helpBuilder.toString());
        root.getChildren().add(helpText);
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
            world.getCannon().setMovingDirection(Cannon.MovingDirection.LEFT);
        } else if (pressedKeys.contains(KeyCode.RIGHT) && !pressedKeys.contains(KeyCode.LEFT)) {
            world.getCannon().setMovingDirection(Cannon.MovingDirection.RIGHT);
        } else {
            world.getCannon().setMovingDirection(Cannon.MovingDirection.NONE);
        }
        if (pressedKeys.contains(KeyCode.UP) && !pressedKeys.contains(KeyCode.DOWN)) {
            world.getCannon().setRotationDirection(Cannon.RotationDirection.CCW);
        } else if (pressedKeys.contains(KeyCode.DOWN) && !pressedKeys.contains(KeyCode.UP)) {
            world.getCannon().setRotationDirection(Cannon.RotationDirection.CW);
        } else {
            world.getCannon().setRotationDirection(Cannon.RotationDirection.NONE);
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
        setCurrentWeaponText(weapon == null ? "None" : weapon.getName());
    }

    /** Set the displayed text of current weapon. */
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

    /** Handle one screen update. */
    @Override
    public void handle(long timeNano) {
        world.update(timeNano);
        world.draw(graphicsContext);
    }

    /** Reason why the game should be stopped. */
    public enum FinishReason {
        /** User requested exit. */
        USER_EXIT,
        /** User requested restart. */
        USER_RESTART,
        /** The goal was accomplished */
        TARGET_DESTROYED
    }
}
