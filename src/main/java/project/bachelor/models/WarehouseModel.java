package project.bachelor.models;

import javafx.beans.property.*;

public class WarehouseModel {
    private final IntegerProperty id;
    private final StringProperty category;
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty stockQuantity;
    private final IntegerProperty inRequest;

    public WarehouseModel(int id, String category, String name, double price, int stockQuantity, int inRequest) {
        this.id = new SimpleIntegerProperty(id);
        this.category = new SimpleStringProperty(category);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.stockQuantity = new SimpleIntegerProperty(stockQuantity);
        this.inRequest = new SimpleIntegerProperty(inRequest);
    }

    // Getters
    public int getId() { return id.get(); }
    public String getCategory() { return category.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public int getStockQuantity() { return stockQuantity.get(); }
    public int getInRequest() { return inRequest.get(); }

    // Setters
    public void setStockQuantity(int value) { stockQuantity.set(value); }
    public void setInRequest(int value) { inRequest.set(value); }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public IntegerProperty stockQuantityProperty() { return stockQuantity; }
    public IntegerProperty inRequestProperty() { return inRequest; }
}
