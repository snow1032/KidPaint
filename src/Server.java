import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class Server {
	DatagramSocket UDP_socket; // socket used to receive packet
	ServerSocket srvSocket; // socket used to be server socket
	ArrayList<Socket> list = new ArrayList<Socket>();
	static int[][] sketch = new int[50][50];

	public Server() throws IOException {
		UDP_socket = new DatagramSocket(20000); // socket used to udp receive packet
		srvSocket = new ServerSocket(20562); // socket used for tcp
	}

	public static void main(String[] args) {

		try {
			Server receiver = new Server();
			System.out.println("\nUDP : Waiting for data...");
			new Thread(() -> receiver.UDPreceive()).start();

			new Thread(() -> {
				try {
					receiver.TCPreceive();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}).start();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/* testing function */
	private void sendsketch(Socket cSocket, int[][] data) throws IOException {
		DataOutputStream out = new DataOutputStream(cSocket.getOutputStream());

		synchronized (list) {

			for (int i = 0; i < 50; i++) {
				for (int p = 0; p < 50; p++) {
					out.writeInt(100);
					out.writeInt(i);
					out.writeInt(p);
					out.writeInt(data[i][p]);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

		}

	}

	public void TCPreceive() throws IOException {

		while (true) {
			System.out.printf("Listening at port %d...\n", 33333);
			Socket clientSocket = srvSocket.accept();

			synchronized (list) {
				list.add(clientSocket);
				System.out.printf("Total %d clients are connected.\n\n", list.size());
				if(list.size()>1) {
					sendsketch(clientSocket, sketch); // testing
				}
				
			}

			new Thread(() -> {
				try {
					while (true) {
						serve(clientSocket);

					}
				} catch (IOException e) {
					System.err.println("connection dropped.");
					System.out.printf("Total %d clients are connected.\n", list.size() - 1);
					synchronized (list) {
						list.remove(clientSocket);
					}
				}
				try {
					clientSocket.close();
				} catch (IOException e) {

				}

			}).start();
		}
	}

	/* ???? function */
	void request(Socket clientSocket) {
		if (list.size() <= 1)
			return;
		System.out.println("start");
		Socket head = list.get(0);
		try {
			DataInputStream in = new DataInputStream(head.getInputStream());
			DataOutputStream out = new DataOutputStream(head.getOutputStream());
			out.writeInt(110);

			System.out.println("reading");
			int[][] buffer = new int[50][50];
			for (int i = 0; i < 50; i++) {
				for (int j = 0; j < 50; j++) {
					System.out.println(i * 50 + j);
					buffer[i][j] = in.readInt();
				}
			}

			System.out.println("writing");
			DataOutputStream out2 = new DataOutputStream(clientSocket.getOutputStream());
			out2.writeInt(120);
			Thread.sleep(10);
			for (int i = 0; i < 50; i++) {
				for (int j = 0; j < 50; j++) {
					out.writeInt(buffer[i][j]);
					try {
						Thread.sleep(1);
					} catch (Exception ion) {
					}
				}
			}

		} catch (Exception ex) {
		}
	}

	private void serve(Socket clientSocket) throws IOException {

		byte[] buffer = new byte[1024];
		byte[] buffer2 = new byte[1024];
		System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
				clientSocket.getPort());

		DataInputStream in = new DataInputStream(clientSocket.getInputStream());
		// DataInputStream in2 = in;
		while (true) {
			buffer2 = buffer;
			// int len = in.read(buffer, 0, buffer.length);
			// System.out.println("len = " + len);
			//
			// int len2 = in2.read(buffer2,0,buffer2.length);
			// System.out.println("len2 = " + len2);

			// savePIXEL(buffer2,len2);
			int type = in.readInt();
			if (type == 10) {
				int len = in.readInt();
				in.read(buffer, 0, len);
				System.out.println(new String(buffer, 0, len));
				forward_message(clientSocket, type, new String(buffer, 0, len));
			} else if (type == 20) { // if type is = 20, means it is a pixel information
				int col = in.readInt();
				int row = in.readInt();
				int color = in.readInt();
				sketch[col][row] = color;

				forward_pixel_and_area(clientSocket, type, col, row, color);
			} else if (type == 30) { // if type is = 20, means it is a pixel information
				int col = in.readInt();
				int row = in.readInt();
				int color = in.readInt();
				sketch[col][row] = color;

				forward_pixel_and_area(clientSocket, type, col, row, color);
			} else if (type == 40) { // if type is = 20, means it is a pixel information
				int col = in.readInt();
				int row = in.readInt();
				int color = in.readInt();
				sketch[col][row] = color;

				forward_pixel_and_area(clientSocket, type, col, row, color);
			} else if (type == 50) { // if type is = 20, means it is a pixel information
				forward_clear_cmd(clientSocket, type);
				sketch  = new int[50][50];
			}

			// forward(clientSocket, buffer, len);

		}
	}
	//
	// private void forward(Socket clientSocket, byte[] data, int len) throws
	// IOException {
	//
	//
	// synchronized (list) {
	//
	// for (int i = 0; i < list.size(); i++) {
	//
	// try {
	// if (list.get(i) != clientSocket) {
	//
	// Socket socket = list.get(i);
	//
	// DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	//
	// out.write(data, 0, len);
	// System.out.println("check point 9");
	// }
	// } catch (IOException e) {
	// // the connection is dropped but the socket is not yet removed.
	//
	// }
	//
	// }
	// }
	// }

	/* testing for sending pixel */
	private void forward_pixel_and_area(Socket clientSocket, int type, int col, int row, int color) throws IOException {

		synchronized (list) {

			for (int i = 0; i < list.size(); i++) {

				try {
					if (list.get(i) != clientSocket) {
						Socket socket = list.get(i);
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						out.writeInt(type);
						out.writeInt(col);
						out.writeInt(row);
						out.writeInt(color);

					}
				} catch (IOException e) {
					// the connection is dropped but the socket is not yet removed.

				}

			}
		}
	}

	/* testing for sending char message */
	private void forward_message(Socket clientSocket, int type, String message) throws IOException {
		synchronized (list) {

			for (int i = 0; i < list.size(); i++) {

				try {
					if (list.get(i) != clientSocket) {
						Socket socket = list.get(i);
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						out.writeInt(type);
						out.writeInt(message.length());
						out.write(message.getBytes(), 0, message.length());

					}
				} catch (IOException e) {
					// the connection is dropped but the socket is not yet removed.

				}

			}
		}
	}

	private void forward_clear_cmd(Socket clientSocket,int type) throws IOException {
		synchronized (list) {

			for (int i = 0; i < list.size(); i++) {

				try {
					if (list.get(i) != clientSocket) {
						Socket socket = list.get(i);
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						out.writeInt(type);

					}
				} catch (IOException e) {
					// the connection is dropped but the socket is not yet removed.

				}

			}
		}
	}

	private void savePIXEL(byte[] buffer, int len) throws IOException {

		// int type = in2.readInt();
		// if(type == 20) {
		// int col = in2.readInt();
		// int row = in2.readInt();
		// int color = in2.readInt();
		// System.out.println("col : "+col + ", row : "+row + ", color : "+color);
		// }

	}

	public void UDPreceive() {

		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
		try {
			while (true) {
				UDP_socket.receive(packet);
				byte[] data = packet.getData();
				String str = new String(data, 0, packet.getLength());
				int size = packet.getLength();
				String srcAddr = packet.getAddress().toString();
				int srcPort = packet.getPort();
				System.out.println("Received data:\t" + str);
				System.out.println("data size:\t" + size);
				System.out.println("sent by:\t" + srcAddr);
				System.out.println("via port:\t" + srcPort);

				String inetAddress = (String) InetAddress.getLocalHost().getHostAddress().replace('/', '/');
				; // get the local server ip
				String destin = srcAddr.substring(1, srcAddr.length());
				System.out.println("local ip : " + inetAddress);
				System.out.println("dest ip : " + destin);

				UDPsendMsg(inetAddress, destin, srcPort);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void UDPsendMsg(String str, String destIP, int port) throws IOException {
		InetAddress destination = InetAddress.getByName(destIP);
		System.out.println("destin ip : " + destIP);

		DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), destination, port);
		UDP_socket.send(packet);
		// TCPreceive();
	}

}
