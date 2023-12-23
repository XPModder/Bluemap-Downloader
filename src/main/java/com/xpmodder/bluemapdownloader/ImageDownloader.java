package com.xpmodder.bluemapdownloader;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.concurrent.TimeUnit;

public class ImageDownloader {

    static String requestURL, folder;
    static int width, height, requests, requestPause;
    static double w, h;

    public static void startDownload(String server, String map, int zoom, int widthIn, int heightIn, int requestsIn, int requestPauseIn){

        requestURL = "https://" + server + "/maps/" + map + "/tiles/" + zoom + "/";
        width = widthIn;
        height = heightIn;
        requests = requestsIn;
        requestPause = requestPauseIn;
        folder = "./" + map;

        w = BluemapDownloader.mainGrid.getWidth() / ((((int)Math.ceil(((width / 10.0) - 2) / 2.0) * 2) + 2) * 10);
        h = BluemapDownloader.mainGrid.getHeight() / ((((int)Math.ceil(((height / 10.0) - 2) / 2.0) * 2) + 2) * 10);

        runDownloads();

    }

    public static double getProgress(int max, int current){

        double currentValue = current * 1.0d;
        return currentValue / max;

    }

    public static void updateProgress(int max, int current){
        Platform.runLater(() -> {
            BluemapDownloader.progressBar.setProgress(getProgress(max, current));
        });
    }

    public static void addImageToGrid(BufferedImage image, int col, int row){

        int[] type_int_agrb = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        IntBuffer buffer = IntBuffer.wrap(type_int_agrb);

        PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(image.getWidth(), image.getHeight(), buffer, pixelFormat);

        ImageView imageView = new ImageView(new WritableImage(pixelBuffer));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(w);
        imageView.setFitHeight(h);
        imageView.setSmooth(true);

        Platform.runLater(() -> {
            BluemapDownloader.mainGrid.add(imageView, col, row);
        });

    }

    public static int downloadFile(String url, int counter, int x, int z, int col, int row){

        if(counter % requests == 0){
            try {
                TimeUnit.SECONDS.sleep(requestPause);
            }
            catch (InterruptedException ex){
                Platform.runLater(() -> {
                    Text text = new Text("Pause between downloads was interrupted!");
                    text.setFill(Color.ORANGE);
                    BluemapDownloader.log.getChildren().add(text);
                });
            }
        }

        int ret = 500;

        File dir = new File(folder);
        if(!dir.isDirectory()){
            if(!dir.mkdirs()){
                ret = 501;
            }
        }

        File file = new File(folder, "x" + x + "z" + z + ".png");

        BufferedImage bufferedImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);

        try{
            URL uri = new URL(url);

            Image image = ImageIO.read(uri);

            bufferedImage.getGraphics().drawImage(image, 0, 0, null);

            ret = 200;
        }
        catch (IIOException ex){
            bufferedImage = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
            bufferedImage.getGraphics().setColor(new java.awt.Color(0, 0, 0, 0));
            bufferedImage.getGraphics().fillRect(0, 0, 500, 500);

            ret = 200;
        }
        catch (Exception ex){
            ret = 505;
        }

        try{
            ImageIO.write(bufferedImage, "png", file);
            addImageToGrid(bufferedImage, col, row);
        }
        catch (Exception ex){
            ret = 505;
        }

