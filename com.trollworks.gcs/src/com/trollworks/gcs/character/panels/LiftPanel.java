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

package com.trollworks.gcs.character.panels;

import com.trollworks.gcs.character.CharacterSheet;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.page.DropPanel;
import com.trollworks.gcs.page.PageLabel;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutAlignment;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.units.WeightValue;

/** The character lift panel. */
public class LiftPanel extends DropPanel {
    /**
     * Creates a new lift panel.
     *
     * @param sheet The sheet to display the data for.
     */
    public LiftPanel(CharacterSheet sheet) {
        super(new PrecisionLayout().setColumns(2).setMargins(0).setSpacing(2, 0), I18n.text("举起&移动物品"));
        GURPSCharacter gch = sheet.getCharacter();
        createRow(sheet, gch.getBasicLift(), I18n.text("基本举力"), I18n.text("角色可以在一秒内单手举过头顶的重量"));
        createRow(sheet, gch.getOneHandedLift(), I18n.text("单手举力"), I18n.text("角色可以在两秒内单手举过头顶的重量"));
        createRow(sheet, gch.getTwoHandedLift(), I18n.text("双手举力"), I18n.text("角色可以在四秒内双手举过头顶的重量"));
        createRow(sheet, gch.getShoveAndKnockOver(), I18n.text("推倒"), I18n.text("角色可以推行或推倒的重量"));
        createRow(sheet, gch.getRunningShoveAndKnockOver(), I18n.text("助跑后推倒"), I18n.text("角色可以在助跑后推行或推倒的重量"));
        createRow(sheet, gch.getCarryOnBack(), I18n.text("背起"), I18n.text("角色可以背着的重量"));
        createRow(sheet, gch.getShiftSlightly(), I18n.text("略微挪动"), I18n.text("角色可以在地板上略微挪动的重量"));
    }

    private void createRow(CharacterSheet sheet, WeightValue weight, String title, String tooltip) {
        add(new PageLabel(weight.toString(), tooltip), new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.END).setGrabHorizontalSpace(true));
        add(new PageLabel(title, tooltip), new PrecisionLayoutData().setGrabHorizontalSpace(true));
    }
}
