package com.example.ftpgui.gui;

import com.example.ftpgui.FTPClient;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * This class provides methods used by {@link com.example.ftpgui.gui.FTPClientGUI} to interact
 * with {@link FTPClient} (those methods are basically action handlers for gui class).
 */
class FTPClientInteractor {
    private static final int MESSAGE_DELAY_MILLIS = 2000;
    private static final int CONNECTION_TIMEOUT_MILLIS = 2000;

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

    private static final String UP_FOLDER_NAME = "../";
    private static final String FILE_CHOOSER_TITLE_PREFIX = "Choose directory to save \"";

    private Stage mainStage;
    private FTPClient client = new FTPClient();
    private Path currentPath;
    private Task currentTask;
    private PauseTransition pauseTransition;
    private BiConsumer<String, Boolean> guiMessageListener;

    private Consumer<List<FTPClient.ListingItem>> onConnectActionSucceeded;
    private Runnable onConnectActionFailed;
    private Consumer<List<FTPClient.ListingItem>> onWalkActionSucceeded;
    private Runnable onWalkActionFailed;
    private Runnable onSaveActionSucceeded;
    private Runnable onSaveActionFailed;
    private Runnable onDisconnectActionSucceeded;
    private Runnable onDisconnectActionFailed;

    /**
     * Creates interactor.
     *
     * @param mainStage          used to show save dialog in gui.
     * @param guiMessageListener used to show messages in gui.
     */
    FTPClientInteractor(@NonNull Stage mainStage,
                        @NonNull BiConsumer<String, Boolean> guiMessageListener) {
        this.mainStage = mainStage;
        this.guiMessageListener = guiMessageListener;
    }

    /**
     * This callback will be invoked with a list of current observable directories
     * if connectAction succeeds. Could be null.
     */
    void setOnConnectActionSucceeded(
            @Nullable Consumer<List<FTPClient.ListingItem>> onConnectActionSucceeded) {
        this.onConnectActionSucceeded = onConnectActionSucceeded;
    }

    /** This callback will be invoked if connectAction fails. Could be null. */
    void setOnConnectActionFailed(@Nullable Runnable onConnectActionFailed) {
        this.onConnectActionFailed = onConnectActionFailed;
    }

    /**
     * This callback will be invoked with a list of current observable directories
     * if walkAction succeeds. Could be null.
     */
    void setOnWalkActionSucceeded(
            @Nullable Consumer<List<FTPClient.ListingItem>> onWalkActionSucceeded) {
        this.onWalkActionSucceeded = onWalkActionSucceeded;
    }

    /** This callback will be invoked if walkAction fails. Could be null. */
    void setOnWalkActionFailed(@Nullable Runnable onWalkActionFailed) {
        this.onWalkActionFailed = onWalkActionFailed;
    }

    /** This callback will be invoked if saveAction succeeds. Could be null. */
    void setOnSaveActionSucceeded(@Nullable Runnable onSaveActionSucceeded) {
        this.onSaveActionSucceeded = onSaveActionSucceeded;
    }

    /** This callback will be invoked if saveAction fails. Could be null. */
    void setOnSaveActionFailed(@Nullable Runnable onSaveActionFailed) {
        this.onSaveActionFailed = onSaveActionFailed;
    }

    /** This callback will be invoked if disconnectAction succeeds. Could be null. */
    void setOnDisconnectActionSucceeded(@Nullable Runnable onDisconnectActionSucceeded) {
        this.onDisconnectActionSucceeded = onDisconnectActionSucceeded;
    }

    /** This callback will be invoked if disconnectAction fails. Could be null. */
    void setOnDisconnectActionFailed(@Nullable Runnable onDisconnectActionFailed) {
        this.onDisconnectActionFailed = onDisconnectActionFailed;
    }

    /** Returns current path at which directories on server are observed. */
    Path getCurrentPath() {
        return currentPath;
    }

