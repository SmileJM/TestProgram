/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Smile
 */
public class ChatServer extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		Parent parent = FXMLLoader.load(getClass().getResource("server.fxml"));

		Scene scene = new Scene(parent);
		primaryStage.setTitle("Server");
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		// X 버튼 누를때 프로그램 종료
		primaryStage.setOnCloseRequest(e -> System.exit(0));
		primaryStage.show();
	}
}
