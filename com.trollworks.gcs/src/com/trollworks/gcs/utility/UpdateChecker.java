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

import com.trollworks.gcs.GCS;
import com.trollworks.gcs.library.Library;
import com.trollworks.gcs.menu.library.LibraryUpdateCommand;
import com.trollworks.gcs.settings.Settings;
import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.MarkdownDocument;
import com.trollworks.gcs.ui.border.EmptyBorder;
import com.trollworks.gcs.ui.border.LineBorder;
import com.trollworks.gcs.ui.widget.AttributedTextField;
import com.trollworks.gcs.ui.widget.MessageType;
import com.trollworks.gcs.ui.widget.Modal;
import com.trollworks.gcs.ui.widget.ScrollPanel;
import com.trollworks.gcs.ui.widget.WindowUtils;
import com.trollworks.gcs.utility.task.Tasks;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Provides a background check for updates. */
public final class UpdateChecker implements Runnable {
    private static String  APP_RESULT;
    private static String  APP_RELEASE_NOTES;
    private static boolean NEW_APP_VERSION_AVAILABLE;
    private        Mode    mMode;

    private enum Mode {
        CHECK,
        NOTIFY,
        DONE
    }

    /**
     * Initiates a check for updates.
     */
    public static void check() {
        Thread thread = new Thread(new UpdateChecker(), UpdateChecker.class.getSimpleName());
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.setDaemon(true);
        thread.start();
    }

    /** @return Whether a new app version is available. */
    public static synchronized boolean isNewAppVersionAvailable() {
        return NEW_APP_VERSION_AVAILABLE;
    }

    /** @return The result of the new app check. */
    public static synchronized String getAppResult() {
        return APP_RESULT != null ? APP_RESULT : I18n.text("检查GCS更新……");
    }

    public static synchronized String getAppReleaseNotes() {
        return APP_RELEASE_NOTES;
    }

    private static synchronized void setAppResult(String result, String releaseNotes, boolean available) {
        APP_RESULT = result;
        APP_RELEASE_NOTES = releaseNotes;
        NEW_APP_VERSION_AVAILABLE = available;
    }

    /** Go to the update location on the web, if a new version is available. */
    public static void goToUpdate() {
        if (isNewAppVersionAvailable() && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(GCS.WEB_SITE));
            } catch (Exception exception) {
                Modal.showError(null, exception.getMessage());
            }
        }
    }

    private UpdateChecker() {
        mMode = Mode.CHECK;
    }

    @Override
    public void run() {
        switch (mMode) {
            case CHECK -> {
                setAppResult(null, null, false);
                checkForAppUpdates();
                checkForLibraryUpdates();
                if (mMode == Mode.NOTIFY) {
                    EventQueue.invokeLater(this);
                } else {
                    mMode = Mode.DONE;
                }
            }
            case NOTIFY -> tryNotify();
        }
    }

    private void checkForAppUpdates() {
        if (GCS.VERSION.isZero()) {
            // Development version. Bail.
            setAppResult(I18n.text("开发版本不会自动搜寻GCS更新"), null, false);
        } else {
            Version       minimum  = new Version(4, 17, 0);
            List<Release> releases = Release.load("richardwilkes", "gcs", GCS.VERSION, (version, notes) -> version.compareTo(minimum) >= 0);
            if (releases == null) {
                setAppResult(I18n.text("无法访问GCS repository"), null, false);
                return;
            }
            int count = releases.size() - 1;
            if (count >= 0 && releases.get(count).getVersion().equals(GCS.VERSION)) {
                releases.remove(count);
            }
            if (releases.isEmpty()) {
                setAppResult(I18n.text("GCS没有可用更新"), null, false);
            } else {
                Release  release   = new Release(releases);
                Settings prefs     = Settings.getInstance();
                Version  available = release.getVersion();
                setAppResult(String.format(I18n.text("GCS v%s 可用！"), available), release.getNotes(), true);
                if (available.compareTo(prefs.getLastSeenGCSVersion()) > 0) {
                    prefs.setLastSeenGCSVersion(available);
                    prefs.save();
                    mMode = Mode.NOTIFY;
                }
            }
        }
    }

    private void checkForLibraryUpdates() {
        for (Library lib : Library.LIBRARIES) {
            if (lib != Library.USER) {
                List<Release> releases = lib.checkForAvailableUpgrade();
                Version       lastSeen = lib.getLastSeen();
                lib.setAvailableUpgrade(releases);
                if (lib.getAvailableUpgrade().getVersion().compareTo(lastSeen) > 0) {
                    mMode = Mode.NOTIFY;
                }
            }
        }
    }

    private void tryNotify() {
        if (GCS.isNotificationAllowed()) {
            mMode = Mode.DONE;
            if (isNewAppVersionAvailable()) {
                if (presentUpdateToUser(getAppResult(), getAppReleaseNotes()).getResult() == Modal.OK) {
                    goToUpdate();
                }
                return;
            }
            for (Library lib : Library.LIBRARIES) {
                if (lib != Library.USER) {
                    Release release = lib.getAvailableUpgrade();
                    if (release != null && !release.unableToAccessRepo() && release.hasUpdate() && !release.getVersion().equals(lib.getVersionOnDisk())) {
                        lib.setLastSeen(release.getVersion());
                        LibraryUpdateCommand.askUserToUpdate(lib, release);
                    }
                }
            }
        } else {
            Tasks.scheduleOnUIThread(this, 250, TimeUnit.MILLISECONDS, this);
        }
    }

    public static Modal presentUpdateToUser(String title, String text) {
        AttributedTextField markdown = new AttributedTextField(new MarkdownDocument(text), null, null);
        markdown.setBorder(new EmptyBorder(2, 4, 2, 4));
        Dimension size     = markdown.getPreferredSize();
        int       maxWidth = Math.min(900, WindowUtils.getMaximumWindowBounds().width * 3 / 2);
        if (size.width > maxWidth) {
            size = markdown.getPreferredSizeForWidth(maxWidth);
            size.width = maxWidth;
            markdown.setPreferredSize(size);
        }
        markdown.setBackground(Colors.CONTENT);
        markdown.setFocusable(false);
        markdown.setEditable(false);
        ScrollPanel scroller = new ScrollPanel(markdown);
        scroller.setBorder(new LineBorder(Colors.DIVIDER));
        Modal modal = Modal.prepareToShowMessage(null, title, MessageType.WARNING, scroller);
        modal.addButton(I18n.text("忽略"), Modal.CANCEL);
        modal.addButton(I18n.text("更新"), Modal.OK);
        modal.setResizable(true);
        modal.presentToUser();
        return modal;
    }
}
