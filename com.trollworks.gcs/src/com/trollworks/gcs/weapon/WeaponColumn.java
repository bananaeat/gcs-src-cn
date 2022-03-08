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

package com.trollworks.gcs.weapon;

import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.ui.widget.outline.Cell;
import com.trollworks.gcs.ui.widget.outline.Column;
import com.trollworks.gcs.ui.widget.outline.EditorHeaderCell;
import com.trollworks.gcs.ui.widget.outline.ListHeaderCell;
import com.trollworks.gcs.ui.widget.outline.ListTextCell;
import com.trollworks.gcs.ui.widget.outline.Outline;
import com.trollworks.gcs.ui.widget.outline.OutlineModel;
import com.trollworks.gcs.ui.widget.outline.TextCell;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Numbers;

import javax.swing.SwingConstants;

/** Definitions for weapon columns. */
public enum WeaponColumn {
    /** The weapon name/description. */
    DESCRIPTION {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            if (weaponClass == MeleeWeaponStats.class) {
                return I18n.text("近战武器");
            }
            if (weaponClass == RangedWeaponStats.class) {
                return I18n.text("远程武器");
            }
            return I18n.text("武器");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的名称/描述");
        }

        @Override
        public Cell getCell(boolean forEditor) {
            return new WeaponDescriptionCell();
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            StringBuilder builder = new StringBuilder();
            String        notes   = weapon.getNotes();
            builder.append(weapon);
            if (!notes.isEmpty()) {
                builder.append(" - ");
                builder.append(notes);
            }
            return builder.toString();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return !forEditor;
        }
    },
    /** The weapon usage type. */
    USAGE {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("适用类型");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的使用类型（挥舞，戳击，投掷，射击等）");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return weapon.getUsage();
        }
    },
    /** The weapon skill level. */
    LEVEL {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("等级");
        }

        @Override
        public String getToolTip() {
            return I18n.text("此武器的技能等级");
        }

        @Override
        public Cell getCell(boolean forEditor) {
            if (forEditor) {
                return new TextCell(SwingConstants.RIGHT, false);
            }
            return new ListTextCell(SwingConstants.RIGHT, false);
        }

        @Override
        public Object getData(WeaponStats weapon) {
            return Integer.valueOf(weapon.getSkillLevel());
        }

        @Override
        public String getToolTip(WeaponDisplayRow weapon) {
            DataFile dataFile = weapon.getWeapon().getOwner().getDataFile();
            if (dataFile instanceof GURPSCharacter && dataFile.getSheetSettings().skillLevelAdjustmentsDisplay().tooltip()) {
                return weapon.getSkillLevelToolTip();
            }
            return null;
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            int level = weapon.getSkillLevel();
            if (level < 0) {
                return "-";
            }
            return Numbers.format(level);
        }
    },
    /** The weapon accuracy. */
    ACCURACY {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("准确度（Acc）");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的准确度加值");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((RangedWeaponStats) weapon).getAccuracy();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == RangedWeaponStats.class;
        }
    },
    /** The weapon parry. */
    PARRY {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("招架");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的招架值");
        }

        @Override
        public String getToolTip(WeaponDisplayRow weapon) {
            return weapon.getParryToolTip();
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((MeleeWeaponStats) weapon).getResolvedParry(null);
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == MeleeWeaponStats.class;
        }
    },
    /** The weapon block. */
    BLOCK {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("格挡");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的格挡值");
        }

        @Override
        public String getToolTip(WeaponDisplayRow weapon) {
            return weapon.getBlockToolTip();
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((MeleeWeaponStats) weapon).getResolvedBlock(null);
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == MeleeWeaponStats.class;
        }
    },
    /** The weapon damage. */
    DAMAGE {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("伤害");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器造成的伤害");
        }

        @Override
        public String getToolTip(WeaponDisplayRow weapon) {
            return weapon.getDamageToolTip();
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return weapon.getDamage().getResolvedDamage();
        }
    },
    /** The weapon reach. */
    REACH {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("触及");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的触及距离");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((MeleeWeaponStats) weapon).getReach();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == MeleeWeaponStats.class;
        }
    },
    /** The weapon range. */
    RANGE {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("射程");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的射程");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((RangedWeaponStats) weapon).getResolvedRange();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == RangedWeaponStats.class;
        }
    },
    /** The weapon rate of fire. */
    RATE_OF_FIRE {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("射速（RoF）");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的射速");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((RangedWeaponStats) weapon).getRateOfFire();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == RangedWeaponStats.class;
        }
    },
    /** The weapon shots. */
    SHOTS {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("弹数");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器在换弹/充能前可以射出的弹数");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((RangedWeaponStats) weapon).getShots();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == RangedWeaponStats.class;
        }
    },
    /** The weapon bulk. */
    BULK {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("笨重度");
        }

        @Override
        public String getToolTip() {
            return I18n.text("由于武器笨重对技能的调整值");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((RangedWeaponStats) weapon).getBulk();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == RangedWeaponStats.class;
        }
    },
    /** The weapon recoil. */
    RECOIL {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("后坐力（Rcl）");
        }

        @Override
        public String getToolTip() {
            return I18n.text("武器的后坐力调整值");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return ((RangedWeaponStats) weapon).getRecoil();
        }

        @Override
        public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
            return weaponClass == RangedWeaponStats.class;
        }
    },
    /** The weapon minimum strength. */
    MIN_ST {
        @Override
        public String toString(Class<? extends WeaponStats> weaponClass) {
            return I18n.text("力量（ST）");
        }

        @Override
        public String getToolTip() {
            return I18n.text("为恰当使用武器所需的最低力量");
        }

        @Override
        public String getDataAsText(WeaponStats weapon) {
            return weapon.getStrength();
        }
    };

    /**
     * @param weapon The {@link WeaponStats} to get the data from.
     * @return An object representing the data for this column.
     */
    public Object getData(WeaponStats weapon) {
        return getDataAsText(weapon);
    }

    /**
     * @param weapon The {@link WeaponStats} to get the data from.
     * @return Text representing the data for this column.
     */
    public abstract String getDataAsText(WeaponStats weapon);

    /** @return The tooltip for the column. */
    public abstract String getToolTip();

    /**
     * @param weapon The {@link WeaponDisplayRow} to get the data from.
     * @return The tooltip for a specific row within the column.
     */
    public String getToolTip(WeaponDisplayRow weapon) {
        return null;
    }

    /**
     * @param forEditor Whether this is for an editor or not.
     * @return The {@link Cell} used to display the data.
     */
    public Cell getCell(boolean forEditor) {
        if (forEditor) {
            return new TextCell(SwingConstants.LEFT, false);
        }
        return new ListTextCell(SwingConstants.LEFT, false);
    }

    /**
     * @param weaponClass The weapon class to check.
     * @param forEditor   Whether this is for an editor or not.
     * @return Whether this column is valid for the specified weapon class.
     */
    public boolean isValidFor(Class<? extends WeaponStats> weaponClass, boolean forEditor) {
        return true;
    }

    /**
     * @param weaponClass The weapon class to use.
     * @return The title of the column.
     */
    public abstract String toString(Class<? extends WeaponStats> weaponClass);

    @Override
    public final String toString() {
        return toString(WeaponStats.class);
    }

    /**
     * Adds all relevant {@link Column}s to a {@link Outline}.
     *
     * @param outline     The {@link Outline} to use.
     * @param weaponClass The weapon class to use.
     * @param forEditor   Whether this is for an editor or not.
     */
    public static void addColumns(Outline outline, Class<? extends WeaponStats> weaponClass, boolean forEditor) {
        OutlineModel model = outline.getModel();
        for (WeaponColumn one : values()) {
            if (one.isValidFor(weaponClass, forEditor)) {
                Column column = new Column(one.ordinal(), one.toString(weaponClass), one.getToolTip(), one.getCell(forEditor));
                column.setHeaderCell(forEditor ? new EditorHeaderCell() : new ListHeaderCell(true));
                model.addColumn(column);
            }
        }
    }
}
