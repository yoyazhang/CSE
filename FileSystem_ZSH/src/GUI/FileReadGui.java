package GUI;

import Exception.ErrorCode;
import File.My_File;
import Sys.FileSystem;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class FileReadGui extends Application {
    private int fmId;
    private TabPane tabPane;
    private VBox wrapper;
    private FlowPane table;
    private Button newFile = new Button("New File");

    public FileReadGui(int fmId){
        this.fmId = fmId;
        tabPane = new TabPane();
        Tab mainTab = new Tab();
        mainTab.setText("Fm-" + fmId);
        table = new FlowPane();
        wrapper = new VBox();
        wrapper.getChildren().add(table);
        wrapper.getChildren().add(newFile);
        mainTab.setContent(wrapper);
        tabPane.setId("mainTab");
        wrapper.setId("mainBox");
        table.setId("table");
        tabPane.getTabs().add(mainTab);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(tabPane, 650, 300);
        tabPane.getStylesheets().add("Style/Button.css");
        tabPane.getStylesheets().add("Style/FileRead.css");
        newFile.setOnAction(e->{
            try{
                My_File newFile = FileSystem.getFileManager(fmId).newFile(FileSystem.getNewIndex());
                // 更新展示的界面
                // 删除原本的内容并添加新的
                VBox newBox = generateNodeOfFile(newFile.getId());
                table.getChildren().add(newBox);
                setActionForOneFile(newBox);
            }catch (ErrorCode error){
                Stage stage = new Stage();
                IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(error.getErrorCode()));
                errorHandler.start(stage);
            }
        });

        listFilesInPane();
        setActionForFiles();

        primaryStage.setTitle("FM-" + fmId);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private void listFilesInPane(){
        for(int f: FileSystem.getFileManager(fmId).getFileIds()){
            table.getChildren().add(generateNodeOfFile(f));
        }
    }
    private VBox generateNodeOfFile(int f){
        Image image = null;
        try{
            image = new Image(new FileInputStream("images/file.jpeg"));
        }catch (FileNotFoundException e){
            throw new ErrorCode(ErrorCode.IO_EXCEPTION);
        }
        ImageView v = new ImageView(image);
        v.setFitWidth(30);
        v.setFitHeight(30);
        Label l = new Label("File-" + f);
        VBox vbox = new VBox(v,l);
        vbox.setPrefSize(60,40);
        vbox.setId(f + "");
        return vbox;
    }
    private void setActionForOneFile(Node v){
        v.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node node = event.getPickResult().getIntersectedNode();
            //给node对象添加下来菜单；
            FileMenu menu = FileMenu.getInstance();
            for(MenuItem item: menu.getItems()){
                item.setOnAction(e->{
                    switch (item.getId()) {
                        case "meta": {
                            GridPane content = layoutMeta(Integer.parseInt(v.getId()));
                            if (content != null) {
                                Tab newTab = new Tab("File-" + v.getId() + " meta");
                                newTab.setContent(content);
                                content.getStylesheets().add("./Style/meta.css");
                                tabPane.getTabs().add(newTab);
                            }
                            break;
                        }
                        case "smartCat": {
                            Text content = getWholeFileData(Integer.parseInt(v.getId()));
                            if (content != null) {
                                Tab newTab = new Tab("File-" + v.getId() + " content");
                                newTab.setContent(content);
                                tabPane.getTabs().add(newTab);
                            }
                            break;
                        }
                        case "smartCopy": {
                            try {
                                My_File src = FileSystem.getFile(Integer.parseInt(v.getId()));
                                My_File newFile = src.getFileManager().newFile(FileSystem.getNewIndex());
                                newFile.copy(src);
                                newFile.writeIntoFiles();
                                //更新
                                VBox newBox = generateNodeOfFile(newFile.getId());
                                table.getChildren().add(newBox);
                                setActionForOneFile(newBox);
                                // 提示用户完成
                                Stage stage = new Stage();
                                IncidentHandler errorHandler = new IncidentHandler("File copy finished!");
                                errorHandler.start(stage);
                            } catch (ErrorCode error) {
                                Stage stage = new Stage();
                                IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(error.getErrorCode()));
                                errorHandler.start(stage);
                            }
                            break;
                        }
                        default: {
                            try{
                                FineOperationGui operator = new FineOperationGui(Integer.parseInt(v.getId()));
                                Stage stage = new Stage();
                                operator.start(stage);
                            }catch (ErrorCode error){
                                Stage stage = new Stage();
                                IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(error.getErrorCode()));
                                errorHandler.start(stage);
                                break;
                            }
                        }
                    }
                });
            }
            menu.show(node, javafx.geometry.Side.BOTTOM, 0, 0);
        });
    }
    private void setActionForFiles(){
        for (Node v: table.getChildren()){
            setActionForOneFile(v);
        }
    }
    private GridPane layoutMeta(int fId){
        GridPane pane = new GridPane();
        My_File f = null;
        try{
            f = FileSystem.getFile(fId);
        }catch (ErrorCode e){
            // 弹框提示
            Stage stage = new Stage();
            IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(e.getErrorCode()));
            errorHandler.start(stage);
            return null;
        }
        pane.add(new Text("id: "),0,0);
        pane.add(new Text(fId + ""),1,0);
        pane.add(new Text("FM: "),0, 1);
        pane.add(new Text(fmId + ""),1,1);
        pane.add(new Text("metaPath: "),0,2);
        pane.add(new Text(f.getMetaPath()),1,2);
        pane.add(new Text("blocks: "),0,3);
        int i = 3;
        for (ArrayList<int[]> level: f.getLogicBlocks()){
            StringBuilder names = new StringBuilder();
            for (int[] ints: level){
                names.append("BM").append(ints[0]).append("-b").append(ints[1]).append(" ");
            }
            pane.add(new Text(names.toString()),1,i++);
        }
        return pane;
    }

    private Text getWholeFileData(int fId){
        My_File f;
        Text text;
        try{
            f = FileSystem.getFile(fId);
            f.move(0,0);
            String content = new String(f.read(f.getSize()));
//            System.out.println(content);
            text = new Text(content);
        }catch (ErrorCode e){
            // 弹框提示
            Stage stage = new Stage();
            IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(e.getErrorCode()));
            errorHandler.start(stage);
            return null;
        }
        return text;
    }

}
class FileMenu extends ContextMenu {
    /** * 单例 */
    private static FileMenu INSTANCE = null;

    /** * 私有构造函数 */
    private FileMenu() {
        MenuItem viewMeta = new MenuItem("viewMeta");
        viewMeta.setId("meta");
        MenuItem smartCat = new MenuItem("smartCat");
        smartCat.setId("smartCat");
        MenuItem smartCopy = new MenuItem("smartCopy");
        smartCopy.setId("smartCopy");
        MenuItem fineOperate = new MenuItem("fineOperate");
        fineOperate.setId("fineOperate");
        getItems().add(viewMeta);
        getItems().add(smartCat);
        getItems().add(smartCopy);
        getItems().add(fineOperate);
    }

    /** * 获取实例 * @return GlobalMenu */
    static FileMenu getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileMenu();
        }
        return INSTANCE;
    }
}
