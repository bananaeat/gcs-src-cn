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

import com.trollworks.gcs.body.HitLocation;
import com.trollworks.gcs.body.HitLocationTable;
import com.trollworks.gcs.character.CharacterSheet;
import com.trollworks.gcs.page.DropPanel;
import com.trollworks.gcs.page.PageHeader;
import com.trollworks.gcs.page.PageLabel;
import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutAlignment;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.Separator;
import com.trollworks.gcs.ui.widget.Wrapper;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Numbers;
import com.trollworks.gcs.utility.text.Text;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.SwingConstants;

/** The character body type panel. */
public class BodyTypePanel extends DropPanel {
    /**
     * Creates a new hit location panel.
     *
     * @param sheet The sheet to display the data for.
     */
    public BodyTypePanel(CharacterSheet sheet) {
        super(new PrecisionLayout().setColumns(6).setSpacing(2, 0).setMargins(0),
                sheet.getCharacter().getSheetSettings().getHitLocations().getName());

        Separator sep = new Separator();
        add(sep, new PrecisionLayoutData().setHorizontalSpan(7).
                setHorizontalAlignment(PrecisionLayoutAlignment.FILL).setGrabHorizontalSpace(true));
        addHorizontalBackground(sep, Colors.DIVIDER);

        PageHeader header = new PageHeader(I18n.text("掷骰"));
        add(header, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE));
        addHorizontalBackground(header, Colors.HEADER);

        sep = new Separator(true);
        add(sep, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.FILL));
        addVerticalBackground(sep, Colors.DIVIDER);

        header = new PageHeader(I18n.text("位置"),
                I18n.text("位置和命中此位置的命中惩罚"));
        add(header, new PrecisionLayoutData().setHorizontalSpan(2).setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE));

        sep = new Separator(true);
        add(sep, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.FILL));
        addVerticalBackground(sep, Colors.DIVIDER);

        header = new PageHeader(I18n.text("DR"));
        add(header, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.MIDDLE));

        addTable(sheet, sheet.getCharacter().getSheetSettings().getHitLocations(), 0, Colors.BANDING, Colors.CONTENT, false);

        PrecisionLayout layout = (PrecisionLayout) getLayout();
        int             count  = getComponentCount();
        for (int i = count - 6; i < count; i++) {
            Component           comp       = getComponent(i);
            PrecisionLayoutData layoutData = layout.getLayoutData(comp);
            layoutData.setGrabVerticalSpace(true);
            if (!(comp instanceof Separator)) {
                layoutData.setBeginningVerticalAlignment();
            }
        }
    }

    private boolean addTable(CharacterSheet sheet, HitLocationTable table, int depth, Color band1Color, Color band2Color, boolean band) {
        for (HitLocation location : table.getLocations()) {
            String name   = location.getTableName();
            String prefix = Text.makeFiller(depth * 3, ' ');

            PageLabel first = new PageLabel(prefix + location.getRollRange());
            first.setToolTipText(String.format(I18n.text("命中 %s 位置所需的随机掷骰值"), name));
            first.setHorizontalAlignment(SwingConstants.CENTER);
            add(first, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.FILL));
            addHorizontalBackground(first, band ? band1Color : band2Color);

            add(new Separator(true), new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.FILL));

            PageLabel label = new PageLabel(prefix + name);
            label.setToolTipText(location.getDescription());
            add(label, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.FILL));

            label = new PageLabel(Numbers.formatWithForcedSign(location.getHitPenalty()));
            label.setToolTipText(String.format(I18n.text("瞄准 %s 位置的命中惩罚"), name));
            label.setHorizontalAlignment(SwingConstants.RIGHT);
            add(label, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.FILL).setLeftMargin(2));

            add(new Separator(true), new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.FILL));

            StringBuilder tooltip = new StringBuilder();
            label = new PageLabel(location.getDisplayDR(sheet.getCharacter(), tooltip));
            label.setToolTipText(String.format(I18n.text("覆盖 %s 位置的DR %s"), name, tooltip));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.FILL));

            band = !band;
            if (location.getSubTable() != null) {
                band = addTable(sheet, location.getSubTable(), depth + 1, band1Color, band2Color, band);
            }
        }
        return band;
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension size = super.getMaximumSize();
        size.width = getPreferredSize().width;
        return size;
    }

    private Wrapper createDivider() {
        Wrapper panel = new Wrapper();
        panel.setOnlySize(1, 1);
        add(panel);
        return panel;
    }
}
