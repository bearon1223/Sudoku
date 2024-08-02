package com.mysudoku.game.solver;

import com.badlogic.gdx.utils.Array;
import com.mysudoku.game.Utils;
import com.mysudoku.game.gameboard.Board;
import com.mysudoku.game.gameboard.Cell;

public class Solver {
    Board b;
    int[] previousBoard = new int[81];
    boolean stuck = false;

    public Solver(Board b) {
        this.b = b;
        previousBoard = b.getCellIDs();
        for (Cell c : b.getBoard()) {
            c.findAllCandidates();
        }
        this.stuck = false;
    }

    public void isolatedSingles() {
        // find Naked Singles
        Array<Cell> singleCell = new Array<>();
        for (int i = 0; i < b.getBoard().length; i++) {
            if (b.getBoard()[i].getID() == 0) {
                if (Integer.bitCount(b.getBoard()[i].getCandidates()) == 1) {
                    singleCell.add(b.getBoard()[i]);
                }
            }
        }

        // Set all the found singles to the known value
        for (Cell c : singleCell) {
            c.setID(c.getCandidates());
            Array<Cell> others2 = b.getAllRelaventCells(c);
            for (Cell c2 : others2) {
                c2.setCandidates(c2.getCandidates() & ~c.getID());
            }
        }
    }

    public void isolatedDoubles() {
        // pointing();
        Array<Cell> cellsToBeChecked = new Array<>();
        // Gather all cells with exactly 2 candidates
        for (Cell c : b.getBoard()) {
            if (Integer.bitCount(c.getCandidates()) == 2) {
                cellsToBeChecked.add(c);
            }
        }

        // Check the 3x3s
        for (Cell c : cellsToBeChecked) {
            // Get the needed cells
            Array<Cell> relaventCells = b.getSubBoard(c);
            for (Cell r : relaventCells) {
                // see if the candidates match
                if (c.getCandidates() == r.getCandidates()) {
                    Array<Cell> relaventCells2 = b.getSubBoard(r);
                    for (Cell r2 : relaventCells2) {
                        if (r2 != r && r2 != c) {
                            // remove the matching candidates from the other cells
                            r2.setCandidates(r2.getCandidates() & ~(c.getCandidates()));
                        }
                    }
                }
            }
        }

        // Check the rows (Steps are the same as the 3x3)
        for (Cell c : cellsToBeChecked) {
            Array<Cell> relaventCells = b.getRow(c);
            for (Cell r : relaventCells) {
                if (c.getCandidates() == r.getCandidates()) {
                    Array<Cell> relaventCells2 = b.getRow(r);
                    for (Cell r2 : relaventCells2) {
                        if (r2 != r && r2 != c) {
                            r2.setCandidates(r2.getCandidates() & ~(c.getCandidates()));
                        }
                    }
                }
            }
        }

        // Check the Columns *Steps are the same as the 3x3
        for (Cell c : cellsToBeChecked) {
            Array<Cell> relaventCells = b.getCol(c);
            for (Cell r : relaventCells) {
                if (c.getCandidates() == r.getCandidates()) {
                    Array<Cell> relaventCells2 = b.getCol(r);
                    for (Cell r2 : relaventCells2) {
                        if (r2 != r && r2 != c) {
                            r2.setCandidates(r2.getCandidates() & ~(c.getCandidates()));
                        }
                    }
                }
            }
        }
    }

    public void hiddenSingles() {
        for (Cell c : b.getBoard()) {
            if (c.getID() == 0) {
                Array<Cell> otherCells = b.getSubBoard(c);
                int candidates = c.getCandidates();
                for (Cell c2 : otherCells) {
                    // If when you remove all the c2 candidates from c and you end up with only 1
                    // number, use it
                    if (c2 != c)
                        candidates = candidates & ~(c2.getCandidates()); // 010100001 & ~(010100010) = 010100001 &
                                                                         // 101011101 = 0000000001
                }
                if (Integer.bitCount(candidates) == 1) {
                    c.setID(candidates);
                    Array<Cell> others2 = b.getAllRelaventCells(c);
                    for (Cell c2 : others2) {
                        c2.setCandidates(c2.getCandidates() & ~c.getID());
                    }
                }
            }
        }
    }

