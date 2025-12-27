package org.example.producerconsumergui.Memento;

import org.example.producerconsumergui.Model.Color;
import org.example.producerconsumergui.Model.Product;

public class SimulationEvent {
    private final Event event;
    private final long timestamp;
    private final int entityId;
    private final Product product ;
    private final Color color;

    public SimulationEvent(Event event, int entityId, Product product, Color color) {
        this.event = event;
        this.timestamp = System.currentTimeMillis();
        this.entityId = entityId;
        this.product = product;
        this.color = color;
    }

    public Event getEvent() {
        return event;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getEntityId() {
        return entityId;
    }

    public Product getProduct() {
        return product;
    }

    public Color getColor() {
        return color;
    }
}
