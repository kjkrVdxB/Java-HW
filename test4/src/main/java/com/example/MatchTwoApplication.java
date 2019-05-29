package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static java.lang.System.exit;

public class MatchTwoApplication extends Application {
    private final static int WINDOW_HEIGHT = 300;
    private final static int WINDOW_WIDTH = 300;
    private final static int MIN_WINDOW_HEIGHT = 200;
    private final static int MIN_WINDOW_WIDTH = 200;
    private final static int MISMATCH_OPENED_DELAY_MILLISECONDS = 2000;

    private int fieldSize;
    private MatchTwoField matchTwoField;
    private Button[][] buttonsGrid;

    @Override
    public void start(Stage stage) {
        var parameters = getParameters();
        var unnamedParameters = parameters.getRaw();

        if (unnamedParameters.size() != 1) {
            showUsageErrorAndExit();
        }
        try {
            fieldSize = Integer.valueOf(unnamedParameters.get(0));
        } catch (NumberFormatException e) {
            showUsageErrorAndExit();
        }

        if (fieldSize % 2 != 0) {
            showUsageErrorAndExit();
        }

        matchTwoField = new MatchTwoField(fieldSize);

        stage.setTitle("Match two");
        var fieldGrid = new GridPane();
        buttonsGrid = new Button[fieldSize][fieldSize];

        for (int row = 0; row < fieldSize; ++row) {
            var rowConstraints = new RowConstraints();
            rowConstraints.setFillHeight(true);
            rowConstraints.setVgrow(Priority.ALWAYS);
            rowConstraints.setPercentHeight(100.0 / fieldSize);
            fieldGrid.getRowConstraints().add(rowConstraints);
        }
        for (int col = 0; col < fieldSize; ++col) {
            var columnConstraints = new ColumnConstraints();
            columnConstraints.setFillWidth(true);
            columnConstraints.setHgrow(Priority.ALWAYS);
            columnConstraints.setPercentWidth(100.0 / fieldSize);
            fieldGrid.getColumnConstraints().add(columnConstraints);
        }

        for (int i = 0; i < fieldSize; i++) {
            for (int j = 0; j < fieldSize; ++j) {
                var button = createButton(i, j);
                buttonsGrid[i][j] = button;
                fieldGrid.add(button, i, j);
            }
        }

        stage.setScene(new Scene(fieldGrid));
        stage.setHeight(WINDOW_HEIGHT);
        stage.setWidth(WINDOW_WIDTH);
        stage.setMinWidth(MIN_WINDOW_WIDTH);
        stage.setMinHeight(MIN_WINDOW_HEIGHT);
        stage.show();
    }

    private Button createButton(int i, int j) {
        Button button = new Button();
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setOnAction(e -> handleButtonPress(i, j, e));
        return button;
    }

    private void handleButtonPress(int i, int j, ActionEvent e) {
        Button source = (Button) e.getSource();
        Integer numberToShow = matchTwoField.open(ImmutablePair.of(i, j));
        if (numberToShow != null) {
            source.setText(String.valueOf(numberToShow));
            if (matchTwoField.finishedOpening()) {
                if (matchTwoField.matched()) {
                    matchTwoField.completeOpening();
                    if (matchTwoField.finished()) {
                        alertAndExit("Good job!");
                    }
                    return;
                }
                new Thread(() -> {
                    try {
                        Thread.sleep(MISMATCH_OPENED_DELAY_MILLISECONDS);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(() -> {
                        Pair<Integer, Integer> firstPosition = matchTwoField.getFirstOpened();
                        Pair<Integer, Integer> secondOpened = matchTwoField.getSecondOpened();
                        buttonsGrid[firstPosition.getLeft()][firstPosition.getRight()].setText("");
                        buttonsGrid[secondOpened.getLeft()][secondOpened.getRight()].setText("");
                        matchTwoField.completeOpening();
                    });
                }).start();
            }
        }
    }

    private static void alertAndExit(String message) {
        var alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Game finished");
        alert.showAndWait();
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static void showUsageErrorAndExit() {
        System.out.println("Expected one even integer argument - field size");
        Platform.exit();
        exit(0);
    }

}
