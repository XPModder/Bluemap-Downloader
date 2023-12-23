module com.xpmodder.bluemapdownloader {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;


    opens com.xpmodder.bluemapdownloader to javafx.fxml;
    exports com.xpmodder.bluemapdownloader;
}