    public void patternCheck(int count) {
        // Row Checks
        for (Cell c : b.getBoard()) {
            if (c.getID() != 0 || Integer.bitCount(c.getCandidates()) != count)
                continue;
            Array<Cell> cellsToCheck = new Array<>();
            Array<Cell> tripletConfirmed = new Array<>();
            // Grab all the cells in the row with less than or equal to 'count = 3'
            // candidates
            if (Integer.bitCount(c.getCandidates()) <= count) {
                Array<Cell> relaventCells = b.getRow(c);
                for (Cell c2 : relaventCells) {
                    if (c2.getID() != 0)
                        continue;
                    if (Integer.bitCount(c2.getCandidates()) <= count)
                        cellsToCheck.add(c2);
                }
            }

            // look for a valid countlet (triplet) by counting all the ones with the same 3
            // candidates
            if (cellsToCheck.size >= count - 1) {
                int counter = 1;
                for (Cell c2 : cellsToCheck) {
                    if (c2.getID() != 0)
                        continue;
                    if (Integer.bitCount(c2.getCandidates() | c.getCandidates()) == count) {
                        counter++;
                        tripletConfirmed.add(c2);
                    }
                }

                // if the triplet is valid, remove those 3 from the others
                if (counter == count) {
                    Array<Cell> otherCells = b.getRow(c);
                    for (Cell c2 : otherCells) {
                        if (c2.getID() != 0)
                            continue;
                        else if (!tripletConfirmed.contains(c2, true) && c2 != c) {
                            c2.setCandidates(c2.getCandidates() & ~(c.getCandidates()));
                        }
                    }
                }
            }
        }

        // Col Checks
        for (Cell c : b.getBoard()) {
            if (c.getID() != 0 || Integer.bitCount(c.getCandidates()) != count)
                continue;
            Array<Cell> cellsToCheck = new Array<>();
            Array<Cell> tupletConfirmed = new Array<>();
            // Grab all the cells in the col with less than or equal to 'count = 3'
            // candidates
            if (Integer.bitCount(c.getCandidates()) <= count) {
                Array<Cell> relaventCells = b.getCol(c);
                for (Cell c2 : relaventCells) {
                    if (c2.getID() != 0)
                        continue;
                    if (Integer.bitCount(c2.getCandidates()) <= count)
                        cellsToCheck.add(c2);
                }
            }

            // look for a valid countlet (triplet) by counting all the ones with the same 3
            // candidates
            if (cellsToCheck.size >= count - 1) {
                int counter = 1;
                for (Cell c2 : cellsToCheck) {
                    if (c2.getID() != 0)
                        continue;
                    if (Integer.bitCount(c2.getCandidates() | c.getCandidates()) == count) {
                        counter++;
                        tupletConfirmed.add(c2);
                    }
                }

                // if the triplet is valid, remove those 3 from the others
                if (counter == count) {
                    Array<Cell> otherCells = b.getCol(c);
                    for (Cell c2 : otherCells) {
                        if (c2.getID() != 0)
                            continue;
                        else if (!tupletConfirmed.contains(c2, true) && c2 != c) {
                            c2.setCandidates(c2.getCandidates() & ~(c.getCandidates()));
                        }
                    }
                }
            }
        }

        // Sub-board checks
        for (Cell c : b.getBoard()) {
            if (c.getID() != 0 || Integer.bitCount(c.getCandidates()) != count)
                continue;
            Array<Cell> cellsToCheck = new Array<>();
            Array<Cell> tripletConfirmed = new Array<>();
            // Grab all the cells in the col with less than or equal to 'count = 3'
            // candidates
            if (Integer.bitCount(c.getCandidates()) <= count) {
                Array<Cell> relaventCells = b.getSubBoard(c);
                for (Cell c2 : relaventCells) {
                    if (c2.getID() != 0)
                        continue;
                    if (Integer.bitCount(c2.getCandidates()) <= count)
                        cellsToCheck.add(c2);
                }
            }

            // look for a valid countlet (triplet) by counting all the ones with the same 3
            // candidates
            if (cellsToCheck.size >= count - 1) {
                int counter = 1;
                for (Cell c2 : cellsToCheck) {
                    if (c2.getID() != 0)
                        continue;
                    if (Integer.bitCount(c2.getCandidates() | c.getCandidates()) == count) {
                        counter++;
                        tripletConfirmed.add(c2);
                    }
                }

                // if the triplet is valid, remove those 3 from the others
                if (counter == count) {
                    Array<Cell> otherCells = b.getSubBoard(c);
                    for (Cell c2 : otherCells) {
                        if (c2.getID() != 0)
                            continue;
                        else if (!tripletConfirmed.contains(c2, true) && c2 != c) {
                            c2.setCandidates(c2.getCandidates() & ~(c.getCandidates()));
                        }
                    }
                }
            }
        }
    }

