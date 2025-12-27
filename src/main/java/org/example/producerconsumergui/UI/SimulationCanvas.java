package org.example.producerconsumergui.UI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.producerconsumergui.Model.Product;
import org.example.producerconsumergui.Model.SimulationCallback;
import org.example.producerconsumergui.Model.SimulationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimulationCanvas extends BorderPane {

    // Canvas and drawing area
    private Pane drawingPane;
    private ScrollPane scrollPane;

    // Components
    private List<MachineNode> machineNodes = new ArrayList<>();
    private List<QueueNode> queueNodes = new ArrayList<>();
//    private List<ConnectionLine> connections = new ArrayList<>();

    // State
    private DrawMode currentMode = DrawMode.SELECT;
    private MachineNode connectionSource = null;
    // private QueueNode inputQueue = null;
    private List<QueueNode> inputQueues = new ArrayList<>();

    // UI Controls
    private ToggleButton selectBtn, machineBtn, queueBtn, connectBtn, deleteBtn;
    private Button startBtn, stopBtn, replayBtn, pauseBtn, clearBtn;
    private Label statusLabel, modeLabel, queueCountLabel, machineCountLabel;
    private Slider speedSlider;
    private ProgressBar replayProgress;

    // Simulation manager
    private SimulationManager manager;

    // Grid settings
    private static final int GRID_SIZE = 20;
    private static final boolean SNAP_TO_GRID = true;

    // ID counters
    private int nextMachineId = 1;
    private int nextQueueId = 0;

    private ObservableList<ConnectionLine> connections = FXCollections.observableArrayList();

    public SimulationCanvas() {
        setupUI();
        setupManager();
    }

    private void setupUI() {
        // Top toolbar
        setTop(createToolbar());

        // Center drawing area
        drawingPane = new Pane();
        drawingPane.setPrefSize(2000, 1500);
        drawingPane.setStyle("-fx-background-color: #f5f5f5;");
        drawGridBackground();

        scrollPane = new ScrollPane(drawingPane);
        scrollPane.setPannable(true);
        setCenter(scrollPane);

        // Bottom control panel
        setBottom(createControlPanel());

        // Right side panel
        setRight(createSidePanel());
    }

    private void drawGridBackground() {
        for (int x = 0; x < 2000; x += GRID_SIZE) {
            Line line = new Line(x, 0, x, 1500);
            line.setStroke(Color.gray(0.9));
            line.setStrokeWidth(0.5);
            drawingPane.getChildren().add(line);
        }
        for (int y = 0; y < 1500; y += GRID_SIZE) {
            Line line = new Line(0, y, 2000, y);
            line.setStroke(Color.gray(0.9));
            line.setStrokeWidth(0.5);
            drawingPane.getChildren().add(line);
        }
    }

    private ToolBar createToolbar() {
        ToolBar toolbar = new ToolBar();
        toolbar.setPadding(new Insets(10));

        // Toggle group for drawing modes
        ToggleGroup modeGroup = new ToggleGroup();

        selectBtn = new ToggleButton("🖱 Select");
        selectBtn.setToggleGroup(modeGroup);
        selectBtn.setSelected(true);
        selectBtn.setOnAction(e -> currentMode = DrawMode.SELECT);

        machineBtn = new ToggleButton("⚙️ Add Machine");
        machineBtn.setToggleGroup(modeGroup);
        machineBtn.setOnAction(e -> currentMode = DrawMode.ADD_MACHINE);

        queueBtn = new ToggleButton("📦 Add Queue");
        queueBtn.setToggleGroup(modeGroup);
        queueBtn.setOnAction(e -> currentMode = DrawMode.ADD_QUEUE);

        connectBtn = new ToggleButton("🔗 Connect");
        connectBtn.setToggleGroup(modeGroup);
        connectBtn.setOnAction(e -> {
            currentMode = DrawMode.CONNECT;
            connectionSource = null;
        });

        deleteBtn = new ToggleButton("🗑 Delete");
        deleteBtn.setToggleGroup(modeGroup);
        deleteBtn.setOnAction(e -> currentMode = DrawMode.DELETE);

        Separator sep1 = new Separator();

        clearBtn = new Button("🧹 Clear All");
        clearBtn.setOnAction(e -> clearCanvas());

        modeLabel = new Label("Mode: Select");
        modeLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        modeLabel.setStyle("-fx-text-fill: #2196F3;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getItems().addAll(
                selectBtn, machineBtn, queueBtn, connectBtn, deleteBtn,
                sep1, clearBtn, spacer, modeLabel
        );

        return toolbar;
    }

    private HBox createControlPanel() {
        HBox controlPanel = new HBox(15);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.setStyle("-fx-background-color: #263238; -fx-border-color: #37474F; -fx-border-width: 1 0 0 0;");

        startBtn = new Button("▶ Start Simulation");
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        startBtn.setOnAction(e -> startSimulation());

        stopBtn = new Button("⏹ Stop");
        stopBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        stopBtn.setDisable(true);
        stopBtn.setOnAction(e -> stopSimulation());

        replayBtn = new Button("🔄 Replay");
        replayBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        replayBtn.setDisable(true);
        replayBtn.setOnAction(e -> replaySimulation());

        pauseBtn = new Button("⏯ Pause");
        pauseBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        pauseBtn.setDisable(true);
        pauseBtn.setOnAction(e -> togglePause());

        Separator sep = new Separator();
        sep.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Label speedLabel = new Label("Speed:");
        speedLabel.setStyle("-fx-text-fill: white;");

        speedSlider = new Slider(0.25, 4.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.75);
        speedSlider.setPrefWidth(200);
        speedSlider.setDisable(true);

        Label speedValue = new Label("1.0x");
        speedValue.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        speedSlider.valueProperty().addListener((obs, old, val) -> {
            speedValue.setText(String.format("%.1fx", val.doubleValue()));
            if (manager != null && manager.isReplaying()) {
                manager.setReplaySpeed(val.doubleValue());
            }
        });

        replayProgress = new ProgressBar(0);
        replayProgress.setPrefWidth(200);
        replayProgress.setVisible(false);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusLabel = new Label("Ready to design");
        statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14;");

        controlPanel.getChildren().addAll(
                startBtn, stopBtn, replayBtn, pauseBtn,
                sep, speedLabel, speedSlider, speedValue,
                replayProgress, spacer, statusLabel
        );

        return controlPanel;
    }

    private VBox createSidePanel() {
        VBox sidePanel = new VBox(15);
        sidePanel.setPadding(new Insets(15));
        sidePanel.setPrefWidth(250);
        sidePanel.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 1;");

        Label title = new Label("Statistics");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        machineCountLabel = new Label("Machines: 0");
        machineCountLabel.setFont(Font.font("System", 14));

        queueCountLabel = new Label("Queues: 0");
        queueCountLabel.setFont(Font.font("System", 14));

        Label connectionsLabel = new Label("Connections: 0");
        connectionsLabel.setFont(Font.font("System", 14));
        connectionsLabel.textProperty().bind(
                javafx.beans.binding.Bindings.createStringBinding(
                        () -> "Connections: " + connections.size(),
                        javafx.collections.FXCollections.observableList(connections)
                )
        );

        Separator sep1 = new Separator();

        Label infoTitle = new Label("Instructions");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextArea instructions = new TextArea(
                "1. Click 'Add Machine' and click on canvas to place machines\n\n" +
                        "2. Click 'Add Queue' and click to place queues\n\n" +
                        "3. Click 'Connect' then:\n   - Click a machine\n   - Click input queue\n   - Click output queue\n\n" +
                        "4. Right-click a queue and set it as input queue\n\n" +
                        "5. Click 'Start Simulation' to run\n\n" +
                        "6. After stopping, click 'Replay' to watch again\n\n" +
                        "7. Drag elements to reposition them"
        );
        instructions.setWrapText(true);
        instructions.setEditable(false);
        instructions.setPrefHeight(350);
        instructions.setStyle("-fx-font-size: 11;");

        sidePanel.getChildren().addAll(
                title, machineCountLabel, queueCountLabel, connectionsLabel,
                sep1, infoTitle, instructions
        );

        return sidePanel;
    }

    private void setupManager() {
        manager = new SimulationManager();

        // Setup callback for simulation events
        manager.setCallback(new SimulationCallback() {
            @Override
            public void onSimulationStarted() {
                statusLabel.setText("Simulation running...");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
            }

            @Override
            public void onSimulationStopped(int eventCount) {
                statusLabel.setText("Simulation stopped - " + eventCount + " events recorded");
                statusLabel.setStyle("-fx-text-fill: #FF9800;");
            }

            @Override
            public void onReplayStarted(int totalEvents) {
                statusLabel.setText("Replaying " + totalEvents + " events...");
                statusLabel.setStyle("-fx-text-fill: #2196F3;");
                replayProgress.setProgress(0);
            }

            @Override
            public void onReplayFinished() {
                statusLabel.setText("Replay finished");
                statusLabel.setStyle("-fx-text-fill: #4CAF50;");
                replayProgress.setProgress(1.0);

                pauseBtn.setDisable(true);
                speedSlider.setDisable(true);
                replayBtn.setDisable(false);
                enableDrawingTools();
            }

            @Override
            public void onReplayProgress(double progress) {
                replayProgress.setProgress(progress);
            }

            @Override
            public void onProductArrived(Product product) {
                // Visual feedback when product arrives
            }

            @Override
            public void onProductEnqueued(int queueId, Product product) {
                // Product added to queue
            }

            @Override
            public void onProductDequeued(int queueId, Product product) {
                // Product removed from queue
            }

            @Override
            public void onQueueSizeChanged(int queueId, int size) {
                // Update queue display
                for (QueueNode node : queueNodes) {
                    if (node.queueId == queueId) {
                        node.setQueueSize(size);
                        break;
                    }
                }
            }

            @Override
            public void onMachineStartedProcessing(int machineId, Product product) {
                // Machine started work
            }

            @Override
            public void onMachineFinishedProcessing(int machineId, Product product) {
                // Flash the machine
                for (MachineNode node : machineNodes) {
                    if (node.machineId == machineId) {
                        node.flash();
                        break;
                    }
                }
            }

            @Override
            public void onMachineColorChanged(int machineId, org.example.producerconsumergui.Model.Color color) {
                // Update machine color
                for (MachineNode node : machineNodes) {
                    if (node.machineId == machineId) {
                        node.setCurrentColor(color);
                        break;
                    }
                }
            }

            @Override
            public void onMachineStatusChanged(int machineId, String status) {
                // Update machine status
                for (MachineNode node : machineNodes) {
                    if (node.machineId == machineId) {
                        node.setStatus(status);
                        break;
                    }
                }
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });

        // Setup mouse handlers for drawing
        drawingPane.setOnMouseClicked(e -> {
            double x = SNAP_TO_GRID ? snapToGrid(e.getX()) : e.getX();
            double y = SNAP_TO_GRID ? snapToGrid(e.getY()) : e.getY();

            switch (currentMode) {
                case ADD_MACHINE:
                    addMachine(x, y);
                    break;
                case ADD_QUEUE:
                    addQueue(x, y);
                    break;
            }
        });
    }

    private void handleMachineClick(MachineNode node) {
        if (currentMode == DrawMode.CONNECT) {
            if (connectionSource == null) {
                // First click - select source machine
                connectionSource = node;
                node.circle.setStroke(Color.ORANGE);
                node.circle.setStrokeWidth(4);
                statusLabel.setText("Now click an input queue, then an output queue");
            }
        } else if (currentMode == DrawMode.DELETE) {
            deleteMachine(node);
        }
    }

    private QueueNode tempInputQueue = null;

    private void handleQueueClick(QueueNode node) {
        if (currentMode == DrawMode.CONNECT && connectionSource != null) {
            if (tempInputQueue == null) {
                // Second click - select input queue
                tempInputQueue = node;
                node.rect.setStroke(Color.ORANGE);
                node.rect.setStrokeWidth(4);
                statusLabel.setText("Now click the output queue");
            } else {
                // Third click - select output queue and create connection
                QueueNode outputQueue = node;

                // Create connection
                ConnectionLine conn = new ConnectionLine(connectionSource, tempInputQueue, outputQueue);
                connections.add(conn);
                drawingPane.getChildren().add(0, conn); // Add behind nodes

                // Reset styles
                connectionSource.circle.setStroke(Color.BLACK);
                connectionSource.circle.setStrokeWidth(2);
                tempInputQueue.rect.setStroke(Color.BLACK);
                tempInputQueue.rect.setStrokeWidth(2);

                // Reset state
                connectionSource = null;
                tempInputQueue = null;

                statusLabel.setText("Connection created: M" + conn.source.machineId +
                        " ← Q" + conn.inputQueue.queueId +
                        " → Q" + conn.outputQueue.queueId);
                updateStatistics();
            }
        } else if (currentMode == DrawMode.DELETE) {
            deleteQueue(node);
        }
    }

    private double snapToGrid(double value) {
        return Math.round(value / GRID_SIZE) * GRID_SIZE;
    }

    private void addMachine(double x, double y) {
        double centerX = x - MachineNode.RADIUS;
        double centerY = y - MachineNode.RADIUS;
        MachineNode node = new MachineNode(nextMachineId++, centerX, centerY);
        machineNodes.add(node);
        drawingPane.getChildren().add(node);

        // Make draggable
        makeDraggable(node);

        // Click handler
        node.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                handleMachineClick(node);
                e.consume();
            }
        });

        // Right-click menu
        node.setOnContextMenuRequested(e -> {
            showMachineContextMenu(node, e.getScreenX(), e.getScreenY());
        });

        updateStatistics();
        statusLabel.setText("Machine M" + node.machineId + " added");
    }

    private void addQueue(double x, double y) {
        double centerX = x - QueueNode.WIDTH / 2.0;
        double centerY = y - QueueNode.HEIGHT / 2.0;
        QueueNode node = new QueueNode(nextQueueId++, centerX, centerY);
        queueNodes.add(node);
        drawingPane.getChildren().add(node);

        // Make draggable
        makeDraggable(node);

        // Click handler
        node.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                handleQueueClick(node);
                e.consume();
            }
        });

        // Right-click menu
        node.setOnContextMenuRequested(e -> {
            showQueueContextMenu(node, e.getScreenX(), e.getScreenY());
        });

        updateStatistics();
        statusLabel.setText("Queue Q" + node.queueId + " added");
    }

    private void makeDraggable(Region node) {
        final double[] dragDelta = new double[2];

        node.setOnMousePressed(e -> {
            if (currentMode == DrawMode.SELECT) {
                double mouseX = e.getSceneX();
                double mouseY = e.getSceneY();
                double paneX = drawingPane.sceneToLocal(mouseX, mouseY).getX();
                double paneY = drawingPane.sceneToLocal(mouseX, mouseY).getY();
                dragDelta[0] = node.getLayoutX() - paneX;
                dragDelta[1] = node.getLayoutY() - paneY;
                node.setStyle(node.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);");
                // Disable canvas panning while dragging
                scrollPane.setPannable(false);
                e.consume();
            }
        });

        node.setOnMouseDragged(e -> {
            if (currentMode == DrawMode.SELECT) {
                double mouseX = e.getSceneX();
                double mouseY = e.getSceneY();
                double paneX = drawingPane.sceneToLocal(mouseX, mouseY).getX();
                double paneY = drawingPane.sceneToLocal(mouseX, mouseY).getY();
                double newX = paneX + dragDelta[0];
                double newY = paneY + dragDelta[1];

                if (SNAP_TO_GRID) {
                    newX = snapToGrid(newX);
                    newY = snapToGrid(newY);
                }

                node.setLayoutX(newX);
                node.setLayoutY(newY);

                updateConnections();
                e.consume();
            }
        });

        node.setOnMouseReleased(e -> {
            node.setStyle(node.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 0);", ""));
            // Re-enable canvas panning after drag
            scrollPane.setPannable(true);
            e.consume();
        });
    }

    private void showMachineContextMenu(MachineNode node, double x, double y) {
        ContextMenu menu = new ContextMenu();

        MenuItem delete = new MenuItem("Delete Machine");
        delete.setOnAction(e -> deleteMachine(node));

        MenuItem changeColor = new MenuItem("Change Color");
        changeColor.setOnAction(e -> changeMachineColor(node));

        menu.getItems().addAll(changeColor, delete);
        menu.show(node, x, y);
    }

    private void showQueueContextMenu(QueueNode node, double x, double y) {
        ContextMenu menu = new ContextMenu();

        boolean isInput = inputQueues.contains(node);
        MenuItem toggleInput = new MenuItem(isInput ? "✓ Input Queue" : "Set as Input Queue");
        toggleInput.setOnAction(e -> toggleInputQueue(node));

        MenuItem delete = new MenuItem("Delete Queue");
        delete.setOnAction(e -> deleteQueue(node));

        menu.getItems().addAll(toggleInput, delete);
        menu.show(node, x, y);
    }

    private void toggleInputQueue(QueueNode node) {
        if (inputQueues.contains(node)) {
            inputQueues.remove(node);
            node.setInputQueue(false);
            statusLabel.setText("Queue Q" + node.queueId + " unset as input queue");
        } else {
            inputQueues.add(node);
            node.setInputQueue(true);
            statusLabel.setText("Queue Q" + node.queueId + " set as input queue");
        }
    }

    private void changeMachineColor(MachineNode node) {
        // Color picker dialog would go here
        // For now, cycle through colors
        org.example.producerconsumergui.Model.Color[] colors = org.example.producerconsumergui.Model.Color.values();
        int currentIndex = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == node.originalColor) {
                currentIndex = i;
                break;
            }
        }
        node.setOriginalColor(colors[(currentIndex + 1) % colors.length]);
    }

    private void deleteMachine(MachineNode node) {
        machineNodes.remove(node);
        drawingPane.getChildren().remove(node);

        // Remove associated connections
        connections.removeIf(conn -> conn.source == node);
        drawingPane.getChildren().removeAll(
                connections.stream()
                        .filter(conn -> conn.source == node)
                        .toList()
        );

        updateConnections();
        updateStatistics();
    }

    private void deleteQueue(QueueNode node) {
        inputQueues.remove(node);
        queueNodes.remove(node);
        drawingPane.getChildren().remove(node);

        // Remove associated connections
        connections.removeIf(conn -> conn.inputQueue == node || conn.outputQueue == node);

        updateConnections();
        updateStatistics();
    }

    private void updateConnections() {
        for (ConnectionLine conn : connections) {
            conn.update();
        }
    }

    private void updateStatistics() {
        machineCountLabel.setText("Machines: " + machineNodes.size());
        queueCountLabel.setText("Queues: " + queueNodes.size());
    }

    private void clearCanvas() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Canvas");
        alert.setHeaderText("Clear all elements?");
        alert.setContentText("This will remove all machines, queues, and connections.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            machineNodes.clear();
            queueNodes.clear();
            connections.clear();
            inputQueues.clear();
            drawingPane.getChildren().clear();
            drawGridBackground();
            updateStatistics();
            statusLabel.setText("Canvas cleared");
        }
    }

    private void startSimulation() {
        if (inputQueues.isEmpty()) {
            showError("Please set at least one input queue first (right-click a queue)");
            return;
        }

        if (machineNodes.isEmpty()) {
            showError("Please add at least one machine");
            return;
        }

        // Build simulation - add all components to manager
        for (MachineNode node : machineNodes) {
            manager.addMachine(node.machineId, node.originalColor);
        }

        for (QueueNode node : queueNodes) {
            manager.addQueue(node.queueId);
        }

        // Set all input queues in the manager
        for (QueueNode node : inputQueues) {
            manager.addInputQueue(node.queueId);
        }

        // Add all connections
        for (ConnectionLine conn : connections) {
            manager.connectMachineToQueues(
                    conn.source.machineId,
                    conn.inputQueue.queueId,
                    conn.outputQueue.queueId
            );
        }

        // Start
        boolean started = manager.startSimulation();

        if (started) {
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
            replayBtn.setDisable(true);
            disableDrawingTools();
        }
    }

    private void stopSimulation() {
        System.out.println("SimulationCanvas.stopSimulation: Stopping simulation.");
        manager.stopSimulation();

        startBtn.setDisable(false);
        stopBtn.setDisable(true);
        boolean shouldDisableReplay = manager.hasSavedSimulations();
        System.out.println("[DEBUG] stopSimulation: Setting replayBtn.setDisable(" + shouldDisableReplay + ")");
        replayBtn.setDisable(!shouldDisableReplay);
        enableDrawingTools();
    }

    private void replaySimulation() {
        System.out.println("SimulationCanvas.replaySimulation: Attempting to replay.");
        System.out.println("[DEBUG] replayBtn.isDisabled() before startReplay: " + replayBtn.isDisabled());
        boolean started = manager.startReplay();

        if (started) {
            System.out.println("SimulationCanvas.replaySimulation: Replay started.");
            replayBtn.setDisable(true);
            pauseBtn.setDisable(false);
            speedSlider.setDisable(false);
            replayProgress.setVisible(true);
            disableDrawingTools();
        } else {
            System.out.println("SimulationCanvas.replaySimulation: Replay could not be started.");
            // Show error if replay could not be started
            showError("Replay could not be started. Make sure you have run and stopped a simulation with events recorded.");
        }
        System.out.println("[DEBUG] replayBtn.isDisabled() after startReplay: " + replayBtn.isDisabled());
    }

    private void togglePause() {
        manager.pauseReplay();
        pauseBtn.setText(manager.isReplaying() ? "⏯ Resume" : "⏯ Pause");
    }

    private void disableDrawingTools() {
        selectBtn.setDisable(true);
        machineBtn.setDisable(true);
        queueBtn.setDisable(true);
        connectBtn.setDisable(true);
        deleteBtn.setDisable(true);
        clearBtn.setDisable(true);
    }

    private void enableDrawingTools() {
        selectBtn.setDisable(false);
        machineBtn.setDisable(false);
        queueBtn.setDisable(false);
        connectBtn.setDisable(false);
        deleteBtn.setDisable(false);
        clearBtn.setDisable(false);
    }

    private void showError(String message) {
        System.out.println("SimulationCanvas.showError: " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    enum DrawMode {
        SELECT, ADD_MACHINE, ADD_QUEUE, CONNECT, DELETE
    }
}

