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

package com.trollworks.gcs.modifier;

import com.trollworks.gcs.datafile.PageRefCell;
import com.trollworks.gcs.datafile.RefCell;
import com.trollworks.gcs.ui.FontAwesome;
import com.trollworks.gcs.ui.widget.outline.Cell;
import com.trollworks.gcs.ui.widget.outline.Column;
import com.trollworks.gcs.ui.widget.outline.EditorHeaderCell;
import com.trollworks.gcs.ui.widget.outline.ListHeaderCell;
import com.trollworks.gcs.ui.widget.outline.ListTextCell;
import com.trollworks.gcs.ui.widget.outline.MultiCell;
import com.trollworks.gcs.ui.widget.outline.Outline;
import com.trollworks.gcs.ui.widget.outline.OutlineModel;
import com.trollworks.gcs.ui.widget.outline.TextCell;
import com.trollworks.gcs.utility.I18n;

import javax.swing.SwingConstants;

/** AdvantageModifier Columns */
public enum AdvantageModifierColumnID {
    /** The enabled/disabled column. */
    ENABLED {
        @Override
        public String toString() {
            return I18n.text("启用");
        }

        @Override
        public String getToolTip() {
            return I18n.text("此修正因子是否启用");
        }

        @Override
        public Cell getCell(boolean forEditor) {
            return new ModifierCheckCell(forEditor);
        }

        @Override
        public String getDataAsText(AdvantageModifier modifier) {
            return modifier.isEnabled() ? FontAwesome.CHECK_CIRCLE : "";
        }

        @Override
        public boolean shouldDisplay(boolean forEditor) {
            return forEditor;
        }
    },
    /** The description. */
    DESCRIPTION {
        @Override
        public String toString() {
            return I18n.text("修正因子");
        }

        @Override
        public String getToolTip() {
            return I18n.text("描述此修正因子的名称与备注");
        }

        @Override
        public String getToolTip(AdvantageModifier modifier) {
            return modifier.getDescriptionToolTipText();
        }

        @Override
        public Cell getCell(boolean forEditor) {
            return new MultiCell(forEditor);
        }

        @Override
        public String getDataAsText(AdvantageModifier modifier) {
            return modifier.getDescriptionText();
        }
    },
    /** The total cost modifier. */
    COST_MODIFIER_TOTAL {
        @Override
        public String toString() {
            return I18n.text("花费修正因子");
        }

        @Override
        public String getToolTip() {
            return I18n.text("此修正因子的花费修正因子");
        }

        @Override
        public Cell getCell(boolean forEditor) {
            if (forEditor) {
                return new TextCell(SwingConstants.LEFT, false);
            }
            return new ListTextCell(SwingConstants.LEFT, false);
        }

        @Override
        public String getDataAsText(AdvantageModifier modifier) {
            return modifier.getCostDescription();
        }
    },

    /** The page reference. */
    REFERENCE {
        @Override
        public String toString() {
            return I18n.text("引用");
        }

        @Override
        public String getToolTip() {
            return PageRefCell.getStdToolTip(I18n.text("优势修正因子"));
        }

        @Override
        public String getToolTip(AdvantageModifier modifier) {
            return PageRefCell.getStdCellToolTip(modifier.getReference());
        }

        @Override
        public Cell getCell(boolean forEditor) {
            if (forEditor) {
                return new RefCell();
            }
            return new PageRefCell();
        }

        @Override
        public String getDataAsText(AdvantageModifier modifier) {
            return modifier.getReference();
        }
    };

    /**
     * @param modifier The {@link AdvantageModifier} to get the data from.
     * @return An object representing the data for this column.
     */
    public Object getData(AdvantageModifier modifier) {
        return getDataAsText(modifier);
    }

    /**
     * @param modifier The {@link AdvantageModifier} to get the data from.
     * @return Text representing the data for this column.
     */
    public abstract String getDataAsText(AdvantageModifier modifier);

    /** @return The tooltip for the column. */
    public abstract String getToolTip();

    /**
     * @param modifier The {@link AdvantageModifier} to get the data from.
     * @return The tooltip for a specific row within the column.
     */
    public String getToolTip(AdvantageModifier modifier) {
        return null;
    }

    /**
     * @param forEditor Whether this is for an editor or not.
     * @return The {@link Cell} used to display the data.
     */
    public abstract Cell getCell(boolean forEditor);

    /** @return Whether this column should be displayed for the specified data file. */
    public boolean shouldDisplay(boolean forEditor) {
        return true;
    }

    /**
     * Adds all relevant {@link Column}s to a {@link Outline}.
     *
     * @param outline   The {@link Outline} to use.
     * @param forEditor Whether this is for an editor or not.
     */
    public static void addColumns(Outline outline, boolean forEditor) {
        OutlineModel model = outline.getModel();
        for (AdvantageModifierColumnID one : values()) {
            if (one.shouldDisplay(forEditor)) {
                Column column = new Column(one.ordinal(), one.toString(), one.getToolTip(), one.getCell(forEditor));
                column.setHeaderCell(forEditor ? new EditorHeaderCell() : new ListHeaderCell(false));
                model.addColumn(column);
            }
        }
    }
}
