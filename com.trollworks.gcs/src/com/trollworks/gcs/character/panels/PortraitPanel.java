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

package com.trollworks.gcs.character.panels;

import com.trollworks.gcs.character.CharacterSheet;
import com.trollworks.gcs.character.Profile;
import com.trollworks.gcs.page.DropPanel;
import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.Fonts;
import com.trollworks.gcs.ui.GraphicsUtilities;
import com.trollworks.gcs.ui.border.TitledBorder;
import com.trollworks.gcs.ui.image.Img;
import com.trollworks.gcs.ui.scale.Scale;
import com.trollworks.gcs.ui.widget.Modal;
import com.trollworks.gcs.utility.Dirs;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Log;
import com.trollworks.gcs.utility.PathUtils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;

/** The character portrait. */
public class PortraitPanel extends DropPanel implements DropTargetListener {
    private CharacterSheet mSheet;

    /**
     * Creates a new character portrait.
     *
     * @param sheet The owning sheet.
     */
    public PortraitPanel(CharacterSheet sheet) {
        super(null, true);
        setBorder(new TitledBorder(Fonts.PAGE_LABEL_PRIMARY, I18n.text("头像")));
        mSheet = sheet;
        setToolTipText(MessageFormat.format(I18n.text("双击来设置角色头像。所选图像的尺寸应该是宽高比3：4的。{0}x{1}的尺寸最为理想。"),
                Integer.valueOf(Profile.PORTRAIT_WIDTH * 2), Integer.valueOf(Profile.PORTRAIT_HEIGHT * 2)));
        if (GraphicsUtilities.hasUserDisplay()) {
            setDropTarget(new DropTarget(this, DnDConstants.ACTION_COPY, this));
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    choosePortrait();
                }
            }
        });
    }

    /** Allows the user to choose a portrait for their character. */
    public void choosePortrait() {
        Path path = Modal.presentOpenFileDialog(null, I18n.text("选择一个头像"), Dirs.GENERAL,
                FileType.IMAGE_FILTERS);
        if (path != null) {
            try {
                mSheet.getCharacter().getProfile().setPortrait(Img.create(path));
            } catch (Exception exception) {
                Modal.showError(this, MessageFormat.format(I18n.text("无法加载\n{0}."), path.normalize().toAbsolutePath()));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Insets     insets = getInsets();
        Graphics2D gc     = GraphicsUtilities.prepare(g);
        gc.setColor(Colors.CONTENT);
        gc.fillRect(insets.left, insets.top, getWidth() - (insets.left + insets.right), getHeight() - (insets.top + insets.bottom));
        mSheet.getCharacter().getProfile().getPortraitWithFallback().paintIcon(this, gc, insets.left, insets.top);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        Scale  scale  = Scale.get(this);
        Insets insets = getInsets();
        return new Dimension(insets.left + scale.scale(Profile.PORTRAIT_WIDTH) + insets.right, insets.top + scale.scale(Profile.PORTRAIT_HEIGHT) + insets.bottom);
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        acceptOrRejectDrag(dtde);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        acceptOrRejectDrag(dtde);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
        acceptOrRejectDrag(dtde);
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
        // Unused
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Image img = (Image) dtde.getTransferable().getTransferData(DataFlavor.imageFlavor);
                try {
                    mSheet.getCharacter().getProfile().setPortrait(Img.create(img));
                } catch (Exception exception) {
                    Modal.showError(this, I18n.text("无法加载图像。"));
                }
                dtde.dropComplete(true);
                dtde.getDropTargetContext().getComponent().requestFocus();
            } catch (Exception exception) {
                Log.error(exception);
            }
            return;
        } else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                @SuppressWarnings("unchecked") List<File> transferData = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                for (File file : transferData) {
                    try {
                        mSheet.getCharacter().getProfile().setPortrait(Img.create(file));
                        break;
                    } catch (Exception exception) {
                        Modal.showError(this, MessageFormat.format(I18n.text("无法加载\n{0}."), PathUtils.getFullPath(file)));
                    }
                }
                dtde.dropComplete(true);
                dtde.getDropTargetContext().getComponent().requestFocus();
            } catch (Exception exception) {
                Log.error(exception);
            }
            return;
        }
        dtde.dropComplete(false);
    }

    private static void acceptOrRejectDrag(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.imageFlavor) || dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }
}
