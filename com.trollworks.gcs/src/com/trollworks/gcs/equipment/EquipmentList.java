/*
 * Copyright (c) 1998-2020 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.gcs.equipment;

import com.trollworks.gcs.common.ListFile;
import com.trollworks.gcs.common.LoadState;
import com.trollworks.gcs.io.xml.XMLNodeType;
import com.trollworks.gcs.io.xml.XMLReader;
import com.trollworks.gcs.ui.RetinaIcon;
import com.trollworks.gcs.ui.image.Images;
import com.trollworks.gcs.ui.widget.outline.OutlineModel;
import com.trollworks.gcs.utility.FileType;

import java.io.IOException;

/** A list of equipment. */
public class EquipmentList extends ListFile {
    /** The current version. */
    public static final int    CURRENT_VERSION  = 1;
    /** The XML tag for {@link EquipmentList}s. */
    public static final String TAG_CARRIED_ROOT = "equipment_list";
    /** The XML tag for {@link EquipmentList}s. */
    public static final String TAG_OTHER_ROOT   = "other_equipment_list";
    /** The extension for {@link EquipmentList}s. */
    public static final String EXTENSION        = "eqp";

    @Override
    public int getXMLTagVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public String getXMLTagName() {
        return TAG_CARRIED_ROOT;
    }

    @Override
    public FileType getFileType() {
        return FileType.EQUIPMENT;
    }

    @Override
    public RetinaIcon getFileIcons() {
        return Images.EQP_FILE;
    }

    @Override
    protected void loadList(XMLReader reader, LoadState state) throws IOException {
        OutlineModel model  = getModel();
        String       marker = reader.getMarker();
        do {
            if (reader.next() == XMLNodeType.START_TAG) {
                String name = reader.getName();
                if (Equipment.TAG_EQUIPMENT.equals(name) || Equipment.TAG_EQUIPMENT_CONTAINER.equals(name)) {
                    model.addRow(new Equipment(this, reader, state), true);
                } else {
                    reader.skipTag(name);
                }
            }
        } while (reader.withinMarker(marker));
    }
}