    public void pointing(boolean debug) {
        for (Cell c : b.getBoard()) {
            if (c.getID() != 0)
                continue;
            Array<Cell> suboard = b.getSubBoard(c);
            Array<Cell> col = b.getCol(c);
            Array<Cell> cellsToCheck = new Array<>();
            // get the Cells in the subboard that are also in the column of the cell we are
            // checking
            for (Cell c2 : suboard) {
                if (col.contains(c2, false))
                    cellsToCheck.add(c2);
            }
            int otherCandidates = 0;

            // get all the candidates that the cells have in common (roughly)
            for (Cell c2 : cellsToCheck) {
                otherCandidates = otherCandidates | c2.getCandidates();
            }

            // split the candidates into separate tests.
            int[] testValues = Utils.splitIDs(c.getCandidates() & otherCandidates);

            for (int i = 0; i < testValues.length; i++) {
                boolean valid = true;
                int pointedValue = testValues[i];

                // check to see if other cells have that same candidate
                for (Cell c2 : suboard) {
                    if (c2.getLocation()[1] != c.getLocation()[1]) {
                        if ((pointedValue & (c2.getCandidates())) != 0) {
                            valid = false;
                        }
                    }
                }

                // printing debug info
                if (debug && valid)
                    System.out.printf("Cell: %d, %d, candidates: %s, pointer value: %s, valid: %b\n",
                            c.getLocation()[0],
                            c.getLocation()[1], Utils.paddedBinary(c.getCandidates(), 9),
                            Utils.paddedBinary(pointedValue, 9), valid);
                // just a check
                if (pointedValue == 0)
                    valid = false;

                if (!valid)
                    continue;

                if (debug)
                    System.out.printf("\tCell: %d, %d, removed %s from col %d\n", c.getLocation()[0],
                            c.getLocation()[1],
                            Utils.paddedBinary(pointedValue, 9), c.getLocation()[1]);

                for (Cell c2 : col) {
                    if (suboard.contains(c2, false))
                        continue;
                    c2.setCandidates(c2.getCandidates() & ~pointedValue);
                }
            }
        }

        // same checks as above but for rows
        for (Cell c : b.getBoard()) {
            if (c.getID() != 0)
                continue;
            Array<Cell> suboard = b.getSubBoard(c);
            Array<Cell> row = b.getRow(c);
            Array<Cell> cellsToCheck = new Array<>();

            for (Cell c2 : suboard) {
                if (row.contains(c2, false))
                    cellsToCheck.add(c2);
            }
            int otherCandidates = 0;

            for (Cell c2 : cellsToCheck) {
                otherCandidates = otherCandidates | c2.getCandidates();
            }

            int[] testValues = Utils.splitIDs(c.getCandidates() & otherCandidates);

            for (int i = 0; i < testValues.length; i++) {
                boolean valid = true;
                int pointedValue = testValues[i];
                for (Cell c2 : suboard) {
                    if (c2.getLocation()[0] != c.getLocation()[0]) {
                        if ((pointedValue & (c2.getCandidates())) != 0) {
                            valid = false;
                        }
                    }
                }

                // just a check
                if (pointedValue == 0)
                    valid = false;

                if (debug && valid)
                    System.out.printf("Cell: %d, %d, candidates: %s, pointer value: %s, valid: %b\n",
                            c.getLocation()[0],
                            c.getLocation()[1], Utils.paddedBinary(c.getCandidates(), 9),
                            Utils.paddedBinary(pointedValue, 9), valid);
                if (!valid)
                    continue;

                if (debug)
                    System.out.printf("\tCell: %d, %d, removed %s from row %d\n", c.getLocation()[0],
                            c.getLocation()[1],
                            Utils.paddedBinary(pointedValue, 9), c.getLocation()[0]);

                for (Cell c2 : row) {
                    if (suboard.contains(c2, false))
                        continue;
                    c2.setCandidates(c2.getCandidates() & ~pointedValue);
                }
            }
        }
    }

    public void updateCandidates(boolean debug) {
        isolatedDoubles();
        patternCheck(3);
        patternCheck(4);
        isolatedDoubles();
        pointing(debug);
        if (debug)
            System.out.println();
    }

    public boolean isStuck() {
        return stuck;
    }

    public void update(boolean debug) {
        updateCandidates(debug);
        isolatedSingles();
        updateCandidates(debug);
        hiddenSingles();
        updateCandidates(debug);
        if (Utils.areBoardsTheSame(b.getCellIDs(), previousBoard)) {
            stuck = true;
        } else {
            stuck = false;
        }
        previousBoard = b.getCellIDs();
    }
}
