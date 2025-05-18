package project.bachelor.models;

import javafx.beans.property.*;

public class ProductsModel {
    private final IntegerProperty id;
    private final StringProperty category;
    private final StringProperty name;
    private final IntegerProperty stockQuantity;
    private final DoubleProperty price;

    public ProductsModel(int id, String category, String name, int stockQuantity, double price) {
        this.id = new SimpleIntegerProperty(id);
        this.category = new SimpleStringProperty(category);
        this.name = new SimpleStringProperty(name);
        this.stockQuantity = new SimpleIntegerProperty(stockQuantity);
        this.price = new SimpleDoubleProperty(price);
    }

    // ID
    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Category
    public String getCategory() {
        return category.get();
    }

    public StringProperty categoryProperty() {
        return category;
    }

    // Name
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    // Stock Quantity
    public int getStockQuantity() {
        return stockQuantity.get();
    }

    public IntegerProperty stockQuantityProperty() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity.set(stockQuantity);
    }

    // Price
    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }
}
