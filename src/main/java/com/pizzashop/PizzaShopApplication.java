package com.pizzashop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PizzaShopApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // set part
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pizza/view/MainView.fxml"));
        Parent root = loader.load();


        primaryStage.setTitle("Pizza Shop Management System");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}