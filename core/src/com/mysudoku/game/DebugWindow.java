package com.mysudoku.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mysudoku.game.UIHelper.Button;
import com.mysudoku.game.UIHelper.Slider;
import com.mysudoku.game.UIHelper.Window;
import com.mysudoku.game.generator.Generator;

public class DebugWindow extends Window {
    private Button generateButton, solveButton, candidatesButton, updateCandidatesButton;
    private Slider s;
    private int depth = 81, generatedDepth = 81, maxDepth = 81, tryCount = 0, total = 0;
    private long timeTaken = 0;
    private float avgTryCount, avgMaxDepth, avgTimeTaken;
    private boolean extra = false;
    private String log = "";
    private int logOffset = 0;
    private long holdFrame = 0;
    private boolean generating = false;
    Generator gen;

    public DebugWindow(Sudoku app) {
        super(app, 800, 10, 350, 400, "Debug Screen");
        generateButton = new Button(this, 205, 5, 140, 50, "Generate");
        solveButton = new Button(this, 205, 60, 140, 50, "Update");
        updateCandidatesButton = new Button(this, 205, 115, 140, 50, "Candidates Only");
        candidatesButton = new Button(this, 205, 170, 140, 50, "Candidates");
        s = new Slider(this, 205, 230, 140, 10, 0, 81, 81, "");
        gen = new Generator(true, app);
    }

    @Override
    protected void render() {
        if (!shift) {
            if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                logOffset++;
                holdFrame = Gdx.graphics.getFrameId();
            } else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
                logOffset--;
                holdFrame = Gdx.graphics.getFrameId();
            }

            if (Gdx.graphics.getFrameId() - holdFrame > 30) {
                if (Gdx.input.isKeyPressed(Keys.UP)) {
                    logOffset++;
                } else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
                    logOffset--;
                }
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Keys.UP)) {
                logOffset = app.getLogSize();
            } else if (Gdx.input.isKeyJustPressed(Keys.DOWN)) {
                logOffset = 0;
            }
        }
        if (logOffset < 0)
            logOffset = 0;
        if (logOffset > app.getLogSize() - 16)
            logOffset = app.getLogSize() - 16;

        text(String.format("FPS: %d", Gdx.graphics.getFramesPerSecond()), 10, getHeight() - 10f, 100);
        text(String.format("Depth: %d", depth), 195, 255, 100);

        if (total != 0 && extra) {
            text(String.format(
                    "Generator Info:\n  Cells Removed: %d\n       avg: %3.2f\n  Tries: %d\n       avg: %3.2f\n  Time Taken : %d ms\n       avg: %3.2f ms",
                    generatedDepth - maxDepth, (avgMaxDepth / total), tryCount, (avgTryCount / total),
                    timeTaken / 1000 / 1000, (avgTimeTaken / total)), 205, getHeight() - 10, 180);
        } else {
            text(String.format(
                    "Generator Info:\n  Cells Removed: %d\n  Tries: %d\n  Time Taken : %d ms",
                    generatedDepth - maxDepth, tryCount, timeTaken / 1000 / 1000),
                    205, getHeight() - 10, 180);
        }

        text("Press \"A\" for averages\nShift Click Candidates for Binary View.\nShift + S will conduct a stress test", 10, getHeight() - 35, 205);
        text("Log:", 10, 280, 180);
        text(log, 12, 260, 205);

        depth = (int) s.getValue();
        if (Gdx.input.isKeyJustPressed(Keys.A))
            extra = !extra;
    }

    public void update() {
        Sudoku s = app;
        if (!isOpen())
            return;
        if (generateButton.clicked(Gdx.input.getX(), Gdx.input.getY()) && !generating) {
            generatedDepth = depth;
            gen.generateAsync(generatedDepth);
            generating = true;
        }

        // When the asynchronous generation is done, update everything
        if (gen.isBoardReady() && generating) {
            s.setScreen(new AutoSolveSudoku(s, gen.getBoard(), gen.getSolution()));
            gen = new Generator(true, s);
            generating = false;
            updateAverageData(s.maxDepth, s.tryCount, s.timeTaken);
        }

        s.stepSolver = solveButton.clicked(Gdx.input.getX(), Gdx.input.getY());

        if (!shift && candidatesButton.clicked(Gdx.input.getX(), Gdx.input.getY())) {
            s.showCandidates = !s.showCandidates;
            s.showBinary = false;
        } else if (candidatesButton.clicked(Gdx.input.getX(), Gdx.input.getY())) {
            if (s.showCandidates) {
                s.showBinary = !s.showBinary;
            } else {
                s.showCandidates = true;
                s.showBinary = true;
            }
        }
        log = s.getLog(logOffset);
    }

    public boolean getStepCandidates() {
        return updateCandidatesButton.clicked(Gdx.input.getX(), Gdx.input.getY());
    }

    public int getDepth() {
        generatedDepth = depth;
        return depth;
    }

    public void updateAverageData(int maxDepth, int tryCount, long timeTaken) {
        this.maxDepth = maxDepth;
        this.tryCount = tryCount;
        this.timeTaken = timeTaken;
        avgMaxDepth += generatedDepth - maxDepth;
        avgTryCount += tryCount;
        avgTimeTaken += Math.round(timeTaken / 1000 / 1000);
        total++;
    }

    @Override
    protected void renderUIElements(ShapeRenderer r) {
        generateButton.render(r);
        solveButton.render(r);
        candidatesButton.render(r);
        updateCandidatesButton.render(r);
        s.render(r);
    }

    @Override
    protected void renderTextUIElements(SpriteBatch batch, BitmapFont font) {
        generateButton.showText(batch, font);
        solveButton.showText(batch, font);
        candidatesButton.showText(batch, font);
        updateCandidatesButton.showText(batch, font);
    }

    public void shutdown() {
        gen.shutdown();
    }
}