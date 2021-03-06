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

import com.trollworks.gcs.ui.layout.FlexGrid;
import com.trollworks.gcs.ui.layout.FlexRow;
import com.trollworks.gcs.ui.layout.FlexSpacer;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.utility.I18n;

/** A contained weight prerequisite editor panel. */
public class ContainedWeightPrereqEditor extends PrereqEditor {
    /**
     * Creates a new contained weight prerequisite editor panel.
     *
     * @param row    The owning row.
     * @param prereq The prerequisite to edit.
     * @param depth  The depth of this prerequisite.
     */
    public ContainedWeightPrereqEditor(ListRow row, ContainedWeightPrereq prereq, int depth) {
        super(row, prereq, depth);
    }

    @Override
    protected void rebuildSelf(FlexRow left, FlexGrid grid, FlexRow right) {
        ContainedWeightPrereq prereq = (ContainedWeightPrereq) mPrereq;

        FlexRow row = new FlexRow();
        row.add(addHasPopup(prereq.has()));
        row.add(addChangeBaseTypePopup());
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 0, 1);

        row = new FlexRow();
        row.add(addNumericComparePopup(prereq.getWeightCompare(), I18n.text(" ")));
        row.add(addWeightCompareField(prereq.getWeightCompare()));
        row.add(new FlexSpacer(0, 0, true, false));
        grid.add(row, 1, 1);
    }
}
