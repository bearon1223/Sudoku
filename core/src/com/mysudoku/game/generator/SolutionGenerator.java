package com.mysudoku.game.generator;

import java.util.Random;

public class SolutionGenerator {

    private boolean is_valid(int[] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row * 9 + i] == num) {
                return false;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (board[i * 9 + col] == num) {
                return false;
            }
        }
        int[] subBoardStart = { (int) Math.floor(row / 3f) * 3, (int) Math.floor(col / 3f) * 3 };

        for (int i = 0; i < 3; i++) { // row
            for (int j = 0; j < 3; j++) { // col
                if (board[(i + subBoardStart[0]) * 9 + (j + subBoardStart[1])] == num) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean solve_sudoku(int[] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row * 9 + col] == 0) {
                    int[] randomNums = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
                    Random rand = new Random();

                    for (int i = 0; i < randomNums.length; i++) {
                        int randomIndexToSwap = rand.nextInt(randomNums.length);
                        int temp = randomNums[randomIndexToSwap];
                        randomNums[randomIndexToSwap] = randomNums[i];
                        randomNums[i] = temp;
                    }
                    for (int num : randomNums) {
                        if (is_valid(board, row, col, num)) {
                            board[row * 9 + col] = num;
                            if (solve_sudoku(board))
                                return true;
                            board[row * 9 + col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    public int[] generate_sudoku() {
        int[] board = new int[81];
        for (int i = 0; i < 81; i++) {
            board[i] = 0;
        }
        solve_sudoku(board);
        return board;
    }
}
