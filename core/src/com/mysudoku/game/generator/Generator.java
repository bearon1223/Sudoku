package com.mysudoku.game.generator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.async.AsyncExecutor;
import com.badlogic.gdx.utils.async.AsyncResult;
import com.mysudoku.game.Sudoku;
import com.mysudoku.game.Utils;
import com.mysudoku.game.gameboard.Board;
import com.mysudoku.game.gameboard.Cell;
import com.mysudoku.game.gameboard.DebugSudokuBoards;
import com.mysudoku.game.solver.Solver;

public class Generator {
    private int[] solution;
    private Board board;
    private int[] remainingIndicies;
    private int[] yetToBeTestedIndicies;
    private int tryCount = 0;
    private boolean debug = false;
    private int maxDepth = 0;
    private long startTime;
    private final Sudoku app;
    private AsyncExecutor executor;
    private AsyncResult<Void> futureTask;

    public Generator(int[] intendedSolution, boolean debug, Sudoku app) {
        this.solution = DebugSudokuBoards.convertToIDs(intendedSolution);
        board = new Board(solution);
        remainingIndicies = new int[81];
        yetToBeTestedIndicies = new int[81];
        for (int i = 0; i < 81; i++) {
            remainingIndicies[i] = i;
            yetToBeTestedIndicies[i] = i;
        }
        if (debug)
            System.out.println("Generator Setup");
        this.app = app;
        app.log("Generator Setup Complete");
        startTime = System.nanoTime();
        this.executor = new AsyncExecutor(1); // Single-threaded executor
    }

    public Generator(boolean debug, Sudoku app) {
        SolutionGenerator gen = new SolutionGenerator();
        this.solution = DebugSudokuBoards.convertToIDs(gen.generate_sudoku());
        board = new Board(solution.clone());
        remainingIndicies = new int[81];
        yetToBeTestedIndicies = new int[81];
        for (int i = 0; i < 81; i++) {
            remainingIndicies[i] = i;
            yetToBeTestedIndicies[i] = i;
        }
        this.debug = debug;
        if (debug)
            System.out.println("Generator Setup");
        this.app = app;
        app.log("Generator Setup Complete");
        startTime = System.nanoTime();
        this.executor = new AsyncExecutor(1); // Single-threaded executor
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getTryCount() {
        return tryCount;
    }

    private int[] removeIndex(int[] array, int index) {
        int[] newArray = new int[array.length - 1];
        for (int i = 0; i < array.length; i++) {
            if (i < index) {
                newArray[i] = array[i];
            } else if (i > index) {
                newArray[i - 1] = array[i];
            }
        }
        return newArray;
    }

    public int[] getSolution() {
        return solution;
    }

    public void generateAsync(int depth){
        if(futureTask == null || futureTask.isDone()){
            startTime = System.nanoTime();
            futureTask = executor.submit(() -> {
                generateSudoku(depth);
                return null;
            });
        }
    }

    public void generateSudoku(int depth) {
        if (tryCount == 0) {
            app.generating = true;
        }
        Board newBoard = new Board(this.board.getBoard());
        tryCount++;

        // get a random cell (using index arrays to ensure we don't try the same spot
        // twice) and set it to zero
        int index = MathUtils.random(0, yetToBeTestedIndicies.length - 1);
        newBoard.getBoard()[remainingIndicies[yetToBeTestedIndicies[index]]].setID(0b0000000);

        // get the board ID for later comparisons
        int[] boardID = newBoard.getCellIDs();

        // solve the board
        Solver s = new Solver(newBoard);
        if (depth > 0) {
            for (int i = 0; i <= 50; i++) {
                s.update(false);
                if (areBoardsTheSame(newBoard.getCellIDs(), solution)) {
                    break;
                }
            }
        }

        if (depth > 0 && yetToBeTestedIndicies.length > 1) {
            if (areBoardsTheSame(newBoard.getCellIDs(), solution)) {
                // set the mainboard to this board ID and remove the used index from the
                // remainingIndicies array while also resetting the yetToBeTestedIndicies. Then
                // generate with a lower depth
                this.board = new Board(boardID);
                remainingIndicies = removeIndex(remainingIndicies, yetToBeTestedIndicies[index]);
                yetToBeTestedIndicies = new int[remainingIndicies.length];
                for (int i = 0; i < remainingIndicies.length; i++) {
                    yetToBeTestedIndicies[i] = i;
                }
                generateSudoku(depth - 1);
            } else {
                yetToBeTestedIndicies = removeIndex(yetToBeTestedIndicies, index);
                generateSudoku(depth);
            }
        } else {
            for (Cell c : this.board.getBoard()) {
                c.findAllCandidates();
            }
            if (debug)
                System.out.printf("Lowest Depth: %d; try count: %d\n\n", depth, tryCount);
            maxDepth = depth;
            app.maxDepth = depth;
            app.tryCount = tryCount;
            app.timeTaken = System.nanoTime() - startTime;
            app.log("Generated, time: %3.2f ms", app.timeTaken / 1000f / 1000f);
        }
    }

    private boolean areBoardsTheSame(int[] a, int[] b) {
        return Utils.areBoardsTheSame(a, b);
    }

    public boolean isBoardReady() {
        return futureTask != null && futureTask.isDone();
    }

    public void shutdown() {
        executor.dispose();
    }

    public Board getBoard() {
        return board;
    }
}