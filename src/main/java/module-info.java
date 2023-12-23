module com.xpmodder.bluemapdownloader {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.xpmodder.bluemapdownloader to javafx.fxml;
    exports com.xpmodder.bluemapdownloader;
}