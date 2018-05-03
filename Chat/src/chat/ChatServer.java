package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
	private ExecutorService executorService;
	private ServerSocket serverSocket;
	private List<Client> connections = new Vector<>();
	String clientInfo;
	String serverInfo;
	int playerNo = -1;

	private void startServer() {
		executorService = Executors.newFixedThreadPool(20);

		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("192.168.0.4", 50001));
		} catch (IOException e) {
			stopServer();
			return;
		}

		Runnable acceptTask = () -> {
			while (true) {
				try {
					Socket socket = serverSocket.accept();
					clientInfo = "[" + socket.getRemoteSocketAddress() + " �� ������ �����մϴ�.]";
					System.out.println(clientInfo);

					Client client = new Client(socket);
					connections.add(client);
				} catch (IOException e) {
					stopServer();
					break;
				}
			}
		};
		executorService.submit(acceptTask);
		
		Runnable playerNoTask = () -> {
			while(true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				
				}
				if(connections.size() != playerNo) {
					serverInfo = "[" + connections.size() + " ���� ä�� ������ �������Դϴ�.]";
					System.out.println(serverInfo);
					playerNo = connections.size();
				}
				if(Thread.interrupted()) {
					break;
				}
			}
		};
		executorService.submit(playerNoTask);
	}

	private void stopServer() {
		// client ��ü, socket ����
		try {
			for (Client client : connections) {
				client.socket.close();
				client.socket = null;				
			}
			connections.clear();
			serverSocket.close();
			executorService.shutdownNow();

			serverInfo = "[������ ����Ǿ����ϴ�.]";
		} catch (IOException e) {
			e.printStackTrace();
		}

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
						InputStream inputStream = socket.getInputStream();
						byte[] bytes = new byte[100];
						int readBytes = inputStream.read(bytes);						
						if (readBytes == -1) {
							throw new Exception();
						}
						
						String strData = new String(bytes, 0, readBytes);
						
						for (Client client : connections) {
							client.send(strData);
						}
					}

				} catch (Exception e2) {
					clientInfo = "[" + socket.getRemoteSocketAddress() + " ���� ����]";
					connections.remove(Client.this);
					System.out.println(clientInfo);
					try {
						socket.close();
					} catch (IOException e3) {

					}

				}
			};
			executorService.submit(receiveTask);
		}

		private void send(String strData) {
			try {
				OutputStream outputStream = socket.getOutputStream();
				String[] arrData = strData.split(",@@");
				String message = "";				
				if(arrData[1].equals("id1")) {
					message = "["+arrData[0] +" ���� ä�ÿ� �����Ͽ����ϴ�.]";
				} else if(arrData[1].equals("id2")) {
					message = "["+arrData[0] +" ���� ä�ÿ��� �����̽��ϴ�.]";
				}else {
					message= "[" +arrData[0] + "]: " + arrData[2];	
				}			

				byte[] bytes = message.getBytes();
				outputStream.write(bytes);
				outputStream.flush();
			} catch (IOException e) {
				clientInfo = "[" + socket.getRemoteSocketAddress() + " ���� ����]";
				connections.remove(Client.this);
				System.out.println(clientInfo);
				try {
					socket.close();
				} catch (IOException e1) {

				}
			}
		}
	}

	public static void main(String[] args) {
		ChatServer server = new ChatServer();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		server.startServer();
		System.out.println("[��������: //exit]");
		while(true) {
			try {
				String stop = br.readLine();
				if(stop.equals("//exit")) {
					System.out.println("���α׷� ����");
					server.stopServer();
					break;	
				}				
			} catch (IOException e) {
			}
		}
	}
}
