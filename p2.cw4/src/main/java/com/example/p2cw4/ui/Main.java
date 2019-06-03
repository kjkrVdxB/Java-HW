package com.example.p2cw4.ui;

import com.example.p2cw4.FTPClient;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

public class Main extends Application {
    private VBox root;
    private ListView<FTPClient.ListingItem> listView;
    private ObservableList<FTPClient.ListingItem> items = FXCollections.observableArrayList();
    private HBox bottomMainHBox;
    private Button button;

    private HBox bottomParametersHBox;
    private TextField adressField;
    private TextField portField;

    private StackPane bottomTextStackPane;
    private Text bottomText;

    private Image folderImage;
    private Image fileImage;
    private Image upImage;

    private FTPClient client = new FTPClient();
    private String currentPath;

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        initializeViews();
        loadImages();
        setupListView();

        setMainScreen();

        stage.setTitle("FTP Client");
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    private void connectAction() {
        button.setDisable(true);

        String address = adressField.getCharacters().toString().trim();
        String portString = portField.getCharacters().toString().trim();
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException exception) {
            setMessage("port must be integer", true);
            setDelayedAction(this::setMainScreen);
            return;
        }

        setMessage("connecting", false);

        var task = new Task<List<FTPClient.ListingItem>>() {
            @Override
            protected List<FTPClient.ListingItem> call() throws Exception {
                client.connect(address, port);
                currentPath = ".";
                return client.executeList(currentPath.toString());
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                items.clear();
                items.add(new FTPClient.ListingItem(FTPClient.ListingItem.Type.DIRECTORY, "../"));
                items.addAll(getValue());
                setConnectedScreen();
            }

            @Override
            protected void failed() {
                super.failed();
                setMessage("could not connect", true);
                setDelayedAction(() -> setMainScreen());
            }
        };

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void disconnectAction() {
        button.setDisable(true);
        setMessage("disconnecting", false);

        var task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                client.disconnect();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                setMainScreen();
            }

            @Override
            protected void failed() {
                super.failed();
                setMessage("could not disconnect properly", true);
                client = new FTPClient();
                setDelayedAction(() -> setMainScreen());
            }
        };

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void setMessage(String message, boolean isError) {
        bottomText.setText(message);
        if (isError) {
            bottomText.setStyle("-fx-fill: red");
        } else {
            bottomText.setStyle("-fx-fill: black");
        }

        bottomMainHBox.getChildren().remove(0);
        bottomMainHBox.getChildren().add(0, bottomTextStackPane);
    }

    private void setConnectedScreen() {
        setMessage("connected", false);
        button.setText("disconnect");
        button.setDisable(false);
        button.setOnAction(event -> disconnectAction());
    }

    private void setMainScreen() {
        items.clear();
        bottomMainHBox.getChildren().remove(0);
        bottomMainHBox.getChildren().add(0, bottomParametersHBox);
        button.setText("connect");
        button.setDisable(false);
        button.setOnAction(event -> connectAction());
    }

    private void setDelayedAction(Runnable action) {
        // FIXME add constant
        var pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> action.run());
        pause.play();
    }

    private void initializeViews() throws Exception {
        root = FXMLLoader.load(Main.class.getResource("/mainLayout.fxml").toURI().toURL());

        listView = new ListView<>(items);
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.getChildren().add(0, listView);

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

    private void loadImages() throws URISyntaxException, MalformedURLException {
        folderImage = new Image(Main.class.getResource("/folder.png").toURI().toURL().toString(),
                0, 15, true, false);
        fileImage = new Image(Main.class.getResource("/file.png").toURI().toURL().toString(),
                0, 15, true, false);
        upImage = new Image(Main.class.getResource("/up.png").toURI().toURL().toString(),
                0, 15, true, false);

    }

    private void setupListView() {
        listView.setCellFactory(listView -> new ListCell<>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(FTPClient.ListingItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setOnMouseClicked(null);
                } else {
                    if (item.getName().equals("../")) {
                        imageView.setImage(upImage);
                    } else if (item.getType() == FTPClient.ListingItem.Type.DIRECTORY) {
                        imageView.setImage(folderImage);
                    } else {
                        imageView.setImage(fileImage);
                    }

                    if (item.getType() == FTPClient.ListingItem.Type.DIRECTORY) {
                        setOnMouseClicked(event -> {
                            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2) {
                                walkAction(item.getName());
                            }
                        });
                    } else {
                        setOnMouseClicked(null);
                    }

                    listView.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            walkAction(listView.getSelectionModel().getSelectedItem().getName());
                        }
                    });

                    setGraphicTextGap(20);
                    setText(item.getName());
                    setGraphic(imageView);
                }
            }
        });

    }

    private void walkAction(String relativePath) {
        setMessage("updating", false);
        var task = new Task<List<FTPClient.ListingItem>>() {
            @Override
            protected List<FTPClient.ListingItem> call() throws Exception {
                try {
                    currentPath = currentPath + "/" + relativePath;
                    return client.executeList(currentPath);
                } catch(Exception except) {
                    except.printStackTrace();
                    throw except;
                }
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                items.clear();
                items.add(new FTPClient.ListingItem(FTPClient.ListingItem.Type.DIRECTORY, "../"));
                items.addAll(getValue());
                setConnectedScreen();
            }

            @Override
            protected void failed() {
                super.failed();
                setMessage("updating error", true);
                setDelayedAction(() -> setConnectedScreen());
            }
        };

        Thread backgroundThread = new Thread(task);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
}
