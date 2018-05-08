/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatWithFX.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Smile
 */
public class ChatClient extends Application{
    
    public static void main(String[] args)  {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
         Parent parent = FXMLLoader.load(getClass().getResource("client.fxml"));
        
         Scene scene = new Scene(parent);
         primaryStage.setTitle("Client");
         primaryStage.setScene(scene);
         primaryStage.setResizable(false);
         primaryStage.show();
    }
}
