package com.mysudoku.game.UIHelper;

import static com.badlogic.gdx.math.MathUtils.clamp;
import static com.badlogic.gdx.math.MathUtils.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Slider {
    private float x, y, w, h, sx, sy;
    private float value, upperLimit, lowerLimit, sliderWidth, sliderHeight;
    private boolean isSelected = false;
    private String t;
    private Window window;

    public Slider(Window window, float x, float y, float w, float h, float lowerLimit, float upperLimit,
            float defaultValue, String t) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        this.sx = x;
        this.sy = y;

        this.sliderWidth = 10;
        this.sliderHeight = 10;

        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;

        this.t = t;

        this.value = clamp(defaultValue, lowerLimit, upperLimit) / (upperLimit);
        this.window = window;
        // System.out.println(this.value);
    }

    public Slider(float x, float y, float w, float h, float lowerLimit, float upperLimit, float defaultValue,
            String t) {
        this(null, x, y, w, h, lowerLimit, upperLimit, defaultValue, t);
    }

    public Slider(float x, float y, float w, float h) {
        this(x, y, w, h, 0, 100, 0, "Default Text");
    }

    public Slider(float x, float y, float w, float h, float lowerLimit, float upperLimit) {
        this(x, y, w, h, lowerLimit, upperLimit, upperLimit, "Default Text");
    }

    public void updateData(float x, float y) {
        // Update the x, y, w, and h values to account for moving windows if it is in a
        // moving window
        this.x = this.sx + x;
        this.y = this.sy + y;
    }

    public boolean clicked(float mouseX, float mouseY) {
        boolean mousePressed = Gdx.input.isButtonPressed(0);
        // Check if the mouse position is inside of the button and the mouse is just
        // pressed.
        return mouseOver(mouseX, mouseY) && mousePressed;
    }

    public boolean mouseOver(float mouseX, float mouseY) {
        // Check if the mouse position is inside of the button.
        return mouseX > x + value * (w - sliderWidth) && mouseX < x + value * (w) + sliderWidth &&
                Gdx.graphics.getHeight() - mouseY > y - sliderHeight / 2
                && Gdx.graphics.getHeight() - mouseY < y + h + sliderHeight;
    }

    public float getValue() {
        return clamp(value * (upperLimit - lowerLimit), lowerLimit, upperLimit);
    }

    public void renderText(SpriteBatch batch, BitmapFont font) {
        font.setColor(Color.BLACK);
        font.draw(batch, t + ": " + String.valueOf(Math.round(getValue())), x + w * 1.2f, y + h);
    }

    public void render(ShapeRenderer renderer) {
        if (window != null)
            updateData(window.getX(), window.getY());
        renderer.setColor(Color.WHITE);
        renderer.rect(x, y, w, h);
        renderer.setColor(Color.LIGHT_GRAY);

        float mouseX = Gdx.input.getX(), mouseY = Gdx.input.getY();
        if (mouseOver(mouseX, mouseY) || isSelected)
            renderer.setColor(Color.GRAY);

        if (clicked(mouseX, mouseY) && !isSelected)
            isSelected = true;
        else if (!Gdx.input.isButtonPressed(0) && isSelected)
            isSelected = false;

        if (isSelected) {
            value = map(x, x + w - sliderWidth, 0, 1, clamp(mouseX - sliderWidth / 2, x, x + w - sliderWidth));
            // System.out.printf("Value: %f\n", value);
        }

        renderer.rect(x + value * (w - sliderHeight), y - sliderHeight / 2, sliderWidth, h + sliderHeight);
    }
}
