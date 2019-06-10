package com.example.ftpgui.gui;

import com.example.ftpgui.FTPClient;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/** JavaFX graphical user interface which wraps the FTPClient class */
public class FTPClientGUI extends Application {
    private static final Background ERROR_BACKGROUND = new Background(
            new BackgroundFill(Color.web("FFA1A1"), CornerRadii.EMPTY, Insets.EMPTY));
    private static final Background CONNECTED_BACKGROUND = new Background(
            new BackgroundFill(Color.web("BCFFBC"), CornerRadii.EMPTY, Insets.EMPTY));
    private static final Insets REFRESH_BUTTON_INSTES = new Insets(1, 1, 1, 5);

    private static final int MIN_WIDTH = 300;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int ICONS_HEIGHT = 15;
    private static final int LIST_VIEW_ITEM_TEXT_GAP = 20;
    private static final int DELAY_MILLIS = 2000;
    private static final int CONNECTION_TIMEOUT_MILLIS = 2000;

    private static final String LAYOUT_FXML_RESOURCE_PATH = "/mainLayout.fxml";
    private static final String FOLDER_PNG_RESOURCE_PATH = "/folder.png";
    private static final String FILE_PNG_RESOURCE_PATH = "/file.png";
    private static final String UP_PNG_RESOURCE_PATH = "/up.png";

    private static final String BOTTOM_MAIN_HBOX_SELECTOR = "#bottomMainHBox";
    private static final String BUTTON_SELECTOR = "#button";
    private static final String BOTTOM_PARAMETERS_HBOX_SELECTOR = "#bottomParametersHBox";
    private static final String ADDRESS_FIELD_SELECTOR = "#addressField";
    private static final String PORT_FIELD_SELECTOR = "#portField";

    private static final String PORT_FORMAT_MESSAGE = "port must be integer";
    private static final String CONNECTING_MESSAGE = "connecting";
    private static final String CONNECTION_ERROR_MESSAGE = "could not connect";
    private static final String DISCONNECTING_MESSAGE = "disconnecting";
    private static final String DISCONNECTION_ERROR_MESSAGE = "could not disconnect properly";
    private static final String UPDATING_MESSAGE = "updating";
    private static final String UPDATING_ERROR_MESSAGE = "updating error";
    private static final String SAVING_FILE_MESSAGE = "saving file";
    private static final String DIRECTORY_NOT_CHOSEN_MESSAGE = "directory was not chosen properly";
    private static final String FILE_DOWNLOADED_MESSAGE = "file downloaded successfully";
    private static final String DOWNLOAD_ERROR_MESSAGE = "download error";

    private static final String DISCONNECT_BUTTON_TEXT = "disconnect";
    private static final String CONNECT_BUTTON_TEXT = "connect";
    private static final String REFRESH_BUTTON_TEXT = "refresh";
    private static final String TITLE = "FTP Client";
    private static final String UP_FOLDER_NAME = "../";
    private static final String FILE_CHOOSER_TITLE_PREFIX = "Choose directory to save \"";

    private VBox root;
    private ListView<FTPClient.ListingItem> listView;
    private ObservableList<FTPClient.ListingItem> items = FXCollections.observableArrayList();
    private HBox bottomMainHBox;
    private Button mainButton;
    private Button refreshButton;

    private HBox bottomParametersHBox;
    private TextField addressField;
    private TextField portField;

    private StackPane bottomTextStackPane;
    private Text bottomText;

    private Image folderImage;
    private Image fileImage;
    private Image upImage;

    private Stage mainStage;
    private FTPClient client = new FTPClient();
    private Path currentPath;
    private Task currentTask;
    private PauseTransition pauseTransition;

    /** Launches GUI application */
    public static void main(String[] args) {
        Application.launch(FTPClientGUI.class, args);
    }

