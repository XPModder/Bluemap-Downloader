package com.xpmodder.bluemapdownloader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BluemapDownloader extends Application {

    public static GridPane mainGrid;
    public static VBox log = new VBox(5.0);
    public static ProgressBar progressBar = new ProgressBar(0);
    public static Button startButton;

    @Override
    public void start(Stage stage) throws IOException {

        Scene scene = new Scene(getLayout(), 1200, 700);
        stage.setTitle("Bluemap Downloader");
        stage.setScene(scene);
        stage.show();

        Text text = new Text("Application started!");
        log.getChildren().add(text);
    }

    public static void main(String[] args) {
        launch();
    }


    public void endDownload(){
        progressBar.setProgress(0);
        startButton.setDisable(false);
    }


    public void startDownload(String server, String map, int zoom, int width, int height, int requests, int requestGap, boolean stitch, boolean allDownloaded){

        int finalWidth1 = width;
        int finalHeight1 = height;
        Platform.runLater(() -> {
            Text text = new Text("Starting Download of " + map + " from " + server + " at zoom level " + zoom + "!");
            text.setFill(Color.BLACK);
            log.getChildren().add(text);
            Text text1 = new Text("Downloading at least an area of " + finalWidth1 + " x " + finalHeight1 + " blocks!");
            log.getChildren().add(text1);

            mainGrid.getChildren().clear();

        });

        if(zoom == 1){
            width = (int)Math.ceil(width / 500.0);
            height = (int)Math.ceil(height  / 500.0);
        }
        else if(zoom == 2){
            width = (int)Math.ceil(width / 2500.0);
            height = (int)Math.ceil(height / 2500.0);
        }
        else if(zoom == 3){
            width = (int)Math.ceil(width / 12500.0);
            height = (int)Math.ceil(height / 12500.0);
        }

        ImageDownloader.startDownload(server, map, zoom, width, height, requests, requestGap);

        if(stitch){

            Platform.runLater(() -> {
                Text text = new Text("Stitching...");
                log.getChildren().add(text);
                progressBar.setProgress(-1);
            });

            int finalWidth = width;
            int finalHeight = height;

            Platform.runLater(() -> {

                BufferedImage bufferedImage;

                if(allDownloaded){
                    bufferedImage = new BufferedImage(mainGrid.getColumnCount() * 500, mainGrid.getRowCount() * 500, BufferedImage.TYPE_INT_ARGB);
                }
                else {
                    bufferedImage = new BufferedImage(finalWidth * 500, finalHeight * 500, BufferedImage.TYPE_INT_ARGB);
                }

                int startCol = (mainGrid.getColumnCount() - finalWidth) / 2;
                int startRow = (mainGrid.getRowCount() - finalHeight) / 2;

                for(Node node : mainGrid.getChildren()){

                    ImageView view = (ImageView) node;

                    if((GridPane.getColumnIndex(node) < startCol || GridPane.getRowIndex(node) < startRow) && !allDownloaded){
                        continue;
                    }

                    int x = (GridPane.getColumnIndex(node) - startCol) * 500;
                    int y = (GridPane.getRowIndex(node) - startRow) * 500;

                    Image part = SwingFXUtils.fromFXImage(view.getImage(), null);

                    bufferedImage.getGraphics().drawImage(part, x, y, null);

                }

                try{
                    ImageIO.write(bufferedImage, "png", new File("./" + map + ".png"));
                }
                catch (Exception ex){
                    Text text = new Text("Error writing image! " + ex.fillInStackTrace());
                    text.setFill(Color.RED);
                    log.getChildren().add(text);

                    endDownload();
                    return;
                }

                Text text = new Text("Image saved successfully!");
                text.setFill(Color.GREEN);
                log.getChildren().add(text);

                endDownload();

            });

        }

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
        zoomChoice.setValue("1");
        HBox.setHgrow(zoomChoice, Priority.ALWAYS);
        HBox zoomBox = new HBox(zoomLabel, zoomChoice);
        zoomBox.setSpacing(7.5d);

        Label sizeLabel = new Label("Size (width x height):");
        Spinner<Integer> widthSpinner = new Spinner<>();
        widthSpinner.setEditable(true);
        widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000000, 10000, 500));
        Label xLabel = new Label("x");
        Spinner<Integer> heightSpinner = new Spinner<>();
        heightSpinner.setEditable(true);
        heightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000000, 10000, 500));
        HBox sizeBox = new HBox(sizeLabel, widthSpinner, xLabel, heightSpinner);
        sizeBox.setSpacing(7.5d);

        Label numRequestLabel = new Label("Max consecutive requests:");
        Spinner<Integer> numRequests = new Spinner<>();
        numRequests.setEditable(true);
        numRequests.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100000000, 100));
        HBox numRequestBox = new HBox(numRequestLabel, numRequests);
        numRequestBox.setSpacing(7.5d);

        Label requestPauseLabel = new Label("Pause between request bursts:");
        Spinner<Integer> requestPause = new Spinner<>();
        requestPause.setEditable(true);
        requestPause.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100000000, 1));
        HBox.setHgrow(requestPause, Priority.ALWAYS);
        HBox requestPauseBox = new HBox(requestPauseLabel, requestPause);
        requestPauseBox.setSpacing(7.5d);

        CheckBox stitchCheck = new CheckBox("Stitch to single Image");
        stitchCheck.setSelected(true);
        Region spacer = new Region();
        CheckBox allCheck = new CheckBox("Stitch ALL downloaded");
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox checkboxes = new HBox(stitchCheck, spacer, allCheck);
        checkboxes.setSpacing(7.5d);


        startButton = new Button("Start Download");
        startButton.setOnMouseClicked(event -> {
            String server = serverAddress.getText();
            String map = mapName.getText();
            int zoom = Integer.parseInt(zoomChoice.getValue());
            widthSpinner.cancelEdit();
            heightSpinner.cancelEdit();
            numRequests.cancelEdit();
            requestPause.cancelEdit();
            int width = widthSpinner.getValue();
            int height = heightSpinner.getValue();
            int requests = numRequests.getValue();
            int requestGap = requestPause.getValue();
            boolean stitch = stitchCheck.isSelected();
            boolean allDownloaded = allCheck.isSelected();

            Thread workThread = new Thread(() -> startDownload(server, map, zoom, width, height, requests, requestGap, stitch, allDownloaded));
            workThread.start();

            startButton.setDisable(true);
        });
        startButton.setMaxWidth(Double.MAX_VALUE);

        progressBar.setMaxWidth(Double.MAX_VALUE);

        VBox settingsBox = new VBox(settingsLabel, serverBox, mapBox, zoomBox, sizeBox, numRequestBox, requestPauseBox, checkboxes, startButton, progressBar);
        settingsBox.setSpacing(10.0d);
        settingsBox.setBorder(new Border(new BorderStroke(null, null, null, new BorderWidths(5.0d))));
        settingsBox.fillWidthProperty().set(true);


        Label logLabel = new Label("Log:");
        logLabel.setFont(new Font("Arial", 18));
        logLabel.setStyle("-fx-font-weight: bold");

        ScrollPane scrollPane = new ScrollPane(log);
        scrollPane.vvalueProperty().bind(log.heightProperty());
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox logBox = new VBox(logLabel, scrollPane);
        logBox.setSpacing(10.0);

        SplitPane rightLayout = new SplitPane(settingsBox, logBox);
        rightLayout.setOrientation(Orientation.VERTICAL);

        borderPane.setRight(rightLayout);

        borderPane.setCenter(mainGrid);

        return borderPane;

    }


}

