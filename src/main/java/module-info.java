/**
 * Module definition สำหรับ Pizza Shop Application
 */
module pizzashop {
    // JavaFX dependencies
    requires javafx.controls;
    requires javafx.fxml;

    // FormsFX dependency
    //requires com.dlsc.formsfx;

    // Java base modules
    requires java.base;
    requires java.desktop;

    // Export packages สำหรับ FXML และ reflection
    exports com.pizzashop;
    exports com.pizzashop.controller;
    exports com.pizzashop.model;

    // เปิดให้ JavaFX FXML เข้าถึง controllers
    opens com.pizzashop.controller to javafx.fxml;
    opens com.pizzashop to javafx.fxml;
}