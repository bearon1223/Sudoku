package com.mysudoku.game.gameboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.mysudoku.game.Utils;

public class Board {
    Cell[] board = new Cell[81]; // 9x9

    public Board() {
        for (int i = 0; i < 9; i++) { // row
            for (int j = 0; j < 9; j++) { // col
                int nytBoard = DebugSudokuBoards.generatedBroken[i * 9 + j];
                if (nytBoard != 0)
                    board[i * 9 + j] = new Cell(0b1 << (nytBoard - 1), this, i, j);
                if (nytBoard == 0) {
                    board[i * 9 + j] = new Cell(0, this, i, j);
                }
            }
        }
    }

    public Board(int[] board) {
        for (int i = 0; i < 9; i++) { // row
            for (int j = 0; j < 9; j++) { // col
                int id = board[i * 9 + j];
                if (id != 0)
                    this.board[i * 9 + j] = new Cell(id, this, i, j);
                if (id == 0) {
                    this.board[i * 9 + j] = new Cell(0, this, i, j);
                }
            }
        }
    }

    public Board(Cell[] board) {
        for (int i = 0; i < 9; i++) { // row
            for (int j = 0; j < 9; j++) { // col
                int id = board[i * 9 + j].getID();
                if (id != 0)
                    this.board[i * 9 + j] = new Cell(id, this, i, j);
                if (id == 0) {
                    this.board[i * 9 + j] = new Cell(0, this, i, j);
                }
            }
        }
    }

    public Array<Cell> getRow(Cell c) {
        int[] loc = c.getLocation(); // (row, col) = (3, 5) thus index is row*9 + col = 27+5 = 32
        Array<Cell> relaventCells = new Array<>(); // to get the other columns we need (0, 5) -> (8, 5)
        for (int i = 0; i < 9; i++) {
            Cell s = board[loc[0] * 9 + i];
            if (s != c)
                relaventCells.add(s);
        }
        return relaventCells;
    }

    public Array<Cell> getCol(Cell c) {
        int[] loc = c.getLocation(); // (row, col) = (3, 5) thus index is row*9 + col = 27+5 = 32 to get the other
                                     // rows, we need (3, 0) -> (3, 8)
        Array<Cell> relaventCells = new Array<>();
        for (int i = 0; i < 9; i++) {
            Cell s = board[i * 9 + loc[1]];
            if (s != c)
                relaventCells.add(s);
        }
        return relaventCells;
    }

    public int getRemainingCellCount() {
        int count = 0;
        for (Cell b : board) {
            if (b.getID() == 0) {
                count++;
            }
        }
        return count;
    }

    public Array<Cell> getSubBoard(Cell c) {
        int[] loc = c.getLocation();
        Array<Cell> relaventCells = new Array<>();

        // to get the 3x3 its in, we need to check where it is in the 3x3. If we take
        // its location, divide it by 3 and floor it, we should get the specific 3x3.
        // Then using that information, we could multiply it by 3 to get the start of
        // the 3x3 and loop from there.
        int[] subBoardStart = { (int) Math.floor(loc[0] / 3f) * 3, (int) Math.floor(loc[1] / 3f) * 3 };

        // Then loop over the cells
        for (int i = 0; i < 3; i++) { // row
            for (int j = 0; j < 3; j++) { // col
                Cell s = board[(i + subBoardStart[0]) * 9 + (j + subBoardStart[1])];
                if (s != c)
                    relaventCells.add(s);
            }
        }
        return relaventCells;
    }

    public Array<Cell> getAllRelaventCells(Cell c) {
        int[] loc = c.getLocation(); // (row, col) = (3, 5) thus index is row*9 + col = 27+5 = 32 to get the other
                                     // rows, we need (3, 0) -> (3, 8)
        Array<Cell> relaventCells = new Array<>(); // to get the other columns we need (0, 5) -> (8, 5)
        for (int i = 0; i < 9; i++) {
            Cell s = board[loc[0] * 9 + i];
            if (s != c)
                relaventCells.add(s);
        }
        for (int i = 0; i < 9; i++) {
            Cell s = board[i * 9 + loc[1]];
            if (s != c)
                relaventCells.add(s);
        }
        // to get the 3x3 its in, we need to check where it is in the 3x3. If we take
        // its location, divide it by 3 and floor it, we should get the specific 3x3.
        // Then using that information, we could multiply it by 3 to get the start of
        // the 3x3 and loop from there.
        int[] subBoardStart = { (int) Math.floor(loc[0] / 3f) * 3, (int) Math.floor(loc[1] / 3f) * 3 };

        // Then using row
        for (int i = 0; i < 3; i++) { // row
            for (int j = 0; j < 3; j++) { // col
                Cell s = board[(i + subBoardStart[0]) * 9 + (j + subBoardStart[1])];
                if (s != c)
                    relaventCells.add(s);
            }
        }
        return relaventCells;
    }

