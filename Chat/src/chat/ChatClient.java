package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChatClient {
	private Socket socket;
	private String clientInfo;
	private static String id = "";

	public static void main(String[] args) {
		ChatClient client = new ChatClient();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("[아이디를 입력하세요.]: ");

		try {
			id = br.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		client.startClient(id);

		while (true) {
			try {
				String message = br.readLine();

				if (client.socket == null) {
					client.stopClient(id);
					break;
				} else if (message.equals("//exit")) {
					client.stopClient(id);
					break;
				} else if (message.equals("//connect") && client.socket.isClosed()) {
					client.startClient(id);
				} else if (message.equals("")) {

				} else {
					message = id + ",@@id3,@@" + message;
					client.send(message);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("client 종료");
	}

	private void send(String message) {
		try {
			OutputStream outputStream = socket.getOutputStream();
			byte[] bytes = message.getBytes();
			outputStream.write(bytes);
			outputStream.flush();
		} catch (IOException e) {
			try {
				socket.close();
			} catch (IOException e1) {

			}
			stopClient(id);
		}

	}

	private void startClient(String id) {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					socket = new Socket();
					socket.connect(new InetSocketAddress("192.168.0.4", 50001));
					clientInfo = "[채팅서버와 연결되었습니다. 채팅종료: //exit]";
					send(id + ",@@id1,@@");
					System.out.println(clientInfo);
					receive();

				} catch (IOException e) {
					clientInfo = "[채팅서버와 연결에 실패하였습니다. 연결요청: //connect]";
					System.out.println(clientInfo);
					stopClient(id);
				}
			}
		};
		thread.start();
	}

	private void receive() {
		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						InputStream inputStream = socket.getInputStream();
						byte[] bytes = new byte[100];
						int readBytes = inputStream.read(bytes);
						if (readBytes == -1) {
							throw new Exception();
						}
						String message = new String(bytes, 0, readBytes);
						System.out.println(message);
					}
				} catch (Exception e) {
					System.out.println("[채팅서버와의 연결이 끊겼습니다. 연결요청: //connect]");
					stopClient(id);
				}
			}
		};
		thread.start();
	}

	private void stopClient(String id) {
		if (socket != null && !socket.isClosed()) {
			send(id + ",@@id2,@@");
			try {
				socket.close();
			} catch (IOException e) {

			}
		}

	}
}
