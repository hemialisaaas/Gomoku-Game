package com.example.gomokuexample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class GomokuGameFX extends Application {
    private static final int TILE_SIZE = 30;
    private static final int BOARD_SIZE = 20;
    private static final int TIME_LIMIT = 30;
    private static final int VIEW_CHANCES = 3;
    private GomokuGame game;
    private Canvas canvas;
    private Label statusLabel;
    private Label timerLabel;
    private Timeline timer;
    private int timeLeft;
    private boolean boardVisible = true;
    private int[] viewChances = {VIEW_CHANCES, VIEW_CHANCES};
    private boolean playVsComputer = false;

    @Override
    public void start(@SuppressWarnings("exports") Stage primaryStage) {
        game = new GomokuGame();

        canvas = new Canvas(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        canvas.setOnMouseClicked(event -> handleMouseClick(event.getX(), event.getY()));
        drawBoard();

        statusLabel = new Label("Player 1's Turn");
        statusLabel.setFont(new Font(20));

        timerLabel = new Label("Time Left: " + TIME_LIMIT + "s");
        timerLabel.setFont(new Font(16));
        startTimer();

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetGame());

        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undoMove());

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> redoMove());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveGame());

        Button loadButton = new Button("Load");
        loadButton.setOnAction(e -> loadGame());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> primaryStage.close());

        Button toggleVisibilityButton = new Button("Toggle Visibility");
        toggleVisibilityButton.setOnAction(e -> toggleVisibility());

        Button viewBoardButton = new Button("View Board");
        viewBoardButton.setOnAction(e -> temporaryViewBoard());

        Button playVsComputerButton = new Button("Play vs Computer");
        playVsComputerButton.setOnAction(e -> togglePlayVsComputer());

        HBox controls = new HBox(10, resetButton, undoButton, redoButton, saveButton, loadButton, toggleVisibilityButton, viewBoardButton, playVsComputerButton, exitButton);

        VBox root = new VBox(10, statusLabel, timerLabel, canvas, controls);
        Scene scene = new Scene(root);

        primaryStage.setTitle("Gomoku Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @SuppressWarnings("unused")
    private Button createButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.setOnMouseEntered(event -> applyHoverEffect(button));
        button.setOnMouseExited(event -> removeHoverEffect(button));
        button.setOnAction(action);
        return button;
    }

    private void applyHoverEffect(Button button) {
        button.setEffect(new DropShadow(10, Color.GRAY));
        button.setStyle("-fx-background-color: #d1d1d1;");
    }

    private void removeHoverEffect(Button button) {
        button.setEffect(null);
        button.setStyle("-fx-background-color: #f0f0f0;");
    }

    private void togglePlayVsComputer() {
        playVsComputer = !playVsComputer;
        statusLabel.setText(playVsComputer ? "Playing vs Computer" : "Two Player Mode");
        resetGame();
    }

    private void handleMouseClick(double x, double y) {
        int row = (int) (y / TILE_SIZE);
        int col = (int) (x / TILE_SIZE);

        if (game.move(row, col)) {
            drawBoard();
            if (game.isGameOver()) {
                int winner = game.getCurrentPlayer();
                statusLabel.setText("Player " + winner + " wins!");
                timer.stop();
            } else {
                statusLabel.setText("Player " + game.getCurrentPlayer() + "'s Turn");
                resetTimer();

                if (playVsComputer && game.getCurrentPlayer() == 2) {
                    computerMove();
                }
            }
        } else {
            statusLabel.setText("Invalid Move. Try again.");
            flashInvalidMove(row, col);
        }
    }

    private void computerMove() {
        int[] move = game.getBestMove();
        if (move != null && game.move(move[0], move[1])) {
            drawBoard();
            if (game.isGameOver()) {
                statusLabel.setText("Computer wins!");
                timer.stop();
            } else {
                statusLabel.setText("Player 1's Turn");
                resetTimer();
            }
        } else {
            statusLabel.setText("Computer failed to move. Skipping turn.");
        }
    }
    

    private void flashInvalidMove(int row, int col) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.RED);
        gc.fillOval(col * TILE_SIZE + 5, row * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        new Timeline(new KeyFrame(Duration.seconds(0.5), e -> drawBoard())).play();
    }

    private void drawBoard() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (boardVisible) {
            for (int i = 0; i <= BOARD_SIZE; i++) {
                gc.strokeLine(i * TILE_SIZE, 0, i * TILE_SIZE, TILE_SIZE * BOARD_SIZE);
                gc.strokeLine(0, i * TILE_SIZE, TILE_SIZE * BOARD_SIZE, i * TILE_SIZE);
            }

            int[][] board = game.getBoard();
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    if (board[row][col] == 1) {
                        gc.setFill(Color.BLACK);
                        gc.fillOval(col * TILE_SIZE + 5, row * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                    } else if (board[row][col] == 2) {
                        gc.setFill(Color.WHITE);
                        gc.fillOval(col * TILE_SIZE + 5, row * TILE_SIZE + 5, TILE_SIZE - 10, TILE_SIZE - 10);
                    }
                }
            }
        }
    }

    private void temporaryViewBoard() {
        int currentPlayer = game.getCurrentPlayer() - 1;
        if (viewChances[currentPlayer] > 0) {
            viewChances[currentPlayer]--;
            boardVisible = true;
            drawBoard();
            statusLabel.setText("Player " + (currentPlayer + 1) + " view chances left: " + viewChances[currentPlayer]);
            new Timeline(new KeyFrame(Duration.seconds(3), e -> toggleVisibility())).play();
        } else {
            statusLabel.setText("No view chances left for Player " + (currentPlayer + 1));
        }
    }

    private void toggleVisibility() {
        if (viewChances[0] > 0 || viewChances[1] > 0) {
            boardVisible = !boardVisible;
            drawBoard();
        }
    }

    private void resetGame() {
        game = new GomokuGame();
        statusLabel.setText("Player 1's Turn");
        drawBoard();
        resetTimer();
    }

    private void undoMove() {
        if (game.undo()) {
            drawBoard();
            statusLabel.setText("Undo successful. Player " + game.getCurrentPlayer() + "'s Turn");
        } else {
            statusLabel.setText("No moves to undo.");
        }
    }

    private void redoMove() {
        if (game.redo()) {
            drawBoard();
            statusLabel.setText("Redo successful. Player " + game.getCurrentPlayer() + "'s Turn");
        } else {
            statusLabel.setText("No moves to redo.");
        }
    }

    private void saveGame() {
        try {
            game.saveGame("gomoku_save.dat");
            statusLabel.setText("Game saved successfully!");
        } catch (IOException e) {
            statusLabel.setText("Failed to save game.");
        }
    }

    private void loadGame() {
        try {
            game.loadGame("gomoku_save.dat");
            drawBoard();
            statusLabel.setText("Game loaded successfully!");
            resetTimer();
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Failed to load game.");
        }
    }

    private void startTimer() {
        timeLeft = TIME_LIMIT;
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            timerLabel.setText("Time Left: " + timeLeft + "s");
            if (timeLeft <= 0) {
                statusLabel.setText("Time's up! Player " + game.getCurrentPlayer() + " loses their turn.");
                game.skipTurn();  // Buat metode ini di GomokuGame
                resetTimer();
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void resetTimer() {
        timer.stop();
        startTimer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
