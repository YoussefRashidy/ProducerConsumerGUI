package org.example.producerconsumergui.Memento;

import java.util.ArrayList;
import java.util.List;

public class SimulationRecorder {
    private final List<SimulationEvent> simulationEvents = new ArrayList<>();
    private boolean isRecording = false;

    public void startRecording() {
        simulationEvents.clear();
        isRecording = true;
    }

    public void stopRecording() {
        isRecording = false;
    }

    public synchronized void recordEvent(SimulationEvent event) {
        if (!isRecording) return;
        simulationEvents.add(event);
    }

    public SimulationMemento createMemento() {
        return new SimulationMemento(simulationEvents) ;
    }

}