    /** Connects to server at specified address and specified port. */
    void connectAction(String address, String portString) {
        int port;
        try {
            port = Integer.parseInt(portString);
        } catch (NumberFormatException exception) {
            guiMessageListener.accept(PORT_FORMAT_MESSAGE, true);
            setDelayedAction(() -> runIfNotNull(onConnectActionFailed));
            return;
        }

        guiMessageListener.accept(CONNECTING_MESSAGE, false);

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
                if (onConnectActionSucceeded != null) {
                    connectOrWalkActionSucceeded(getValue(), true);
                }
            }

            @Override
            protected void failed() {
                super.failed();
                guiMessageListener.accept(CONNECTION_ERROR_MESSAGE, true);
                currentTask = null;
                setDelayedAction(() -> runIfNotNull(onConnectActionFailed));
            }
        };

        startCurrentTask();
    }

    /**
     * Changes current server's observed directory.
     * Must only be called after successful connectAction.
     */
    void walkAction(@NonNull String relativePath) {
        guiMessageListener.accept(UPDATING_MESSAGE, false);

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
                if (onWalkActionSucceeded != null) {
                    connectOrWalkActionSucceeded(getValue(), false);
                }
            }

            @Override
            protected void failed() {
                super.failed();
                guiMessageListener.accept(UPDATING_ERROR_MESSAGE, true);
                currentTask = null;
                setDelayedAction(() -> runIfNotNull(onWalkActionFailed));
            }
        };

        startCurrentTask();
    }

    /** Tries to save file at given path (relative to current directory). */
    void saveAction(@NonNull String relativePath) {
        guiMessageListener.accept(SAVING_FILE_MESSAGE, false);

        var fileChooser = new DirectoryChooser();
        String title = FILE_CHOOSER_TITLE_PREFIX + relativePath + "\"";
        fileChooser.setTitle(title);
        var selectedDirectory = fileChooser.showDialog(mainStage);
        if (selectedDirectory == null) {
            guiMessageListener.accept(DIRECTORY_NOT_CHOSEN_MESSAGE, true);
            setDelayedAction(() -> runIfNotNull(onSaveActionFailed));
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
                guiMessageListener.accept(FILE_DOWNLOADED_MESSAGE, false);
                currentTask = null;
                setDelayedAction(() -> runIfNotNull(onSaveActionSucceeded));
            }

            @Override
            protected void failed() {
                super.failed();
                guiMessageListener.accept(DOWNLOAD_ERROR_MESSAGE, true);
                currentTask = null;
                setDelayedAction(() -> runIfNotNull(onSaveActionFailed));
            }
        };

        startCurrentTask();
    }

    /** Disconnects from server. */
    void disconnectAction() {
        guiMessageListener.accept(DISCONNECTING_MESSAGE, false);
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
                runIfNotNull(onDisconnectActionSucceeded);
            }

            @Override
            protected void failed() {
                super.failed();
                guiMessageListener.accept(DISCONNECTION_ERROR_MESSAGE, true);
                client = new FTPClient();
                currentTask = null;
                setDelayedAction(() -> runIfNotNull(onDisconnectActionFailed));
            }
        };

        startCurrentTask();
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
        pauseTransition = new PauseTransition(Duration.millis(MESSAGE_DELAY_MILLIS));
        pauseTransition.setOnFinished(event -> {
            action.run();
            pauseTransition = null;
        });
        pauseTransition.play();
    }

    private void connectOrWalkActionSucceeded(@NonNull List<FTPClient.ListingItem> results,
                                              boolean isConnect) {
        currentTask = null;
        var list = new ArrayList<FTPClient.ListingItem>();
        list.add(new FTPClient.ListingItem(FTPClient.ListingItem.Type.DIRECTORY, UP_FOLDER_NAME));
        list.addAll(results);

        if (isConnect){
            onConnectActionSucceeded.accept(list);
        } else {
            onWalkActionSucceeded.accept(list);
        }
    }

    private void startCurrentTask() {
        var backgroundThread = new Thread(currentTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    private void runIfNotNull(@Nullable Runnable runnable) {
        if (runnable != null) {
            runnable.run();
        }
    }
}
