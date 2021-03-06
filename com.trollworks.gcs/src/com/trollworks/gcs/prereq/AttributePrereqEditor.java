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

package com.trollworks.gcs.prereq;

import com.trollworks.gcs.attribute.AttributeChoice;
import com.trollworks.gcs.ui.layout.FlexGrid;
import com.trollworks.gcs.ui.layout.FlexRow;
import com.trollworks.gcs.ui.layout.FlexSpacer;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.utility.I18n;

/** An attribute prerequisite editor panel. */
public class AttributePrereqEditor extends PrereqEditor {
    /**
     * Creates a new attribute prerequisite editor panel.
     *
     * @param row    The owning row.
     * @param prereq The prerequisite to edit.
     * @param depth  The depth of this prerequisite.
     */
    public AttributePrereqEditor(ListRow row, AttributePrereq prereq, int depth) {
        super(row, prereq, depth);
    }

    @Override
    protected void rebuildSelf(FlexRow left, FlexGrid grid, FlexRow right) {
        AttributePrereq prereq = (AttributePrereq) mPrereq;

        FlexRow row = new FlexRow();
        row.add(addHasPopup(prereq.has()));
        row.add(addChangeBaseTypePopup());
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 0, 1);

        row = new FlexRow();
        row.add(addChangeTypePopup());
        row.add(addChangeSecondTypePopup());
        row.add(addNumericComparePopup(prereq.getValueCompare(), I18n.text(" ")));
        row.add(addNumericCompareField(prereq.getValueCompare(), 0, 99999, false));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 1, 1);
    }

    private PopupMenu<AttributeChoice> addChangeTypePopup() {
        return addAttributePopup(mRow.getDataFile(), "%s", ((AttributePrereq) mPrereq).getWhich(), false, (p) -> {
            AttributeChoice selectedItem = p.getSelectedItem();
            if (selectedItem != null) {
                ((AttributePrereq) mPrereq).setWhich(selectedItem.getAttribute());
            }
        });
    }

    private PopupMenu<AttributeChoice> addChangeSecondTypePopup() {
        String combinedWith = ((AttributePrereq) mPrereq).getCombinedWith();
        if (combinedWith == null) {
            combinedWith = " ";
        }
        return addAttributePopup(mRow.getDataFile(), I18n.text("与 %s 组合"), combinedWith, true, (p) -> {
            AttributeChoice selectedItem = p.getSelectedItem();
            if (selectedItem != null) {
                String choice = selectedItem.getAttribute();
                if (" ".equals(choice)) {
                    choice = null;
                }
                ((AttributePrereq) mPrereq).setCombinedWith(choice);
            }
        });
    }
}
