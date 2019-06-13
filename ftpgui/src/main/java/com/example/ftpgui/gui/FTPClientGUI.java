package com.example.ftpgui.gui;

import com.example.ftpgui.FTPClient;
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
import javafx.stage.Stage;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.List;

/** JavaFX graphical user interface which wraps the FTPClient class */
public class FTPClientGUI extends Application {
    private static final Background ERROR_BACKGROUND = new Background(
            new BackgroundFill(Color.web("FFA1A1"), CornerRadii.EMPTY, Insets.EMPTY));
    private static final Background CONNECTED_BACKGROUND = new Background(
            new BackgroundFill(Color.web("BCFFBC"), CornerRadii.EMPTY, Insets.EMPTY));
    private static final Insets REFRESH_BUTTON_INSETS =
            new Insets(1, 1, 1, 5);

    private static final int MIN_WIDTH = 300;
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int ICONS_HEIGHT = 15;
    private static final int LIST_VIEW_ITEM_TEXT_GAP = 20;

    private static final String LAYOUT_FXML_RESOURCE_PATH = "/mainLayout.fxml";
    private static final String FOLDER_PNG_RESOURCE_PATH = "/folder.png";
    private static final String FILE_PNG_RESOURCE_PATH = "/file.png";
    private static final String UP_PNG_RESOURCE_PATH = "/up.png";

    private static final String BOTTOM_MAIN_HBOX_SELECTOR = "#bottomMainHBox";
    private static final String BUTTON_SELECTOR = "#button";
    private static final String BOTTOM_PARAMETERS_HBOX_SELECTOR = "#bottomParametersHBox";
    private static final String ADDRESS_FIELD_SELECTOR = "#addressField";
    private static final String PORT_FIELD_SELECTOR = "#portField";

    private static final String DISCONNECT_BUTTON_TEXT = "disconnect";
    private static final String CONNECT_BUTTON_TEXT = "connect";
    private static final String REFRESH_BUTTON_TEXT = "refresh";
    private static final String TITLE = "FTP Client";
    private static final String UP_FOLDER_NAME = "../";

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

    private FTPClientInteractor interactor;

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
        interactor = new FTPClientInteractor(stage, this::setMessage);
        setupInteractorCallbacks();

        initializeViews();
        loadImages();
        setupListView();

        setMainScreen();

        stage.setTitle(TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        stage.show();
    }

    private void setupInteractorCallbacks() {
        interactor.setOnConnectActionSucceeded(this::connectOrWalkActionSucceeded);
        interactor.setOnConnectActionFailed(this::setMainScreen);

        interactor.setOnWalkActionSucceeded(this::connectOrWalkActionSucceeded);
        interactor.setOnWalkActionFailed(this::setConnectedScreen);

        interactor.setOnSaveActionSucceeded(this::setConnectedScreen);
        interactor.setOnSaveActionFailed(this::setConnectedScreen);

        interactor.setOnDisconnectActionSucceeded(this::setMainScreen);
        interactor.setOnDisconnectActionFailed(this::setMainScreen);
    }

    private void connectOrWalkActionSucceeded(@NonNull List<FTPClient.ListingItem> results) {
        items.clear();
        items.addAll(results);
        setConnectedScreen();
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
        HBox.setMargin(refreshButton, REFRESH_BUTTON_INSETS);
        refreshButton.setOnAction(event -> interactor.walkAction("."));
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
                        listView.setDisable(true);
                        if (item.getType() == FTPClient.ListingItem.Type.DIRECTORY) {
                            interactor.walkAction(item.getName());
                        } else {
                            interactor.saveAction(item.getName());
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
            listView.setDisable(true);
            var item = listView.getSelectionModel().getSelectedItem();
            if (item.getType() == FTPClient.ListingItem.Type.DIRECTORY) {
                interactor.walkAction(item.getName());
            } else {
                interactor.saveAction(item.getName());
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
        String pathToShow = interactor.getCurrentPath().toString();
        if (pathToShow.equals("")) {
            pathToShow = ".";
        }

        setMessage("path:  " + pathToShow, false);
        listView.setDisable(false);

        bottomMainHBox.getChildren().add(1, refreshButton);
        bottomMainHBox.setBackground(CONNECTED_BACKGROUND);

        mainButton.setText(DISCONNECT_BUTTON_TEXT);
        mainButton.setDisable(false);
        mainButton.setOnAction(event -> {
            mainButton.setDisable(true);
            listView.setDisable(true);
            interactor.disconnectAction();
        });
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

        mainButton.setOnAction(event -> {
            mainButton.setDisable(true);
            var address = addressField.getCharacters().toString().trim();
            var portString = portField.getCharacters().toString().trim();
            interactor.connectAction(address, portString);
        });
    }
}
