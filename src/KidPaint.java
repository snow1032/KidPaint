import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Container;

import javax.swing.border.LineBorder;

public class KidPaint {

	static DatagramSocket UDP_socket; // socket used to receive packet

	static String ip = "";
	static int srcPort;

	public KidPaint() throws SocketException {

		UDP_socket = new DatagramSocket();
		UDP_socket.setReuseAddress(true);
	}

	public void setName(String ip) {
		this.ip = ip;
	}

	public void UDPreceive() throws IOException {

		DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
		UDP_socket.receive(packet);
		byte[] data = packet.getData();
		String str = new String(data, 0, packet.getLength());
		setName(str);
		int size = packet.getLength();
		String srcAddr = packet.getAddress().toString();
		srcPort = packet.getPort();
		System.out.println("Received data:\t" + str);
		System.out.println("data size:\t" + size);
		System.out.println("sent by:\t" + srcAddr);
		System.out.println("via port:\t" + srcPort);
		System.out.println(srcPort + " " + str);
		//

	}

	public static void UDPsendMsg(String str, String destIP, int port) throws IOException {

		InetAddress destination = InetAddress.getByName(destIP);
		// System.out.println(destIP);

		DatagramPacket packet = new DatagramPacket(str.getBytes(), str.length(), destination, port);
		UDP_socket.send(packet);

	}

	public static void main(String[] args) throws IOException {

		JTextField name_input_field;
		JLabel message_display;
		JButton btnConfirm;
		JFrame frame;
		JPanel panel;

		frame = new JFrame("Login");
		// Setting the width and height of frame
		frame.setSize(350, 200);

		panel = new JPanel();
		panel.setLayout(null);

		message_display = new JLabel("User:");
		message_display.setBounds(10, 20, 80, 25);
		panel.add(message_display);

		name_input_field = new JTextField(20);
		name_input_field.setBounds(100, 20, 165, 25);
		name_input_field.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				System.out.println("You released key #" + e.getKeyCode());
				if (e.getKeyCode() == 10) {

					System.out.println(name_input_field.getText());

					UI ui = UI.getInstance(); // get the instance of UI
					ui.setName(name_input_field.getText());

					KidPaint udp;
					try {
						udp = new KidPaint();
						UDPsendMsg(name_input_field.getText(), "255.255.255.255", 20000);
						udp.UDPreceive();
						ui.setServer_IP_Port(ip, 20562);
						ui.TCP_Connect();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					frame.setVisible(false);

					ui.setData(new int[50][50], 20); // set the data array and block size. comment this statement to use
														// the default data array and block size.

					ui.setVisible(true); // set the ui

				}
			}
		});

		panel.add(name_input_field);

		btnConfirm = new JButton("login");
		btnConfirm.setBounds(10, 80, 80, 25);
		btnConfirm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(name_input_field.getText());

				UI ui = UI.getInstance(); // get the instance of UI
				ui.setName(name_input_field.getText());

//				KidPaint udp;
//				try {
//					udp = new KidPaint();
//					UDPsendMsg(name_input_field.getText(), "255.255.255.255", 45678);
//					udp.UDPreceive();
//					ui.setServer_IP_Port(ip, 33333);
//					ui.TCP_Connect();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				frame.setVisible(false);

				ui.setData(new int[50][50], 20); // set the data array and block size. comment this statement to use
													// the default data array and block size.

				ui.setVisible(true); // set the ui
			}
		});

		panel.add(btnConfirm);
		frame.add(panel);
		frame.setVisible(true);

	}

}
