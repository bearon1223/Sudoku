package com.mysudoku.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mysudoku.game.UIHelper.ButtonPanel;
import com.mysudoku.game.gameboard.Board;
import com.mysudoku.game.gameboard.Cell;
import com.mysudoku.game.solver.Solver;

import static com.mysudoku.game.Utils.hasCandidate;
import static com.mysudoku.game.Utils.numberToID;

public class PlaySudoku implements Screen {
    private Board b;
    private final Sudoku app;
    private OrthographicCamera camera;
    private ButtonPanel numberPanel, settingsPanel;
    private boolean alt, autoCandidates = false, candidatesMode = false;
    private Cell selectedCell = null;
    private int[] solution, originalBoard;
    private long startTime;

    public PlaySudoku(Sudoku app, Board b) {
        this.b = b;
        int[] board = b.getCellIDs();
        originalBoard = b.getCellIDs();
        Board b2 = new Board(board);
        Solver s = new Solver(b2);
        for (int i = 0; i < 50; i++) {
            s.update(false);
        }
        solution = b2.getCellIDs();
        this.app = app;
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        numberPanel = new ButtonPanel(800, 52, 700 / 9f, 700 / 9f, 1, 9, 2);
        String[] names = { "Candidates Mode", "Auto Candidates" };
        settingsPanel = new ButtonPanel(850 + 700 / 9f, 752 - 700 / 9f * 2f, 700 / 3f, 700 / 9f, 1, 2, 2, names);
        b.clearAllCandidates();
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float size = Math.min((width - 100f) / 9f, (height - 100f) / 9f);
        float offsetX = 50f;
        float offsetY = height / 2 - size * 4.5f;

        numberPanel.setSize(b.getSize(), b.getSize());
        numberPanel.setLocation(2 * offsetX + size*9, offsetY + 2);

        settingsPanel.setSize(700 / 3f * width / 1200, b.getSize());
        settingsPanel.setLocation((850 + 700 / 9f) * width / 1200, offsetY + 2 + b.getSize() * 7);

        ScreenUtils.clear(Color.BLACK);
        camera.update();
        app.renderer.setProjectionMatrix(camera.combined);
        app.renderer.begin(ShapeType.Filled);
        b.drawCells(app.renderer, true);
        numberPanel.draw(app.renderer);
        settingsPanel.draw(app.renderer);
        app.renderer.end();

        app.batch.setProjectionMatrix(camera.combined);
        app.batch.begin();
        b.drawText(app.batch, app.NumberFont, app.fontD, false, false, true);
        numberPanel.drawText(app.batch, app.font);
        settingsPanel.drawText(app.batch, app.font);
        app.batch.end();

        update();
    }

