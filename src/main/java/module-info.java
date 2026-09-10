module com.managementsystem.demo1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.managementsystem.demo1 to javafx.fxml;
    exports com.managementsystem.demo1;
}