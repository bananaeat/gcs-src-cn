/*
 * Copyright ©1998-2021 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.character.panels;

import com.trollworks.gcs.character.Armor;
import com.trollworks.gcs.character.CharacterSheet;
import com.trollworks.gcs.character.FieldFactory;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.character.HitLocationTable;
import com.trollworks.gcs.character.HitLocationTableEntry;
import com.trollworks.gcs.page.DropPanel;
import com.trollworks.gcs.page.PageField;
import com.trollworks.gcs.page.PageHeader;
import com.trollworks.gcs.page.PageLabel;
import com.trollworks.gcs.ui.ThemeColor;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutAlignment;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.Wrapper;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Text;

import java.awt.Dimension;
import java.text.MessageFormat;
import javax.swing.SwingConstants;

/** The character hit location panel. */
public class HitLocationPanel extends DropPanel {
    /**
     * Creates a new hit location panel.
     *
     * @param sheet The sheet to display the data for.
     */
    public HitLocationPanel(CharacterSheet sheet) {
        super(new PrecisionLayout().setColumns(7).setSpacing(2, 0).setMargins(0), String.format(I18n.Text("%s Locations"), sheet.getCharacter().getProfile().getHitLocationTable().toString()));

        addHorizontalBackground(createHeader(I18n.Text("Roll"), null), ThemeColor.HEADER);
        addVerticalBackground(createDivider(), ThemeColor.DIVIDER);
        createHeader(I18n.Text("Where"), null);
        addVerticalBackground(createDivider(), ThemeColor.DIVIDER);
        createHeader(I18n.Text("Penalty"), I18n.Text("The hit penalty for targeting a specific hit location"));
        addVerticalBackground(createDivider(), ThemeColor.DIVIDER);
        createHeader(I18n.Text("DR"), null);

        GURPSCharacter   character = sheet.getCharacter();
        Armor            armor     = character.getArmor();
        HitLocationTable table     = character.getProfile().getHitLocationTable();
        boolean          band      = false;
        for (HitLocationTableEntry entry : table.getEntries()) {
            PageLabel first = createLabel(entry.getRoll(), MessageFormat.format(I18n.Text("<html><body>The random roll needed to hit the <b>{0}</b> hit location</body></html>"), entry.getName()), true);
            if (band) {
                band = false;
                addHorizontalBackground(first, ThemeColor.BANDING);
            } else {
                band = true;
            }
            createDivider();
            createLabel(entry.getName(), Text.wrapPlainTextForToolTip(entry.getLocation().getDescription()), true);
            createDivider();
            createLabel(Integer.toString(entry.getHitPenalty()), MessageFormat.format(I18n.Text("<html><body>The hit penalty for targeting the <b>{0}</b> hit location</body></html>"), entry.getName()), false);
            createDivider();
            createDRField(sheet, armor.getValueForID(entry.getKey()), MessageFormat.format(I18n.Text("<html><body>The total DR protecting the <b>{0}</b> hit location</body></html>"), entry.getName()));
        }
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = super.getMaximumSize();
        size.width = getPreferredSize().width;
        return size;
    }

    private PageHeader createHeader(String title, String tooltip) {
        PageHeader header = new PageHeader(title, tooltip);
        add(header, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE));
        return header;
    }

    private Wrapper createDivider() {
        Wrapper panel = new Wrapper();
        panel.setOnlySize(1, 1);
        add(panel);
        return panel;
    }

    private PageLabel createLabel(String title, String tooltip, boolean center) {
        PageLabel label = new PageLabel(title, null);
        label.setToolTipText(Text.wrapPlainTextForToolTip(tooltip));
        label.setHorizontalAlignment(center ? SwingConstants.CENTER : SwingConstants.RIGHT);
        add(label, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.FILL));
        return label;
    }

    private void createDRField(CharacterSheet sheet, Object value, String tooltip) {
        PageField field = new PageField(FieldFactory.POSINT5, value, sheet, SwingConstants.RIGHT, tooltip, ThemeColor.ON_PAGE);
        add(field, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.FILL));
    }
}