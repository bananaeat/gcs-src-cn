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

/** EquipmentModifier Columns */
public enum EquipmentModifierColumnID {
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
        public String getDataAsText(EquipmentModifier modifier) {
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
            return I18n.text("描述此修正因子的名称和备注");
        }

        @Override
        public String getToolTip(EquipmentModifier modifier) {
            return modifier.getDescriptionToolTipText();
        }

        @Override
        public Cell getCell(boolean forEditor) {
            return new MultiCell(forEditor);
        }

        @Override
        public String getDataAsText(EquipmentModifier modifier) {
            return modifier.getDescriptionText();
        }
    },
    /** The tech level. */
    TECH_LEVEL {
        @Override
        public String toString() {
            return I18n.text("科技等级(TL)");
        }

        @Override
        public String getToolTip() {
            return I18n.text("此装备修正因子的科技等级");
        }

        @Override
        public Cell getCell(boolean forEditor) {
            if (forEditor) {
                return new TextCell(SwingConstants.RIGHT, false);
            }
            return new ListTextCell(SwingConstants.RIGHT, false);
        }

        @Override
        public String getDataAsText(EquipmentModifier modifier) {
            return modifier.getTechLevel();
        }
    },
    /** The cost adjustment. */
    COST_ADJUSTMENT {
        @Override
        public String toString() {
            return I18n.text("价格调整");
        }

        @Override
        public String getToolTip() {
            return I18n.text("此修正因子的价格调整");
        }

        @Override
        public Cell getCell(boolean forEditor) {
            if (forEditor) {
                return new TextCell(SwingConstants.LEFT, false);
            }
            return new ListTextCell(SwingConstants.LEFT, false);
        }

        @Override
        public String getDataAsText(EquipmentModifier modifier) {
            return modifier.getCostDescription();
        }
    },
    /** The weight adjustment. */
    WEIGHT_ADJUSTMENT {
        @Override
        public String toString() {
            return I18n.text("重量调整");
        }

        @Override
        public String getToolTip() {
            return I18n.text("此装备修正因子的重量调整");
        }

        @Override
        public Cell getCell(boolean forEditor) {
            if (forEditor) {
                return new TextCell(SwingConstants.LEFT, false);
            }
            return new ListTextCell(SwingConstants.LEFT, false);
        }

        @Override
        public String getDataAsText(EquipmentModifier modifier) {
            return modifier.getWeightDescription();
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
            return PageRefCell.getStdToolTip(I18n.text("装备修正因子"));
        }

        @Override
        public String getToolTip(EquipmentModifier modifier) {
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
        public String getDataAsText(EquipmentModifier modifier) {
            return modifier.getReference();
        }
    };

    /**
     * @param modifier The {@link EquipmentModifier} to get the data from.
     * @return An object representing the data for this column.
     */
    public Object getData(EquipmentModifier modifier) {
        return getDataAsText(modifier);
    }

    /**
     * @param modifier The {@link EquipmentModifier} to get the data from.
     * @return Text representing the data for this column.
     */
    public abstract String getDataAsText(EquipmentModifier modifier);

    /** @return The tooltip for the column. */
    public abstract String getToolTip();

    /**
     * @param modifier The {@link EquipmentModifier} to get the data from.
     * @return The tooltip for a specific row within the column.
     */
    public String getToolTip(EquipmentModifier modifier) {
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
        for (EquipmentModifierColumnID one : values()) {
            if (one.shouldDisplay(forEditor)) {
                Column column = new Column(one.ordinal(), one.toString(), one.getToolTip(), one.getCell(forEditor));
                column.setHeaderCell(forEditor ? new EditorHeaderCell() : new ListHeaderCell(false));
                model.addColumn(column);
            }
        }
    }
}
