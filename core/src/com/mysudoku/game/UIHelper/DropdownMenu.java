package com.mysudoku.game.UIHelper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;

public class DropdownMenu {
	private float x, y, w, h, sx, sy;
	private final float ItemSize;
	private Window win;
	private Button[] menuButtons;
	private boolean isOpen = false;

	public DropdownMenu(Window win, float x, float y, float w, int menuLength, float itemSize, String[] names) {
		this.ItemSize = itemSize;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = menuLength * ItemSize;
		this.win = win;

		this.sx = x;
		this.sy = y;

		String[] buttonNames = new String[menuLength];
		if (names.length < menuLength) {
			for (int i = 0; i < names.length; i++) {
				buttonNames[i] = names[i];
			}
			for (int i = names.length; i < menuLength; i++) {
				buttonNames[i] = String.valueOf(i);
			}
		} else {
			for (int i = 0; i < menuLength; i++) {
				buttonNames[i] = names[i];
			}
		}

		if (win == null) {
			menuButtons = new Button[menuLength];
			for (int i = 0; i < menuButtons.length; i++) {
				menuButtons[i] = new Button(x, y + ItemSize * i, w, ItemSize, buttonNames[i], new Color(0x055da1ff));
			}
		} else {
			menuButtons = new Button[menuLength];
			for (int i = 0; i < menuButtons.length; i++) {
				menuButtons[i] = new Button(win, x, y - h + 2 + ItemSize * i, w, ItemSize, buttonNames[i], new Color(0x055da1ff));
			}
		}
	}

	public DropdownMenu(float x, float y, float w, int menuLength, float itemSize, String[] names) {
		this(null, x, y, w, menuLength, itemSize, names);
	}

	public void update() {
		if (win != null) {
			this.x = win.getX() + this.sx;
			this.y = win.getY() + this.sy;
		}
		float mouseX = Gdx.input.getX();
		float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
		if (mouseX > x && mouseX < x + w && mouseY > y && mouseY < y + 15 && Gdx.input.isButtonJustPressed(0)) {
			isOpen = !isOpen;
		}
	}

	public void shapeRenderer(ShapeRenderer r) {
		update();
		if (isOpen) {
			r.setColor(new Color(0x055da1ff));
			for (Button b : menuButtons) {
				b.render(r);
			}
		}
		r.setColor(new Color(0x257dc1ff));
		r.rect(x, y, w, 15);
	}
	
	public Button getButton(int id) {
		return menuButtons[id];
	}

	public void textRenderer(SpriteBatch batch, BitmapFont font) {
		font.draw(batch, "Menu", x + 2, y + 13);
		if (isOpen) {
			for (Button b : menuButtons) {
				b.showText(Color.WHITE, batch, font, Align.left);
			}
		}
	}
}
