package org.example.producerconsumergui.Model;

import org.example.producerconsumergui.Memento.Event;
import org.example.producerconsumergui.Memento.SimulationEvent;
import org.example.producerconsumergui.Memento.SimulationRecorder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Machine implements Runnable, MachineObserver {
    private int id;
    private Color originalColor = Color.GREEN;
    private Color currentColor;
    private List<SimulationQueue> inputQueues = new ArrayList<>();
    private List<SimulationQueue> outputQueues = new ArrayList<>();
    private SimulationRecorder recorder;

    private volatile String status = "IDLE";
    private final Object lock = new Object();
    private boolean hasBeenNotified = false;
    private SimulationQueue notifiedQueue = null;

    public Machine(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Color getOriginalColor() {
        return originalColor;
    }

    public void setOriginalColor(Color originalColor) {
        this.originalColor = originalColor;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public List<SimulationQueue> getInputQueues() {
        return inputQueues;
    }

    public void setInputQueues(List<SimulationQueue> inputQueues) {
        this.inputQueues = inputQueues;
    }

    public List<SimulationQueue> getOutputQueues() {
        return outputQueues;
    }

    public void setOutputQueues(List<SimulationQueue> outputQueues) {
        this.outputQueues = outputQueues;
    }

    public void addInputQueue(SimulationQueue inputQueue) {
        inputQueues.add(inputQueue);
    }

    public void addOutputQueue(SimulationQueue outputQueue) {
        outputQueues.add(outputQueue);
    }


    @Override
    public void notifyProduct(SimulationQueue queue) {
        synchronized (lock) {
            this.hasBeenNotified = true;
            this.notifiedQueue = queue;
            this.lock.notify();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                SimulationQueue sourceQueue = null;
                for (SimulationQueue inputQueue : inputQueues) {
                    inputQueue.registerMachine(this);
                }
                synchronized (lock) {
                    while (!hasBeenNotified) {
                        status = "WAITING";
                        lock.wait(); // Spurious Wakeup protection
                    }
                    hasBeenNotified = false;
                    sourceQueue = notifiedQueue;
                }

                Product product = sourceQueue.dequeue();
                if (product == null) continue;

                if (recorder != null) {
                    recorder.recordEvent(new SimulationEvent(
                            Event.MACHINE_STARTED_PROCESSING,
                            id,
                            product,
                            product.getProductColor()
                    ));
                }

                currentColor = product.getProductColor();
                if (recorder != null) {
                    recorder.recordEvent(new SimulationEvent(
                            Event.MACHINE_COLOR_CHANGED,
                            id,
                            product,
                            product.getProductColor()
                    ));
                }

                status = "PROCESSING";
                Thread.sleep((int) (Math.random() * 3000));

                var outputQueue = getOutputQueue();
                outputQueue.enqueue(product);

                if (recorder != null) {
                    recorder.recordEvent(new SimulationEvent(
                            Event.MACHINE_FINISHED_PROCESSING,
                            id,
                            product,
                            product.getProductColor()
                    ));
                }
                currentColor = originalColor;

                if (recorder != null) {
                    recorder.recordEvent(new SimulationEvent(
                            Event.MACHINE_COLOR_CHANGED,
                            id,
                            null,
                            currentColor
                    ));
                }
                status = "IDLE" ;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private SimulationQueue getAvailableQueue() {
        for (SimulationQueue inputQueue : inputQueues) {
            if (!inputQueue.isEmpty()) return inputQueue;
        }
        return null;
    }

    private SimulationQueue getOutputQueue() {
        return outputQueues.stream()
                .min(Comparator.comparingInt(SimulationQueue::size))
                .orElse(outputQueues.getFirst());
    }

    public void setRecorder(SimulationRecorder recorder) {
        this.recorder = recorder;
    }

    public String getStatus() {
        return this.status;
    }
}
