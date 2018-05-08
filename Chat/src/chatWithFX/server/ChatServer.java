/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatWithFX.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Smile
 */
public class ChatServer extends Application{
    
    public static void main(String[] args)  {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
         Parent parent = FXMLLoader.load(getClass().getResource("server.fxml"));
        
         Scene scene = new Scene(parent);
         primaryStage.setTitle("Server");
         primaryStage.setScene(scene);
         primaryStage.setResizable(false);
         // Eclipse 상에서 에러발생 / NetBeans 에서는 정상 작동 / 닫기 버튼 누르면 서버 종료
//         primaryStage.setOnCloseRequest(e -> ServerController.instance.stopServer());
         primaryStage.show();         
    }
}
