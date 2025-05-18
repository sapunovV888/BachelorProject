package project.bachelor.models;

import javafx.beans.property.*;

public class CashDeskModel {
    private final StringProperty date;
    private final StringProperty cashierName;
    private final DoubleProperty revenue;

    public CashDeskModel(String date, String cashierName, double revenue) {
        this.date = new SimpleStringProperty(date);
        this.cashierName = new SimpleStringProperty(cashierName);
        this.revenue = new SimpleDoubleProperty(revenue);
    }

    // Getters
    public String getDate() {
        return date.get();
    }

    public String getCashierName() {
        return cashierName.get();
    }

    public double getRevenue() {
        return revenue.get();
    }

    // Property getters (для таблиці)
    public StringProperty dateProperty() {
        return date;
    }

    public StringProperty cashierNameProperty() {
        return cashierName;
    }

    public DoubleProperty revenueProperty() {
        return revenue;
    }
}
