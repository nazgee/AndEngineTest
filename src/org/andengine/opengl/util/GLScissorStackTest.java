package org.andengine.opengl.util;

import junit.framework.TestCase;

import org.andengine.util.AssertUtils;

/**
 * (c) 2013 Zynga Inc.
 *
 * @author Michal Stawinski <michal.stawinski@gmail.com>
 * @since 11:00:11 - 07.01.2013
 */
public class GLScissorStackTest extends TestCase {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	GLScissorStack mGLScissorStack;
	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.mGLScissorStack = new GLScissorStack();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	public void testPushPop() {
		int pushed[] = buildRect(50, 50, 100, 100);
		int expected[] = buildRect(50, 50, 100, 100);

		pushScissor(pushed);
		assertScissor(expected);
		popScissor();
		assertScissorDisabled();
	}

	/**
	 * Both clip rectangles the same
	 */
	public void testPushPushPopPopSame() {
		runPushPushPopPop(/*1st*/ 25, 25, 50, 50, /*2nd*/ 25, 25, 50, 50);
	}

	/**
	 * Completely contained one into another (centered around (50, 50))
	 */
	public void testPushPushPopPopCenteredSelfContained() {
		runPushPushPopPop(/*1st*/ 25, 25, 50, 50, /*2nd*/ 40, 40, 20, 20);
	}

	/**
	 * Bigger one (in both dimensions) squeezed into smaller one (centered around (50, 50))
	 */
	public void testPushPushPopPopCenteredShrinkXY() {
		runPushPushPopPop(/*1st*/ 25, 25, 50, 50, /*2nd*/ 0, 0, 100, 100);
	}

	/**
	 * Bigger one (in x dimension) squeezed into smaller one (centered around (50, 50))
	 */
	public void testPushPushPopPopCenteredShrinkX() {
		runPushPushPopPop(/*1st*/ 25, 25, 50, 50, /*2nd*/ 0, 25, 100, 50);
	}

	/**
	 * Bigger one (in y dimension) squeezed into smaller one (centered around (50, 50))
	 */
	public void testPushPushPopPopCenteredShrinkY() {
		runPushPushPopPop(/*1st*/ 25, 25, 50, 50, /*2nd*/ 25, 0, 50, 100);
	}

	/**
	 * Same size rectangles intersecting to get 25% of area; 1st centered (50, 50), 2nd (25, 25)
	 */
	public void testPushPushPopPopAcentricShrinkXY() {
		runPushPushPopPop(/*1st*/ 25, 25, 50, 50, /*2nd*/ 0, 0, 50, 50);
	}

	private void runPushPushPopPop(int pX1, int pY1, int pWidth1, int pHeight1, int pX2, int pY2, int pWidth2, int pHeight2) {
		int pushed1[] = buildRect(pX1, pY1, pWidth1, pHeight1);
		int pushed2[] = buildRect(pX2, pY2, pWidth2, pHeight2);
		int expected2[] = buildIntersection(pushed1, pushed2);
		int expected1[] = buildIntersection(pushed1, pushed1);

		pushScissor(pushed1);
		assertScissor(pushed1);
		pushScissor(pushed2);
		assertScissor(expected2);
		popScissor();
		assertScissor(expected1);
		popScissor();
		assertScissorDisabled();
	}

	private int[] pushScissor(int[] pRect) {
		this.mGLScissorStack.glPushScissor(pRect[GLScissorStack.GLSCISSOR_X_INDEX], pRect[GLScissorStack.GLSCISSOR_Y_INDEX], pRect[GLScissorStack.GLSCISSOR_WIDTH_INDEX], pRect[GLScissorStack.GLSCISSOR_HEIGHT_INDEX]);
		return pRect;
	}

	private void popScissor() {
		this.mGLScissorStack.glPopScissor();
	}

	/**
	 * XXX Expected behavior has to be decided yet. So far it does not matter (ClipEntity calls pGLState.setScissorTestEnabled(false) when we reach stack bottom)
	 */
	private void assertScissorDisabled() {
		/*
		 * We want clipping disabled at this point. Not sure what we should expect here. (-inf, -inf, +inf, +inf) seems most appropriate but
		 * we have not settled it down yet.
		 */
	}

	private int[] assertScissor(int[] pExpected) {
		int readback[] = new int[GLScissorStack.GLSCISSOR_SIZE];
		this.mGLScissorStack.getScissor(readback);
		AssertUtils.assertArrayEquals(readback, pExpected);
		return readback;
	}

	private int[] buildIntersection(int[] pRect1, int[] pRect2) {
		int intersected[] = new int[GLScissorStack.GLSCISSOR_SIZE];

		final int x1 = pRect1[GLScissorStack.GLSCISSOR_X_INDEX];
		final int y1 = pRect1[GLScissorStack.GLSCISSOR_Y_INDEX];
		final int w1 = pRect1[GLScissorStack.GLSCISSOR_WIDTH_INDEX];
		final int h1 = pRect1[GLScissorStack.GLSCISSOR_HEIGHT_INDEX];

		final int x2 = pRect2[GLScissorStack.GLSCISSOR_X_INDEX];
		final int y2 = pRect2[GLScissorStack.GLSCISSOR_Y_INDEX];
		final int w2 = pRect2[GLScissorStack.GLSCISSOR_WIDTH_INDEX];
		final int h2 = pRect2[GLScissorStack.GLSCISSOR_HEIGHT_INDEX];

		final int xMin = Math.max(x1, x2);
		final int xMax = Math.min(x1 + w1, x2 + w2);

		final int yMin = Math.max(y1, y2);
		final int yMax = Math.min(y1 + h1, y2 + h2);

		if (xMax > xMin) {
			intersected[GLScissorStack.GLSCISSOR_X_INDEX] = xMin;
			intersected[GLScissorStack.GLSCISSOR_WIDTH_INDEX] = xMax - xMin;
		} else {
			intersected[GLScissorStack.GLSCISSOR_X_INDEX] = pRect1[GLScissorStack.GLSCISSOR_X_INDEX];
			intersected[GLScissorStack.GLSCISSOR_WIDTH_INDEX] = 0;
		}

		if (yMax > yMin) {
			intersected[GLScissorStack.GLSCISSOR_Y_INDEX] = yMin;
			intersected[GLScissorStack.GLSCISSOR_HEIGHT_INDEX] = yMax - yMin;
		} else {
			intersected[GLScissorStack.GLSCISSOR_Y_INDEX] = pRect1[GLScissorStack.GLSCISSOR_Y_INDEX];
			intersected[GLScissorStack.GLSCISSOR_HEIGHT_INDEX] = 0;
		}

		return intersected;
	}

	private int[] buildRect(int pX, int pY, int pWidth, int pHeight) {
		int rect[] = new int[4];

		rect[GLScissorStack.GLSCISSOR_X_INDEX] = pX;
		rect[GLScissorStack.GLSCISSOR_Y_INDEX] = pY;
		rect[GLScissorStack.GLSCISSOR_WIDTH_INDEX] = pWidth;
		rect[GLScissorStack.GLSCISSOR_HEIGHT_INDEX] = pHeight;

		return rect;
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
