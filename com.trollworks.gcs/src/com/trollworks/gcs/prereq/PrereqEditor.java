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

package com.trollworks.gcs.prereq;

import com.trollworks.gcs.ui.FontAwesome;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.layout.FlexGrid;
import com.trollworks.gcs.ui.layout.FlexRow;
import com.trollworks.gcs.ui.widget.EditorPanel;
import com.trollworks.gcs.ui.widget.FontIconButton;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Log;

import javax.swing.JComponent;

/** A generic prerequisite editor panel. */
public abstract class PrereqEditor extends EditorPanel {
    private static final Class<?>[] BASE_TYPES = new Class<?>[]{
            AttributePrereq.class,
            AdvantagePrereq.class,
            SkillPrereq.class,
            SpellPrereq.class,
            ContainedWeightPrereq.class,
            ContainedQuantityPrereq.class
    };

    /** The prerequisite this panel represents. */
    protected Prereq  mPrereq;
    /** The row this prerequisite will be attached to. */
    protected ListRow mRow;
    private   int     mDepth;

    /**
     * Creates a new generic prerequisite editor panel.
     *
     * @param row    The owning row.
     * @param prereq The prerequisite to edit.
     * @param depth  The depth of this prerequisite.
     */
    protected PrereqEditor(ListRow row, Prereq prereq, int depth) {
        super(20 * depth);
        mRow = row;
        mPrereq = prereq;
        mDepth = depth;
        rebuild();
    }

    /** Rebuilds the contents of this panel with the current prerequisite settings. */
    protected final void rebuild() {
        removeAll();
        FlexGrid grid  = new FlexGrid();
        FlexRow  left  = new FlexRow();
        FlexRow  right = new FlexRow();
        if (mPrereq.getParent() != null) {
            AndOrLabel andOrLabel = new AndOrLabel(mPrereq);
            add(andOrLabel);
            left.add(andOrLabel);
        }
        grid.add(left, 0, 0);
        rebuildSelf(left, grid, right);
        if (mDepth > 0) {
            FontIconButton button = new FontIconButton(FontAwesome.TRASH,
                    mPrereq instanceof PrereqList ? I18n.text("移除此先决条件列表") :
                            I18n.text("移除此先决条件"), (b) -> removeSelfAndDescendents());
            add(button);
            right.add(button);
        }
        grid.add(right, 0, 2);
        grid.apply(this);
        revalidate();
        repaint();
    }

    /**
     * Sub-classes must implement this method to add any components they want to be visible.
     *
     * @param left  The left-side {@link FlexRow}, situated in grid row 0, column 0.
     * @param grid  The general {@link FlexGrid}. Add items in column 1.
     * @param right The right-side {@link FlexRow}, situated in grid row 0, column 2.
     */
    protected abstract void rebuildSelf(FlexRow left, FlexGrid grid, FlexRow right);

    protected PopupMenu<String> addHasPopup(boolean has) {
        String hasText         = I18n.text("拥有");
        String doesNotHaveText = I18n.text("没有");
        PopupMenu<String> popup = new PopupMenu<>(new String[]{hasText, doesNotHaveText},
                (p) -> ((HasPrereq) mPrereq).setHas(p.getSelectedIndex() == 0));
        popup.setSelectedItem(has ? hasText : doesNotHaveText, false);
        add(popup);
        return popup;
    }

    /** @return The {@link PopupMenu} that allows the base prereq type to be changed. */
    protected PopupMenu<String> addChangeBaseTypePopup() {
        String[] choices = {I18n.text("属性"), I18n.text("优势"), I18n.text("技能"),
                I18n.text("法术"), I18n.text("容纳重量为"),
                I18n.text("容纳数量为")};
        Class<?> type    = mPrereq.getClass();
        String   current = choices[0];
        int      length  = BASE_TYPES.length;
        for (int i = 0; i < length; i++) {
            if (type.equals(BASE_TYPES[i])) {
                current = choices[i];
                break;
            }
        }
        PopupMenu<String> popup = new PopupMenu<>(choices, (p) -> {
            Class<?> t = BASE_TYPES[p.getSelectedIndex()];
            if (!mPrereq.getClass().equals(t)) {
                JComponent parent    = (JComponent) getParent();
                PrereqList list      = mPrereq.getParent();
                int        listIndex = list.getIndexOf(mPrereq);
                try {
                    Prereq prereq;
                    if (t == ContainedWeightPrereq.class) {
                        prereq = new ContainedWeightPrereq(list, mRow.getDataFile().getSheetSettings().defaultWeightUnits());
                    } else {
                        prereq = (Prereq) t.getConstructor(PrereqList.class).newInstance(list);
                    }
                    if (prereq instanceof HasPrereq left && mPrereq instanceof HasPrereq right) {
                        left.setHas(right.has());
                    }
                    list.add(listIndex, prereq);
                    list.remove(mPrereq);
                    parent.add(prereq.createPrereqEditor(mRow, mDepth), UIUtilities.getIndexOf(parent, this));
                } catch (Exception exception) {
                    // Shouldn't have a failure...
                    Log.error(exception);
                }
                parent.remove(this);
                parent.revalidate();
                parent.repaint();
                ListPrereqEditor.setLastItemType(t);
            }
        });
        popup.setSelectedItem(current, false);
        add(popup);
        return popup;
    }

    /** @return The depth of this prerequisite. */
    public int getDepth() {
        return mDepth;
    }

    /** @return The underlying prerequisite. */
    public Prereq getPrereq() {
        return mPrereq;
    }

    private void removeSelfAndDescendents() {
        JComponent parent = (JComponent) getParent();
        int        index  = UIUtilities.getIndexOf(parent, this);
        int        count  = countSelfAndDescendents(mPrereq);
        for (int i = 0; i < count; i++) {
            parent.remove(index);
        }
        mPrereq.removeFromParent();
        parent.revalidate();
        parent.repaint();
    }

    private static int countSelfAndDescendents(Prereq prereq) {
        int count = 1;
        if (prereq instanceof PrereqList) {
            for (Prereq one : ((PrereqList) prereq).getChildren()) {
                count += countSelfAndDescendents(one);
            }
        }
        return count;
    }
}
