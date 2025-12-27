package org.example.producerconsumergui.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class QueueNode extends StackPane {
    public static final double WIDTH = 120;
    public static final double HEIGHT = 80;

    public final int queueId;
    Rectangle rect;
    private Label idLabel;
    private Label sizeLabel;
    private ProgressBar visualBar;
    private VBox contentBox;
    private boolean isInputQueue = false;

    public QueueNode(int id, double x, double y) {
        this.queueId = id;

        // Create rectangle
        rect = new Rectangle(WIDTH, HEIGHT);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);
        rect.setStrokeWidth(2);
        rect.setArcWidth(10);
        rect.setArcHeight(10);

        // Create content
        contentBox = new VBox(5);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(5));

        idLabel = new Label("Q" + id);
        idLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        sizeLabel = new Label("Size: 0");
        sizeLabel.setFont(Font.font("System", 11));

        visualBar = new ProgressBar(0);
        visualBar.setPrefWidth(100);
        visualBar.setPrefHeight(15);
        visualBar.setStyle("-fx-accent: #2196F3;");

        contentBox.getChildren().addAll(idLabel, sizeLabel, visualBar);

        getChildren().addAll(rect, contentBox);
        setLayoutX(x);
        setLayoutY(y);

        // Hover effect
        setOnMouseEntered(e -> {
            rect.setStrokeWidth(3);
            rect.setStroke(Color.ORANGE);
        });

        setOnMouseExited(e -> {
            rect.setStrokeWidth(2);
            rect.setStroke(isInputQueue ? Color.GREEN : Color.BLACK);
        });
    }

    public void setQueueSize(int size) {
        sizeLabel.setText("Size: " + size);

        // Update visual bar (assuming max size of 20 for visualization)
        double progress = Math.min(size / 20.0, 1.0);
        visualBar.setProgress(progress);

        // Change color based on fullness
        if (progress > 0.8) {
            visualBar.setStyle("-fx-accent: #f44336;"); // Red when nearly full
        } else if (progress > 0.5) {
            visualBar.setStyle("-fx-accent: #FF9800;"); // Orange when half full
        } else {
            visualBar.setStyle("-fx-accent: #2196F3;"); // Blue when empty/low
        }
    }

    public void setInputQueue(boolean isInput) {
        this.isInputQueue = isInput;
        if (isInput) {
            rect.setStroke(Color.GREEN);
            rect.setStrokeWidth(3);
            idLabel.setText("Q" + queueId + " (INPUT)");
            idLabel.setStyle("-fx-text-fill: green;");
        } else {
            rect.setStroke(Color.BLACK);
            rect.setStrokeWidth(2);
            idLabel.setText("Q" + queueId);
            idLabel.setStyle("-fx-text-fill: black;");
        }
    }

    public double getCenterX() {
        // Return center X in parent's coordinate space
        return getLayoutX() + getWidth() / 2;
    }

    public double getCenterY() {
        // Return center Y in parent's coordinate space
        return getLayoutY() + getHeight() / 2;
    }

    public double getNodeWidth() {
        return WIDTH;
    }

    public double getNodeHeight() {
        return HEIGHT;
    }
}
