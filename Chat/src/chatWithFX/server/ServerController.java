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
            btnStartStop.setText("STOP");
        } else {
            stopServer();
            btnStartStop.setText("START");
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

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    String clientInfo = "[" + socket.getRemoteSocketAddress() + " 접속]";
                    Platform.runLater(() -> display(clientInfo));
                    Client client = new Client(socket);
                    connections.add(client);
                } catch (IOException ex) {
                    stopServer();
                    break;
                }
            }
        };
        executorService.submit(acceptTask);
    }

    private void stopServer() {
        try {
            for (Client client : connections) {
                client.socket.close();
            }
            connections.clear();
            serverSocket.close();
            executorService.shutdownNow();
            Platform.runLater(() -> {
                display("[Stop Server]");
                btnStartStop.setText("START");
            });
        } catch (IOException ex) {

        }
    }

    private void display(String string) {
        txtDisplay.appendText(string + "\n");
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
                        byte[] bytes = new byte[100];
                        int readBytesNo = is.read(bytes);
                        if (readBytesNo == -1) {
                            throw new Exception();
                        }
                        String strData = "[" + socket.getRemoteSocketAddress() + "]: ";
                        strData += new String(bytes, 0, readBytesNo);

                        for (Client client : connections) {
                            client.send(strData);
                        }
                    }
                } catch (Exception ex) {
                    try {
                        String clientInfo = "[Exception" + socket.getRemoteSocketAddress() + "]";
                        Platform.runLater(() -> display(clientInfo));
                        socket.close();
                        connections.remove(Client.this);
                        String serverInfo = "[" + connections.size() + "명 접속중]";
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
                String serverInfo = "[" + connections.size() + "명 접속중]";
                Platform.runLater(() -> display(serverInfo));
                try {
                    socket.close();
                } catch (IOException ex1) {

                }
            }
        }
    }
}
