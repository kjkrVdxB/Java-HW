package com.example.cannon;

import com.example.cannon.game.Game;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.FileNotFoundException;
import java.util.function.Consumer;

public class CannonApplication extends Application {
    public static final double HEIGHT = 600;
    public static final double WIDTH = 800;
    private static String worldMapFileName = "test.layout";

    @Override
    public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Cannon");
        stage.sizeToScene();
        stage.setResizable(false);

        var onFinish = new Consumer<Game.FinishReason>() {
            @Override
            public void accept(Game.FinishReason finishReason) {
                if (finishReason == Game.FinishReason.USER_EXIT) {
                    Platform.exit();
                } else if (finishReason == Game.FinishReason.USER_RESTART) {
                    Platform.runLater(() -> {
                        startGame(root, worldMapFileName, this);
                    });
                } else if (finishReason == Game.FinishReason.TARGET_DESTROYED) {
                    Platform.runLater(() -> {
                        ButtonType restart = new ButtonType("Restart", ButtonBar.ButtonData.CANCEL_CLOSE);
                        ButtonType exit = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Target destroyed.", restart, exit);
                        alert.setTitle("Game finished");
                        alert.setHeaderText(null);
                        var buttonResult = alert.showAndWait();
                        if (buttonResult.isEmpty() || buttonResult.get() == exit) {
                            Platform.exit();
                        } else {
                            startGame(root, worldMapFileName, this);
                        }
                    });
                }
            }
        };
        startGame(root, worldMapFileName, onFinish);

        stage.show();
    }

    private void startGame(@NonNull Group root, @NonNull String filename, Consumer<Game.FinishReason> onFinishCallBack) {
        try {
            new Game(root, new SimpleWorldLoader(filename), onFinishCallBack).start();
        } catch (FileNotFoundException e) {
            showFileNotFoundAlert(filename);
        }
    }

    private void showFileNotFoundAlert(@NonNull String filename) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Game loading error");
        alert.setHeaderText(null);
        alert.setContentText("World map file not found: " + filename);
        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}