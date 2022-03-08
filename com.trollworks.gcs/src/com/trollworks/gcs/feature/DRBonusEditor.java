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

import com.trollworks.gcs.body.HitLocation;
import com.trollworks.gcs.body.HitLocationTable;
import com.trollworks.gcs.settings.SheetSettings;
import com.trollworks.gcs.ui.layout.FlexComponent;
import com.trollworks.gcs.ui.layout.FlexGrid;
import com.trollworks.gcs.ui.layout.FlexRow;
import com.trollworks.gcs.ui.layout.FlexSpacer;
import com.trollworks.gcs.ui.widget.EditorField;
import com.trollworks.gcs.ui.widget.Label;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.utility.I18n;

import java.awt.Insets;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

/** A DR bonus editor. */
public class DRBonusEditor extends FeatureEditor {
    private EditorField mTypeField;

    /**
     * Create a new DR bonus editor.
     *
     * @param row   The row this feature will belong to.
     * @param bonus The bonus to edit.
     */
    public DRBonusEditor(ListRow row, DRBonus bonus) {
        super(row, bonus);
    }

    @Override
    protected void rebuildSelf(FlexGrid grid, FlexRow right) {
        DRBonus bonus = (DRBonus) getFeature();
        FlexRow row   = new FlexRow();
        row.add(addChangeBaseTypePopup());
        LeveledAmount amount = bonus.getAmount();
        row.add(addLeveledAmountField(amount, -99999, 99999));
        row.add(addLeveledAmountPopup(amount));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 0, 0);

        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        Label label = new Label(I18n.text("到"));
        add(label);
        row.add(new FlexComponent(label, true));
        HitLocationTable locations = SheetSettings.get(getRow().getCharacter()).getHitLocations();
        PopupMenu<HitLocation> popup = new PopupMenu<>(locations.getUniqueHitLocations(), (p) -> {
            HitLocation location = p.getSelectedItem();
            if (location != null) {
                ((DRBonus) getFeature()).setLocation(location.getID());
            }
        });
        popup.setSelectedItem(locations.lookupLocationByID(((DRBonus) getFeature()).getLocation()), false);
        add(popup);
        row.add(popup);
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 1, 0);

        row = new FlexRow();
        row.setInsets(new Insets(0, 20, 0, 0));
        label = new Label(I18n.text("防御"));
        add(label);
        row.add(new FlexComponent(label, true));
        DefaultFormatter formatter = new DefaultFormatter();
        formatter.setOverwriteMode(false);
        mTypeField = new EditorField(new DefaultFormatterFactory(formatter), this, SwingConstants.LEFT, bonus.getSpecialization(), null);
        add(mTypeField);
        row.add(mTypeField);
        label = new Label(I18n.text("攻击"));
        add(label);
        row.add(new FlexComponent(label, true));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 2, 0);
    }

    @Override
    public void editorFieldChanged(EditorField field) {
        if (field == mTypeField) {
            ((DRBonus) getFeature()).setSpecialization(mTypeField.getText().trim());
        } else {
            super.editorFieldChanged(field);
        }
    }
}
