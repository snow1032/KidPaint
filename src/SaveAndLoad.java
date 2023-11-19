import java.io.*;

public class SaveAndLoad {
    public static int row;
    public static int col;
    static void save(final String path, final int[][] data) throws IOException {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(path));

        out.writeInt(data.length);
        out.writeInt(data[0].length);
        for (int[] row : data) {
            for (int value : row) {
                out.writeInt(value);
            }
        }
        out.close();
    }

    static int[][] load(final String path) throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(path));
        row = in.readInt();
        col = in.readInt();

        int[][] drawing = new int[row][col];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                drawing[i][j] = in.readInt();
            }
        }

        in.close();
        return drawing;
    }
}