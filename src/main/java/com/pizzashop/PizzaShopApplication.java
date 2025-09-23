package com.pizzashop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * คลาสหลักสำหรับเปิดโปรแกรม Pizza Shop
 */
public class PizzaShopApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // โหลด FXML ไฟล์สำหรับหน้าหลัก
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pizza/view/MainView.fxml"));
        Parent root = loader.load();

        // ตั้งค่าหน้าต่างหลัก
        primaryStage.setTitle("Pizza Shop Management System");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        // แสดงหน้าต่าง
        primaryStage.show();
    }

    /**
     * Method หลักสำหรับเริ่มโปรแกรม
     */
    public static void main(String[] args) {
        launch(args);
    }
}