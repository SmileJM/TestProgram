/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatWithFX.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Smile
 */
public class ServerController implements Initializable {

	@FXML
	private Button btnStartStop;
	@FXML
	private TextArea txtDisplay;

	private ExecutorService executorService;
	private ServerSocket serverSocket;
	private List<Client> connections = new Vector<>();
	public static ServerController instance;
	private boolean serverStart;
	private String time;
	private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Initializes the controller class.
	 */
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		btnStartStop.setOnAction(e -> handleBtnStartStop(e));
	}

	private void handleBtnStartStop(ActionEvent e) {
		if (btnStartStop.getText().equals("START")) {
			startServer();
		} else {
			stopServer();
		}
	}

	private void startServer() {
		executorService = Executors.newFixedThreadPool(20);
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("192.168.0.4", 50001));
		} catch (IOException ex) {
			stopServer();
			return;
		}

		Runnable acceptTask = () -> {
			Platform.runLater(() -> {
				btnStartStop.setText("STOP");
				display("[Start Server]");
			});
			serverStart = true;

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

	public void stopServer() {

		if (serverStart) {
			serverStart = false;
			try {
				for (Client client : connections) {
					client.socket.close();
				}
				connections.clear();
				serverSocket.close();
				executorService.shutdownNow();
			} catch (Exception ex) {

			}

			Platform.runLater(() -> {
				display("[Stop Server]");
				btnStartStop.setText("START");
			});
		}

	}

	private void display(String string) {
		time = "[" + sdf.format(new Date()) + "]-";
		txtDisplay.appendText( time + string + "\n");
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
						byte[] bytes = new byte[200];
						int readBytesNo = is.read(bytes);
						if (readBytesNo == -1) {
							throw new Exception();
						}

						String strData = new String(bytes, 0, readBytesNo);
						String[] arrData = strData.split(",@#");
						String message = "";
						if (arrData[1].equals("id3")) {
							message = "[" + arrData[0] + "]: " + arrData[2];
						} else {
							message = arrData[0] + arrData[2] + " (" + connections.size() + " 명 참여)";
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
				byte[] bytes = strData.getBytes();
				os.write(bytes);
				os.flush();
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
