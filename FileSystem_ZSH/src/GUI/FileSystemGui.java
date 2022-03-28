package GUI;

import Block.BlockManager;
import File.FileManager;
import GUI.FileReadGui;
import Sys.FileSystem;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class FileSystemGui extends Application {
    private BorderPane mainPane = new BorderPane();
    private Button refresh = new Button("Refresh");
    private FlowPane fileManagers = new FlowPane();
    private FlowPane blockManagers = new FlowPane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws FileNotFoundException {
        Scene scene = new Scene(mainPane, 650, 300);
        FileSystem.initialize(5,3);
        setMainPane();
        mainPane.getStylesheets().add("./Style/system.css");
        primaryStage.setTitle("Zsh_FileSystem");
        //primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void setMainPane() throws FileNotFoundException {
        setFilePart();
        setBlockPart();
        Line line = new Line(250,0,250,300);
        mainPane.setCenter(line);
        mainPane.setId("main");
        fileManagers.setId("fms");
        blockManagers.setId("bms");
    }
    private void setFilePart() throws FileNotFoundException {
        fileManagers.setHgap(10);
        fileManagers.setVgap(10);
        for (FileManager fm: FileSystem.getFms()){
            Image image = new Image(new FileInputStream("images/fm.png"));
            ImageView v = new ImageView(image);
            v.setFitWidth(30);
            v.setFitHeight(30);
            Label l = new Label("fm-" + fm.getFmID());
            VBox vbox = new VBox(v,l);
            vbox.setPrefSize(45,40);
            vbox.setOnMouseClicked(e->{
                Stage stage = new Stage();
                FileReadGui f = new FileReadGui(fm.getFmID());
                f.start(stage);
            });
            fileManagers.getChildren().add(vbox);
        }
        fileManagers.setPrefWrapLength(400);
        fileManagers.setLayoutY(20);
        fileManagers.setLayoutX(20);
        mainPane.setLeft(fileManagers);
    }
    private void setBlockPart() throws FileNotFoundException {
        blockManagers.setVgap(10);
        blockManagers.setHgap(10);
        for (BlockManager bm: FileSystem.getBms()){
            Image image = new Image(new FileInputStream("images/bm.png"));
            ImageView v = new ImageView(image);
            v.setFitWidth(30);
            v.setFitHeight(30);
            Label l = new Label("bm-" + bm.getBmID());
            VBox vbox = new VBox(v,l);
            vbox.setPrefSize(45,40);
            vbox.setOnMouseClicked(e->{
                try {
                    Stage stage = new Stage();
                    BlockReadGui b = new BlockReadGui(bm.getBmID());
                    b.start(stage);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            });
            blockManagers.getChildren().add(vbox);
        }
        blockManagers.setPrefWrapLength(240);
        blockManagers.setLayoutX(10);
        blockManagers.setLayoutY(30);
        mainPane.setRight(blockManagers);
    }
}
