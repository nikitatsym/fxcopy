package com.example.fxcopy;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class CopyController {

    @FXML
    public Label fromLabel;

    @FXML
    public Label toLabel;

    private Properties config;

    @FXML
    public Label progressLabel;
    @FXML
    private Button startCopyButton;

    @FXML
    private Button stopCopyButton;

    @FXML
    private ProgressBar copyProgressBar;

    private Task<Void> copyTask;

    private ExecutorService executorService;

    private Path sourcePath;

    private Path targetPath;

    public static final long CHUNK_SIZE = 1024*1024;

    @FXML
    public void initialize() {
        stopCopyButton.setDisable(true);
        startCopyButton.setDisable(true);
        executorService = Executors.newSingleThreadExecutor();

        updateAndCheckPaths();
    }

    @FXML
    protected void startCopy() {
        copyTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                copyFile(sourcePath, targetPath, progress -> updateProgress(progress, 1.0), true);
                if (isCancelled()) {
                    updateProgress(0, 1.0);
                    Platform.runLater(() -> toggleButtons(false));
                }
                return null;
            }
        };

        copyTask.setOnSucceeded(event -> toggleButtons(false));
        copyTask.setOnFailed(event -> toggleButtons(false));
        copyTask.setOnCancelled(event -> stopCopyButton.setDisable(true));

        copyProgressBar.progressProperty().bind(copyTask.progressProperty());
        progressLabel.textProperty().bind(Bindings.format("%.2f%%", copyTask.progressProperty().multiply(100)));

        toggleButtons(true);

        executorService.submit(copyTask);
    }

    @FXML
    protected void stopCopy() {
        if (copyTask != null && copyTask.isRunning()) {
            copyTask.cancel();
        }
    }

    private void toggleButtons(boolean isCopying) {
        Platform.runLater(() -> {
            startCopyButton.setDisable(isCopying);
            stopCopyButton.setDisable(!isCopying);
        });
    }


    public void shutdownExecutor() {
        if (copyTask != null) {
            copyTask.cancel();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    private Properties loadConfig() {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream("src/main/resources/config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }


    public static void copyFile(Path sourcePath, Path targetPath, Consumer<Double> progressCallback, boolean removeIfInterrupted) {
        try (FileChannel source = FileChannel.open(sourcePath);
             FileChannel target = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            long totalBytes = source.size();
            long bytesCopied = 0;
            while (bytesCopied < totalBytes) {
                long bytesTransferred = source.transferTo(bytesCopied, CHUNK_SIZE, target);
                bytesCopied += bytesTransferred;
                double progress = (double) bytesCopied / totalBytes;
                progressCallback.accept(progress);
            }
        } catch (ClosedByInterruptException e) {
            if (removeIfInterrupted) {
                try {
                    Files.deleteIfExists(targetPath);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateAndCheckPaths() {
        config = loadConfig();
        sourcePath = Paths.get(config.getProperty("sourceFilePath"));
        targetPath = Paths.get(config.getProperty("targetFilePath"));

        fromLabel.setText(sourcePath.toString());
        toLabel.setText(targetPath.toString());

        validatePaths();
    }

    private void setError(boolean state) {
        startCopyButton.setDisable(state);
    }

    private void validatePaths() {
        boolean error = false;
        if (!Files.exists(sourcePath)) {
            fromLabel.getStyleClass().add("errorPath");
            fromLabel.setText(fromLabel.getText() + " (does not exist)");
            error = true;
        } else if (Files.isDirectory(sourcePath)) {
            fromLabel.getStyleClass().add("errorPath");
            fromLabel.setText(fromLabel.getText() + " (not a file)");
            error = true;
        } else {
            fromLabel.getStyleClass().remove("errorPath");
        }

        if (targetPath.getParent() == null || !Files.exists(targetPath.getParent())) {
            toLabel.getStyleClass().add("errorPath");
            toLabel.setText(toLabel.getText() + " (directory does not exist)");
            error = true;
        } else if (Files.isDirectory(targetPath)) {
            toLabel.getStyleClass().add("errorPath");
            toLabel.setText(toLabel.getText() + " (is a directory)");
            error = true;
        } else {
            toLabel.getStyleClass().remove("errorPath");
        }

        setError(error);

    }

}