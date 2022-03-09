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

package com.trollworks.gcs.feature;

import com.trollworks.gcs.criteria.IntegerCriteria;
import com.trollworks.gcs.criteria.StringCriteria;
import com.trollworks.gcs.ui.layout.FlexGrid;
import com.trollworks.gcs.ui.layout.FlexRow;
import com.trollworks.gcs.ui.layout.FlexSpacer;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.utility.I18n;

import java.awt.Insets;

/** A weapon damage bonus editor. */
public class WeaponDamageBonusEditor extends FeatureEditor {
    /**
     * Create a new weapon skill bonus editor.
     *
     * @param row   The row this feature will belong to.
     * @param bonus The bonus to edit.
     */
    public WeaponDamageBonusEditor(ListRow row, WeaponDamageBonus bonus) {
        super(row, bonus);
    }

    @Override
    protected void rebuildSelf(FlexGrid grid, FlexRow right) {
        WeaponDamageBonus bonus = (WeaponDamageBonus) getFeature();

        FlexRow row = new FlexRow();
        row.add(addChangeBaseTypePopup());
        LeveledAmount amount = bonus.getAmount();
        row.add(addLeveledAmountField(amount, -999, 999));
        row.add(addLeveledAmountPopup(amount));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 0, 0);

        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        PopupMenu<WeaponSelectionType> popup = new PopupMenu<>(WeaponSelectionType.values(), (p) -> {
            boolean needRebuild = ((WeaponDamageBonus) getFeature()).setWeaponSelectionType(p.getSelectedItem());
            notifyActionListeners();
            if (needRebuild) {
                rebuild();
            }
        });
        popup.setSelectedItem(bonus.getWeaponSelectionType(), false);
        add(popup);
        row.add(popup);
        grid.add(row, 1, 0);
        switch (bonus.getWeaponSelectionType()) {
            case THIS_WEAPON -> rebuildThisWeapon(grid);
            case WEAPONS_WITH_NAME -> rebuildWeaponsWithName(grid, row);
            case WEAPONS_WITH_REQUIRED_SKILL -> rebuildWeaponsWithRequiredSkill(grid, row);
            default -> row.add(new FlexSpacer(0, 0, true, false));
        }
    }

    private void rebuildThisWeapon(FlexGrid grid) {
        FlexRow row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        StringCriteria criteria = ((WeaponDamageBonus) getFeature()).getSpecializationCriteria();
        row.add(addStringComparePopup(criteria, I18n.text("且使用类型 ")));
        row.add(addStringCompareField(criteria));
        grid.add(row, 2, 0);
    }

    private void rebuildWeaponsWithName(FlexGrid grid, FlexRow row) {
        WeaponDamageBonus bonus    = (WeaponDamageBonus) getFeature();
        StringCriteria    criteria = bonus.getNameCriteria();
        row.add(addStringComparePopup(criteria, null));
        row.add(addStringCompareField(criteria));

        int i = 2;
        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        criteria = bonus.getSpecializationCriteria();
        row.add(addStringComparePopup(criteria, I18n.text("且使用类型 ")));
        row.add(addStringCompareField(criteria));
        grid.add(row, i++, 0);

        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        criteria = bonus.getCategoryCriteria();
        row.add(addStringComparePopup(criteria, I18n.text("且类型 ")));
        row.add(addStringCompareField(criteria));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, i, 0);
    }

    private void rebuildWeaponsWithRequiredSkill(FlexGrid grid, FlexRow row) {
        WeaponDamageBonus bonus    = (WeaponDamageBonus) getFeature();
        StringCriteria    criteria = bonus.getNameCriteria();
        row.add(addStringComparePopup(criteria, null));
        row.add(addStringCompareField(criteria));

        int i = 2;
        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        criteria = bonus.getSpecializationCriteria();
        row.add(addStringComparePopup(criteria, I18n.text("且分支 ")));
        row.add(addStringCompareField(criteria));
        grid.add(row, i++, 0);

        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        IntegerCriteria levelCriteria = bonus.getRelativeLevelCriteria();
        row.add(addNumericComparePopup(levelCriteria, I18n.text("且相对技能等级 ")));
        row.add(addNumericCompareField(levelCriteria, -999, 999, true));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, i++, 0);

        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        criteria = bonus.getCategoryCriteria();
        row.add(addStringComparePopup(criteria, I18n.text("且类型 ")));
        row.add(addStringCompareField(criteria));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, i, 0);
    }
}
