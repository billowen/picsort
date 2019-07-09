package com.example.picsort;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PicsortApplication extends Application {
    static private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        springContext = SpringApplication.run(PicsortApplication.class);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setControllerFactory(springContext::getBean);
        fxmlLoader.setLocation(getClass().getResource("RootLayout.fxml"));
        AnchorPane rootNode = fxmlLoader.load();
        RootLayoutController controller = fxmlLoader.getController();
        controller.setParentStage(primaryStage);
        primaryStage.setTitle("Picture Sort");
//        System.out.println(getClass().getResource("pic-sort.ico").toString());
//        primaryStage.getIcons().add(new Image(getClass().getResource("pic-sort.ico").toString()));
        Scene scene = new Scene(rootNode);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        springContext.stop();
    }
}
