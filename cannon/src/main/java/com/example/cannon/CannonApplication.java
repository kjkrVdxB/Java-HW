package com.example.cannon;

import com.example.cannon.game.Game;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileNotFoundException;

public class CannonApplication extends Application {
    public static final double HEIGHT = 600;
    public static final double WIDTH = 800;

    @Override public void start(Stage stage) throws FileNotFoundException {
        Group root = new Group();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Cannon");
        stage.setResizable(false);

        new Game(root, new SimpleWorldLoader("test.layout")).start();

        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}