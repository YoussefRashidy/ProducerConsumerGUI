package org.example.producerconsumergui.Model;

import org.example.producerconsumergui.Memento.Event;
import org.example.producerconsumergui.Memento.SimulationEvent;
import org.example.producerconsumergui.Memento.SimulationRecorder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SimulationQueue {
    private int id;
    private final Queue<Product> queue = new LinkedList<>();
    private final List<MachineObserver> observerList = new ArrayList<>();
    private SimulationRecorder recorder;

    public SimulationQueue(int id) {
        this.id = id;
    }

    public void setRecorder(SimulationRecorder recorder) {
        this.recorder = recorder;
    }

    public void enqueue(Product product) {
        MachineObserver machine = null;
        synchronized (this) {
            queue.add(product);
            // Record product enqueue
            if (recorder != null) {
                recorder.recordEvent(new SimulationEvent(
                        Event.PRODUCT_ENQUEUED,
                        id,
                        product,
                        product.getProductColor()
                ));
            }
            if (!observerList.isEmpty()) {
                machine = observerList.removeFirst();
            }
        }
        if (machine != null) {
            machine.notifyProduct(this);
        }
    }

    public synchronized Product dequeue() {
        Product product =  queue.poll();
        if (product != null&& recorder != null) {
            recorder.recordEvent(new SimulationEvent(
                    Event.PRODUCT_DEQUEUED,
                    id,
                    product,
                    product.getProductColor()
            ));
        }
        return product;
    }

    public void registerMachine(MachineObserver observer) {
        MachineObserver machine = null;
        synchronized (this) {
            if (!observerList.contains(observer)) {
                observerList.add(observer);
            }
            if (!queue.isEmpty()) {
                machine = observerList.removeFirst();
            }
        }
        if (machine != null) machine.notifyProduct(this);
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized int size() {
        return queue.size();
    }


    public int getId() {
        return this.id ;
    }

    public void enqueueWithoutRecord(Product product) {
        synchronized (this) {
            queue.add(product);
        }
    }

    public void dequeueWithoutRecord(Product product) {
        synchronized (this) {
            queue.remove(product);
        }
    }
}
