/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatWithFX.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

/**
 * FXML Controller class
 *
 * @author Smile
 */
public class ClientController implements Initializable {

	@FXML
	private Button btnConn;
	@FXML
	private TextField txtInput;
	@FXML
	private TextField txtId;
	@FXML
	private Button btnSend;
	@FXML
	private TextArea txtDisplay;

	private Socket socket;
	private String displayInfo;
	private String id;
	private String time;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		btnConn.setOnAction(e -> handleBtnConn(e));
		btnSend.setOnAction(e -> handleBtnSend(e));
		txtInput.setOnKeyPressed(e -> handleTxtInput(e));
	}

	private void handleTxtInput(KeyEvent e) {
		if (e.getCode().getName().equals("Enter")) {
			handleBtnSend(new ActionEvent());
		}
	}

	private void handleBtnConn(ActionEvent e) {
		if (txtId.getText().equals("")) {
			return;
		}
		id = txtId.getText();

		if (btnConn.getText().equals("Connect")) {
			startClient();
		} else {
			stopClient();
		}
	}

	private void handleBtnSend(ActionEvent e) {
		if (txtInput.getText().equals("")) {
			txtInput.setPromptText("Enter Contents");
			return;
		}
		send(id + ",@#id3,@#" + txtInput.getText());
		txtInput.clear();
		txtInput.requestFocus();
	}

	private void stopClient() {
		if (!socket.isClosed() && socket != null) {
			send(id + ",@#id2,@#" + " 님이 나가셨습니다.");
			try {
				socket.close();
				Platform.runLater(() -> {
					btnConn.setText("Connect");
					display("채팅방에서 나왔습니다.");
				});
				txtId.setEditable(true);
			} catch (IOException ex) {
			}
		}
	}

	private void startClient() {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress("192.168.0.4", 50001));
			Platform.runLater(() -> btnConn.setText("Disconnect"));
			txtId.setEditable(false);
			send(id + ",@#id1,@#" + " 님이 입장하였습니다.");
			receive();
		} catch (IOException ex) {
			displayInfo = "[잠시후에 다시 시도해보세요~]";
			Platform.runLater(() -> display(displayInfo));
		}
	}

	private void display(String display) {
		time = "[" + sdf.format(new Date()) + "]-";
		txtDisplay.appendText(time + display + "\n");
	}

	private void send(String txtInput) {
		try {
			OutputStream os = socket.getOutputStream();
			byte[] bytes = (txtInput).getBytes();
			os.write(bytes);
			os.flush();
		} catch (IOException ex) {
			stopClient();
		}
	}

	private void receive() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						InputStream is = socket.getInputStream();
						byte[] bytes = new byte[200];
						int readBytesNo = is.read(bytes);
						if (readBytesNo == -1) {
							throw new Exception();
						}
						String message = new String(bytes, 0, readBytesNo);
						Platform.runLater(() -> display(message));
					}
				} catch (Exception ex) {
					stopClient();
				}
			}
		};
		thread.start();
	}
}
