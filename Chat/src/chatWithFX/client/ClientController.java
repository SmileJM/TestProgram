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
import java.util.ResourceBundle;
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
public class ClientController implements Initializable {

    @FXML
    private Button btnConn;
    @FXML
    private TextField txtInput;
    @FXML
    private Button btnSend;
    @FXML
    private TextArea txtDisplay;

    private Socket socket;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        btnConn.setOnAction(e -> handleBtnConn(e));
        btnSend.setOnAction(e -> handleBtnSend(e));
    }

    private void handleBtnConn(ActionEvent e) {
        if (btnConn.getText().equals("Connect")) {
            btnConn.setText("Disconnect");
            startClient();
        } else {
            btnConn.setText("Connect");
            stopClient();
        }
    }

    private void handleBtnSend(ActionEvent e) {
        send(txtInput);
        txtInput.setText("");
    }

    private void stopClient() {
        if(!socket.isClosed() && socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                
            }
        }
    }

    private void startClient() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress("192.168.0.4", 50001));
            receive();
        } catch (IOException ex) {

        }
    }

    private void display(String display) {
        txtDisplay.appendText(display + "\n");
    }

    private void send(TextField txtInput) {
        try {
            OutputStream os = socket.getOutputStream();
            byte[] bytes = txtInput.getText().getBytes();
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
                        byte[] bytes = new byte[100];
                        int readBytesNo = is.read(bytes);
                        if(readBytesNo == -1) {
                            throw new Exception();
                        }
                        String message = new String(bytes, 0, readBytesNo);
                        Platform.runLater(()->display(message));                                
                    }
                } catch (Exception ex) {
                    stopClient();
                }
            }
        };
        thread.start();
    }
}
