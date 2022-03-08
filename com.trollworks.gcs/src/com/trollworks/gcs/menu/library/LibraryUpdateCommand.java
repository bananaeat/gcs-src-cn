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

package com.trollworks.gcs.menu.library;

import com.trollworks.gcs.library.Library;
import com.trollworks.gcs.library.LibraryUpdater;
import com.trollworks.gcs.menu.Command;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.widget.Modal;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Release;
import com.trollworks.gcs.utility.UpdateChecker;
import com.trollworks.gcs.utility.Version;

import java.awt.event.ActionEvent;

public class LibraryUpdateCommand extends Command {
    private Library mLibrary;

    public LibraryUpdateCommand(Library library) {
        super(library.getTitle(), "lib:" + library.getKey());
        mLibrary = library;
    }

    @Override
    public void adjust() {
        Release upgrade = mLibrary.getAvailableUpgrade();
        String  title   = mLibrary.getTitle();
        if (upgrade == null) {
            setTitle(String.format(I18n.text("检查%s更新"), title));
            setEnabled(false);
            return;
        }
        if (upgrade.unableToAccessRepo()) {
            setTitle(String.format(I18n.text("无法访问线上库%s"), title));
            setEnabled(false);
            return;
        }
        if (!upgrade.hasUpdate()) {
            setTitle(String.format(I18n.text("%s没有可用版本"), title));
            setEnabled(false);
            return;
        }
        Version versionOnDisk    = mLibrary.getVersionOnDisk();
        Version availableVersion = upgrade.getVersion();
        if (availableVersion.equals(versionOnDisk)) {
            setTitle(String.format(I18n.text("%s已是最新（重新下载 v%s）"), title, versionOnDisk));
        } else {
            setTitle(String.format(I18n.text("更新%s至v%s"), title, availableVersion));
        }
        setEnabled(!UIUtilities.inModalState());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Release availableUpgrade = mLibrary.getAvailableUpgrade();
        if (availableUpgrade != null) {
            askUserToUpdate(mLibrary, availableUpgrade);
        }
    }

    public static void askUserToUpdate(Library library, Release release) {
        if (UpdateChecker.presentUpdateToUser(String.format(I18n.text("%s v%s 可用！"),
                library.getTitle(), release.getVersion()), I18n.text("""
                备注：库中的内容将被移除和替换。其他库中的内容不会被更改。

                """) + release.getNotes()).getResult() == Modal.OK) {
            LibraryUpdater.download(library, release);
        }
    }
}
