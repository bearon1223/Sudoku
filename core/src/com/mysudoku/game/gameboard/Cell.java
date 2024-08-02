package com.mysudoku.game.gameboard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.mysudoku.game.Utils;

public class Cell {
	private int id = 0, row, col;
	private int candidates = 0;
	private Board b;
	private boolean colorSet = false, selected = false, selected2;
	private Color setColor = Color.BLACK;
	private boolean wasSet = false;

	public Cell(int id, Board b, int row, int col) {
		this.id = id; // binary integer representing 9 8 7 6 5 4 3 2 1
		this.b = b;
		this.row = row;
		this.col = col;
	}

	public int getID() {
		return id;
	}

	public int[] getLocation() {
		int[] loc = { row, col };
		return loc;
	}

	public int getCandidates() {
		if (id != 0)
			candidates = 0;
		return candidates;
	}

	public void findAllCandidates() {
		int others = 0b000000000;
		for (Cell c : b.getAllRelaventCells(this)) {
			others = others | c.getID();
		}
		this.candidates = getCandidates(others);
	}

	public int getCandidates(int others) {
		int allowable = 0b111111111; // 9 8 7 6 5 4 3 2 1
		return allowable & ~(others);
	}

	public void setColor(Color c) {
		colorSet = true;
		setColor = c;
	}

	public void setID(int id) {
		if (this.id == id)
			System.out.println("WARNING: id is already set to this value");
		this.id = id;
		if (id != 0)
			wasSet = true;
		else
			wasSet = false;
	}

	public void resetColor() {
		colorSet = false;
	}

	public void setCandidates(int newCandidate) {
		this.candidates = newCandidate;
	}

	public Color getColor() {
		Color c = new Color(Color.WHITE);
		if (id != 0 && !wasSet) {
			c = new Color(0.7f, 0.7f, 0.7f, 1f);
		} else if (id != 0) {
			c = Color.WHITE;
		}
		if (colorSet)
			c = setColor;
		if (selected) {
			c = new Color(0.8f, 0.8f, 1f, 1f);
		}
		if (selected2) {
			c = (Color.GOLD);
		}
		return c;
	}

	public void setSelected(boolean t) {
		selected = t;
	}

	public void setSelected2(boolean t) {
		selected2 = t;
	}

	public void clearCandidates() {
		candidates = 0;
	}

	public boolean wasSet() {
		return wasSet;
	}

	public void render(ShapeRenderer r, float size, float offsetX, float offsetY, boolean useSetColor) {
		r.setColor(Color.WHITE);
		if (id != 0 && !wasSet) {
			r.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
		} else if (id != 0) { // if it was set, and its id is no longer zero meaning it has a set value, keep
								// it white
			r.setColor(Color.WHITE);
		}
		if (colorSet && useSetColor)
			r.setColor(setColor);
		if (selected && !colorSet) {
			r.setColor(new Color(0.8f, 0.8f, 1f, 1f));
		}
		if (selected2) {
			r.setColor(Color.GOLD);
		}
		r.rect(col * size + offsetX + 2f, row * size + offsetY + 2f, size - 2f, size - 2f);
	}

	public void drawText(SpriteBatch batch, BitmapFont font, BitmapFont smallFont, float size, float offsetX,
			float offsetY, boolean debug, boolean binary, boolean showCandidates) {
		font.setColor(Color.BLACK);
		if (id == 0 && debug) {
			if (binary)
				font.draw(batch, Utils.paddedBinary(candidates, 9) + "\n987654321\n" + candidates, col * size + offsetX,
						row * size + offsetY + size / 1.6f, size, 1, true);
		} else if (id != 0) {
			String string = Utils.paddedBinary(id, 9) + "\n987654321\n" + id;
			if (!binary || !debug)
				string = String.valueOf(Utils.idToNumber(id).first());
			font.draw(batch, string, col * size + offsetX,
					row * size + offsetY + size / 2 + (binary ? smallFont.getCapHeight() / 2 * 2 : font.getCapHeight() / 2),
					size, 1, true);
		}

		if (!binary && showCandidates && id == 0) {
			float cOffset = 5;
			float cSize = size - cOffset * 2 - 2f;
			float wh = cSize / 3f; // width and height of the candidate text square

			// loop through and if there is a candidate there, draw it
			for (int i = 1; i <= 9; i++) {
				if (((candidates & 0b1 << (i - 1)) >> (i - 1)) == 1) {
					// depending on the value, put it into a 3x3 grid of numbers
					smallFont.draw(batch, String.valueOf(i), col * size + offsetX + (wh * ((i - 1) % 3)) + cOffset,
							row * size + offsetY + cOffset + 18 + (float) (wh * (2 - Math.floor((i - 1) / 3))), wh, 1,
							true);
				}
			}
		}
	}

	public void drawText(SpriteBatch batch, BitmapFont font, BitmapFont smallFont, float size, float offsetX,
			float offsetY, boolean debug, boolean binary) {
		drawText(batch, font, smallFont, size, offsetX, offsetY, debug, binary, debug);
	}

	public boolean isWithin(float x, float y, float sizeX, float sizeY, float offsetX, float offsetY) {
		return (x > col * sizeX + offsetX + 2f && x < col * sizeX + offsetX + sizeX - 2f
				&& y > row * sizeY + offsetY + 2f && y < row * sizeY + offsetY + sizeY - 2f);
	}

	public void debug() {
		int c1 = 0b000000100; // 3
		int c2 = 0b000000010; // 2
		int c3 = 0b010000000; // 8
		int c4 = 0b000100000; // 6
		int c5 = 0b001000000; // 7
		int c6 = 0b100000000; // 9
		int c7 = 0b010000000; // 8
		int c8 = 0b000000100; // 3
		int c9 = 0;
		int others = c1 | c2 | c3 | c4 | c5 | c6 | c7 | c8 | c9;
		System.out.println(Integer.toBinaryString(others));
		int candidates = getCandidates(others);
		System.out.println(Integer.toBinaryString(candidates));
		Array<Integer> num = Utils.idToNumber(candidates);
		System.out.printf("Length of Candidates: %d, Candidates (Should be 1, 4, 5): %s", num.size,
				String.valueOf(num));
	}
}
