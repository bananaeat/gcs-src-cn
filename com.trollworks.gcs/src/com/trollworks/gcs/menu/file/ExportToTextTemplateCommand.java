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
import com.trollworks.gcs.character.TextTemplate;
import com.trollworks.gcs.library.Library;
import com.trollworks.gcs.menu.Command;
import com.trollworks.gcs.settings.QuickExport;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.widget.Modal;
import com.trollworks.gcs.utility.Dirs;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.PathUtils;

import java.awt.event.ActionEvent;
import java.nio.file.Path;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ExportToTextTemplateCommand extends Command {
    private Path mTemplatePath;

    public ExportToTextTemplateCommand(Path templatePath, Library library) {
        super(PathUtils.getLeafName(templatePath, false) + "…", "ExportTextTemplate-" + library.getKey() + "-" + PathUtils.getLeafName(templatePath, true));
        mTemplatePath = templatePath;
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
            String ext  = PathUtils.getExtension(mTemplatePath);
            if (name.isBlank()) {
                name = I18n.text("未命名") + "." + ext;
            }
            Path path = Modal.presentSaveFileDialog(dockable, getTitle(), Dirs.GENERAL, name,
                    new FileNameExtensionFilter(ext + I18n.text(" 文件"), ext));
            if (path != null) {
                performExport(dockable, mTemplatePath, path);
            }
        }
    }

    public static void performExport(SheetDockable dockable, Path templatePath, Path exportPath) {
        if (new TextTemplate(dockable.getSheet()).export(exportPath, templatePath)) {
            dockable.recordQuickExport(new QuickExport(templatePath, exportPath));
        } else {
            Modal.showError(dockable, String.format(I18n.text("将人物卡导出为%s时发生了一个错误。"), PathUtils.getLeafName(templatePath, false)));
        }
    }
}
