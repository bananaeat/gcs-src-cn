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

package com.trollworks.gcs.calendar;

import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.json.JsonMap;
import com.trollworks.gcs.utility.json.JsonWriter;

import java.io.IOException;

/** Month holds information about a month within the calendar. */
public class Month {
    private static final String KEY_NAME = "name";
    private static final String KEY_DAYS = "days";
    public               String mName;
    public               int    mDays;

    /** Create a new month. */
    public Month(String name, int days) {
        mName = name;
        mDays = days;
    }

    /** Create a new month from json. */
    public Month(JsonMap m) {
        mName = m.getString(KEY_NAME);
        mDays = m.getInt(KEY_DAYS);
    }

    /** Save the data as json. */
    public void save(JsonWriter w) throws IOException {
        w.startMap();
        w.keyValue(KEY_NAME, mName);
        w.keyValue(KEY_DAYS, mDays);
        w.endMap();
    }

    /** @return null if the month data is usable. */
    public String checkValidity() {
        if (mName == null || mName.isBlank()) {
            return I18n.text("日历月名称不得为空");
        }
        if (mDays < 1) {
            return I18n.text("日历月至少需要有一天");
        }
        return null;
    }
}
