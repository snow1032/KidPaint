import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

enum PaintMode {
	Pixel, Area
};

public class UI extends JFrame {
	Socket socket;
	/* server information */
	private String ServerIP = "";
	private int Server_TCP_Port;

	DataOutputStream out;

	private String UserName;
	private JLabel UserName_Show;

	private JTextField msgField;
	private JTextArea chatArea;
	private JPanel pnlColorPicker;
	private JPanel paintPanel;
	private JToggleButton tglPen;
	private JToggleButton tglBucket;

	// add a save button
	private JToggleButton tglSave;

	// add a load button
	private JToggleButton tglLoad;

	// add a clear button
	private JToggleButton tglClear;

	private static UI instance;
	private int selectedColor = -543230; // golden

	int[][] data = new int[50][50]; // pixel color data array
	int blockSize = 16;
	PaintMode paintMode = PaintMode.Pixel;

	/**
	 * get the instance of UI. Singleton design pattern.
	 * 
	 * @return
	 */
	public static UI getInstance() {

		if (instance == null)
			instance = new UI();

		return instance;
	}

	/**
	 * private constructor. To create an instance of UI, call UI.getInstance()
	 * instead.
	 */
	private UI() {

		setTitle("KidPaint");

		JPanel basePanel = new JPanel();
		getContentPane().add(basePanel, BorderLayout.CENTER);
		basePanel.setLayout(new BorderLayout(0, 0));

		paintPanel = new JPanel() {

			// refresh the paint panel
			@Override
			public void paint(Graphics g) {
				super.paint(g);

				Graphics2D g2 = (Graphics2D) g; // Graphics2D provides the setRenderingHints method

				// enable anti-aliasing
				RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHints(rh);

				// clear the paint panel using black
				g2.setColor(Color.black);
				g2.fillRect(0, 0, this.getWidth(), this.getHeight());

				// draw and fill circles with the specific colors stored in the data array
				for (int x = 0; x < data.length; x++) {
					for (int y = 0; y < data[0].length; y++) {
						g2.setColor(new Color(data[x][y]));
						g2.fillArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
						g2.setColor(Color.darkGray);
						g2.drawArc(blockSize * x, blockSize * y, blockSize, blockSize, 0, 360);
					}
				}
			}
		};

		paintPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			// handle the mouse-up event of the paint panel
			@Override
			public void mouseReleased(MouseEvent e) {
				if (paintMode == PaintMode.Area && e.getX() >= 0 && e.getY() >= 0) {
					paintArea(e.getX() / blockSize, e.getY() / blockSize);


				}

			}
		});

		paintPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (paintMode == PaintMode.Pixel && e.getX() >= 0 && e.getY() >= 0)
					paintPixel(e.getX() / blockSize, e.getY() / blockSize);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
			}

		});

		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));

		JScrollPane scrollPaneLeft = new JScrollPane(paintPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		basePanel.add(scrollPaneLeft, BorderLayout.CENTER);

		JPanel toolPanel = new JPanel();
		basePanel.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		pnlColorPicker = new JPanel();
		pnlColorPicker.setPreferredSize(new Dimension(24, 24));
		pnlColorPicker.setBackground(new Color(selectedColor));
		pnlColorPicker.setBorder(new LineBorder(new Color(0, 0, 0)));

		// show the color picker
		pnlColorPicker.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				ColorPicker picker = ColorPicker.getInstance(UI.instance);
				Point location = pnlColorPicker.getLocationOnScreen();
				location.y += pnlColorPicker.getHeight();
				picker.setLocation(location);
				picker.setVisible(true);
			}

		});

		// add a label to show the name
		UserName_Show = new JLabel(UserName);
		toolPanel.add(UserName_Show);

		// add a new button "save"
		tglSave = new JToggleButton("Save");

		toolPanel.add(tglSave);

		// add a new button "Load"
		tglLoad = new JToggleButton("Load");

		toolPanel.add(tglLoad);

		// add a new button "clear"
		tglClear = new JToggleButton("Clear");
		toolPanel.add(tglClear);

		toolPanel.add(pnlColorPicker);

		tglPen = new JToggleButton("Pen");
		tglPen.setSelected(true);
		toolPanel.add(tglPen);

		tglBucket = new JToggleButton("Bucket");
		toolPanel.add(tglBucket);

		// change the paint mode to PIXEL mode
		tglPen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(true);
				tglBucket.setSelected(false);
				tglSave.setSelected(false);
				tglLoad.setSelected(false);
				paintMode = PaintMode.Pixel;

			}
		});

		// change the paint mode to AREA mode
		tglBucket.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				tglPen.setSelected(false);
				tglBucket.setSelected(true);
				tglSave.setSelected(false);
				tglLoad.setSelected(false);
				paintMode = PaintMode.Area;
			}
		});

		// add the save button action
		tglSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				tglSave.setSelected(true);
				tglLoad.setSelected(false);
				try {
					SaveData();
					tglSave.setSelected(false);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		// add the load button action
		tglLoad.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				tglSave.setSelected(false);
				tglLoad.setSelected(true);
				LoadData();
				tglLoad.setSelected(false);

			}
		});

		// add the clear button action
		tglClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tglPen.setSelected(false);
				tglBucket.setSelected(false);
				tglSave.setSelected(false);
				tglClear.setSelected(true);
				tglLoad.setSelected(false);
				ClearPaint();
				try {
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					out.writeInt(50);
					tglClear.setSelected(false);
				} catch (IOException e) {
					chatArea.append("Unable to send a 'Clear' Message !!!! \n");
				}

			}
		});

		/*--------------------chat room------------------*/

		JPanel msgPanel = new JPanel();

		getContentPane().add(msgPanel, BorderLayout.EAST);

		msgPanel.setLayout(new BorderLayout(0, 0));

		msgField = new JTextField(); // text field for inputting message

		msgPanel.add(msgField, BorderLayout.SOUTH);

		// handle key-input event of the message field
		msgField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10) { // if the user press ENTER
					onTextInputted(msgField.getText());
					msgField.setText("");
				}
			}

		});

		// -------------------show messag AREA-------------------------------------
		chatArea = new JTextArea(); // the read only text area for showing messages
		chatArea.setEditable(false);
		chatArea.setLineWrap(true);

		JScrollPane scrollPaneRight = new JScrollPane(chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneRight.setPreferredSize(new Dimension(300, this.getHeight()));
		msgPanel.add(scrollPaneRight, BorderLayout.CENTER);

		this.setSize(new Dimension(800, 600));
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}// end of whole ui component

	/**
	 * it will be invoked if the user selected the specific color through the color
	 * picker
	 * 
	 * @param colorValue
	 *            - the selected color
	 */
	public void selectColor(int colorValue) {
		SwingUtilities.invokeLater(() -> {
			selectedColor = colorValue;
			pnlColorPicker.setBackground(new Color(colorValue));
		});
	}

	/**
	 * it will be invoked if the user inputted text in the message field
	 * 
	 * @param text
	 *            - user inputted text
	 * @throws IOException
	 */
	private void onTextInputted(String text) {

		try {
			chatArea.append(text + "\n");
			String message_with_name = UserName + " :  " + text;
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(10);
			out.writeInt(message_with_name.replace("\r\n", "").length());
			out.write(message_with_name.getBytes(), 0, message_with_name.length());
		} catch (IOException e) {
			chatArea.append("Unable to send message to the server!\n");
		}

	}

	/**
	 * change the color of a specific pixel
	 * 
	 * @param col,
	 *            row - the position of the selected pixel
	 */
	public void paintPixel(int col, int row) {

		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = selectedColor;

		/* send pixel value */
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			out.writeInt(20);
			out.writeInt(col);
			out.writeInt(row);
			out.writeInt(selectedColor);
		} catch (IOException e) {
			// chatArea.append("Unable to send message to the server!\n");
		} finally {

		}

		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}

	public void paintPixel_Value(int col, int row, int color) {
		if (col >= data.length || row >= data[0].length)
			return;

		data[col][row] = color;
		paintPanel.repaint(col * blockSize, row * blockSize, blockSize, blockSize);
	}

	/**
	 * change the color of a specific area
	 * 
	 * @param col,
	 *            row - the position of the selected pixel
	 * @return a list of modified pixels
	 */
	public List paintArea(int col, int row) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();
		
		/* send pixel value */
