package com.mysudoku.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;

public class Sudoku extends Game {
	SpriteBatch batch;
	ShapeRenderer renderer;
	BitmapFont font, NumberFont;
	BitmapFont fontD;
	DebugWindow debugScreen;
	boolean stepSolver = false, showCandidates = false, showBinary = false;

	public int maxDepth = 81;
	public int tryCount = 0;
	public long timeTaken = 0;
	public boolean generating = false;
	public Array<String> log = new Array<>();

	@Override
	public void create() {
		batch = new SpriteBatch();
		renderer = new ShapeRenderer();

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Calibri Bold.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 24;
		font = generator.generateFont(parameter);

		generator = new FreeTypeFontGenerator(Gdx.files.internal("impact.ttf"));
		parameter.size = 40;
		NumberFont = generator.generateFont(parameter);

		generator = new FreeTypeFontGenerator(Gdx.files.internal("calibri.ttf"));
		parameter.size = 14;
		fontD = generator.generateFont(parameter);
		debugScreen = new DebugWindow(this);
		debugScreen.close();

		log.add("Sudoku Startup Complete");

		setScreen(new AutoSolveSudoku(this));
	}

	@Override
	public void render() {
		super.render();

		if (debugScreen.isOpen()) {
			renderer.begin(ShapeType.Filled);
			debugScreen.shapeRenderer(renderer);
			renderer.end();

			batch.begin();
			debugScreen.textRenderer(batch, fontD);
			batch.end();

			debugScreen.update();
		} else {
			stepSolver = false;
		}

		if (Gdx.input.isKeyJustPressed(Keys.D)) {
			debugScreen.reOpen();
		}
		if (Gdx.input.isKeyJustPressed(Keys.C) && Gdx.input.isKeyPressed(Keys.SHIFT_LEFT)) {
			this.log.clear();
			log("Log Cleared");
		}
		generating = false;
	}

	public void log(String s, Object... args) {
		log.add(String.format(s, args));
	}

	public String getLog() {
		String log = "";
		for (int i = this.log.size - 15; i < this.log.size; i++) {
			if (i < 0 | i >= this.log.size)
				continue;
			log += "\n" + this.log.get(i);
		}
		return log;
	}

	public int getLogSize() {
		return this.log.size;
	}

	public String getLog(int offset) {
		String log = "";
		for (int i = this.log.size - 15 - offset; i < this.log.size - offset; i++) {
			if (i < 0 | i >= this.log.size)
				continue;
			log += this.log.get(i) + "\n";
		}
		return log;
	}

	@Override
	public void dispose() {
		batch.dispose();
		renderer.dispose();
		font.dispose();
		fontD.dispose();
		NumberFont.dispose();
		debugScreen.shutdown();
	}
}
