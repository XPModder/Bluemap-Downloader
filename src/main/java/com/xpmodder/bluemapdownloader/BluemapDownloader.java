package com.xpmodder.bluemapdownloader;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    public GridPane mainGrid;
    public TextFlow log = new TextFlow();
    public ProgressBar progressBar = new ProgressBar(0);

    @Override
    public void start(Stage stage) throws IOException {

        Scene scene = new Scene(getLayout(), 1000, 480);
        stage.setTitle("Bluemap Downloader");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }


    public Pane getLayout(){

        BorderPane borderPane = new BorderPane();

        mainGrid = new GridPane();

        Label settingsLabel =  new Label("Settings");
        settingsLabel.setFont(new Font("Arial", 18));
        settingsLabel.setStyle("-fx-font-weight: bold");

        Label serverLabel = new Label("Bluemap server url:");
        TextField serverAddress = new TextField();
        HBox.setHgrow(serverAddress, Priority.ALWAYS);
        HBox serverBox = new HBox(serverLabel, serverAddress);
        serverBox.setSpacing(7.5d);

        Label mapLabel = new Label("Map name:");
        TextField mapName = new TextField();
        HBox.setHgrow(mapName, Priority.ALWAYS);
        HBox mapBox = new HBox(mapLabel, mapName);
        mapBox.setSpacing(7.5d);

        Label zoomLabel = new Label("Zoom Level (1-3; 1=max zoom, 1 pixel = 1 block):");
        String[] zoomLevels = {"1", "2", "3"};
        ChoiceBox<String> zoomChoice = new ChoiceBox<>(FXCollections.observableArrayList(zoomLevels));
        HBox.setHgrow(zoomChoice, Priority.ALWAYS);
        HBox zoomBox = new HBox(zoomLabel, zoomChoice);
        zoomBox.setSpacing(7.5d);

        Label sizeLabel = new Label("Size (width x height):");
        Spinner<Integer> widthSpinner = new Spinner<>();
        Label xLabel = new Label("x");
        Spinner<Integer> heightSpinner = new Spinner<>();
        HBox sizeBox = new HBox(sizeLabel, widthSpinner, xLabel, heightSpinner);
        sizeBox.setSpacing(7.5d);

        Label numRequestLabel = new Label("Max consecutive requests:");
        Spinner<Integer> numRequests = new Spinner<>();
        HBox numRequestBox = new HBox(numRequestLabel, numRequests);
        numRequestBox.setSpacing(7.5d);

        Label requestPauseLabel = new Label("Pause between request bursts:");
        Spinner<Integer> requestPause = new Spinner<>();
        HBox.setHgrow(requestPause, Priority.ALWAYS);
        HBox requestPauseBox = new HBox(requestPauseLabel, requestPause);
        requestPauseBox.setSpacing(7.5d);

        CheckBox stitchCheck = new CheckBox("Stitch to single Image");

        Button startButton = new Button("Start Download");
        startButton.setMaxWidth(Double.MAX_VALUE);

        VBox settingsBox = new VBox(settingsLabel, serverBox, mapBox, zoomBox, sizeBox, numRequestBox, requestPauseBox, stitchCheck, startButton);
        settingsBox.setSpacing(10.0d);
        settingsBox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(5.0d))));
        settingsBox.fillWidthProperty().set(true);


        Label logLabel = new Label("Log:");
        logLabel.setFont(new Font("Arial", 18));
        logLabel.setStyle("-fx-font-weight: bold");

        progressBar.setMaxWidth(Double.MAX_VALUE);

        VBox logBox = new VBox(logLabel, log, progressBar);
        VBox.setVgrow(log, Priority.ALWAYS);
        logBox.fillWidthProperty().set(true);
        logBox.setMaxHeight(Double.MAX_VALUE);


        SplitPane rightLayout = new SplitPane(settingsBox, logBox);
        rightLayout.setOrientation(Orientation.VERTICAL);

        borderPane.setRight(rightLayout);

        borderPane.setCenter(mainGrid);

        return borderPane;

    }


}