//		try {
//			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//			out.writeInt(30);
//			out.writeInt(col);
//			out.writeInt(row);
//			out.writeInt(oriColor);
//		} catch (IOException e1) {
//			chatArea.append("Unable to send pixel area to the server!\n");
//		}

		if (oriColor != selectedColor) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = selectedColor;
				
				try {
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					out.writeInt(30);
					out.writeInt(x);
					out.writeInt(y);
					out.writeInt(selectedColor);
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e1) {
					chatArea.append("Unable to send pixel area to the server!\n");
				}
				
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}
			paintPanel.repaint();
		}
		return filledPixels;
	}

	public List paintArea_Value(int col, int row, int value) {
		LinkedList<Point> filledPixels = new LinkedList<Point>();

		if (col >= data.length || row >= data[0].length)
			return filledPixels;

		int oriColor = data[col][row];
		LinkedList<Point> buffer = new LinkedList<Point>();

		if (oriColor != value) {
			buffer.add(new Point(col, row));

			while (!buffer.isEmpty()) {
				Point p = buffer.removeFirst();
				int x = p.x;
				int y = p.y;

				if (data[x][y] != oriColor)
					continue;

				data[x][y] = value;
				filledPixels.add(p);

				if (x > 0 && data[x - 1][y] == oriColor)
					buffer.add(new Point(x - 1, y));
				if (x < data.length - 1 && data[x + 1][y] == oriColor)
					buffer.add(new Point(x + 1, y));
				if (y > 0 && data[x][y - 1] == oriColor)
					buffer.add(new Point(x, y - 1));
				if (y < data[0].length - 1 && data[x][y + 1] == oriColor)
					buffer.add(new Point(x, y + 1));
			}
			paintPanel.repaint();
		}
		return filledPixels;
	}

	/**
	 * set pixel data and block size
	 * 
	 * @param data
	 * @param blockSize
	 */
	public void setData(int[][] data, int blockSize) {
		this.data = data;
		this.blockSize = blockSize;
		paintPanel.setPreferredSize(new Dimension(data.length * blockSize, data[0].length * blockSize));
		paintPanel.repaint();
	}

	/**
	 * save the pixel data into txt file
	 * 
	 * 
	 */
	public void SaveData() throws IOException {

		JFileChooser fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".txt", "txt", "text");
		fileChooser.setFileFilter(filter);
		int retval = fileChooser.showSaveDialog(null);
		if (retval == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (file == null) {
				return;
			}
			if (!file.getName().toLowerCase().endsWith(".txt")) {
				file = new File(file.getParentFile(), file.getName() + ".txt");
			}

			System.out.println(file.toString());

			DataOutputStream storeToFile = new DataOutputStream(new FileOutputStream(file));
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					storeToFile.writeInt(data[i][j]);
				}
			}
			storeToFile.close();
			System.out.println("Data saved !");

		}

	}

	/**
	 * load the pixel data from txt file
	 * 
	 *
	 */

	public void LoadData() {

		String strFilePath = "";
		DataInputStream readFromFile;

		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

		int returnValue = jfc.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();
			System.out.println(selectedFile.getAbsolutePath());
			strFilePath = selectedFile.getAbsolutePath();

		}

		try {
			readFromFile = new DataInputStream(new FileInputStream(strFilePath));
			
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			for (int i = 0; i < data.length; i++) {
				for (int j = 0; j < data[0].length; j++) {
					
					int f = readFromFile.readInt();
					paintPixel_Value(i, j, f);
					out.writeInt(40);
					out.writeInt(i);
					out.writeInt(j);
					out.writeInt(f);
					Thread.sleep(1);
				}
			}

			System.out.println("File Loaded !");

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File Chooser Close ! ");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setName(String name) {
		UserName = name;

	}

	public void setServer_IP_Port(String ip, int port) {
		ServerIP = ip;
		Server_TCP_Port = port;
	}

	public void TCP_Connect() throws IOException {
		/* TCP connection */
		socket = new Socket(ServerIP, 20562);
		TCP_receiveData(socket);

	}

	public void ClearPaint() {
		for (int col = 0; col < data.length; col++) {
			for (int row = 0; row < data[0].length; row++) {
				paintPixel_Value(col, row, -16711423);
			}
		}
	}

	/* receive tcp msg */
	public void TCP_receiveData(Socket socket) {
		Thread t = new Thread(() -> {
			byte[] buffer = new byte[1024];

			while (true) {
				try {
					DataInputStream in = new DataInputStream(socket.getInputStream());

					int type = in.readInt();
					// int len = in.read(buffer, 0,buffer.length);

					System.out.println("type = " + type);

					if (type == 10) { // if type is = 10, means it is a chat room message
						int len2 = in.readInt();
						in.read(buffer, 0, len2);
						SwingUtilities.invokeLater(() -> {
							chatArea.append(new String(buffer, 0, len2) + "\n");
						});
					} else if (type == 20) { // if type is = 20, means it is a pixel information
						int col = in.readInt();
						int row = in.readInt();
						int color = in.readInt();
						SwingUtilities.invokeLater(() -> {
							paintPixel_Value(col, row, color);
						});
					} else if (type == 30) { // if type is = 30, means it is a paint area information
						int col = in.readInt();
						int row = in.readInt();
						int color = in.readInt();
						SwingUtilities.invokeLater(() -> {
//							paintArea_Value(col, row, color);
							paintPixel_Value(col, row, color);
						});
					} else if (type == 40) {
						int col = in.readInt();
						int row = in.readInt();
						int color = in.readInt();
						SwingUtilities.invokeLater(() -> {
							paintPixel_Value(col, row, color);
						});
					} else if (type == 50) {
						SwingUtilities.invokeLater(() -> {
							ClearPaint();
						});

					}else if(type == 100){
//						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//						System.out.println("saasrf");
//						for (int x = 0; x < data.length; x++) {
//							for (int y = 0; y < data[0].length; y++) {
//								System.out.println(x*y);
//									out.writeInt(data[x][y]);
//									Thread.sleep(1);
//							}
//							
//						}
//						System.out.println(in.readInt());
//						System.out.println(in.readInt());
//						System.out.println(in.readInt());
						synchronized(data) {
							int col = in.readInt();
							int row = in.readInt();
							int color = in.readInt();
							SwingUtilities.invokeLater(() -> {
								paintPixel_Value(col, row, color);
							});
							
							Thread.sleep(1);
						}

						
					}

				} catch (IOException e) {
					// e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		});
		t.start();

	}

}
