package GUI;

import File.My_File;
import Sys.FileSystem;
import Exception.ErrorCode;
import javafx.application.Application;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import test.Tools;

import java.awt.*;

public class FineOperationGui extends Application {
    private My_File f;
    private BorderPane mainPane = new BorderPane();
    private Pane content = new Pane();
    private GridPane operatePane = new GridPane();
    private TextField offset = new TextField();
    private ComboBox<Integer> whereSelector;
    private TextArea writeArea = new TextArea();
    private Button write = new Button("WRITE");
    private Button move = new Button("MOVE");
    private TextField size = new TextField();
    private Button setSize = new Button("SETSIZE");

    private Text text = new Text();
    public FineOperationGui(int fid){
        this.f = FileSystem.getFile(fid);
        mainPane.setId("mainPane");
        offset.setId("offset");
        writeArea.setWrapText(true);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(mainPane,600,300);
        setMainPane();
        mainPane.getStylesheets().add("./Style/operate.css");
        mainPane.getStylesheets().add("./Style/Button.css");
        primaryStage.setTitle("Fine operation of file-" + f.getId());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void setMainPane(){
        setContentArea();
        content.setPrefSize(250,200);
        setOperatePane();
        mainPane.setLeft(content);
        mainPane.setRight(operatePane);
    }
    private void setContentArea() throws ErrorCode{
        content.getChildren().remove(text);
        text.setText(Tools.getData(f.getId()));

        text.setWrappingWidth(240);
        text.setLayoutX(10);
        text.setLayoutY(20);
        content.getChildren().add(text);
    }
    private void renewSelector(){
        whereSelector.setItems(FXCollections.observableArrayList(f.getMOVE_HEAD(),f.getMOVE_CURR(),f.getMOVE_TAIL()));
    }
    private void setOperatePane(){
        // 大小设置
        operatePane.setVgap(5);
        operatePane.setHgap(10);
        offset.setPrefSize(100,10);
        writeArea.setPrefSize(100,50);

        operatePane.add(new Text("OFFSET"),0,0);
        operatePane.add(offset,0,1);
        operatePane.add(new Text("WHERE"),1,0);
        // 设置默认值
        whereSelector = new ComboBox<>(FXCollections.observableArrayList(f.getMOVE_HEAD(),f.getMOVE_CURR(),f.getMOVE_TAIL()));
        whereSelector.getSelectionModel().select(0);

        operatePane.add(whereSelector,1,1);
        operatePane.add(move,2,1);
        operatePane.add(writeArea,0,2,1,2);
        operatePane.add(write,2,2);

        operatePane.add(size,0,3);
        operatePane.add(setSize,2,3);
        size.setTranslateY(29);
        setSize.setTranslateY(29);

        write.setOnAction(e->{
            try{
                String source = writeArea.getText();
                f.write(source.getBytes());
            }catch (ErrorCode error){
                // 弹框提示
                Stage stage = new Stage();
                IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(error.getErrorCode()));
                errorHandler.start(stage);
            }
            // 更新左边
            setContentArea();
            renewSelector();
        });
        move.setOnAction(e->{
            try{
                int off = Integer.parseInt(offset.getText());
                int whe =  whereSelector.getValue();
                f.move(off, whe);
            }catch (ErrorCode error){
                // 弹框提示
                Stage stage = new Stage();
                IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(error.getErrorCode()));
                errorHandler.start(stage);
            }
            renewSelector();
        });

        setSize.setOnAction(e->{
            try{
                int newSize =  Integer.parseInt(size.getText());
                f.setNewSize(newSize);
            }catch (ErrorCode error){
                // 弹框提示
                Stage stage = new Stage();
                IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(error.getErrorCode()));
                errorHandler.start(stage);
            }
            // 更新左边
            setContentArea();
            renewSelector();
        });
    }
}
