package project.bachelor.models;

import javafx.beans.property.*;

public class SellModel {

    private final IntegerProperty id;
    private final StringProperty category;
    private final StringProperty name;
    private final DoubleProperty price;
    private final IntegerProperty inCart;

    public SellModel(int id, String category, String name, double price, int inCart) {
        this.id = new SimpleIntegerProperty(id);
        this.category = new SimpleStringProperty(category);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.inCart = new SimpleIntegerProperty(inCart);
    }

    public int getId() {
        return id.get();
    }

    public String getCategory() {
        return category.get();
    }

    public String getName() {
        return name.get();
    }

    public double getPrice() {
        return price.get();
    }

    public int getInCart() {
        return inCart.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public IntegerProperty inCartProperty() {
        return inCart;
    }
}