        return ret;

    }

    public static void runDownloads(){

        int maxX = (int)Math.ceil(((width / 10.0) - 2) / 2.0);
        if(maxX < 0){
            maxX = 0;
        }
        int minX = maxX * -1;

        int maxZ = (int)Math.ceil(((height / 10.0) - 2) / 2.0);
        if(maxZ < 0){
            maxZ = 0;
        }
        int minZ = maxZ * -1;

        int numFiles = (((maxX * 2) + 2) * 10) * (((maxZ * 2) + 2) * 10);

        int counter = 1;

        for(int x = minX; x <= maxX; x++){

            for(int xb = 0; xb <= 9; xb++) {

                for (int z = minZ; z <= maxZ; z++) {

                    for(int zb = 0; zb <= 9; zb++) {

                        String xPos;
                        String zPos;

                        String URL;
                        int result;

                        if(xb != 0 || zb != 0) {

                            if (x == 0 && z == 0){
                                xPos = "-0";
                                zPos = "-0";
                                URL = requestURL + "x" + xPos + "/" + xb + "/z" + zPos + "/" + zb + ".png";

                                int xCoord = -xb;
                                int zCoord = -zb;

                                int col = (xCoord) + ((maxX * 10) + 9);
                                int row = (zCoord) + ((maxZ * 10) + 9);

                                result = downloadFile(URL, counter, xCoord, zCoord, col, row);
                                if(result == 200){
                                    int finalCounter = counter;
                                    Platform.runLater(() -> {
                                        BluemapDownloader.log.getChildren().add(new Text("Image " + finalCounter + " of " + numFiles + " received"));
                                    });
                                }
                                else{
                                    String finalXPos = xPos;
                                    int finalXb = xb;
                                    String finalZPos = zPos;
                                    int finalZb = zb;
                                    int finalResult = result;
                                    Platform.runLater(() -> {
                                        Text text = new Text("Error: Could not get image x" + finalXPos + "/" + finalXb + "/z" + finalZPos + "/" + finalZb + ".png! Code: " + finalResult);
                                        text.setFill(Color.RED);
                                        BluemapDownloader.log.getChildren().add(text);
                                    });
                                }
                                updateProgress(numFiles, counter);
                                counter ++;
                            }
                            if (x == 0) {
                                xPos = "-0";
                                zPos = String.valueOf(z);
                                URL = requestURL + "x" + xPos + "/" + xb + "/z" + zPos + "/" + zb + ".png";

                                int xCoord = -xb;
                                int zCoord = (z * 10) + zb;
                                if(z < 0){
                                    zCoord = (z * 10) - zb;
                                }

                                int col = (xCoord) + ((maxX * 10) + 9);
                                int row = (zCoord) + ((maxZ * 10) + 9);

                                result = downloadFile(URL, counter, xCoord, zCoord, col, row);
                                if(result == 200){
                                    int finalCounter1 = counter;
                                    Platform.runLater(() -> {
                                        BluemapDownloader.log.getChildren().add(new Text("Image " + finalCounter1 + " of " + numFiles + " received"));
                                    });
                                }
                                else{
                                    String finalXPos1 = xPos;
                                    int finalXb1 = xb;
                                    String finalZPos1 = zPos;
                                    int finalZb1 = zb;
                                    int finalResult1 = result;
                                    Platform.runLater(() -> {
                                        Text text = new Text("Error: Could not get image x" + finalXPos1 + "/" + finalXb1 + "/z" + finalZPos1 + "/" + finalZb1 + ".png! Code: " + finalResult1);
                                        text.setFill(Color.RED);
                                        BluemapDownloader.log.getChildren().add(text);
                                    });
                                }
                                updateProgress(numFiles, counter);
                                counter ++;
                            }
                            if (z == 0) {
                                xPos = String.valueOf(x);
                                zPos = "-0";
                                URL = requestURL + "x" + xPos + "/" + xb + "/z" + zPos + "/" + zb + ".png";

                                int xCoord = (x * 10) + xb;
                                if(x < 0){
                                    xCoord = (x * 10) - xb;
                                }
                                int zCoord = -zb;

                                int col = (xCoord) + ((maxX * 10) + 9);
                                int row = (zCoord) + ((maxZ * 10) + 9);

                                result = downloadFile(URL, counter, xCoord, zCoord, col, row);
                                if(result == 200){
                                    int finalCounter2 = counter;
                                    Platform.runLater(() -> {
                                        BluemapDownloader.log.getChildren().add(new Text("Image " + finalCounter2 + " of " + numFiles + " received"));
                                    });
                                }
                                else{
                                    String finalXPos2 = xPos;
                                    int finalXb2 = xb;
                                    String finalZPos2 = zPos;
                                    int finalZb2 = zb;
                                    int finalResult2 = result;
                                    Platform.runLater(() -> {
                                        Text text = new Text("Error: Could not get image x" + finalXPos2 + "/" + finalXb2 + "/z" + finalZPos2 + "/" + finalZb2 + ".png! Code: " + finalResult2);
                                        text.setFill(Color.RED);
                                        BluemapDownloader.log.getChildren().add(text);
                                    });
                                }
                                updateProgress(numFiles, counter);
                                counter ++;
                            }

                        }

                        xPos = String.valueOf(x);
                        zPos = String.valueOf(z);

                        URL = requestURL + "x" + xPos + "/" + xb + "/z" + zPos + "/" + zb + ".png";

                        int xCoord = (x * 10) + xb;
                        if(x < 0){
                            xCoord = (x * 10) - xb;
                        }
                        int zCoord = (z * 10) + zb;
                        if(z < 0){
                            zCoord = (z * 10) - zb;
                        }

                        int col = (xCoord) + ((maxX * 10) + 9);
                        int row = (zCoord) + ((maxZ * 10) + 9);

                        result = downloadFile(URL, counter, xCoord, zCoord, col, row);
                        if(result == 200){
                            int finalCounter3 = counter;
                            Platform.runLater(() -> {
                                BluemapDownloader.log.getChildren().add(new Text("Image " + finalCounter3 + " of " + numFiles + " received"));
                            });
                        }
                        else{
                            String finalXPos3 = xPos;
                            int finalXb3 = xb;
                            String finalZPos3 = zPos;
                            int finalZb3 = zb;
                            int finalResult3 = result;
                            Platform.runLater(() -> {
                                Text text = new Text("Error: Could not get image x" + finalXPos3 + "/" + finalXb3 + "/z" + finalZPos3 + "/" + finalZb3 + ".png! Code: " + finalResult3);
                                text.setFill(Color.RED);
                                BluemapDownloader.log.getChildren().add(text);
                            });
                        }
                        updateProgress(numFiles, counter);
                        counter ++;

                    }

                }

            }

        }

        Platform.runLater(() -> {
            Text text = new Text("Download complete!");
            text.setFill(Color.GREEN);
            BluemapDownloader.log.getChildren().add(text);
        });

    }

}