    public void update() {
        inputChecks();

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        int selectedNumber = 0;
        if (Gdx.input.isKeyJustPressed(Keys.NUM_1) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_1)
                || numberPanel.getButton(1 - 1).clicked(mouseX, mouseY))
            selectedNumber = 1;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_2) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_2)
                || numberPanel.getButton(2 - 1).clicked(mouseX, mouseY))
            selectedNumber = 2;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_3) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_3)
                || numberPanel.getButton(3 - 1).clicked(mouseX, mouseY))
            selectedNumber = 3;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_4) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_4)
                || numberPanel.getButton(4 - 1).clicked(mouseX, mouseY))
            selectedNumber = 4;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_5) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_5)
                || numberPanel.getButton(5 - 1).clicked(mouseX, mouseY))
            selectedNumber = 5;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_6) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_6)
                || numberPanel.getButton(6 - 1).clicked(mouseX, mouseY))
            selectedNumber = 6;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_7) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_7)
                || numberPanel.getButton(7 - 1).clicked(mouseX, mouseY))
            selectedNumber = 7;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_8) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_8)
                || numberPanel.getButton(8 - 1).clicked(mouseX, mouseY))
            selectedNumber = 8;
        else if (Gdx.input.isKeyJustPressed(Keys.NUM_9) || Gdx.input.isKeyJustPressed(Keys.NUMPAD_9)
                || numberPanel.getButton(9 - 1).clicked(mouseX, mouseY))
            selectedNumber = 9;

        if (selectedCell != null && selectedNumber != 0) {
            if (alt || candidatesMode) {
                if (hasCandidate(selectedCell, selectedNumber)) {
                    selectedCell.setCandidates(selectedCell.getCandidates() & ~numberToID(selectedNumber));
                    app.log("Candidate: %d removed", selectedNumber);
                } else {
                    selectedCell.setCandidates(selectedCell.getCandidates() | numberToID(selectedNumber));
                    app.log("Candidate: %d added", selectedNumber);
                }
            } else if (selectedCell.getID() == 0 || selectedCell.wasSet()) {
                selectedCell.setID(numberToID(selectedNumber));
                app.log("ID Set: %d, Cell %d, %d", selectedNumber, selectedCell.getLocation()[0],
                        selectedCell.getLocation()[1]);
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.BACKSPACE) && selectedCell != null) {
            app.log("Selected Cell was cleared");
            if (selectedCell.getID() != 0)
                selectedCell.setID(0);
            else
                selectedCell.clearCandidates();
        }

        for (int i = 0; i < b.getBoard().length; i++) {
            b.getBoard()[i].resetColor();
            if (!b.getBoard()[i].wasSet())
                continue;
            if (b.getBoard()[i].getID() != solution[i]) {
                b.getBoard()[i].setColor(Color.PINK);
            }
        }

        if (Utils.areBoardsTheSame(b.getCellIDs(), solution)) {
            for (Cell c : b.getBoard()) {
                if (c.wasSet())
                    c.setColor(Color.GREEN);
            }
        }
    }

    public void inputChecks() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            app.log("Returning to Auto solve");
            for (Cell c2 : b.getBoard()) {
                c2.setSelected(false);
                c2.setSelected2(false);
            }
            app.setScreen(new AutoSolveSudoku(app, new Board(originalBoard), this.solution));
        }

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        // Cell c = b.getCellOver(mouseX, mouseY);
        Cell c = null;

        if (selectedCell != null) {
            // If the arrow keys are pressed, move the selected cell to the cell in that
            // direction
            if (heldDown(Keys.UP)) {
                if (selectedCell.getLocation()[0] < 8)
                    c = b.getCell(selectedCell.getLocation()[0] + 1, selectedCell.getLocation()[1]);
                else
                    c = selectedCell;
            } else if (heldDown(Keys.DOWN)) {
                if (selectedCell.getLocation()[0] > 0)
                    c = b.getCell(selectedCell.getLocation()[0] - 1, selectedCell.getLocation()[1]);
                else
                    c = selectedCell;
            } else if (heldDown(Keys.LEFT)) {
                if (selectedCell.getLocation()[1] > 0)
                    c = b.getCell(selectedCell.getLocation()[0], selectedCell.getLocation()[1] - 1);
                else
                    c = selectedCell;
            } else if (heldDown(Keys.RIGHT)) {
                if (selectedCell.getLocation()[1] < 8)
                    c = b.getCell(selectedCell.getLocation()[0], selectedCell.getLocation()[1] + 1);
                else
                    c = selectedCell;
            }
        }

        if (Gdx.input.isButtonJustPressed(1)) {
            c = null;
            selectedCell = null;
            for (Cell c2 : b.getBoard()) {
                c2.setSelected(false);
                c2.setSelected2(false);
            }
        }

        if (checkInputs()) {
            for (Cell c2 : b.getBoard()) {
                c2.setSelected(false);
                c2.setSelected2(false);
            }
        }

        if (Gdx.input.isButtonJustPressed(0)) {
            c = b.getCellOver(mouseX, mouseY);
            if (c != null) {
                c.setSelected2(true);
                selectedCell = c;
                Array<Cell> a = b.getAllRelaventCells(c);
                for (Cell c2 : a) {
                    if (c2.getID() == 0 || c2.wasSet())
                        c2.setSelected(true);
                }
            }
        } else if (checkInputs() && c != null) {
            c.setSelected2(true);
            selectedCell = c;
            Array<Cell> a = b.getAllRelaventCells(c);
            for (Cell c2 : a) {
                if (c2.getID() == 0 || c2.wasSet())
                    c2.setSelected(true);
            }
        }

        if (settingsPanel.getButton(0).clicked(mouseX, mouseY)) {
            autoCandidates = !autoCandidates;
            if (!autoCandidates) {
                b.clearAllCandidates();
                app.log("Clearing Candidates");
                settingsPanel.getButton(0).setText("Auto Candidates");
            } else {
                if (Gdx.input.isKeyPressed(Keys.ALT_LEFT)) {
                    Solver s = new Solver(this.b);
                    s.updateCandidates(false);
                    app.log("Adding Auto Smart Candidates");
                } else {
                    for (Cell c2 : b.getBoard()) {
                        c2.findAllCandidates();
                    }
                    app.log("Adding Normal Candidates");
                }
                settingsPanel.getButton(0).setText("Manual Candidates");
            }
        } else if (settingsPanel.getButton(1).clicked(mouseX, mouseY)) {
            candidatesMode = !candidatesMode;
            if (candidatesMode)
                settingsPanel.getButton(1).setText("Number Mode");
            else
                settingsPanel.getButton(1).setText("Candidates Mode");
        }

        if (Gdx.input.isKeyJustPressed(Keys.ALT_LEFT) || Gdx.input.isKeyJustPressed(Keys.ALT_RIGHT)) {
            alt = true;
        } else if (!Gdx.input.isKeyPressed(Keys.ALT_LEFT) && !Gdx.input.isKeyPressed(Keys.ALT_RIGHT)) {
            alt = false;
        }
    }

    private boolean checkInputs() {
        boolean bPressed = Gdx.input.isButtonJustPressed(0);
        boolean aPressed = (key(Keys.UP) || key(Keys.DOWN) || key(Keys.LEFT) || key(Keys.RIGHT));
        return (aPressed && !bPressed) || (bPressed && !aPressed);
    }

    private boolean key(int keycode) {
        return heldDown(keycode);
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }

    private boolean heldDown(int Key) {
        if (Gdx.input.isKeyJustPressed(Key)) {
            startTime = System.currentTimeMillis();
            return true;
        }
        return System.currentTimeMillis() - startTime > 375f && Gdx.input.isKeyPressed(Key)
                && Gdx.graphics.getFrameId() % 3 == 0;
    }

}
