package org.example.producerconsumergui.Model;
import org.example.producerconsumergui.Model.*;
import org.example.producerconsumergui.Memento.*;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimulationManager {

    // Model components - maps for quick lookup by ID
    private Map<Integer, Machine> machines = new HashMap<>();
    private Map<Integer, SimulationQueue> queues = new HashMap<>();
    // private SimulationQueue inputQueue;
    private List<SimulationQueue> inputQueues = new ArrayList<>();

    // Memento pattern components
    private SimulationRecorder recorder = new SimulationRecorder();
    private SimulationCareTaker caretaker = new SimulationCareTaker();
    private SimulationReplayer replayer;

    // Threading
    private List<Thread> machineThreads = new ArrayList<>();
    private Thread productGeneratorThread;
    private ScheduledExecutorService uiUpdater;
    private Thread replayThread;

    // State flags
    private volatile boolean isRunning = false;
    private volatile boolean isReplaying = false;
    private volatile boolean isPaused = false;

    // UI callback interface
    private SimulationCallback callback;

    // Product generation
    private int nextProductId = 0;
    private static final int MIN_ARRIVAL_TIME = 500;  // ms
    private static final int MAX_ARRIVAL_TIME = 2000; // ms

    public SimulationManager() {
        // Constructor
    }

    // ============================================
    // SETUP METHODS
    // ============================================

    /**
     * Sets the callback for UI updates
     */
    public void setCallback(SimulationCallback callback) {
        this.callback = callback;
    }

    /**
     * Adds a machine to the simulation
     */
    public void addMachine(int machineId, Color defaultColor) {
        Machine machine = new Machine(machineId);
        machine.setOriginalColor(defaultColor);
        machine.setRecorder(recorder);
        machines.put(machineId, machine);
    }

    /**
     * Adds a queue to the simulation
     */
    public void addQueue(int queueId) {
        SimulationQueue queue = new SimulationQueue(queueId);
        queue.setRecorder(recorder);
        queues.put(queueId, queue);
    }

    /**
     * Adds a queue to the list of input queues (where products arrive)
     */
    public void addInputQueue(int queueId) {
        SimulationQueue queue = queues.get(queueId);
        if (queue != null && !inputQueues.contains(queue)) {
            inputQueues.add(queue);
        }
    }

    /**
     * Removes a queue from the list of input queues
     */
    public void removeInputQueue(int queueId) {
        SimulationQueue queue = queues.get(queueId);
        inputQueues.remove(queue);
    }

    /**
     * Connects a machine to its input and output queues
     */
    public void connectMachineToQueues(int machineId, int inputQueueId, int outputQueueId) {
        Machine machine = machines.get(machineId);
        SimulationQueue inQueue = queues.get(inputQueueId);
        SimulationQueue outQueue = queues.get(outputQueueId);

        if (machine != null && inQueue != null && outQueue != null) {
            machine.addInputQueue(inQueue);
            machine.addOutputQueue(outQueue);
        }
    }

    // ============================================
    // SIMULATION CONTROL
    // ============================================

    /**
     * Starts the simulation
     * Returns true if started successfully, false if there's an error
     */
    public boolean startSimulation() {
        if (isRunning) {
            notifyError("Simulation is already running");
            return false;
        }

        // Validation
        if (inputQueues.isEmpty()) {
            notifyError("No input queue set. Right-click a queue to set it as input.");
            return false;
        }

        if (machines.isEmpty()) {
            notifyError("No machines added. Add at least one machine.");
            return false;
        }

        // Validate all machines have proper connections
        for (Machine machine : machines.values()) {
            if (machine.getInputQueues().isEmpty()) {
                notifyError("Machine M" + machine.getId() + " has no input queue connected.");
                return false;
            }
            if (machine.getOutputQueues().isEmpty()) {
                notifyError("Machine M" + machine.getId() + " has no output queue connected.");
                return false;
            }
        }

        // Start simulation
        isRunning = true;
        nextProductId = 0;
        recorder.startRecording();

        // Clear all queues before starting
        clearAllQueues();

        // Start machine threads
        machineThreads.clear();
        for (Machine machine : machines.values()) {
            Thread thread = new Thread(machine, "Machine-" + machine.getId());
            thread.setDaemon(true);
            machineThreads.add(thread);
            thread.start();
        }

        // Start product generator thread
        productGeneratorThread = new Thread(this::generateProducts, "ProductGenerator");
        productGeneratorThread.setDaemon(true);
        productGeneratorThread.start();

        // Start UI updater (updates UI every 100ms)
        uiUpdater = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "UI-Updater");
            t.setDaemon(true);
            return t;
        });
        uiUpdater.scheduleAtFixedRate(this::updateUI, 0, 100, TimeUnit.MILLISECONDS);

        notifySimulationStarted();
        return true;
    }

    /**
     * Stops the running simulation
     */
    public void stopSimulation() {
        if (!isRunning) {
            System.out.println("stopSimulation: Not running, nothing to stop.");
            return;
        }

        isRunning = false;
        recorder.stopRecording();
        System.out.println("stopSimulation: Stopped recording.");

        // Stop product generator
        if (productGeneratorThread != null) {
            productGeneratorThread.interrupt();
            try {
                productGeneratorThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Stop all machine threads
        for (Thread thread : machineThreads) {
            thread.interrupt();
        }

        // Wait for machine threads to finish
        for (Thread thread : machineThreads) {
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        machineThreads.clear();

        // Stop UI updater
        if (uiUpdater != null) {
            uiUpdater.shutdown();
            try {
                uiUpdater.awaitTermination(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Save the simulation to memento
        SimulationMemento memento = recorder.createMemento();
        System.out.println("stopSimulation: Memento created with event count: " + memento.getEventCount());
        caretaker.addMemento(memento);
        System.out.println("stopSimulation: Memento added to caretaker. Total mementos: " + caretaker.getMementoCount());

        notifySimulationStopped(memento.getEventCount());
    }

    // ============================================
    // REPLAY CONTROL
    // ============================================

    /**
     * Starts replaying the last recorded simulation
     */
    public boolean startReplay() {
        System.out.println("startReplay: Attempting to start replay.");
        if (!caretaker.hasMementos()) {
            System.out.println("startReplay: No simulation to replay. Run a simulation first.");
            notifyError("No simulation to replay. Run a simulation first.");
            return false;
        }

        if (isRunning) {
            System.out.println("startReplay: Simulation is running, stopping first.");
            stopSimulation();
        }

        // Clear all queues for visual replay
        clearAllQueues();

        SimulationMemento memento = caretaker.getLastMemento();
        if (memento == null || memento.getEventCount() == 0) {
            System.out.println("startReplay: No events recorded in the last simulation. Replay cannot start.");
            notifyError("No events recorded in the last simulation. Replay cannot start.");
            return false;
        }

        System.out.println("startReplay: Starting replay with " + memento.getEventCount() + " events.");
        isReplaying = true;
        isPaused = false;

        replayer = new SimulationReplayer(memento);
        replayer.startReplay();

        // Start replay thread
        replayThread = new Thread(this::runReplay, "ReplayThread");
        replayThread.setDaemon(true);
        replayThread.start();

        notifyReplayStarted(memento.getEventCount());
        return true;
    }

    /**
     * The main replay loop - processes events with timing
     */
    private void runReplay() {
        long lastEventTimestamp = -1;
        while (isReplaying && replayer.hasMoreEvents()) {
            if (!isPaused) {
                SimulationEvent event = replayer.peekNext();
                if (event != null) {
                    long eventTimestamp = event.getTimestamp();
                    if (lastEventTimestamp != -1) {
                        long delay = (long) ((eventTimestamp - lastEventTimestamp) / replayer.getReplaySpeed());
                        if (delay > 0) {
                            try {
                                Thread.sleep(delay);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                    event = replayer.getNextEvent();
                    if (event != null) {
                        // Process event on JavaFX thread
                        SimulationEvent finalEvent = event;
                        Platform.runLater(() -> processReplayEvent(finalEvent));
                    }
                    lastEventTimestamp = eventTimestamp;
                }
            }
        }

        isReplaying = false;
        Platform.runLater(this::notifyReplayFinished);
    }

    /**
     * Processes a single replay event and notifies the UI
     */
    private void processReplayEvent(SimulationEvent event) {
        if (callback == null) return;

        switch (event.getEvent()) {
            case PRODUCT_ARRIVED:
                callback.onProductArrived(event.getProduct());
                break;

            case PRODUCT_ENQUEUED: {
                SimulationQueue queue = queues.get(event.getEntityId());
                if (queue != null) {
                    // Add product to queue (without recording event)
                    queue.enqueueWithoutRecord(event.getProduct());
                    callback.onProductEnqueued(event.getEntityId(), event.getProduct());
                    callback.onQueueSizeChanged(event.getEntityId(), queue.size());
                }
                break;
            }

            case PRODUCT_DEQUEUED: {
                SimulationQueue queue = queues.get(event.getEntityId());
                if (queue != null) {
                    // Remove product from queue (without recording event)
                    queue.dequeueWithoutRecord(event.getProduct());
                    callback.onProductDequeued(event.getEntityId(), event.getProduct());
                    callback.onQueueSizeChanged(event.getEntityId(), queue.size());
                }
                break;
            }

            case MACHINE_STARTED_PROCESSING:
                callback.onMachineStartedProcessing(event.getEntityId(), event.getProduct());
                break;

            case MACHINE_FINISHED_PROCESSING:
                callback.onMachineFinishedProcessing(event.getEntityId(), event.getProduct());
                break;

            case MACHINE_COLOR_CHANGED:
                callback.onMachineColorChanged(event.getEntityId(), event.getColor());
                break;
        }

        // Update replay progress
        callback.onReplayProgress(replayer.getProgress());
    }

    /**
     * Toggles pause/resume for replay
     */
    public void pauseReplay() {
        if (replayer != null && isReplaying) {
            if (isPaused) {
                replayer.resumeReplay();
                isPaused = false;
            } else {
                replayer.pauseReplay();
                isPaused = true;
            }
        }
    }

    /**
     * Stops the current replay
     */
    public void stopReplay() {
        isReplaying = false;
        if (replayThread != null) {
            replayThread.interrupt();
        }
        notifyReplayFinished();
    }

    /**
     * Sets the replay speed (1.0 = normal, 2.0 = 2x, 0.5 = half speed)
     */
    public void setReplaySpeed(double speed) {
        if (replayer != null) {
            replayer.setReplaySpeed(speed);
        }
    }

    // ============================================
    // PRODUCT GENERATION
    // ============================================

    /**
     * Generates products at random intervals and adds them to the input queue
     */
    private void generateProducts() {
        Random rand = new Random();
        while (isRunning) {
            try {
                int arrivalTime = MIN_ARRIVAL_TIME + rand.nextInt(MAX_ARRIVAL_TIME - MIN_ARRIVAL_TIME + 1);
                Thread.sleep(arrivalTime);

                // Generate random color
                Color[] colors = Color.values();
                Color randomColor = colors[(int)(Math.random() * colors.length)];

                // Create product
                Product product = new Product(nextProductId++, randomColor);

                // Pick a random input queue to inject the product into (or inject into all)
                if (!inputQueues.isEmpty()) {
                    // Option 1: Inject into all input queues
                    for (SimulationQueue inputQueue : inputQueues) {
                        recorder.recordEvent(new SimulationEvent(
                                Event.PRODUCT_ARRIVED,
                                inputQueue.getId(),
                                product,
                                randomColor
                        ));
                        inputQueue.enqueue(product);
                        if (callback != null) {
                            Platform.runLater(() -> {
                                callback.onProductArrived(product);
                                callback.onQueueSizeChanged(inputQueue.getId(), inputQueue.size());
                            });
                        }
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ============================================
    // UI UPDATE
    // ============================================

    /**
     * Periodically updates the UI with current simulation state
     */
    private void updateUI() {
        if (!isRunning || callback == null) {
            return;
        }

        Platform.runLater(() -> {
            // Update all queue sizes
            for (Map.Entry<Integer, SimulationQueue> entry : queues.entrySet()) {
                callback.onQueueSizeChanged(entry.getKey(), entry.getValue().size());
            }

            // Update all machine colors and status
            for (Map.Entry<Integer, Machine> entry : machines.entrySet()) {
                Machine machine = entry.getValue();
                callback.onMachineColorChanged(machine.getId(), machine.getCurrentColor());
                callback.onMachineStatusChanged(machine.getId(), machine.getStatus());
            }
        });
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    /**
     * Clears all queues
     */
    private void clearAllQueues() {
        for (SimulationQueue queue : queues.values()) {
            while (!queue.isEmpty()) {
                queue.dequeue();
            }
        }
    }

    // ============================================
    // NOTIFICATION METHODS (to UI)
    // ============================================

    private void notifySimulationStarted() {
        if (callback != null) {
            Platform.runLater(() -> callback.onSimulationStarted());
        }
    }

    private void notifySimulationStopped(int eventCount) {
        if (callback != null) {
            Platform.runLater(() -> callback.onSimulationStopped(eventCount));
        }
    }

    private void notifyReplayStarted(int totalEvents) {
        if (callback != null) {
            Platform.runLater(() -> callback.onReplayStarted(totalEvents));
        }
    }

    private void notifyReplayFinished() {
        if (callback != null) {
            callback.onReplayFinished();
        }
    }

    private void notifyError(String message) {
        System.out.println("SimulationManager.notifyError: " + message);
        if (callback != null) {
            Platform.runLater(() -> callback.onError(message));
        }
    }

    // ============================================
    // GETTERS
    // ============================================

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isReplaying() {
        return isReplaying;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean hasSavedSimulations() {
        return caretaker.hasMementos();
    }

    public int getSavedSimulationCount() {
        return caretaker.getMementoCount();
    }

    public void clearSavedSimulations() {
        caretaker.clear();
    }

    // ============================================
    // CLEANUP
    // ============================================

    /**
     * Cleans up resources when shutting down
     */
    public void shutdown() {
        if (isRunning) {
            stopSimulation();
        }
        if (isReplaying) {
            stopReplay();
        }
        machines.clear();
        queues.clear();
    }
}

