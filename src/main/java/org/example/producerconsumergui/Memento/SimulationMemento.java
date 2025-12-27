package org.example.producerconsumergui.Memento;

import java.util.ArrayList;
import java.util.List;

public class SimulationMemento {
    private final List<SimulationEvent> events ;
    private final long timestamp ;

    public SimulationMemento(List<SimulationEvent> simulationEvents) {
        this.timestamp = System.currentTimeMillis();
        this.events = new ArrayList<>(simulationEvents) ;
        // Debug: Print creation of memento and event count
        System.out.println("[DEBUG] SimulationMemento created at timestamp: " + this.timestamp + ", event count: " + simulationEvents.size());
    }

    public List<SimulationEvent> getEvents() {
        // Debug: Print when events are accessed
        System.out.println("[DEBUG] getEvents called, returning " + events.size() + " events");
        return events;
    }

    public long getTimestamp() {
        // Debug: Print when timestamp is accessed
        System.out.println("[DEBUG] getTimestamp called: " + timestamp);
        return timestamp;
    }

    public int getEventCount() {
        // Debug: Print when event count is accessed
        System.out.println("[DEBUG] getEventCount called: " + events.size());
        return events.size();
    }
}
