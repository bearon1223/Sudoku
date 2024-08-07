package com.mysudoku.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mysudoku.game.UIHelper.ButtonPanel;
import com.mysudoku.game.gameboard.Board;
import com.mysudoku.game.gameboard.Cell;
import com.mysudoku.game.gameboard.DebugSudokuBoards;
import com.mysudoku.game.generator.Generator;
import com.mysudoku.game.solver.Solver;

public class AutoSolveSudoku implements Screen {
	private Board b;
	private Solver solver;
	private OrthographicCamera camera;
	private int[] originalBoard, solution;
	private final Sudoku app;
	private boolean debug = false, binary = false, shift = false, idle = false, stress = false;
	private ButtonPanel menuPanel;
	private int solverStepCount = 0;

	public AutoSolveSudoku(Sudoku app) {
		b = new Board();
		originalBoard = b.getCellIDs();
		camera = new OrthographicCamera();
		camera.setToOrtho(false);
		solution = DebugSudokuBoards.convertToIDs(DebugSudokuBoards.generatedBrokenSolution);
		solver = new Solver(b);
		this.app = app;

		for (Cell c : this.b.getBoard()) {
			c.findAllCandidates();
		}

		solver.updateCandidates(false);
		solver.updateCandidates(false);
		String[] names = { "Play Sudoku", "Idle Mode" };
		menuPanel = new ButtonPanel(850 + 700 / 9f, 752 - 700 / 9f * 2f, 700 / 3f, 700 / 9f, 1, 2, 2, names);
	}

	public AutoSolveSudoku(Sudoku app, Board b, int[] solution) {
		this.b = b;
		originalBoard = b.getCellIDs();
		camera = new OrthographicCamera();
		camera.setToOrtho(false);
		this.solution = solution;
		solver = new Solver(b);
		this.app = app;

		for (Cell c : this.b.getBoard()) {
			c.findAllCandidates();
		}
		String[] names = { "Play Sudoku", "Idle Mode" };
		menuPanel = new ButtonPanel(850 + 700 / 9f, 752 - 700 / 9f * 2f, 700 / 3f, 700 / 9f, 1, 2, 2, names);
	}

	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        float size = Math.min((width - 100f) / 9f, (height - 100f) / 9f);
        float offsetY = height / 2 - size * 4.5f;

        menuPanel.setSize(700 / 3f * width / 1200, b.getSize());
        menuPanel.setLocation((850 + 700 / 9f) * width / 1200, offsetY + 2 + b.getSize() * 7);
		ScreenUtils.clear(Color.BLACK, true);
		debug = app.showCandidates;
		binary = app.showBinary;
		camera.update();
		app.renderer.setProjectionMatrix(camera.combined);
		app.renderer.begin(ShapeType.Filled);
		b.drawCells(app.renderer, debug);
		menuPanel.draw(app.renderer);
		app.renderer.end();

		app.batch.setProjectionMatrix(camera.combined);
		app.batch.begin();
		app.fontD.setColor(Color.BLACK);
		app.font.setColor(Color.BLACK);
		app.NumberFont.setColor(Color.BLACK);
		b.drawText(app.batch, binary ? app.fontD : app.NumberFont, app.fontD, debug, binary);
		menuPanel.drawText(app.batch, app.font);
		app.batch.end();

		// Check inputs
		if (app.stepSolver) {
			solver.update(false);
			if (app.debugScreen.isOpen())
				app.log("Updating Solver, Cells left: %d", b.getRemainingCellCount());
			solverStepCount++;
		}
		if ((Gdx.input.isButtonJustPressed(1) && debug && !idle) || app.debugScreen.getStepCandidates()) {
			solver.updateCandidates(false);
		}

