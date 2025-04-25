package com.example.gomokuexample;

import java.io.*;
import java.util.Stack;

public class GomokuGame {
    private static final int BOARD_SIZE = 20;
    private final int[][] board;
    private int currentPlayer;
    private boolean gameOver;
    private Stack<int[]> history;
    private Stack<int[]> redoStack;

    public GomokuGame() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        currentPlayer = 1;
        gameOver = false;
        history = new Stack<>();
        redoStack = new Stack<>();
    }

    public boolean move(int x, int y) {
        if (gameOver || x < 0 || y < 0 || x >= BOARD_SIZE || y >= BOARD_SIZE || board[x][y] != 0) {
            return false;
        }
        board[x][y] = currentPlayer;
        history.push(new int[]{x, y, currentPlayer});
        redoStack.clear();

        if (checkWin(x, y)) {
            gameOver = true;
        } else if (history.size() == BOARD_SIZE * BOARD_SIZE) {
            gameOver = true;
            System.out.println("Draw! No winner.");
        } else {
            currentPlayer = 3 - currentPlayer;
        }
        return true;
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        int[] lastMove = history.pop();
        board[lastMove[0]][lastMove[1]] = 0;
        redoStack.push(lastMove);
        currentPlayer = lastMove[2];
        gameOver = false;
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        int[] redoMove = redoStack.pop();
        return move(redoMove[0], redoMove[1]);
    }

    public void saveGame(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(board);
            out.writeObject(currentPlayer);
            out.writeObject(history);
            out.writeObject(redoStack);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadGame(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            int[][] loadedBoard = (int[][]) in.readObject();
            int loadedPlayer = (int) in.readObject();
            history = (Stack<int[]>) in.readObject();
            redoStack = (Stack<int[]>) in.readObject();
            System.arraycopy(loadedBoard, 0, board, 0, loadedBoard.length);
            currentPlayer = loadedPlayer;
            gameOver = false;
        }
    }

    private boolean checkWin(int x, int y) {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] dir : directions) {
            int count = 1;
            count += countDirection(x, y, dir[0], dir[1]);
            count += countDirection(x, y, -dir[0], -dir[1]);
            if (count >= 5) return true;
        }
        return false;
    }

    private int countDirection(int x, int y, int dx, int dy) {
        int count = 0;
        int player = board[x][y];
        for (int i = 1; i < 5; i++) {
            int nx = x + i * dx;
            int ny = y + i * dy;
            if (nx >= 0 && ny >= 0 && nx < BOARD_SIZE && ny < BOARD_SIZE && board[nx][ny] == player) {
                count++;
            } else break;
        }
        return count;
    }

    public int[][] getBoard() { return board; }
    public int getCurrentPlayer() { return currentPlayer; }
    public boolean isGameOver() { return gameOver; }

    // AI: Get best move for computer
    public int[] getBestMove() {
        int opponent = (currentPlayer == 1) ? 2 : 1;
        int bestRow = -1, bestCol = -1;
        int maxScore = -1;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == 0) {
                    // Try current player's move
                    board[row][col] = currentPlayer;
                    int myScore = evaluateBoard(currentPlayer);
                    board[row][col] = 0;

                    // Try opponent's move to block
                    board[row][col] = opponent;
                    int opponentScore = evaluateBoard(opponent);
                    board[row][col] = 0;

                    int score = Math.max(myScore, opponentScore);

                    if (score > maxScore) {
                        maxScore = score;
                        bestRow = row;
                        bestCol = col;
                    }
                }
            }
        }
        return new int[]{bestRow, bestCol};
    }

    private int evaluateBoard(int player) {
        int maxLine = 0;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] == player) {
                    maxLine = Math.max(maxLine, countDirection(row, col, 1, 0) + countDirection(row, col, -1, 0));
                    maxLine = Math.max(maxLine, countDirection(row, col, 0, 1) + countDirection(row, col, 0, -1));
                    maxLine = Math.max(maxLine, countDirection(row, col, 1, 1) + countDirection(row, col, -1, -1));
                    maxLine = Math.max(maxLine, countDirection(row, col, 1, -1) + countDirection(row, col, -1, 1));
                }
            }
        }
        return maxLine;
    }

    public void skipTurn() {
        throw new UnsupportedOperationException("Unimplemented method 'skipTurn'");
    }

    public void printBoard() {
        System.out.println("\n----------------------------------------------------------");
        System.out.print("   "); // Spasi untuk header kolom
    
        // Header kolom
        for (int i = 1; i <= BOARD_SIZE; i++) {
            System.out.printf("%2d ", i);
        }
        System.out.println();
    
        // Menampilkan isi papan permainan
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.printf("%2d ", i + 1); // Header baris
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 1) {
                    System.out.print("X  "); // Player 1
                } else if (board[i][j] == 2) {
                    System.out.print("O  "); // Player 2
                } else {
                    System.out.print("+  "); // Kosong
                }
            }
            System.out.println();
        }
        System.out.println("----------------------------------------------------------");
        System.out.flush();  // Memastikan output langsung muncul
    }
    
    
}