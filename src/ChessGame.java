import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;

public class ChessGame extends JPanel {
    static int Game_Width = 800;
    static int Game_Height = 800;
    public static ServerSocket socket;
    public static Socket clientSocket;
    public static BufferedReader fromClient;
    public static PrintWriter toClient;
    public static String notification = null;
    public static void handleServerNotifications() {
        new Thread(() -> {
            try {
                while (true) {
                    // Listen for notifications from the server
                    String notification = ChessGame.fromClient.readLine();
                    if (notification != null) {
                        // Logic to handle the notification
                        // For example, you can update the game state, display messages to the user, etc.
                        System.out.println("Received notification from server: " + notification.substring(8));
                        if (notification.length() > 10) {
                            int oldX = notification.charAt(0) - 97;
                            int oldY = 7 - (notification.charAt(2) - 49);
                            int x = notification.charAt(4) - 97;
                            int y = 7 - (notification.charAt(6) - 49);
                            String name = notification.substring(8);
                            GameCanvas.gameManager.removeGameObject(ChessBoard.chessBoard[oldY][oldX].getPiece());
                            ChessBoard.chessBoard[oldY][oldX].setPiece(null);
                            PieceObject piece = new PieceObject(name, Color.BLACK, ChessBoard.chessBoard[y][x].getPos()[0], ChessBoard.chessBoard[y][x].getPos()[1]);
                            ChessBoard.chessBoard[y][x].setPiece(piece);
                            GameCanvas.gameManager.addGameObject(piece);
                            ChessBoard.turn = "WHITE";
                            ChessBoard.moved = false;
                            SwingUtilities.invokeLater(() -> {
                                ChessBoard.statusLabel.setText(ChessBoard.turn + " | White: 10.00 | Black: 10.00");
                            });
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void main(String[] args) {
        try{
            socket = new ServerSocket(1234);
            // wait for a client to connect.
            clientSocket = socket.accept();
            fromClient = new BufferedReader(new InputStreamReader(ChessGame.clientSocket.getInputStream()));
        } catch (
                IOException ioException) {
            ioException.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Chess Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            LaunchScreen homeScreen = new LaunchScreen(frame);
            frame.setContentPane(homeScreen);
            handleServerNotifications();
            frame.pack();
            frame.setSize(Game_Width, Game_Height);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}