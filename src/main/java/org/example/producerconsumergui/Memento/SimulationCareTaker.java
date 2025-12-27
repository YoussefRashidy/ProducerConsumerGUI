package org.example.producerconsumergui.Memento;

import java.util.ArrayDeque;
import java.util.Deque;

public class SimulationCareTaker {
    private final Deque<SimulationMemento> deque = new ArrayDeque<>();

    public void addMemento(SimulationMemento memento) {
        deque.push(memento);
    }

    public SimulationMemento getMemento() {
        return deque.pop();
    }

    public SimulationMemento getLastMemento(){
        return deque.peekLast();
    }

    public boolean isEmpty(){
        return deque.isEmpty();
    }

    public boolean hasMementos() {
        return !deque.isEmpty();
    }

    public int getMementoCount() {
        return  deque.size();
    }

    public void clear() {
        this.deque.clear();
    }
}
