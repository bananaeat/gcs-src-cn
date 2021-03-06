/*
 * Copyright ©1998-2022 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.ui.widget.outline;

import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.Fonts;
import com.trollworks.gcs.ui.TextDrawing;
import com.trollworks.gcs.ui.ThemeFont;
import com.trollworks.gcs.ui.scale.Scale;
import com.trollworks.gcs.utility.text.NumericComparator;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.SwingConstants;

/** Represents text cells in an {@link Outline}. */
public class TextCell implements Cell {
    /** The standard horizontal margin. */
    public static final int     H_MARGIN = 2;
    private             int     mHAlignment;
    private             boolean mWrapped;

    /** Create a new text cell. */
    public TextCell() {
        this(SwingConstants.LEFT);
    }

    /**
     * Create a new text cell.
     *
     * @param alignment The horizontal text alignment to use.
     */
    public TextCell(int alignment) {
        this(alignment, false);
    }

    /**
     * Create a new text cell.
     *
     * @param hAlignment The horizontal text alignment to use.
     * @param wrapped    Pass in {@code true} to enable wrapping.
     */
    public TextCell(int hAlignment, boolean wrapped) {
        mHAlignment = hAlignment;
        mWrapped = wrapped;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Column column, Row one, Row two) {
        Object oneObj = one.getData(column);
        Object twoObj = two.getData(column);
        if (!(oneObj instanceof String) && oneObj.getClass() == twoObj.getClass() && oneObj instanceof Comparable<?>) {
            return ((Comparable<Object>) oneObj).compareTo(twoObj);
        }
        return NumericComparator.caselessCompareStrings(one.getDataAsText(column), two.getDataAsText(column));
    }

    /**
     * @param outline  The outline.
     * @param row      The row.
     * @param column   The column.
     * @param selected Whether or not the selected version of the color is needed.
     * @param active   Whether or not the active version of the color is needed.
     * @return The foreground color.
     */
    public Color getColor(Outline outline, Row row, Column column, boolean selected, boolean active) {
        if (selected) {
            return active ? Colors.ON_SELECTION : Colors.ON_INACTIVE_SELECTION;
        }
        return outline.getForeground();
    }

    @Override
    public int getPreferredWidth(Outline outline, Row row, Column column) {
        Scale   scale         = Scale.get(outline);
        int     scaledHMargin = scale.scale(H_MARGIN);
        boolean wrapped       = mWrapped;
        mWrapped = false;
        String text = getPresentationText(outline, row, column);
        mWrapped = wrapped;
        int  width = TextDrawing.getPreferredSize(scale.scale(getFont(row, column)), text).width;
        Icon icon  = getIcon(row, column);
        if (icon != null) {
            width += scale.scale(icon.getIconWidth()) + scaledHMargin;
        }
        return scaledHMargin + width + scaledHMargin;
    }

    @Override
    public int getPreferredHeight(Outline outline, Row row, Column column) {
        Scale scale     = Scale.get(outline);
        Font  font      = scale.scale(getFont(row, column));
        int   minHeight = TextDrawing.getPreferredSize(font, "Mg").height;
        int   height    = TextDrawing.getPreferredSize(font, getPresentationText(outline, row, column)).height;
        return Math.max(minHeight, height);
    }

    public Icon getIcon(Row row, Column column) {
        return row == null ? null : row.getIcon(column);
    }

