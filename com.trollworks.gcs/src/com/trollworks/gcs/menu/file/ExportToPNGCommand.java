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

package com.trollworks.gcs.menu.file;

import com.trollworks.gcs.character.SheetDockable;
import com.trollworks.gcs.menu.Command;
import com.trollworks.gcs.settings.QuickExport;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.widget.Modal;
import com.trollworks.gcs.utility.Dirs;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.PathUtils;

import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.ArrayList;

public final class ExportToPNGCommand extends Command {
    public static final ExportToPNGCommand INSTANCE = new ExportToPNGCommand();

    private ExportToPNGCommand() {
        super(I18n.text("PNG图片……"), "ToPNG");
    }

    @Override
    public void adjust() {
        setEnabled(!UIUtilities.inModalState() && Command.getTarget(SheetDockable.class) != null);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        SheetDockable dockable = getTarget(SheetDockable.class);
        if (dockable != null) {
            String name = PathUtils.cleanNameForFile(dockable.getSheet().getCharacter().getProfile().getName());
            if (name.isBlank()) {
                name = I18n.text("未命名");
            }
            Path path = Modal.presentSaveFileDialog(dockable, getTitle(), Dirs.GENERAL,
                    name + "." + FileType.PNG.getExtension(), FileType.PNG.getFilter());
            if (path != null) {
                performExport(dockable, path);
            }
        }
    }

    public static void performExport(SheetDockable dockable, Path exportPath) {
        if (dockable.getSheet().saveAsPNG(exportPath, new ArrayList<>())) {
            dockable.recordQuickExport(new QuickExport(exportPath));
        } else {
            Modal.showError(dockable, I18n.text("导出为PNG时发生了一个错误。"));
        }
    }
}
