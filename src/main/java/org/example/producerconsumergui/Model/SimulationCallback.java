package org.example.producerconsumergui.Model;

/**
 * Callback interface for the SimulationManager to communicate with the UI
 */
public interface SimulationCallback {

    // Simulation lifecycle
    void onSimulationStarted();
    void onSimulationStopped(int eventCount);

    // Replay lifecycle
    void onReplayStarted(int totalEvents);
    void onReplayFinished();
    void onReplayProgress(double progress);

    // Product events
    void onProductArrived(Product product);
    void onProductEnqueued(int queueId, Product product);
    void onProductDequeued(int queueId, Product product);

    // Queue updates
    void onQueueSizeChanged(int queueId, int size);

    // Machine events
    void onMachineStartedProcessing(int machineId, Product product);
    void onMachineFinishedProcessing(int machineId, Product product);
    void onMachineColorChanged(int machineId, Color color);
    void onMachineStatusChanged(int machineId, String status);

    // Error handling
    void onError(String message);
}