    @Override
    public void drawCell(Outline outline, Graphics2D gc, Rectangle bounds, Row row, Column column, boolean selected, boolean active) {
        Scale           scale         = Scale.get(outline);
        Font            font          = scale.scale(getFont(row, column));
        int             ascent        = gc.getFontMetrics(font).getAscent();
        StringTokenizer tokenizer     = new StringTokenizer(getPresentationText(outline, row, column), "\n", true);
        int             totalHeight   = getPreferredHeight(outline, row, column);
        int             lineHeight    = TextDrawing.getPreferredSize(font, "Mg").height;
        int             lineCount     = 0;
        Icon            icon          = getIcon(row, column);
        int             scaledHMargin = scale.scale(H_MARGIN);
        int             left          = icon == null ? 0 : scale.scale(icon.getIconWidth()) + scaledHMargin;
        int             cellWidth     = bounds.width - (scaledHMargin + left + scaledHMargin);
        int             vAlignment    = getVAlignment();
        int             hAlignment    = getHAlignment();
        Color           color         = getColor(outline, row, column, selected, active);

        left += bounds.x + scaledHMargin;

        if (icon != null) {
            int iy = bounds.y;
            if (vAlignment != SwingConstants.TOP) {
                int ivDelta = bounds.height - scale.scale(icon.getIconHeight());
                if (vAlignment == SwingConstants.CENTER) {
                    ivDelta /= 2;
                }
                iy += ivDelta;
            }
            gc.setColor(selected ? color : Colors.ACCENT);
            icon.paintIcon(outline, gc, bounds.x + scaledHMargin, iy);
        }

        gc.setColor(color);
        gc.setFont(font);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if ("\n".equals(token)) {
                lineCount++;
            } else {
                String text = TextDrawing.truncateIfNecessary(font, token, cellWidth, getTruncationPolicy());
                int    x    = left;
                int    y    = bounds.y + ascent + lineHeight * lineCount;
                if (hAlignment != SwingConstants.LEFT) {
                    int hDelta = cellWidth - TextDrawing.getWidth(font, text);
                    if (hAlignment == SwingConstants.CENTER) {
                        hDelta /= 2;
                    }
                    x += hDelta;
                }
                if (vAlignment != SwingConstants.TOP) {
                    float vDelta = bounds.height - totalHeight;
                    if (vAlignment == SwingConstants.CENTER) {
                        vDelta /= 2;
                    }
                    y += (int) vDelta;
                }
                gc.drawString(text, x, y);
            }
        }
    }

    /**
     * @param outline The outline being used.
     * @param row     The row.
     * @param column  The column.
     * @return The data of this cell as a string that is prepared for display.
     */
    protected String getPresentationText(Outline outline, Row row, Column column) {
        String text = getData(row, column);
        if (!mWrapped || row == null) {
            return text;
        }
        int width = column.getWidth();
        if (width == -1) {
            return text;
        }
        Scale scale         = Scale.get(outline);
        int   scaledHMargin = scale.scale(H_MARGIN);
        return TextDrawing.wrapToPixelWidth(scale.scale(getFont(row, column)), text, width - (scaledHMargin + scale.scale(row.getOwner().getIndentWidthWithDisclosure(row, column)) + scaledHMargin));
    }

    @Override
    public Cursor getCursor(MouseEvent event, Rectangle bounds, Row row, Column column) {
        return Cursor.getDefaultCursor();
    }

    /** @return The truncation policy. */
    public int getTruncationPolicy() {
        return SwingConstants.CENTER;
    }

    /**
     * @param row    The row.
     * @param column The column.
     * @return The data of this cell as a string.
     */
    protected static String getData(Row row, Column column) {
        if (row != null) {
            String text = row.getDataAsText(column);
            return text == null ? "" : text;
        }
        return column.toString();
    }

    /**
     * @param row    The row.
     * @param column The column.
     * @return The font.
     */
    public ThemeFont getThemeFont(Row row, Column column) {
        return Fonts.FIELD_PRIMARY;
    }

    /**
     * @param row    The row.
     * @param column The column.
     * @return The font.
     */
    public final Font getFont(Row row, Column column) {
        return deriveFont(row, column, getThemeFont(row, column).getFont());
    }

    /**
     * Gets called to allow the font to be derived from a ThemeFont. This was done so that the
     * typical usage of calling getFont() can't be accidentally overriden.
     *
     * @param row    The row.
     * @param column The column.
     * @param font   The font from the theme.
     * @return The font.
     */
    protected Font deriveFont(Row row, Column column, Font font) {
        return font;
    }

    /** @return The horizontal alignment. */
    public int getHAlignment() {
        return mHAlignment;
    }

    /** @param alignment The horizontal alignment. */
    public void setHAlignment(int alignment) {
        mHAlignment = alignment;
    }

    /** @return The vertical alignment. */
    public int getVAlignment() {
        return SwingConstants.TOP;
    }

    @Override
    public String getToolTipText(Outline outline, MouseEvent event, Rectangle bounds, Row row, Column column) {
        String text = row.getToolTip(column);
        if (text == null || text.isBlank()) {
            return null;
        }
        return text;
    }

    @Override
    public boolean participatesInDynamicRowLayout() {
        return mWrapped;
    }

    @Override
    public void mouseClicked(MouseEvent event, Rectangle bounds, Row row, Column column) {
        // Does nothing
    }
}
