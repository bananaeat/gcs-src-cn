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

package com.trollworks.gcs.menu.item;

import com.trollworks.gcs.datafile.PageRefCell;
import com.trollworks.gcs.menu.Command;
import com.trollworks.gcs.pageref.PageRef;
import com.trollworks.gcs.pageref.PageRefSettings;
import com.trollworks.gcs.settings.PageRefSettingsWindow;
import com.trollworks.gcs.settings.Settings;
import com.trollworks.gcs.ui.Selection;
import com.trollworks.gcs.ui.widget.Modal;
import com.trollworks.gcs.ui.widget.outline.ListOutline;
import com.trollworks.gcs.ui.widget.outline.OutlineModel;
import com.trollworks.gcs.ui.widget.outline.OutlineProxy;
import com.trollworks.gcs.ui.widget.outline.Row;
import com.trollworks.gcs.utility.Dirs;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.ReverseListIterator;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Provides the "Open Page Reference" command. */
public class OpenPageReferenceCommand extends Command {
    /** The singleton {@link OpenPageReferenceCommand} for opening a single page reference. */
    public static final OpenPageReferenceCommand OPEN_ONE_INSTANCE  = new OpenPageReferenceCommand(true, COMMAND_MODIFIER);
    /** The singleton {@link OpenPageReferenceCommand} for opening all page references. */
    public static final OpenPageReferenceCommand OPEN_EACH_INSTANCE = new OpenPageReferenceCommand(false, SHIFTED_COMMAND_MODIFIER);
    private             ListOutline              mOutline;

    private OpenPageReferenceCommand(boolean one, int modifiers) {
        super(getTitle(one), getCmd(one), KeyEvent.VK_G, modifiers);
    }

    /**
     * Creates a new OpenPageReferenceCommand.
     *
     * @param outline The outline to work against.
     * @param one     Whether to open just the first page reference, or all of them.
     */
    public OpenPageReferenceCommand(ListOutline outline, boolean one) {
        super(getTitle(one), getCmd(one));
        mOutline = outline;
    }

    private static String getTitle(boolean one) {
        return one ? I18n.text("打开页面引用") : I18n.text("打开每页引用");
    }

    private static String getCmd(boolean one) {
        return one ? "OpenPageReference" : "OpenEachPageReferences";
    }

    @Override
    public void adjust() {
        setEnabled(!getReferences(getTarget()).isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        HasSourceReference target = getTarget();
        if (target != null) {
            List<String> references = getReferences(target);
            if (!references.isEmpty()) {
                String highlight = target.getReferenceHighlight();
                if (this == OPEN_ONE_INSTANCE) {
                    openReference(references.get(0), highlight);
                } else {
                    for (String one : new ReverseListIterator<>(references)) {
                        openReference(one, highlight);
                    }
                }
            }
        }
    }

    public static void openReference(String reference, String highlight) {
        int i = reference.length() - 1;
        while (i >= 0) {
            char ch = reference.charAt(i);
            if (ch >= '0' && ch <= '9') {
                i--;
            } else {
                i++;
                break;
            }
        }
        if (i > 0) {
            int    page;
            String id = reference.substring(0, i);
            try {
                page = Integer.parseInt(reference.substring(i));
            } catch (NumberFormatException nfex) {
                return; // Has no page number, so bail
            }
            PageRefSettings settings = Settings.getInstance().getPDFRefSettings();
            PageRef         ref      = settings.lookup(id, true);
            if (ref == null) {
                Path path = Modal.presentOpenFileDialog(getFocusOwner(),
                        String.format(I18n.text("找到前缀为 \"%s\" 的PDF文件"), id),
                        Dirs.PDF, FileType.PDF.getFilter());
                if (path != null) {
                    ref = new PageRef(id, path, 0);
                    settings.put(ref);
                    PageRefSettingsWindow.rebuild();
                }
            }
            if (ref != null) {
                Settings.getInstance().getGeneralSettings().getPDFViewer().open(ref.getPath(), page + ref.getPageToIndexOffset());
            }
        }
    }

    private HasSourceReference getTarget() {
        HasSourceReference ref     = null;
        ListOutline        outline = mOutline;
        if (outline == null) {
            Component comp = getFocusOwner();
            if (comp instanceof OutlineProxy proxy) {
                comp = proxy.getRealOutline();
            }
            if (comp instanceof ListOutline listOutline) {
                outline = listOutline;
            }
        }
        if (outline != null) {
            OutlineModel model = outline.getModel();
            if (model.hasSelection()) {
                Selection selection = model.getSelection();
                if (selection.getCount() == 1) {
                    Row row = model.getFirstSelectedRow();
                    if (row instanceof HasSourceReference r) {
                        ref = r;
                    }
                }
            }
        }
        return ref;
    }

    public static List<String> getReferences(HasSourceReference ref) {
        List<String> list = new ArrayList<>();
        if (ref != null) {
            String[] refs = PageRefCell.SEPARATORS_PATTERN.split(ref.getReference());
            if (refs.length > 0) {
                for (String one : refs) {
                    String trimmed = one.trim();
                    if (!trimmed.isEmpty()) {
                        list.add(trimmed);
                    }
                }
            }
        }
        return list;
    }
}
