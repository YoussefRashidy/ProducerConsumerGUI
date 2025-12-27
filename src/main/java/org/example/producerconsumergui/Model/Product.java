package org.example.producerconsumergui.Model;

public class Product {
    private int id ;
    private Color productColor;

    public Product(int id, Color productColor) {
        this.id = id;
        this.productColor = productColor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Color getProductColor() {
        return productColor;
    }

    public void setProductColor(Color productColor) {
        this.productColor = productColor;
    }
}
