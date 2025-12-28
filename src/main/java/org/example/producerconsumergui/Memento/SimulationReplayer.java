package org.example.producerconsumergui.Memento;

import java.util.List;

public class SimulationReplayer {
    private final SimulationMemento simulationMemento;
    private final List<SimulationEvent> simulationEvents;
    private int currentEvent;
    private long replayStartTime;
    private long recordingstartTime;
    private long replayEndTime;
    private boolean isPaused = false;
    private long pauseStartTime;
    private long totalPausedTime = 0;

    private double replaySpeed = 1.0;

    public SimulationReplayer(SimulationMemento simulationMemento) {
        if (simulationMemento == null) throw new NullPointerException("simulationMemento can't be null");
        this.simulationMemento = simulationMemento;
        this.simulationEvents = simulationMemento.getEvents();
        if (!simulationEvents.isEmpty())
            this.recordingstartTime = simulationEvents.getFirst().getTimestamp();
    }

    public void startReplay() {
        currentEvent = 0;
        replayStartTime = System.currentTimeMillis();
        totalPausedTime = 0;
        isPaused = false;
    }

    public SimulationEvent getNextEvent() {
        if (isPaused || currentEvent >= simulationEvents.size())
            return null;
        SimulationEvent event = simulationEvents.get(currentEvent);
        long eventOriginalTime = event.getTimestamp() - recordingstartTime;
        long adjustedTime = (long) (eventOriginalTime / replaySpeed);

        long currentTime = System.currentTimeMillis() - replayStartTime - totalPausedTime;
        if (currentTime >= adjustedTime) {
            currentEvent++;
            return event;
        }

        return null;

    }

    public SimulationEvent peekNext(){
        if (currentEvent >= simulationEvents.size())
            return null;
        return simulationEvents.get(currentEvent);
    }

    public void skipToEvent(int index){
        if (index < 0 || index >= simulationEvents.size()) return;
        long targetEventTime = simulationEvents.get(index).getTimestamp() - recordingstartTime;
        replayStartTime = System.currentTimeMillis() - targetEventTime;
    }

    public void pauseReplay() {
        if (!isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
        }
    }
    public void resumeReplay() {
        if (isPaused) {
            isPaused = false;
            totalPausedTime += System.currentTimeMillis() - pauseStartTime;
        }
    }

    public void stopReplay() {
        currentEvent = simulationEvents.size() ;
    }

    public void restart() {
        startReplay();
    }

    public void setReplaySpeed(double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Speed must be positive");
        }

        if (currentEvent > 0 && currentEvent < simulationEvents.size()) {
            long currentEventTime = simulationEvents.get(currentEvent).getTimestamp() - recordingstartTime;
            long adjustedTime = (long) (currentEventTime / this.replaySpeed);
            long elapsed = System.currentTimeMillis() - replayStartTime - totalPausedTime;
            long newAdjustedTime = (long) (currentEventTime / speed);
            replayStartTime += (adjustedTime - newAdjustedTime);
        }

        this.replaySpeed = speed;
    }

    public boolean hasMoreEvents() {
        return currentEvent < simulationEvents.size();
    }

    public int getTotalEvents() {
        return simulationEvents.size();
    }

    public int getCurrentEventIndex() {
        return currentEvent;
    }

    public double getProgress() {
        if (simulationEvents.isEmpty()) {
            return 1.0;
        }
        return (double) currentEvent / simulationEvents.size();
    }

    public double getReplaySpeed() {
        return replaySpeed;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public long getEstimatedTimeRemaining() {
        if (currentEvent >= simulationEvents.size() || simulationEvents.isEmpty()) {
            return 0;
        }

        long lastEventTime = simulationEvents.get(simulationEvents.size() - 1).getTimestamp() - recordingstartTime;
        long currentEventTime = currentEvent < simulationEvents.size()
                ? simulationEvents.get(currentEvent).getTimestamp() - recordingstartTime
                : lastEventTime;

        long remainingOriginalTime = lastEventTime - currentEventTime;
        return (long) (remainingOriginalTime / replaySpeed);
    }
}

