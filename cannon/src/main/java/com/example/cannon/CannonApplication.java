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

import java.io.FileNotFoundException;
import java.util.function.Consumer;

public class CannonApplication extends Application {
    public static final double HEIGHT = 600;
    public static final double WIDTH = 800;

    @Override public void start(Stage stage) throws FileNotFoundException {
        Group root = new Group();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Cannon");
        stage.sizeToScene();
        stage.setResizable(false);

        new Game(root, new SimpleWorldLoader("test.layout"), new Consumer<Game.FinishReason>() {
            @Override
            public void accept(Game.FinishReason finishReason)  {
                if (finishReason == Game.FinishReason.USER_EXIT) {
                    Platform.exit();
                } else if (finishReason == Game.FinishReason.USER_RESTART) {
                    Platform.runLater(() -> {
                        try {
                            new Game(root, new SimpleWorldLoader("test.layout"), this).start();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                } else if (finishReason == Game.FinishReason.TARGET_DESTROYED) {
                    Platform.runLater(() -> {
                        ButtonType restart = new ButtonType("Restart", ButtonBar.ButtonData.CANCEL_CLOSE);
                        ButtonType exit = new ButtonType("Exit", ButtonBar.ButtonData.OK_DONE);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Target destroyed", restart, exit);
                        alert.setTitle("Game finished");
                        alert.setHeaderText(null);
                        var buttonResult = alert.showAndWait();
                        if (buttonResult.isEmpty() || buttonResult.get() == exit) {
                            Platform.exit();
                        } else {
                            try {
                                new Game(root, new SimpleWorldLoader("test.layout"), this).start();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        }).start();

        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}