/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

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
	private Button btnReset;
	@FXML
	private TextField txtInput;
	@FXML
	private TextField txtId;
	@FXML
	private TextField txtIp;
	@FXML
	private TextField txtPort;
	@FXML
	private Button btnSend;
	@FXML
	private TextArea txtDisplay;

	private Socket socket;
	private boolean clientStart;
	private String displayInfo;
	private String id;
	private String ip;
	private String port;
	private String time;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		btnConn.setOnAction(e -> handleBtnConn(e));
		btnSend.setOnAction(e -> handleBtnSend(e));
		btnReset.setOnAction(e -> handleBtnReset(e));
		txtInput.setOnKeyPressed(e -> handleTxtInput(e));
		txtId.setOnKeyPressed(e -> handleTxtId(e));
		txtDisplay.setWrapText(true);
	}

	private void handleBtnReset(ActionEvent e) {
		txtIp.setText("192.168.0.4");
		txtPort.setText("50001");
		txtId.setText("");
		stopClient();
	}

	private void handleTxtId(KeyEvent e) {
		if (e.getCode().getName().equals("Enter")) {
			handleBtnConn(new ActionEvent());
		}
	}

	private void handleTxtInput(KeyEvent e) {
		if (e.getCode().getName().equals("Enter")) {
			handleBtnSend(new ActionEvent());
		}
	}

	private void handleBtnConn(ActionEvent e) {
		if (!clientStart) {
			if (regExp()) {
				String idRegExp = "\\w{2,12}";
				id = txtId.getText();
				if (Pattern.matches(idRegExp, id)) {
					startClient();
				} else {
					Platform.runLater(() -> display("[아이디는 영문, 숫자 최대 12글자 입니다.]"));
				}
			} else {
				Platform.runLater(() -> display("[올바른 IP 주소와 Port 번호를 입력하세요]"));
			}
		} else {
			stopClient();
		}
	}

	private void handleBtnSend(ActionEvent e) {
		if (txtInput.getText().equals("")) {
			txtInput.setPromptText("Enter Contents");
			return;
		} else if (clientStart) {
			send(id + ",@#id3,@#" + txtInput.getText());
		}
		txtInput.clear();
		txtInput.requestFocus();
	}

	private boolean regExp() {
		String ipRegExp = "\\d{3}+\\.+\\d{3}+\\.+\\d{1,3}+\\.+\\d{1,3}";
		String portRegExp = "\\d{1,5}";
		ip = txtIp.getText();
		port = txtPort.getText();

		return Pattern.matches(ipRegExp, ip) && Pattern.matches(portRegExp, port);
	}

	private void stopClient() {
		if (clientStart) {
			clientStart = false;
			if (!socket.isClosed() && socket != null) {
				send(id + ",@#id2,@#" + " 님이 나가셨습니다.");
				try {
					socket.close();
					Platform.runLater(() -> {
						btnConn.setText("Connect");
						display("[채팅방에서 나왔습니다.]");
					});
					txtId.setEditable(true);
				} catch (IOException ex) {
				}
			}
		}
	}

	private void startClient() {
		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, Integer.parseInt(port)));

			Platform.runLater(() -> btnConn.setText("Disconnect"));
			txtId.setEditable(false);
			txtIp.setEditable(false);
			txtPort.setEditable(false);
			send(id + ",@#id1,@#" + " 님이 입장하였습니다.");
			txtInput.requestFocus();
			clientStart = true;

			receive();
		} catch (IOException ex) {
			displayInfo = "[잠시후에 다시 시도해보세요~]";
			Platform.runLater(() -> display(displayInfo));
		}
	}

	private void display(String display) {
		time = "[" + sdf.format(new Date()) + "] - ";
		txtDisplay.appendText(time + display + "\n");
	}

	private void send(String txtInput) {
		try {
			OutputStream os = socket.getOutputStream();
			BufferedOutputStream bos = new BufferedOutputStream(os);
			byte[] bytes = (txtInput).getBytes("UTF-8");
			bos.write(bytes);
			bos.flush();
		} catch (IOException ex) {
			stopClient();
		}
	}

	private void receive() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					char[] data = new char[200];
					while (true) {
						InputStream is = socket.getInputStream();
						Reader reader = new InputStreamReader(is, "UTF-8");
						BufferedReader br = new BufferedReader(reader);
						int readBytesNo = -1;

						readBytesNo = br.read(data);
						if (readBytesNo == -1) {
							// 서버 측에서 접속이 종료되었을 때
							throw new Exception();
						}

						String message = new String(data, 0, readBytesNo);
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