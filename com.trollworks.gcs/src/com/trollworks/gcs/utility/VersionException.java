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

package com.trollworks.gcs.utility;

import java.io.IOException;

/** An exception for data files that are too old or new to be loaded. */
public final class VersionException extends IOException {
    /** @return An VersionException for files that are too old. */
    public static VersionException createTooOld() {
        return new VersionException(I18n.text("这个文件来自更旧版本，无法被加载。"));
    }

    /** @return An VersionException for files that are too new. */
    public static VersionException createTooNew() {
        return new VersionException(I18n.text("这个文件来自更新版本，无法被加载。"));
    }

    private VersionException(String msg) {
        super(msg);
    }
}
