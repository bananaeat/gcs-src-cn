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

package com.trollworks.gcs.menu.help;

import com.trollworks.gcs.GCS;
import com.trollworks.gcs.menu.DynamicMenuEnabler;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Platform;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/** Provides the standard "Help" menu. */
public final class HelpMenuProvider {
    private HelpMenuProvider() {
    }

    public static JMenu createMenu() {
        JMenu menu = new JMenu(I18n.text("帮助"));
        if (!Platform.isMacintosh()) {
            menu.add(new JMenuItem(AboutCommand.INSTANCE));
            menu.addSeparator();
        }
        menu.add(new JMenuItem(new OpenURICommand(I18n.text("赞助GCS开发"), "https://github.com/sponsors/richardwilkes")));
        menu.add(new JMenuItem(new OpenURICommand(I18n.text("捐赠支持GCS开发"), "https://paypal.me/GURPSCharacterSheet")));
        menu.addSeparator();
        menu.add(new JMenuItem(UpdateAppCommand.INSTANCE));
        menu.add(new JMenuItem(new OpenURICommand(I18n.text("版本注释"), "https://github.com/richardwilkes/gcs/releases")));
        menu.add(new JMenuItem(new OpenURICommand(I18n.text("证书"), "https://github.com/richardwilkes/gcs/blob/master/LICENSE")));
        menu.addSeparator();
        menu.add(new JMenuItem(new OpenURICommand(I18n.text("网站"), GCS.WEB_SITE)));
        menu.add(new JMenuItem(new OpenURICommand(I18n.text("邮件发送清单"), "https://groups.io/g/gcs")));
        DynamicMenuEnabler.add(menu);
        return menu;
    }
}