    public void debug(Cell c) {
        int[] loc = c.getLocation(); // (row, col) = (3, 5) thus index is row*9 + col = 27+5 = 32
        for (int i = 0; i < 9; i++) {
            board[loc[0] * 9 + i].setColor(Color.BROWN);
        }
        for (int i = 0; i < 9; i++) {
            board[i * 9 + loc[1]].setColor(Color.BROWN);
        }
        int[] subBoardStart = { (int) Math.floor(loc[0] / 3f) * 3, (int) Math.floor(loc[1] / 3f) * 3 };

        // Then using row
        for (int i = 0; i < 3; i++) { // row
            for (int j = 0; j < 3; j++) { // col
                board[(i + subBoardStart[0]) * 9 + (j + subBoardStart[1])].setColor(Color.BROWN);
            }
        }
        c.setColor(Color.GOLD);
    }

    public void drawText(SpriteBatch batch, BitmapFont font, BitmapFont smallFont, boolean debug, boolean binary) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float size = Math.min((width - 100f) / 9f, (height - 100f) / 9f);
        float offsetX = 50f;
        float offsetY = height / 2 - size * 4.5f;
        font.setColor(Color.BLACK);
        smallFont.setColor(Color.BLACK);
        for (Cell c : board) {
            c.drawText(batch, font, smallFont, size, offsetX, offsetY, debug, binary);
        }
    }

    public void drawText(SpriteBatch batch, BitmapFont font, BitmapFont smallFont, boolean debug, boolean binary,
            boolean showCandidates) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float size = Math.min((width - 100f) / 9f, (height - 100f) / 9f);
        float offsetX = 50f;
        float offsetY = height / 2 - size * 4.5f;
        font.setColor(Color.BLACK);
        smallFont.setColor(Color.BLACK);
        for (Cell c : board) {
            c.drawText(batch, font, smallFont, size, offsetX, offsetY, debug, binary, showCandidates);
        }
    }

    public boolean isComplete() {
        boolean complete = true;
        for (Cell c : board) {
            if (c.getID() == 0)
                complete = false;
        }
        return complete;
    }

    public Cell[] getBoard() {
        return board;
    }

    public void print() {
        for (int i = 0; i < 9; i++) { // row
            for (int j = 0; j < 9; j++) { // col
                System.out.printf("%d, ", Utils.idToNumber(board[i * 9 + j].getID()).isEmpty() ? 0
                        : Utils.idToNumber(board[i * 9 + j].getID()).first());
            }
            System.out.println();
        }
    }

    public int[] getCellIDs() {
        int[] boardIDs = new int[board.length];
        for (int i = 0; i < board.length; i++) {
            boardIDs[i] = board[i].getID();
        }
        return boardIDs;
    }

    public Cell getCell(int row, int col) {
        return board[row * 9 + col];
    }

    public Cell getCellOver(float mouseX, float mouseY) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float size = Math.min((width - 100f) / 9f, (height - 100f) / 9f);

        float offsetX = 50;
        float offsetY = height / 2 - size * 4.5f;

        if (!(mouseX < offsetX + size * 9 && mouseX > offsetX && mouseY < offsetY + size * 9 && mouseY > offsetY))
            return null;

        mouseX -= offsetX;
        mouseY -= offsetY;
        mouseY = size * 9 - mouseY;

        mouseX /= size;
        mouseY /= size;

        int i = (int) Math.floor(mouseY);
        int j = (int) Math.floor(mouseX);

        if (i * 9 + j < 81) {
            return board[i * 9 + j];
        }

        return null;
    }

    public float getSize() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        return Math.min((width - 100f) / 9f, (height - 100f) / 9f);
    }

    public void clearAllCandidates() {
        for (Cell c : board) {
            c.clearCandidates();
        }
    }

    public void drawCells(ShapeRenderer renderer, boolean useSetColor) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float size = Math.min((width - 100f) / 9f, (height - 100f) / 9f);

        float offsetX = 50;
        float offsetY = height / 2 - size * 4.5f;

        renderer.setColor(Color.BLACK);
        renderer.rect(offsetX, offsetY, width - offsetX * 2 + 2, height - offsetY * 2 + 2);
        for (int i = 0; i < board.length; i++) {
            board[i].render(renderer, size, offsetX, offsetY, useSetColor);
        }

        renderer.setColor(Color.BLACK);
        renderer.rectLine(offsetX + (size * 9) / 3f - 2f, offsetY, offsetX + (size * 9) / 3f - 2f, height - offsetY, 4);
        renderer.rectLine(offsetX + (size * 9) * 2 / 3f - 2f, offsetY, offsetX + (size * 9) * 2 / 3f - 2f,
                height - offsetY, 4);

        renderer.rectLine(offsetX, (size * 9) / 3f + offsetY - 2f, offsetX + size * 9f, (size * 9) / 3f + offsetY - 2f,
                4);
        renderer.rectLine(offsetX, (size * 9) * 2 / 3f + offsetY - 2f, offsetX + size * 9f,
                (size * 9) * 2 / 3f + offsetY - 2f, 4);

    }
}
