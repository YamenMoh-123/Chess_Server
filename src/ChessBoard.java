import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ChessBoard extends JPanel {

    private static final int ROWS = 8;
    private static final int COLS = 8;
    private static final int BOARD_SIZE = 800;
    private static final int FONT_SIZE = 16;
    private static final int PADDING_RIGHT = 10;
    private static String[] colNames = {"a", "b", "c", "d", "e", "f", "g", "h"};
    public static boolean moved = false;
    public static String turn = "WHITE";
    private String[][] boardInit = {
            {"Empty", "Empty", "Empty", "Empty", "Empty", "Empty", "Empty", "Empty"},
            {"Empty", "Empty", "Empty", "Empty", "Empty", "Empty", "Empty", "Empty"},
            {"Empty", "Rook", "Empty", "Empty", "Empty", "Empty", "Empty", "Empty"},
            {"Empty", "Empty", "Empty", "Queen", "Empty", "Empty", "Empty", "Empty"},
            {"Empty", "Empty", "Empty", "Empty", "Empty", "Empty", "Empty", "Empty"},
            {"Empty", "Empty", "Empty", "Empty", "Empty", "Rook", "Knight", "Empty"},
            {"Empty", "Pawn", "Empty", "Empty", "Empty", "Bishop", "Empty", "Empty"},
            {"Empty", "Empty", "Empty", "Empty", "Empty", "Bishop", "Empty", "Empty"}
    };

    public static ChessSquare[][] chessBoard = new ChessSquare[ROWS][COLS];

    private ChessSquare previousClickedTile = null;
    public static ArrayList<String> previousMoves = null;
    public static PieceObject whiteKing;
    public static PieceObject blackKing;
    public static boolean movesShown = false;
    public static int whiteMin = Integer.parseInt(LaunchScreen.gameTime); // Initial minutes
    public static int whiteSec = 0; // Initial seconds
    public static int blackMin = Integer.parseInt(LaunchScreen.gameTime); // Initial minutes
    public static int blackSec = 0; // Initial seconds

    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;
    private JLabel turnLabel;

    private Timer timer;

    private ActionListener pieceListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("Clicked");
            System.out.println(moved);
            System.out.println();
            if (!moved) {
                if (((ChessSquare) e.getSource()).getPiece() != null && ((ChessSquare) e.getSource()).getPiece().color == Color.WHITE) {
                    movesShown = false;
                    resetTileColors();
                    displayPossibleMoves(((ChessSquare) e.getSource()).getPiece().validMoves(((ChessSquare) e.getSource()).getName(), ((ChessSquare) e.getSource()).getPiece().name));
                    previousClickedTile = (ChessSquare) e.getSource();
                    previousMoves = previousClickedTile.getPiece().validMoves(previousClickedTile.getName(), previousClickedTile.getPiece().name);
                    movesShown = true;
                    previousClickedTile.setBackground(new Color(205,209,106));
                }
                if ((((ChessSquare) e.getSource()).getPiece() == null || ((ChessSquare) e.getSource()).getPiece().color == Color.BLACK) && previousClickedTile != null) {
                    if (previousClickedTile.getPiece() != null) {
                        resetTileColors();
                        previousClickedTile.setBackground(new Color(205,209,106));
                        displayPossibleMoves(previousClickedTile.getPiece().validMoves(previousClickedTile.getName(), previousClickedTile.getPiece().name));
                        movePiece(((ChessSquare) e.getSource()).getName());
                        try {
                            ChessGame.toClient = new PrintWriter(ChessGame.clientSocket.getOutputStream(), true);
                            ChessGame.toClient.flush();
                            if (previousMoves.contains(((ChessSquare) e.getSource()).getName())) {
                                ChessGame.toClient.println(previousClickedTile.getName() + " " + ((ChessSquare) e.getSource()).getName() + " " + ((ChessSquare) e.getSource()).getPiece().name);
                                moved = true;
                            }
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        turn = "BLACK";
                    }
                }
                if (moved){
                    previousClickedTile = null;
                    movesShown = false;
                    Resources.playSound("Resources/Sounds/move-self.wav");
                }
            }
        }
    };

    public void displayPossibleMoves(ArrayList<String> moves) {
        for (String move : moves) {
            if (chessBoard[7 - (move.charAt(2) - 49)][(move.charAt(0) - 97)].getPiece() != null) {
                chessBoard[7 - (move.charAt(2) - 49)][(move.charAt(0) - 97)].setBackground(new Color(129,150,105));
            }
        }
    }


    public void movePiece(String name) {
        System.out.println("test name " + name + " previous tile" + previousClickedTile);
        System.out.println(previousClickedTile.getPiece().validMoves(previousClickedTile.getName(), previousClickedTile.getPiece().name));
        int x = name.charAt(0) - 97;
        int y = 7 - (name.charAt(2) - 49);
        System.out.println("Going to " + name);
        System.out.println(x);
        System.out.println(y);
        if (previousClickedTile.getPiece().validMoves(previousClickedTile.getName(), previousClickedTile.getPiece().name).contains(name)) {
            if (chessBoard[y][x].getPiece() != null) {
                GameCanvas.gameManager.removeGameObject(chessBoard[y][x].getPiece());
                Resources.playSound("Resources/Sounds/capture.wav");
            }
            GameCanvas.gameManager.removeGameObject(previousClickedTile.getPiece());
            PieceObject piece = new PieceObject(previousClickedTile.getPiece().name, previousClickedTile.getPiece().color, chessBoard[y][x].getPos()[0], chessBoard[y][x].getPos()[1]);
            chessBoard[y][x].setPiece(piece);
            GameCanvas.gameManager.addGameObject(piece);
            previousClickedTile.setPiece(null);
            resetTileColors();
            switchTurn();
        }
    }

    public static void moveResponse(int oldx, int oldy, int x, int y){
        if (chessBoard[y][x].getPiece() != null) {
            GameCanvas.gameManager.removeGameObject(chessBoard[y][x].getPiece());
            Resources.playSound("Resources/Sounds/capture.wav");
        }
        String name = chessBoard[oldy][oldx].getPiece().name;
        GameCanvas.gameManager.removeGameObject(chessBoard[oldy][oldx].getPiece());
        chessBoard[oldy][oldx].setPiece(null);
        PieceObject piece = new PieceObject(name, Color.BLACK,  chessBoard[y][x].getPos()[0], chessBoard[y][x].getPos()[1]);
        chessBoard[y][x].setPiece(piece);
        GameCanvas.gameManager.addGameObject(piece);
        moved = false;
        switchTurn();
    }

    private static void switchTurn() {
        if (turn.equals("WHITE")) {
            turn = "BLACK";
        } else {
            turn = "WHITE";
        }
    }

  

    public void resetTileColors() {
        for (int row = 0; row < ROWS; row++) {
            for (int col= 0; col < COLS; col++) {
                if ((row + col) % 2 == 0) {
                    chessBoard[row][col].setBackground(Color.WHITE);
                } else {
                    chessBoard[row][col].setBackground(LaunchScreen.gameColor);
                }
            }
        }
    }

    public ChessBoard() {
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        JPanel bottomLabels = new JPanel(new GridLayout(1, COLS));
        JPanel sideLabels = new JPanel(new GridLayout(ROWS, 1));
        JPanel timerPanel = new JPanel(new GridLayout(3, 1));

        Font labelFont = new Font("SansSerif", Font.BOLD, FONT_SIZE);

        whiteTimerLabel = new JLabel("White: " + whiteMin + ":" + String.format("%02d", whiteSec));
        blackTimerLabel = new JLabel("Black: " + blackMin + ":" + String.format("%02d", blackSec));
        turnLabel = new JLabel("Turn: " + turn);

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(turn);
                if (turn.equals("BLACK")) {
                    if (blackSec == 0) {
                        blackMin--;
                        blackSec = 59;
                    } else {
                        blackSec--;
                        if (blackMin == 0 && blackSec == 0){
                            timer.stop();
                            JOptionPane.showMessageDialog(ChessBoard.this, "WHITE WINS\nYou won by time!\n",
                                    "Notice", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                } else {
                    if (whiteSec == 0) {
                        whiteMin--;
                        whiteSec = 59;
                    } else {
                        whiteSec--;
                        if (whiteMin == 0 && whiteSec == 0){
                            timer.stop();
                            JOptionPane.showMessageDialog(ChessBoard.this, "BLACK WINS\nYou lost by time.\n",
                                    "Notice", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
                whiteTimerLabel.setText("White: " + whiteMin + ":" + String.format("%02d", whiteSec));
                blackTimerLabel.setText("Black: " + blackMin + ":" + String.format("%02d", blackSec));
                turnLabel.setText("Turn: " + turn);
            }
        });
        timer.start();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                ChessSquare currButton = new ChessSquare();
                currButton.setName(colNames[col] + " " + (ROWS - row));
                currButton.setBackground(LaunchScreen.gameColor);
                currButton.setBorder(new EmptyBorder(0, 0, 0, 0));
                currButton.setPos(col * (BOARD_SIZE / COLS), row * (BOARD_SIZE / ROWS));

                if ((row + col) % 2 == 0) {
                    currButton.setBackground(Color.WHITE);
                }

                currButton.setOpaque(true);
                currButton.addActionListener(pieceListener);
                chessBoard[row][col] = currButton;
                boardPanel.add(chessBoard[row][col]);
            }
            JLabel sideLabel = new JLabel(String.valueOf(ROWS - row));
            sideLabel.setHorizontalAlignment(JLabel.CENTER);
            sideLabel.setFont(labelFont);
            sideLabel.setBorder(new EmptyBorder(0, 0, PADDING_RIGHT, 0));
            sideLabels.add(sideLabel);
        }
        for (String colName : colNames) {
            JLabel bottomLabel = new JLabel(colName);
            bottomLabel.setHorizontalAlignment(JLabel.CENTER);
            bottomLabel.setFont(labelFont);
            bottomLabels.add(bottomLabel);
        }
        timerPanel.setBackground(Color.LIGHT_GRAY);

        // Set font and colors for white timer label
        whiteTimerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        whiteTimerLabel.setForeground(Color.BLACK); // Text color
        whiteTimerLabel.setBackground(Color.WHITE); // Background color
        whiteTimerLabel.setOpaque(true); // Make background color visible

        // Set font and colors for black timer label
        blackTimerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        blackTimerLabel.setForeground(Color.WHITE); // Text color
        blackTimerLabel.setBackground(Color.BLACK); // Background color
        blackTimerLabel.setOpaque(true); // Make background color visible

        // Set font and colors for turn label
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        turnLabel.setForeground(Color.BLACK); // Text color
        turnLabel.setBackground(Color.LIGHT_GRAY); // Background color
        turnLabel.setOpaque(true); // Make background color visible

        timerPanel.add(whiteTimerLabel);
        timerPanel.add(turnLabel);
        timerPanel.add(blackTimerLabel);

        JPanel mainPanel = new JPanel(new BorderLayout());
//        mainPanel.add(timerPanel, BorderLayout.EAST);
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(bottomLabels, BorderLayout.SOUTH);
        mainPanel.add(sideLabels, BorderLayout.WEST);

        add(mainPanel);
        initBoard();
    }

    public void initBoard() {
        for (int row = 0; row < COLS; row++) {
            for (int col = 0; col < ROWS; col++) {
                if(!boardInit[row][col].equals("Empty")){
                    Color color = (row > 3) ? Color.BLACK : Color.WHITE;
                    int []pos = chessBoard[row][col].getPos();
                    PieceObject piece = new PieceObject(boardInit[row][col], color, pos[0], pos[1]);
                    chessBoard[row][col].setPiece(piece);
                    GameCanvas.gameManager.addGameObject(piece);
                }
            }
        }
        whiteKing = new KingObject(chessBoard[5][0].getPos()[0], chessBoard[5][0].getPos()[1], 5, 0, Color.WHITE);
        blackKing = new KingObject(chessBoard[0][5].getPos()[0], chessBoard[0][5].getPos()[1], 0, 5, Color.BLACK);
        GameCanvas.gameManager.addGameObject(whiteKing);
        GameCanvas.gameManager.addGameObject(blackKing);
        chessBoard[5][0].setPiece(whiteKing);
        chessBoard[0][5].setPiece(blackKing);
    }
}
