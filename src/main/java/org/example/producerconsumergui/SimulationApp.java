package org.example.producerconsumergui;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


import org.example.producerconsumergui.UI.SimulationCanvas;

public class SimulationApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        SimulationCanvas canvas = new SimulationCanvas();

        Scene scene = new Scene(canvas, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/org/example/producerconsumergui/style.css").toExternalForm());

        primaryStage.setTitle("Assembly Line Simulation - Queuing Network");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}