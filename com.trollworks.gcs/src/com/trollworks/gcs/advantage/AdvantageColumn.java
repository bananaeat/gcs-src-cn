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

package com.trollworks.gcs.advantage;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.datafile.ListFile;
import com.trollworks.gcs.datafile.PageRefCell;
import com.trollworks.gcs.equipment.FontIconCell;
import com.trollworks.gcs.template.Template;
import com.trollworks.gcs.ui.FontAwesome;
import com.trollworks.gcs.ui.Fonts;
import com.trollworks.gcs.ui.widget.outline.Cell;
import com.trollworks.gcs.ui.widget.outline.Column;
import com.trollworks.gcs.ui.widget.outline.HeaderCell;
import com.trollworks.gcs.ui.widget.outline.ListHeaderCell;
import com.trollworks.gcs.ui.widget.outline.ListTextCell;
import com.trollworks.gcs.ui.widget.outline.MultiCell;
import com.trollworks.gcs.ui.widget.outline.Outline;
import com.trollworks.gcs.ui.widget.outline.OutlineModel;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Numbers;

import javax.swing.SwingConstants;

/** Definitions for advantage columns. */
public enum AdvantageColumn {
    /** The advantage name/description. */
    DESCRIPTION {
        @Override
        public String toString() {
            return I18n.text("优势&劣势");
        }

        @Override
        public String getToolTip() {
            return I18n.text("一个优势的名称，等级和备注");
        }

        @Override
        public String getToolTip(Advantage advantage) {
            return advantage.getDescriptionToolTipText();
        }

        @Override
        public Cell getCell() {
            return new MultiCell();
        }

        @Override
        public boolean shouldDisplay(DataFile dataFile) {
            return true;
        }

        @Override
        public Object getData(Advantage advantage) {
            return getDataAsText(advantage);
        }

        @Override
        public String getDataAsText(Advantage advantage) {
            return advantage.getDescriptionText();
        }
    },
    /** The points spent in the advantage. */
    POINTS {
        @Override
        public String toString() {
            return I18n.text("点数");
        }

        @Override
        public String getToolTip() {
            return I18n.text("花费在该优势上的点数");
        }

        @Override
        public Cell getCell() {
            return new ListTextCell(SwingConstants.RIGHT, false);
        }

        @Override
        public boolean shouldDisplay(DataFile dataFile) {
            return true;
        }

        @Override
        public Object getData(Advantage advantage) {
            return Integer.valueOf(advantage.getAdjustedPoints());
        }

        @Override
        public String getDataAsText(Advantage advantage) {
            return Numbers.format(advantage.getAdjustedPoints());
        }
    },
    /** The type. */
    TYPE {
        @Override
        public String toString() {
            return I18n.text("类型");
        }

        @Override
        public String getToolTip() {
            return I18n.text("优势的类型");
        }

        @Override
        public Cell getCell() {
            return new ListTextCell(SwingConstants.LEFT, true);
        }

        @Override
        public boolean shouldDisplay(DataFile dataFile) {
            return dataFile instanceof ListFile;
        }

        @Override
        public Object getData(Advantage advantage) {
            return getDataAsText(advantage);
        }

        @Override
        public String getDataAsText(Advantage advantage) {
            return advantage.getTypeAsText();
        }
    },
    /** The category. */
    CATEGORY {
        @Override
        public String toString() {
            return I18n.text("类别");
        }

        @Override
        public String getToolTip() {
            return I18n.text("优势所属的类别");
        }

        @Override
        public Cell getCell() {
            return new ListTextCell(SwingConstants.LEFT, true);
        }

        @Override
        public boolean shouldDisplay(DataFile dataFile) {
            return dataFile instanceof ListFile;
        }

        @Override
        public Object getData(Advantage advantage) {
            return getDataAsText(advantage);
        }

        @Override
        public String getDataAsText(Advantage advantage) {
            return advantage.getCategoriesAsString();
        }
    },
    /** The page reference. */
    REFERENCE {
        @Override
        public String toString() {
            return FontAwesome.BOOKMARK;
        }

        @Override
        public String getToolTip() {
            return PageRefCell.getStdToolTip(I18n.text("优势"));
        }

        @Override
        public String getToolTip(Advantage advantage) {
            return PageRefCell.getStdCellToolTip(advantage.getReference());
        }

        @Override
        public Cell getCell() {
            return new PageRefCell();
        }

        @Override
        public HeaderCell getHeaderCell(boolean sheetOrTemplate) {
            return new FontIconCell(Fonts.FONT_AWESOME_SOLID, sheetOrTemplate);
        }

        @Override
        public boolean shouldDisplay(DataFile dataFile) {
            return true;
        }

        @Override
        public Object getData(Advantage advantage) {
            return getDataAsText(advantage);
        }

        @Override
        public String getDataAsText(Advantage advantage) {
            return advantage.getReference();
        }
    };

    /**
     * @param advantage The {@link Advantage} to get the data from.
     * @return An object representing the data for this column.
     */
    public abstract Object getData(Advantage advantage);

    /**
     * @param advantage The {@link Advantage} to get the data from.
     * @return Text representing the data for this column.
     */
    public abstract String getDataAsText(Advantage advantage);

    /** @return The tooltip for the column. */
    public abstract String getToolTip();

    /**
     * @param advantage The {@link Advantage} to get the data from.
     * @return The tooltip for a specific row within the column.
     */
    public String getToolTip(Advantage advantage) {
        return null;
    }

    /** @return The {@link Cell} used to display the data. */
    public abstract Cell getCell();

    /** @return The {@link Cell} used to display the header. */
    public HeaderCell getHeaderCell(boolean sheetOrTemplate) {
        return new ListHeaderCell(sheetOrTemplate);
    }

    /**
     * @param dataFile The {@link DataFile} to use.
     * @return Whether this column should be displayed for the specified data file.
     */
    public abstract boolean shouldDisplay(DataFile dataFile);

    /**
     * Adds all relevant {@link Column}s to a {@link Outline}.
     *
     * @param outline  The {@link Outline} to use.
     * @param dataFile The {@link DataFile} that data is being displayed for.
     */
    public static void addColumns(Outline outline, DataFile dataFile) {
        boolean      sheetOrTemplate = dataFile instanceof GURPSCharacter || dataFile instanceof Template;
        OutlineModel model           = outline.getModel();
        for (AdvantageColumn one : values()) {
            if (one.shouldDisplay(dataFile)) {
                Column column = new Column(one.ordinal(), one.toString(), one.getToolTip(), one.getCell());
                column.setHeaderCell(one.getHeaderCell(sheetOrTemplate));
                model.addColumn(column);
            }
        }
    }
}