    /**
     * The main entry point for JavaFX Application.
     *
     * @throws IOException if resources could not be read properly.
     */
    @Override
    public void start(@NonNull Stage stage) throws IOException {
        mainStage = stage;

        initializeViews();
        loadImages();
        setupListView();

        setMainScreen();

        stage.setTitle(TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        stage.show();
    }

    private void initializeViews() throws IOException {
        root = FXMLLoader.load(FTPClientGUI.class.getResource(LAYOUT_FXML_RESOURCE_PATH));

        listView = new ListView<>(items);
        VBox.setVgrow(listView, Priority.ALWAYS);
        root.getChildren().add(0, listView);

        bottomMainHBox = (HBox) root.lookup(BOTTOM_MAIN_HBOX_SELECTOR);
        mainButton = (Button) root.lookup(BUTTON_SELECTOR);
        constructRefreshButton();

        bottomParametersHBox = (HBox) root.lookup(BOTTOM_PARAMETERS_HBOX_SELECTOR);
        addressField = (TextField) root.lookup(ADDRESS_FIELD_SELECTOR);
        portField = (TextField) root.lookup(PORT_FIELD_SELECTOR);

        constructTextStackPane();
    }

    private void constructRefreshButton() {
        refreshButton = new Button(REFRESH_BUTTON_TEXT);
        refreshButton.setMinWidth(Double.NEGATIVE_INFINITY);
        HBox.setMargin(refreshButton, REFRESH_BUTTON_INSTES);
        refreshButton.setOnAction(event -> walkAction("."));
    }

    private void constructTextStackPane() {
        bottomTextStackPane = new StackPane();
        bottomTextStackPane.setAlignment(Pos.CENTER);
        HBox.setHgrow(bottomTextStackPane, Priority.ALWAYS);
        bottomText = new Text();
        bottomTextStackPane.setMinWidth(0);
        bottomTextStackPane.getChildren().add(bottomText);
    }

    private void loadImages() {
        folderImage = new Image(FTPClientGUI.class.getResourceAsStream(FOLDER_PNG_RESOURCE_PATH),
                0, ICONS_HEIGHT, true, false);
        fileImage = new Image(FTPClientGUI.class.getResourceAsStream(FILE_PNG_RESOURCE_PATH),
                0, ICONS_HEIGHT, true, false);
        upImage = new Image(FTPClientGUI.class.getResourceAsStream(UP_PNG_RESOURCE_PATH),
                0, ICONS_HEIGHT, true, false);
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
                    if (item.getName().equals(UP_FOLDER_NAME)) {
                        imageView.setImage(upImage);
                    } else if (item.getType() == FTPClient.ListingItem.Type.DIRECTORY) {
                        imageView.setImage(folderImage);
                    } else {
                        imageView.setImage(fileImage);
                    }

                    setOnMouseClicked(event -> {
                        if (event.getButton() != MouseButton.PRIMARY | event.getClickCount() < 2) {
                            return;
                        }
                        if (item.getType() == FTPClient.ListingItem.Type.DIRECTORY) {
                            walkAction(item.getName());
                        } else {
                            saveAction(item.getName());
                        }
                    });

                    setGraphicTextGap(LIST_VIEW_ITEM_TEXT_GAP);
                    setText(item.getName());
                    setGraphic(imageView);
                }
            }
        });

        listView.setOnKeyPressed(event -> {
            if (event.getCode() != KeyCode.ENTER) {
                return;
            }
            var item = listView.getSelectionModel().getSelectedItem();
            if (item.getType() == FTPClient.ListingItem.Type.DIRECTORY) {
                walkAction(item.getName());
            } else {
                saveAction(item.getName());
            }
        });
    }

    private void setMessage(@NonNull String message, boolean isError) {
        bottomText.setText(message);
        if (isError) {
            bottomMainHBox.setBackground(ERROR_BACKGROUND);
        }
        bottomMainHBox.getChildren().remove(refreshButton);
        bottomMainHBox.getChildren().remove(0);
        bottomMainHBox.getChildren().add(0, bottomTextStackPane);
    }

    private void setConnectedScreen() {
        String pathToShow = currentPath.toString().equals("") ? "." : currentPath.toString();
        setMessage("path:  " + pathToShow, false);
        listView.setDisable(false);

        bottomMainHBox.getChildren().add(1, refreshButton);
        bottomMainHBox.setBackground(CONNECTED_BACKGROUND);

        mainButton.setText(DISCONNECT_BUTTON_TEXT);
        mainButton.setDisable(false);
        mainButton.setOnAction(event -> disconnectAction());
    }

    private void setMainScreen() {
        items.clear();
        listView.setDisable(true);

        bottomMainHBox.getChildren().remove(refreshButton);
        bottomMainHBox.getChildren().remove(0);
        bottomMainHBox.getChildren().add(0, bottomParametersHBox);
        bottomMainHBox.setBackground(Background.EMPTY);

        mainButton.setText(CONNECT_BUTTON_TEXT);
        mainButton.setDisable(false);
        mainButton.setOnAction(event -> connectAction());
    }

    private void cancelAllAsyncOperations() {
        if (pauseTransition != null) {
            pauseTransition.stop();
            pauseTransition = null;
        }
        if (currentTask != null) {
            currentTask.cancel(true);
            currentTask = null;
        }
    }

    private void setDelayedAction(@NonNull Runnable action) {
        pauseTransition = new PauseTransition(Duration.millis(DELAY_MILLIS));
        pauseTransition.setOnFinished(event -> {
            action.run();
            pauseTransition = null;
        });
        pauseTransition.play();
    }

    private void connectAction() {
        mainButton.setDisable(true);

        var address = addressField.getCharacters().toString().trim();
        var portString = portField.getCharacters().toString().trim();
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException exception) {
            setMessage(PORT_FORMAT_MESSAGE, true);
            setDelayedAction(this::setMainScreen);
            return;
        }

        setMessage(CONNECTING_MESSAGE, false);

        currentTask = new Task<List<FTPClient.ListingItem>>() {
            @Override
            protected List<FTPClient.ListingItem> call() throws Exception {
                client.connect(address, port, CONNECTION_TIMEOUT_MILLIS);
                currentPath = Paths.get("");
                return client.executeList(currentPath.toString());
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                connectOrWalkSucceeded(this);
            }

            @Override
            protected void failed() {
                super.failed();
                setMessage(CONNECTION_ERROR_MESSAGE, true);
                currentTask = null;
                setDelayedAction(() -> setMainScreen());
            }
        };

        startCurrentTask();
    }

    private void walkAction(@NonNull String relativePath) {
        setMessage(UPDATING_MESSAGE, false);
        listView.setDisable(true);

        currentTask = new Task<List<FTPClient.ListingItem>>() {
            @Override
            protected List<FTPClient.ListingItem> call() throws Exception {
                    currentPath = currentPath.resolve(relativePath).normalize();
                    var unixPath = FilenameUtils.separatorsToUnix(currentPath.toString());
                    return client.executeList(unixPath);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                connectOrWalkSucceeded(this);
            }

            @Override
            protected void failed() {
                super.failed();
                setMessage(UPDATING_ERROR_MESSAGE, true);
                currentTask = null;
                setDelayedAction(() -> setConnectedScreen());
            }
        };
       startCurrentTask();
    }

    private void connectOrWalkSucceeded(@NonNull Task<List<FTPClient.ListingItem>> instance) {
        items.clear();
        items.add(new FTPClient.ListingItem(FTPClient.ListingItem.Type.DIRECTORY, UP_FOLDER_NAME));
        items.addAll(instance.getValue());
        currentTask = null;
        setConnectedScreen();
    }

    private void saveAction(@NonNull String relativePath) {
        setMessage(SAVING_FILE_MESSAGE, false);
        listView.setDisable(true);

        var fileChooser = new DirectoryChooser();
        String title = FILE_CHOOSER_TITLE_PREFIX + relativePath + "\"";
        fileChooser.setTitle(title);
        var selectedDirectory = fileChooser.showDialog(mainStage);
        if (selectedDirectory == null) {
            setMessage(DIRECTORY_NOT_CHOSEN_MESSAGE, true);
            setDelayedAction(this::setConnectedScreen);
            return;
        }

        currentTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                var filePath = currentPath.resolve(relativePath);
                var unixFilePath = FilenameUtils.separatorsToUnix(filePath.toString());
                byte[] fileBytes = client.executeGet(unixFilePath);
                Files.write(selectedDirectory.toPath().resolve(relativePath), fileBytes);
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                setMessage(FILE_DOWNLOADED_MESSAGE, false);
                currentTask = null;
                setDelayedAction(() -> setConnectedScreen());
            }

            @Override
            protected void failed() {
                super.failed();
                setMessage(DOWNLOAD_ERROR_MESSAGE, true);
                currentTask = null;
                setDelayedAction(() -> setConnectedScreen());
            }
        };
        startCurrentTask();
    }

    private void disconnectAction() {
        mainButton.setDisable(true);
        setMessage(DISCONNECTING_MESSAGE, false);
        listView.setDisable(true);

        cancelAllAsyncOperations();

        currentTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                client.disconnect();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                currentTask = null;
                setMainScreen();
            }

            @Override
            protected void failed() {
                super.failed();
                setMessage(DISCONNECTION_ERROR_MESSAGE, true);
                client = new FTPClient();
                currentTask = null;
                setDelayedAction(() -> setMainScreen());
            }
        };

        startCurrentTask();
    }

    private void startCurrentTask() {
        var backgroundThread = new Thread(currentTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }
}
