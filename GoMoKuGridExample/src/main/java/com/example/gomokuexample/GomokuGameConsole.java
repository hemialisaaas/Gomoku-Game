package com.example.gomokuexample;

import java.util.Scanner;
import java.io.IOException;

public class GomokuGameConsole {
    private static boolean boardVisible = true; // Status visibilitas papan
    private static int[] viewChances = {3, 3}; // Kesempatan melihat papan (Player 1 & 2)

    public static void main(String[] args) {
        GomokuGame game = new GomokuGame();
        Scanner scanner = new Scanner(System.in);
        String saveFile = "gomoku_save.dat"; // Nama file untuk save/load game

        System.out.println("🎮 Welcome to Gomoku! 🎮");

        while (!game.isGameOver()) {
            int currentPlayer = game.getCurrentPlayer();

            // Menampilkan papan hanya jika tidak disembunyikan
            if (boardVisible) {
                printBoard(game.getBoard());
            } else {
                System.out.println("📛 Board is hidden. Type 'toggle' to show it or 'view' to use a chance.");
            }

            System.out.println("Player " + currentPlayer + "'s turn.");
            System.out.print("Please input the row number and column number, separated by space: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("undo")) {
                if (game.undo()) {
                    System.out.println("✅ Undo successful.");
                } else {
                    System.out.println("❌ No moves to undo.");
                }
                continue;
            }

            if (input.equalsIgnoreCase("redo")) {
                if (game.redo()) {
                    System.out.println("✅ Redo successful.");
                } else {
                    System.out.println("❌ No moves to redo.");
                }
                continue;
            }

            if (input.equalsIgnoreCase("reset")) {
                game = new GomokuGame();
                viewChances = new int[]{3, 3}; // Reset kesempatan melihat papan
                System.out.println("🔄 Game reset.");
                continue;
            }

            if (input.equalsIgnoreCase("save")) {
                try {
                    game.saveGame(saveFile);
                    System.out.println("💾 Game saved.");
                } catch (IOException e) {
                    System.out.println("❌ Failed to save game.");
                }
                continue;
            }

            if (input.equalsIgnoreCase("load")) {
                try {
                    game.loadGame(saveFile);
                    System.out.println("📂 Game loaded.");
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("❌ Failed to load game.");
                }
                continue;
            }

            if (input.equalsIgnoreCase("toggle")) {
                boardVisible = !boardVisible; // Toggle board visibility
                System.out.println(boardVisible ? "✅ Board is now visible." : "🚫 Board is now hidden.");
                continue;
            }

            if (input.equalsIgnoreCase("view")) {
                if (viewChances[currentPlayer - 1] > 0) {
                    viewChances[currentPlayer - 1]--;
                    boardVisible = true;
                    printBoard(game.getBoard());
                    System.out.println("👀 Player " + currentPlayer + " viewed the board. Remaining chances: " + viewChances[currentPlayer - 1]);
                    boardVisible = false; // Setelah melihat, kembali tersembunyi
                } else {
                    System.out.println("❌ No view chances left for Player " + currentPlayer);
                }
                continue;
            }

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("👋 Exiting game. Goodbye!");
                break;
            }

            // Parsing input sebagai koordinat
            String[] parts = input.split(" ");
            if (parts.length != 2) {
                System.out.println("❌ Invalid input. Please input the row number and column number, separated by space.");
                continue;
            }

            try {
                int row = Integer.parseInt(parts[0]) - 1;
                int col = Integer.parseInt(parts[1]) - 1;

                if (!game.move(row, col)) {
                    System.out.println("❌ Invalid move. Try again.");
                } else if (game.isGameOver()) {
                    printBoard(game.getBoard());
                    System.out.println("🎉 Player " + currentPlayer + " wins! 🎉");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please input the row number and column number, separated by space.");
            }
        }

        scanner.close();
    }

    // 🏆 Menampilkan papan Gomoku di terminal
    private static void printBoard(int[][] board) {
        System.out.println("\n   " + "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20");
        System.out.println("   --------------------------------------------------");
        for (int i = 0; i < board.length; i++) {
            System.out.printf("%2d | ", i + 1);
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == 1) {
                    System.out.print("X ");
                } else if (board[i][j] == 2) {
                    System.out.print("O ");
                } else {
                    System.out.print("+ ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
