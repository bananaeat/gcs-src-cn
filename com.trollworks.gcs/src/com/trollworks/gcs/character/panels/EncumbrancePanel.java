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
import com.trollworks.gcs.character.Encumbrance;
import com.trollworks.gcs.character.FieldFactory;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.page.DropPanel;
import com.trollworks.gcs.page.PageField;
import com.trollworks.gcs.page.PageHeader;
import com.trollworks.gcs.page.PageLabel;
import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.FontAwesome;
import com.trollworks.gcs.ui.Fonts;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutAlignment;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.Separator;
import com.trollworks.gcs.ui.widget.Wrapper;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Numbers;

import java.awt.Color;
import java.awt.Container;
import java.text.MessageFormat;
import javax.swing.SwingConstants;

/** The character encumbrance panel. */
public class EncumbrancePanel extends DropPanel {
    /**
     * Creates a new encumbrance panel.
     *
     * @param sheet The sheet to display the data for.
     */
    public EncumbrancePanel(CharacterSheet sheet) {
        super(new PrecisionLayout().setColumns(8).setHorizontalSpacing(2).setVerticalSpacing(0).setMargins(0), I18n.text("Encumbrance, Move & Dodge"), true);

        Separator sep = new Separator();
        add(sep, new PrecisionLayoutData().setHorizontalSpan(8).setHorizontalAlignment(PrecisionLayoutAlignment.FILL).setGrabHorizontalSpace(true));
        addHorizontalBackground(sep, Colors.DIVIDER);

        PageHeader header = new PageHeader(I18n.text("等级"), I18n.text("负重等级"));
        add(header, new PrecisionLayoutData().setHorizontalSpan(2).setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE).setGrabHorizontalSpace(true));
        addHorizontalBackground(header, Colors.HEADER);

        addVerticalBackground(createDivider(), Colors.DIVIDER);

        String maxLoadTooltip = I18n.text("人物在特定负重等级下最多携带多重的物品");
        header = new PageHeader(I18n.text("最大负重"), maxLoadTooltip);
        add(header, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE));

        addVerticalBackground(createDivider(), Colors.DIVIDER);

        String moveTooltip = I18n.text("人物在特定负重等级下的地面移动速度");
        header = new PageHeader(I18n.text("移动"), moveTooltip);
        add(header, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE));

        addVerticalBackground(createDivider(), Colors.DIVIDER);

        String dodgeTooltip = I18n.text("人物在特定负重等级下的闪避等级");
        header = new PageHeader(I18n.text("闪避"), dodgeTooltip);
        add(header, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE));

        GURPSCharacter character = sheet.getCharacter();
        Encumbrance    current   = character.getEncumbranceLevel(false);
        boolean        band      = false;
        for (Encumbrance encumbrance : Encumbrance.values()) {
            Color textColor;
            Color backColor;
            if (current == encumbrance) {
                if (character.isCarryingGreaterThanMaxLoad(false)) {
                    textColor = Colors.ON_OVERLOADED;
                    backColor = Colors.OVERLOADED;
                } else {
                    textColor = Colors.ON_MARKER;
                    backColor = Colors.MARKER;
                }
            } else {
                textColor = Colors.ON_CONTENT;
                backColor = band ? Colors.BANDING : Colors.CONTENT;
            }
            band = !band;
            PageLabel label = new PageLabel(encumbrance == current ? FontAwesome.BALANCE_SCALE : " ", textColor);
            label.setThemeFont(Fonts.ENCUMBRANCE_MARKER);
            add(label, new PrecisionLayoutData().setFillHorizontalAlignment());
            PageLabel level = new PageLabel(MessageFormat.format("{0} {1}",
                    Numbers.format(-encumbrance.getEncumbrancePenalty()), encumbrance), textColor);
            add(level, new PrecisionLayoutData().setGrabHorizontalSpace(true));
            addHorizontalBackground(level, backColor);
            createDivider();
            addPageField(new PageField(FieldFactory.WEIGHT, character.getMaximumCarry(encumbrance),
                    sheet, SwingConstants.RIGHT, maxLoadTooltip), textColor, backColor);
            createDivider();
            addPageField(new PageField(FieldFactory.POSINT5,
                    Integer.valueOf(character.getMove(encumbrance)), sheet, SwingConstants.RIGHT,
                    moveTooltip), textColor, backColor);
            createDivider();
            addPageField(new PageField(FieldFactory.POSINT5,
                    Integer.valueOf(character.getDodge(encumbrance)), sheet, SwingConstants.RIGHT,
                    dodgeTooltip), textColor, backColor);
        }
    }

    private void addPageField(PageField field, Color textColor, Color backColor) {
        field.setForeground(textColor);
        field.setDisabledTextColor(textColor);
        field.setBackground(backColor);
        add(field, new PrecisionLayoutData().setFillHorizontalAlignment());
    }

    private Container createDivider() {
        Wrapper panel = new Wrapper();
        panel.setOnlySize(1, 1);
        add(panel);
        return panel;
    }
}
