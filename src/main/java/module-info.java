module org.example.producerconsumergui {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.producerconsumergui to javafx.fxml;
    exports org.example.producerconsumergui;
}