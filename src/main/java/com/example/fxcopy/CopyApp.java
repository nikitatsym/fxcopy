package com.example.fxcopy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CopyApp extends Application {

    private CopyController copyController;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CopyApp.class.getResource("copy-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 200);
        copyController = fxmlLoader.getController();
        stage.setMinHeight(200);
        stage.setMinWidth(400);
        stage.setTitle("Copy");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (copyController != null) {
            copyController.shutdownExecutor();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}