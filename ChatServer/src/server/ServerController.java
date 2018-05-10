/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Smile
 */
public class ServerController implements Initializable {

	@FXML
	private Button btnStartStop;
	@FXML
	private Button btnReset;
	@FXML
	private TextArea txtDisplay;
	@FXML
	private TextField txtIp;
	@FXML
	private TextField txtPort;

	private ExecutorService executorService;
	private ServerSocket serverSocket;
	private List<Client> connections = new Vector<>();
	private boolean serverStart;
	private String time;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	private String ip;
	private String port;
	private String id;

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		btnStartStop.setOnAction(e -> handleBtnStartStop(e));
		btnReset.setOnAction(e -> handleBtnReset(e));
	}

	private void handleBtnReset(ActionEvent e) {
		if(!serverStart) {
			txtIp.setText("192.168.0.4");
			txtPort.setText("50001");
		}		
	}

	private void handleBtnStartStop(ActionEvent e) {
		if (!serverStart) {
			if (regExp()) {
				startServer();
			} else {
				Platform.runLater(() -> display("[올바른 IP 주소와 Port 번호를 입력하세요]"));
			}
		} else {
			stopServer();
		}
	}

	private boolean regExp() {
		String ipRegExp = "\\d{3}+\\.+\\d{3}+\\.+\\d{1,3}+\\.+\\d{1,3}";
		String portRegExp = "\\d{1,5}";
		ip = txtIp.getText();
		port = txtPort.getText();

		return Pattern.matches(ipRegExp, ip) && Pattern.matches(portRegExp, port);
	}

	private void startServer() {
		executorService = Executors.newFixedThreadPool(20);
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ip, Integer.parseInt(port)));
		} catch (IOException ex) {
			Platform.runLater(() -> display("[아이피 주소를 확인하세요!]"));
			stopServer();
			return;
		}
		Runnable acceptTask = () -> {
			Platform.runLater(() -> {
				btnStartStop.setText("STOP");
				display("[Start Server]");
			});

			serverStart = true;
			txtIp.setEditable(false);
			txtPort.setEditable(false);

			while (true) {
				try {
					Socket socket = serverSocket.accept();
					Client client = new Client(socket);
					connections.add(client);

					String clientInfo = "[" + socket.getRemoteSocketAddress() + " 접속 (" + connections.size()
							+ " 명 참여)]";
					Platform.runLater(() -> display(clientInfo));
				} catch (IOException ex) {
					stopServer();
					break;
				}
			}
		};
		executorService.submit(acceptTask);
	}

	private void stopServer() {
		if (serverStart) {
			serverStart = false;
			txtIp.setEditable(true);
			txtPort.setEditable(true);

			try {
				for (Client client : new Vector<>(connections)) {
					client.socket.close();
				}
				connections.clear();
				serverSocket.close();
				executorService.shutdownNow();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			Platform.runLater(() -> {
				display("[Stop Server]");
				btnStartStop.setText("START");
			});
		}
	}

	private void display(String string) {
		time = "[" + sdf.format(new Date()) + "] - ";
		txtDisplay.appendText(time + string + "\n");
	}

	class Client {
		private Socket socket;

		public Client(Socket socket) {
			this.socket = socket;
			receive();
		}

		private void receive() {
			Runnable receiveTask = () -> {
				try {
					while (true) {
						InputStream is = socket.getInputStream();
						Reader reader = new InputStreamReader(is, "UTF-8");
						BufferedReader br = new BufferedReader(reader);
						char[] data = new char[200];
						int readBytesNo = -1;

						readBytesNo = br.read(data);

						String strData = new String(data, 0, readBytesNo);
						String[] arrData = strData.split(",@#");
						String message = "";

						if (arrData.length == 1) {
							message = arrData[0];
						} else if (arrData[1].equals("id3")) {
							id = arrData[0];
							message = "[" + id + "]: " + arrData[2];
						} else {
							message = "[" + arrData[0] + arrData[2] + "] (" + connections.size() + " 명 참여)";
						}

						for (Client client : connections) {
							client.send(message);
						}
					}
				} catch (Exception ex) {
					try {
						socket.close();
						connections.remove(Client.this);
						String serverInfo = "[" + socket.getRemoteSocketAddress() + " 와의 연결이 끊겼습니다. ("
								+ connections.size() + "명 참여)]";
						Platform.runLater(() -> display(serverInfo));
					} catch (IOException ex1) {
					}
				}
			};
			executorService.submit(receiveTask);
		}

		private void send(String strData) {
			try {
				OutputStream os = socket.getOutputStream();
				BufferedOutputStream bos = new BufferedOutputStream(os);
				byte[] bytes = strData.getBytes("UTF-8");
				bos.write(bytes);
				bos.flush();
			} catch (IOException ex) {
				connections.remove(Client.this);
				String serverInfo = "[" + connections.size() + "명 참여]";
				Platform.runLater(() -> display(serverInfo));
				try {
					socket.close();
				} catch (IOException ex1) {
				}
			}
		}
	}
}