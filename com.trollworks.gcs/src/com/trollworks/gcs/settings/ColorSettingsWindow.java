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

package com.trollworks.gcs.settings;

import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.FontAwesome;
import com.trollworks.gcs.ui.ThemeColor;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.ColorWell;
import com.trollworks.gcs.ui.widget.FontIconButton;
import com.trollworks.gcs.ui.widget.Label;
import com.trollworks.gcs.ui.widget.LayoutConstants;
import com.trollworks.gcs.ui.widget.Panel;
import com.trollworks.gcs.ui.widget.Toolbar;
import com.trollworks.gcs.ui.widget.WindowUtils;
import com.trollworks.gcs.utility.Dirs;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.I18n;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** A window for editing color settings. */
public final class ColorSettingsWindow extends SettingsWindow<Colors> {
    private static ColorSettingsWindow INSTANCE;

    private FontIconButton     mDarkModeButton;
    private List<ColorTracker> mColorWells;
    private boolean            mIgnore;

    /** Displays the color settings window. */
    public static void display() {
        if (!UIUtilities.inModalState()) {
            ColorSettingsWindow wnd;
            synchronized (ColorSettingsWindow.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ColorSettingsWindow();
                }
                wnd = INSTANCE;
            }
            wnd.setVisible(true);
        }
    }

    public ColorSettingsWindow() {
        super(I18n.text("颜色设置"));
        fill();
    }

    @Override
    protected void preDispose() {
        synchronized (ColorSettingsWindow.class) {
            INSTANCE = null;
        }
    }

    @Override
    protected void addToToolBar(Toolbar toolbar) {
        addResetButton(toolbar);
        mDarkModeButton = new FontIconButton(FontAwesome.MOON, I18n.text("暗色模式"),
                (b) -> resetTo(Colors.defaultDarkThemeColors()));
        toolbar.add(mDarkModeButton);
        addActionMenu(toolbar);
    }

    @Override
    protected String getResetButtonGlyph() {
        return FontAwesome.SUN;
    }

    @Override
    protected String getResetButtonTooltip() {
        return I18n.text("亮色模式");
    }

    protected Panel createContent() {
        int cols = 8;
        Panel panel = new Panel(new PrecisionLayout().setColumns(cols).
                setMargins(LayoutConstants.WINDOW_BORDER_INSET), false);
        mColorWells = new ArrayList<>();
        int max = Colors.ALL.size();
        cols /= 2;
        int maxPerCol  = max / cols;
        int excess     = max % (maxPerCol * cols);
        int iterations = maxPerCol;
        if (excess != 0) {
            iterations++;
        }
        for (int i = 0; i < iterations; i++) {
            addColorTracker(panel, Colors.ALL.get(i), 0);
            int index = i;
            for (int j = 1; j < ((i == maxPerCol) ? excess : cols); j++) {
                index += maxPerCol;
                if (j - 1 < excess) {
                    index++;
                }
                if (index < max) {
                    addColorTracker(panel, Colors.ALL.get(index), 8);
                }
            }
        }
        return panel;
    }

    @Override
    public void establishSizing() {
        pack();
        int width = getSize().width;
        setMinimumSize(new Dimension(width, 200));
        setMaximumSize(new Dimension(width, getPreferredSize().height));
    }

    private void addColorTracker(Container parent, ThemeColor color, int leftMargin) {
        ColorTracker tracker = new ColorTracker(color);
        mColorWells.add(tracker);
        parent.add(tracker, new PrecisionLayoutData().setLeftMargin(leftMargin));
        parent.add(new Label(color.toString()), new PrecisionLayoutData().
                setFillHorizontalAlignment().setLeftMargin(4));
    }

    @Override
    protected boolean shouldResetBeEnabled() {
        Colors current = Colors.currentThemeColors();
        mDarkModeButton.setEnabled(!Colors.defaultDarkThemeColors().match(current));
        return !Colors.defaultLightThemeColors().match(current);
    }

    @Override
    protected Colors getResetData() {
        return Colors.defaultLightThemeColors();
    }

    @Override
    protected void doResetTo(Colors data) {
        mIgnore = true;
        for (ColorTracker tracker : mColorWells) {
            tracker.resetTo(data);
        }
        mIgnore = false;
        WindowUtils.repaintAll();
    }

    @Override
    protected Dirs getDir() {
        return Dirs.SETTINGS;
    }

    @Override
    protected FileType getFileType() {
        return FileType.COLOR_SETTINGS;
    }

    @Override
    protected Colors createSettingsFrom(Path path) throws IOException {
        return new Colors(path);
    }

    @Override
    protected void exportSettingsTo(Path path) throws IOException {
        Colors.currentThemeColors().save(path);
    }

    private class ColorTracker extends ColorWell implements ColorWell.ColorChangedListener {
        private int mIndex;

        ColorTracker(ThemeColor color) {
            super(new Color(color.getRGB(), true), null);
            setName(color.toString());
            mIndex = color.getIndex();
            setColorChangedListener(this);
        }

        void resetTo(Colors colors) {
            Color color = colors.getColor(mIndex);
            setWellColor(color);
            Colors.currentThemeColors().setColor(mIndex, color);
        }

        @Override
        public void colorChanged(Color color) {
            if (!mIgnore) {
                Colors.currentThemeColors().setColor(mIndex, color);
                adjustResetButton();
                WindowUtils.repaintAll();
            }
        }
    }
}