		if (idle) {
			if (Gdx.graphics.getFrameId() % 50 == 0 && !b.isComplete() && !solver.isStuck()) {
				solver.update(false);
				solverStepCount++;
				if (b.isComplete())
					app.log("Solved in %d steps", solverStepCount);
			} else if (Gdx.graphics.getFrameId() % 50 == 0) {
				if (solver.isStuck()) {
					app.log("Solver got Stuck!");
					Utils.printArray("\nBoard: ", Utils.idArraytoIntArray(b.getCellIDs()), 9);
					Utils.printArray("\nSolution: ", Utils.idArraytoIntArray(solution), 9);
				}
				Generator gen;
				gen = new Generator(debug, app);
				gen.generate(app.debugScreen.getDepth());
				solution = gen.getSolution();
				this.b = gen.getBoard();
				originalBoard = b.getCellIDs();
				solver = new Solver(b);
				solver.updateCandidates(false);
				solverStepCount = 0;
			}
			if (Gdx.input.isKeyJustPressed(Keys.P)) {
				b.print();
				Utils.printArray("\nSolution: ", Utils.idArraytoIntArray(solution), 9);
			}
		} else if (stress) {
			if (!b.isComplete() && !solver.isStuck()) {
				solver.update(false);
				solverStepCount++;
				if (b.isComplete())
					app.log("Solved in %d steps", solverStepCount);
			} else {
				if (solver.isStuck()) {
					System.err.println("Solver is stuck!!");
					app.log("Solver got Stuck!");
					Utils.printArray("\nBoard: ", Utils.idArraytoIntArray(b.getCellIDs()), 9);
					Utils.printArray("\nSolution: ", Utils.idArraytoIntArray(solution), 9);
					stress = false;

				} else {
					Generator gen;
					gen = new Generator(debug, app);
					gen.generate(81);
					solution = gen.getSolution();
					this.b = gen.getBoard();
					originalBoard = b.getCellIDs();
					solver = new Solver(b);
					solver.updateCandidates(false);
					solverStepCount = 0;
				}
			}
		}

		// switch screens
		if ((menuPanel.getButton(1).clicked(Gdx.input.getX(), Gdx.input.getY()) || Gdx.input.isKeyJustPressed(Keys.P))
				&& !shift && !idle && !stress) {
			app.log("Entering Play Mode");
			app.setScreen(new PlaySudoku(app, new Board(originalBoard)));
		} else if ((menuPanel.getButton(1).clicked(Gdx.input.getX(), Gdx.input.getY())
				|| Gdx.input.isKeyJustPressed(Keys.P)) && shift && !idle && !stress) {
			app.log("Entering Play Mode");
			app.setScreen(new PlaySudoku(app, new Board(b.getBoard())));
		}
		/*
		 * // if (Gdx.input.isKeyJustPressed(Keys.G)) { // // app.setScreen(new
		 * GeneratedSudoku(app)); // Generator gen; // gen = new Generator(debug); //
		 * gen.generate(81); // solution = gen.getSolution(); // this.b =
		 * gen.getBoard(); // originalBoard = b.getCellIDs(); // solver = new Solver(b);
		 * // }
		 * 
		 * // Debug stuff // if (Gdx.input.isKeyJustPressed(Keys.D) && !shift) { //
		 * debug = !debug; // binary = false; // if (debug) { //
		 * app.debugScreen.reOpen(); // idle = false; // stress = false; // } // } // if
		 * (Gdx.input.isKeyJustPressed(Keys.D) && shift) { // if (debug) { // binary =
		 * !binary; // } else { // debug = true; // binary = true; //
		 * app.debugScreen.reOpen(); // idle = false; // stress = false; // } // }
		 */
		if (Gdx.input.isKeyJustPressed(Keys.I) | menuPanel.getButton(0).clicked(Gdx.input.getX(), Gdx.input.getY())) {
			idle = !idle;
			stress = false;
		}

		if (Gdx.input.isKeyJustPressed(Keys.C)) {
			for (int i = 0; i < b.getBoard().length; i++) {
				Cell c = b.getBoard()[i];
				if (c.getID() == solution[i]) {
					c.setColor(Color.GREEN);
				} else
					c.setColor(Color.RED);
			}
		}

		if (shift && Gdx.input.isKeyJustPressed(Keys.S)) {
			stress = !stress;
			idle = false;
		}

		// Check if shift is held down or not
		if (Gdx.input.isKeyJustPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyJustPressed(Keys.SHIFT_RIGHT)) {
			shift = true;
		} else if (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
			shift = false;
		}
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

}
