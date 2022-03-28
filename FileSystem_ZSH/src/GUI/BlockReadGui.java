package GUI;

import Block.Block;
import Exception.ErrorCode;
import Sys.FileSystem;
import test.Tools;
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
import java.util.List;

public class BlockReadGui extends Application {
    private TabPane tabPane;
    private FlowPane table;
    private Tab mainTab = new Tab();
    private int bmId;

    public BlockReadGui(int bmId) throws FileNotFoundException {
        this.bmId = bmId;
        tabPane = new TabPane();
        mainTab.setText("Bm-" + bmId);
        table = new FlowPane();
        for(int b: FileSystem.getBlockManager(bmId).getBlockIds()){
            Image image = new Image(new FileInputStream("images/block.png"));
            ImageView v = new ImageView(image);
            v.setFitWidth(30);
            v.setFitHeight(30);
            Label l = new Label("Block-" + b);
            VBox vbox = new VBox(v,l);
            vbox.setId(b + "");
            vbox.setPrefSize(80,40);
            table.getChildren().add(vbox);
        }
        mainTab.setContent(table);
        tabPane.setId("mainTab");
        table.setId("table");
        tabPane.getTabs().add(mainTab);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(tabPane, 650, 300);
        tabPane.getStylesheets().add("Style/BlockRead.css");
        for (Node v: table.getChildren()){
            v.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                Node node = event.getPickResult().getIntersectedNode();
                //给node对象添加下来菜单；
                BlockMenu menu = BlockMenu.getInstance();
                for(MenuItem item: menu.getItems()){
                    item.setOnAction(e->{
                        Tab newTab = new Tab();
                        if (item.getId().equals("meta")){
                            newTab.setText("Block-" + v.getId() +" meta");
                            GridPane content = layoutMeta(Integer.parseInt(v.getId()));
                            newTab.setContent(content);
                            content.getStylesheets().add("./Style/meta.css");
                            tabPane.getTabs().add(newTab);
                        }else{
                            Text text = layoutData(Integer.parseInt(v.getId()));
                            if (text != null){
                                newTab.setText("Block-" + v.getId() +" data");
                                newTab.setContent(text);
                                tabPane.getTabs().add(newTab);
                            }
                        }
                    });
                }
                menu.show(node, javafx.geometry.Side.BOTTOM, 0, 0);
            });
        }
        primaryStage.setTitle("BM-" + bmId);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private GridPane layoutMeta(int bid){
        GridPane pane = new GridPane();
        Block b = null;
        try{
            b = FileSystem.getBlockManager(bmId).getBlock(bid);
        }catch (ErrorCode e){
            // 弹框提示
            Stage stage = new Stage();
            IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(e.getErrorCode()));
            errorHandler.start(stage);
        }

        pane.add(new Text("id: "),0,0);
        pane.add(new Text(b.getId() + ""),1,0);
        pane.add(new Text("size: "),0, 1);
        pane.add(new Text(b.getSize() + ""),1,1);
        pane.add(new Text("BM: "),0,2);
        pane.add(new Text(bmId + ""),1,2);
        pane.add(new Text("metaPath: "),0,3);
        pane.add(new Text(b.getMetaPath()),1,3);
        pane.add(new Text("dataPath: "),0,4);
        pane.add(new Text(b.getDataPath()),1,4);
        pane.add(new Text("checksum: "),0,5);
        pane.add(new Text(b.getCheckSum()),1,5);
        pane.add(new Text("duplicate blocks: "),0,6);

        List<int[]> duplicates = b.getDuplicatedBlocks();
        for (int i = 0;i < duplicates.size();i++){
            pane.add(new Text("BM" + duplicates.get(i)[0] + "-b" + duplicates.get(i)[1]),1,6 + i);
        }
        return pane;
    }
    private Text layoutData(int bid){
        try{
            Block b = FileSystem.getBlockManager(bmId).getBlock(bid);
            return new Text(Tools.bytesToHex(b.read()));
        }catch (ErrorCode e){
            // 弹框提示
            Stage stage = new Stage();
            IncidentHandler errorHandler = new IncidentHandler(ErrorCode.getErrorText(e.getErrorCode()));
            errorHandler.start(stage);
        }
        return null;
    }


}
class BlockMenu extends ContextMenu {
    /** * 单例 */
    private static BlockMenu INSTANCE = null;

    /** * 私有构造函数 */
    private BlockMenu() {
        MenuItem viewMeta = new MenuItem("view meta");
        viewMeta.setId("meta");
        MenuItem viewData = new MenuItem("view data");
        viewData.setId("data");
        getItems().add(viewMeta);
        getItems().add(viewData);
    }

    /** * 获取实例 * @return GlobalMenu */
    static BlockMenu getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BlockMenu();
        }
        return INSTANCE;
    }

}