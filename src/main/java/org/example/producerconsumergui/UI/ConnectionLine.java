package org.example.producerconsumergui.UI;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class ConnectionLine extends Group {
    public final MachineNode source;
    public final QueueNode inputQueue;
    public final QueueNode outputQueue;

    private Line line1; // Machine to input queue
    private Line line2; // Output queue to machine
    private Polygon arrow1;
    private Polygon arrow2;
    private Text label;

    public ConnectionLine(MachineNode source, QueueNode inputQueue, QueueNode outputQueue) {
        this.source = source;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;

        // Create lines
        line1 = new Line();
        line1.setStroke(Color.DARKBLUE);
        line1.setStrokeWidth(2);
        line1.getStrokeDashArray().addAll(5.0, 5.0);

        line2 = new Line();
        line2.setStroke(Color.DARKGREEN);
        line2.setStrokeWidth(2);

        // Create arrows
        arrow1 = createArrowHead();
        arrow1.setFill(Color.DARKBLUE);

        arrow2 = createArrowHead();
        arrow2.setFill(Color.DARKGREEN);

        // Create label
        label = new Text();
        label.setFont(Font.font("System", 10));
        label.setFill(Color.GRAY);

        getChildren().addAll(line1, arrow1, line2, arrow2, label);

        update();

        // Click to delete
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                // Double-click to delete
                ((javafx.scene.layout.Pane) getParent()).getChildren().remove(this);
            }
        });

        // Hover effect
        setOnMouseEntered(e -> {
            line1.setStrokeWidth(3);
            line2.setStrokeWidth(3);
            line1.setStroke(Color.ORANGE);
            line2.setStroke(Color.ORANGE);
        });

        setOnMouseExited(e -> {
            line1.setStrokeWidth(2);
            line2.setStrokeWidth(2);
            line1.setStroke(Color.DARKBLUE);
            line2.setStroke(Color.DARKGREEN);
        });
    }

    public void update() {
        // Use localToParent to get the center in the parent's coordinate space
        double[] sourceCenter = getNodeCenter(source);
        double[] inputCenter = getNodeCenter(inputQueue);
        double[] outputCenter = getNodeCenter(outputQueue);

        // Calculate connection points on edges
        double angle1 = Math.atan2(sourceCenter[1] - inputCenter[1], sourceCenter[0] - inputCenter[0]);
        double qx1 = inputCenter[0] + Math.cos(angle1) * (inputQueue.getNodeWidth() / 2);
        double qy1 = inputCenter[1] + Math.sin(angle1) * (inputQueue.getNodeHeight() / 2);
        double mx1 = sourceCenter[0] - Math.cos(angle1) * source.getRadius();
        double my1 = sourceCenter[1] - Math.sin(angle1) * source.getRadius();

        line1.setStartX(qx1);
        line1.setStartY(qy1);
        line1.setEndX(mx1);
        line1.setEndY(my1);

        updateArrow(arrow1, mx1, my1, angle1);

        // Update line from machine to output queue
        double angle2 = Math.atan2(outputCenter[1] - sourceCenter[1], outputCenter[0] - sourceCenter[0]);
        double mx2 = sourceCenter[0] + Math.cos(angle2) * source.getRadius();
        double my2 = sourceCenter[1] + Math.sin(angle2) * source.getRadius();
        double qx2 = outputCenter[0] - Math.cos(angle2) * (outputQueue.getNodeWidth() / 2);
        double qy2 = outputCenter[1] - Math.sin(angle2) * (outputQueue.getNodeHeight() / 2);

        line2.setStartX(mx2);
        line2.setStartY(my2);
        line2.setEndX(qx2);
        line2.setEndY(qy2);

        updateArrow(arrow2, qx2, qy2, angle2);

        // Update label position (midpoint of machine)
        label.setX(sourceCenter[0] - 20);
        label.setY(sourceCenter[1] - source.getRadius() - 10);
        label.setText("M" + source.machineId);
    }

    private double[] getNodeCenter(javafx.scene.Node node) {
        // Get the center of the node in the parent's coordinate space
        double x = node.getLayoutX();
        double y = node.getLayoutY();
        double w = node.getBoundsInParent().getWidth();
        double h = node.getBoundsInParent().getHeight();
        if (w == 0 && h == 0 && node instanceof MachineNode) {
            w = ((MachineNode) node).getRadius() * 2;
            h = ((MachineNode) node).getRadius() * 2;
        } else if (w == 0 && h == 0 && node instanceof QueueNode) {
            w = ((QueueNode) node).getNodeWidth();
            h = ((QueueNode) node).getNodeHeight();
        }
        return new double[] { x + w / 2, y + h / 2 };
    }

    private Polygon createArrowHead() {
        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(
                0.0, 0.0,
                -10.0, -5.0,
                -10.0, 5.0
        );
        return arrow;
    }

    private void updateArrow(Polygon arrow, double x, double y, double angle) {
        arrow.setLayoutX(x);
        arrow.setLayoutY(y);
        arrow.setRotate(Math.toDegrees(angle));
    }
}
