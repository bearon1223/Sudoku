package com.mysudoku.game.UIHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mysudoku.game.Sudoku;

public abstract class Window {
	private float x, y, w, h;
	private String windowName;
	private Array<RectHolder> rectHolder = new Array<>();
	private Array<TextHolder> textHolder = new Array<>();
	private Array<TextureHolder> textureHolder = new Array<>();
	private Vector2 distanceToCenter;
	private boolean isMoving = false;
	private boolean isVisible = true, isHidden = false;
	private boolean isSelected = false;
	protected boolean shift = false;
	protected final Sudoku app;
	protected float mouseX, mouseY;

	public Window(Sudoku app, float x, float y, float w, float h, String windowName) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.windowName = windowName;
		this.app = app;
		this.app.log("Window: %s Setup", windowName);
	}

	public Window(Sudoku app, float x, float y, float w, float h) {
		this(app, x, y, w, h, null);
	}

	protected void rect(Color c, float x, float y, float w, float h) {
		rectHolder.add(new RectHolder(x, y, w, h, c));
	}

	protected void rect(float x, float y, float w, float h) {
		rectHolder.add(new RectHolder(x, y, w, h, Color.WHITE));
	}

	protected void text(String t, float x, float y, float w) {
		textHolder.add(new TextHolder(x, y, w, t));
	}

	protected void texture(Texture t, float x, float y, float w, float h) {
		textureHolder.add(new TextureHolder(x, y, w, h, t));
	}

	public void reOpen() {
		isVisible = true;
		isHidden = false;
	}

	public void close() {
		isVisible = false;
	}

	public boolean isOpen() {
		return isVisible;
	}

	/**
	 * Render code that is called by the Shape Renderer
	 */
	protected abstract void render();

	/**
	 * Buttons, Dropdown Menus, Sliders, all UIElements are ran here
	 * 
	 * @param r
	 */
	protected abstract void renderUIElements(ShapeRenderer r);

	/**
	 * Buttons, Dropdown Menus, Sliders, all UIElements are ran here
	 * 
	 * @param batch
	 * @param font
	 */
	protected abstract void renderTextUIElements(SpriteBatch batch, BitmapFont font);

	/**
	 * Checks if the window should be moving, is hidden, or is closed.
	 */
	private void update() {
		// Get mouse location and transform the y position to the coordinate space
		// (you see how simple it is this sentence is kind of a joke)
		mouseX = Gdx.input.getX();
		mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

		// Check if it should be moving and calculate its offset from center
		if (Gdx.input.isButtonJustPressed(0)
				&& (mouseX > x && mouseX < x + w - 20 && mouseY > y + h && mouseY < y + h + 20)) {
			distanceToCenter = new Vector2(x + w / 2 - mouseX, y + h + 10 - mouseY);
			isMoving = true;
		}

		if (Gdx.input.isButtonJustPressed(1)
				&& (mouseX > x && mouseX < x + w - 20 && mouseY > y + h && mouseY < y + h + 20)) {
			isHidden = !isHidden;
		}

		if (Gdx.input.isButtonPressed(0) && (isMoving)) {
			x = -w / 2 + mouseX + distanceToCenter.x;
			y = -h - 10 + mouseY + distanceToCenter.y;
		} else {
			isMoving = false;
		}

		if (x < -w / 2) {
			x = -w / 2;
		} else if (x > Gdx.graphics.getWidth() - w / 2) {
			x = Gdx.graphics.getWidth() - w / 2;
		}

		if (y < -h) {
			y = -h;
		} else if (y > Gdx.graphics.getHeight() - h - 20) {
			y = Gdx.graphics.getHeight() - h - 20;
		}

		if (Gdx.input.isButtonJustPressed(0)
				&& (mouseX > x + w - 20 && mouseX < x + w && mouseY > y + h && mouseY < y + h + 20)) {
			isVisible = false;
			System.out.printf("Closing %s\n", windowName);
		}

		if (!isHidden) {
			if (Gdx.input.isButtonJustPressed(0) && !isSelected
					&& (mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h + 20) && isVisible) {
				isSelected = true;
			}
			if (Gdx.input.isButtonJustPressed(0) && isSelected && !isMoving && isVisible
					&& !(mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + h + 20)) {
				isSelected = false;
			}
		} else {
			if (Gdx.input.isButtonJustPressed(0) && !isSelected
					&& (mouseX > x && mouseX < x + w && mouseY > y + h && mouseY < y + h + 20) && isVisible) {
				isSelected = true;
			}
			if (Gdx.input.isButtonJustPressed(0) && isSelected && !isMoving && isVisible
					&& !(mouseX > x && mouseX < x + w && mouseY > y + h && mouseY < y + h + 20)) {
				isSelected = false;
			}
		}
		if (Gdx.input.isKeyJustPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyJustPressed(Keys.SHIFT_RIGHT)) {
			shift = true;
		} else if (!Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) && !Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT)) {
			shift = false;
		}

		mouseY = Gdx.input.getY();
	}

	public void shapeRenderer(ShapeRenderer renderer) {
		if (windowName != null) {
			if (!isVisible)
				return;
			update();
			// draw window, title bar, and close button
			renderer.setColor(new Color(0x055da1ff));
			renderer.rect(x, y + h, w, 20);
			renderer.setColor(new Color(0x257dc1ff));
			renderer.rect(x + w - 20, y + h, 20, 20);
			if (isHidden)
				return;
			renderer.setColor(new Color(0x1a1a1aff));
			renderer.rect(x, y, w, h);
		}
		// call render function and draw whatever was placed in the rectHolder array
		render();
		for (RectHolder r : rectHolder) {
			float x = r.x, y = r.y, w = r.w, h = r.h;
			x += this.x;
			y += this.y;
			renderer.setColor(r.c);
			renderer.rect(x, y, w, h);
		}
		rectHolder.clear();
		renderUIElements(renderer);
	}

	public void textRenderer(SpriteBatch batch, BitmapFont font) {
		if (!isVisible)
			return;
		if (windowName != null) {
			font.setColor(Color.WHITE);
			font.draw(batch, windowName, x + 5, y + h + 13, w - 25, Align.left, true);
		}
		if (isHidden)
			return;
		for (TextureHolder t : textureHolder) {
			batch.draw(t.c, t.x, t.y, t.w, t.h);
		}
		for (TextHolder t : textHolder) {
			font.draw(batch, t.t, t.x + x, t.y + y, t.w, Align.left, true);
		}
		textHolder.clear();
		renderTextUIElements(batch, font);

	}

	/**
	 * @return window x location
	 */
	public float getX() {
		return x;
	}

	/**
	 * @return window y location
	 */
	public float getY() {
		return y;
	}

	/**
	 * @return window width
	 */
	public float getWidth() {
		return w;
	}

	/**
	 * @return window height
	 */
	public float getHeight() {
		return h;
	}

	/**
	 * Sets the X location
	 * 
	 * @param x
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * Sets the Y Location
	 * 
	 * @param y
	 */
	public void setY(float y) {
		this.y = y;
	}

	public boolean isSelected() {
		return isSelected;
	}

	// Holder classes to hold values to draw in the renderers.
	private class RectHolder {
		public float x, y, w, h;
		public Color c;

		public RectHolder(float x, float y, float w, float h, Color c) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.c = c;
		}
	}

	private class TextHolder {
		public float x, y, w;
		public String t;

		public TextHolder(float x, float y, float w, String t) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.t = t;
		}
	}

	private class TextureHolder {
		public float x, y, w, h;
		public Texture c;

		public TextureHolder(float x, float y, float w, float h, Texture c) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.c = c;
		}
	}
}