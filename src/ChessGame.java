import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintWriter;

public class ChessGame extends JPanel {
    static int Game_Width = 930;
    static int Game_Height = 830;
    public static ServerSocket socket;
    public static Socket clientSocket;
    public static BufferedReader fromClient;
    public static PrintWriter toClient;
    public static String notification;
    public static void handleServerNotifications() {
        new Thread(() -> {
            try {
                while (true) {
                    // Listen for notifications from the server
                    notification = ChessGame.fromClient.readLine();
                    if (notification != null) {
                        // check if the notifcation is a mate or draw, if so end the game
                        if(notification.equals("Mate")) {
                            JOptionPane.showMessageDialog(null, "Checkmate! You win!", "Notice", JOptionPane.INFORMATION_MESSAGE);
                            System.exit(0);
                        }
                        else if(notification.equals("Draw")) {
                            JOptionPane.showMessageDialog(null, "Draw! No winner.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                            System.exit(0);
                        }
                        else {
                            // Parse the notification and update the game state
                            String[] parts = notification.split(" ");
                            if (notification.length() > 10) {
                                System.out.println("Received notification from client: " + notification);
                                int oldX = parts[0].charAt(0) - 97;
                                int oldY = 7 - (parts[1].charAt(0) - 49);
                                int x = parts[2].charAt(0) - 97;
                                int y = 7 - (parts[3].charAt(0) - 49);
                                boolean enPassant = Boolean.parseBoolean(parts[5]);
                                String piece = parts[4];
                                boolean enPassantHappened = Boolean.parseBoolean(parts[6]);
                                ChessBoard.moveResponse(oldX, oldY, x, y, piece, enPassant, enPassantHappened);
                                Resources.playSound("Resources/Sounds/move-self.wav");

                            }
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Opponent disconnected.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                e.printStackTrace();
            }
        }).start();
    }
    public static void main(String[] args) {
        try{
            socket = new ServerSocket(1234);
            // wait for a client to connect.
            System.out.println("Waiting for client to connect...");
            clientSocket = socket.accept();
            fromClient = new BufferedReader(new InputStreamReader(ChessGame.clientSocket.getInputStream()));
        } catch (
                IOException ioException) {
                ioException.printStackTrace();
        }
        System.out.println("Client connected!");
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SERVER");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Create the launch screen
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