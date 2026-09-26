module org.roniyaakobi.regitprototype {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.roniyaakobi.regitprototype to javafx.fxml;
    exports org.roniyaakobi.regitprototype;
}