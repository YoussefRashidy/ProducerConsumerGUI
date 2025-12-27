package org.example.producerconsumergui.UI;

import org.example.producerconsumergui.Model.Color;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class MachineNode extends StackPane {
    static final double RADIUS = 40;

    public final int machineId;
    Circle circle;
    private Label label;
    private Label statusLabel;

    public Color originalColor = Color.BLUE;
    public Color currentColor = Color.BLUE;

    public MachineNode(int id, double x, double y) {
        this.machineId = id;

        // Create circle
        circle = new Circle(RADIUS);
        circle.setFill(Paint.valueOf(originalColor.getHexCode()));
        circle.setStroke(javafx.scene.paint.Color.BLACK);
        circle.setStrokeWidth(2);

        // Create ID label
        label = new Label("M" + id);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        label.setStyle("-fx-text-fill: white;");

        // Create status label (small text below)
        statusLabel = new Label("IDLE");
        statusLabel.setFont(Font.font("System", 10));
        statusLabel.setStyle("-fx-text-fill: white;");
        statusLabel.setTranslateY(15);

        getChildren().addAll(circle, label, statusLabel);
        setLayoutX(x);
        setLayoutY(y);

        // Hover effect
        setOnMouseEntered(e -> {
            circle.setStrokeWidth(3);
            circle.setStroke(javafx.scene.paint.Color.ORANGE);
        });

        setOnMouseExited(e -> {
            circle.setStrokeWidth(2);
            circle.setStroke(javafx.scene.paint.Color.BLACK);
        });
    }

    public void setOriginalColor(Color color) {
        this.originalColor = color;
        this.currentColor = color;
        updateColor();
    }

    public void setCurrentColor(Color color) {
        this.currentColor = color;
        updateColor();
    }

    private void updateColor() {
        circle.setFill(Paint.valueOf(currentColor.getHexCode()));
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void flash() {
        // Flash animation when machine finishes processing
        FadeTransition fade1 = new FadeTransition(Duration.millis(150), circle);
        fade1.setFromValue(1.0);
        fade1.setToValue(0.3);

        FadeTransition fade2 = new FadeTransition(Duration.millis(150), circle);
        fade2.setFromValue(0.3);
        fade2.setToValue(1.0);

        SequentialTransition flash = new SequentialTransition(fade1, fade2, fade1, fade2);
        flash.play();
    }

    public double getCenterX() {
        // Return center X in parent's coordinate space
        return getLayoutX() + getWidth() / 2;
    }

    public double getCenterY() {
        // Return center Y in parent's coordinate space
        return getLayoutY() + getHeight() / 2;
    }

    public double getRadius() {
        return RADIUS;
    }
}