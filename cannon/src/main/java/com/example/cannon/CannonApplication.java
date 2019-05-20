package com.example.cannon;

import com.example.cannon.game.Game;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CannonApplication extends Application {
    public static final double HEIGHT = 600;
    public static final double WIDTH = 800;

    @Override public void start(Stage stage) {
        Group root = new Group();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Cannon");
        stage.setResizable(false);

        var gameState = new Game(root);

        new AnimationTimer() {
            @Override
            public void handle(long nowNano) {
                gameState.update(nowNano);
            }
        }.start();

        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}