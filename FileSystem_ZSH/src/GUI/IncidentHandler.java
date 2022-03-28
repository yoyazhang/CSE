package GUI;

import Exception.ErrorCode;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class IncidentHandler extends Application {
    private Pane pane;
    public IncidentHandler(String message){
        pane = new Pane();
        Text content = new Text(message);
        pane.getChildren().add(content);
        content.setLayoutY(30);
        content.setLayoutX(10);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(pane, 350, 150);
        pane.getStylesheets().add("Style/incident.css");
        primaryStage.setTitle("incident!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
