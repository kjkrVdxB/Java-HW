package com.example.p2cw4.ui;

import com.example.p2cw4.FTPClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.IOException;
import java.util.List;

public class Main extends Application {
    private VBox root;
    private ListView listView;
    private HBox bottomMainHBox;
    private Button button;

    private HBox bottomParametersHBox;
    private TextField adressField;
    private TextField portField;

    private StackPane bottomTextStackPane;
    private Text bottomText;

    @Override
    public void start(Stage stage) throws Exception {
        initializeViews();

        button.setOnAction(event -> {
            var client = new FTPClient();
            try {
                client.connect(adressField.toString(), Integer.parseInt(portField.toString()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<ImmutablePair<String, Boolean>> list = null;
            try {
                list = client.executeList(".");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        stage.setTitle("FTP Client");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void initializeViews() throws Exception {
        root = FXMLLoader.load(Main.class.getResource("/mainLayout.fxml").toURI().toURL());
        listView = (ListView) root.lookup("#listView");
        bottomMainHBox = (HBox) root.lookup("#bottomMainHBox");
        button = (Button) root.lookup("#button");

        bottomParametersHBox = (HBox) root.lookup("#bottomParametersHBox");
        adressField = (TextField) root.lookup("#adressField");
        portField = (TextField) root.lookup("#portField");

        constructTextStackPane();
    }

    private void constructTextStackPane() {
        bottomTextStackPane = new StackPane();
        bottomTextStackPane.setAlignment(Pos.CENTER);
        HBox.setHgrow(bottomTextStackPane, Priority.ALWAYS);
        bottomText = new Text();
        bottomTextStackPane.getChildren().add(bottomText);
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
