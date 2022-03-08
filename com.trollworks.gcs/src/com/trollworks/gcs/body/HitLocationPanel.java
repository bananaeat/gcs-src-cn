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

package com.trollworks.gcs.body;

import com.trollworks.gcs.character.FieldFactory;
import com.trollworks.gcs.ui.FontAwesome;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutAlignment;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.ContentPanel;
import com.trollworks.gcs.ui.widget.EditorField;
import com.trollworks.gcs.ui.widget.FontIconButton;
import com.trollworks.gcs.ui.widget.Label;
import com.trollworks.gcs.ui.widget.MultiLineTextField;
import com.trollworks.gcs.utility.Dice;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.ID;

import java.awt.Container;
import java.awt.Rectangle;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class HitLocationPanel extends ContentPanel implements DocumentListener {
    private HitLocation        mLocation;
    private Runnable           mAdjustCallback;
    private EditorField        mIDField;
    private MultiLineTextField mDescriptionField;
    private FontIconButton     mMoveUpButton;
    private FontIconButton     mMoveDownButton;
    private FontIconButton     mAddSubTableButton;
    private ContentPanel       mCenter;

    public HitLocationPanel(HitLocation location, Runnable adjustCallback) {
        super(new PrecisionLayout().setColumns(3).setMargins(0), false);
        mLocation = location;
        mAdjustCallback = adjustCallback;

        ContentPanel left = new ContentPanel(new PrecisionLayout(), false);
        add(left, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        mMoveUpButton = new FontIconButton(FontAwesome.ARROW_ALT_CIRCLE_UP, I18n.text("上移"), (b) -> {
            BodyTypePanel parent = (BodyTypePanel) getParent();
            int           index  = UIUtilities.getIndexOf(parent, this);
            if (index > 0) {
                parent.remove(index);
                parent.add(this, new PrecisionLayoutData().setGrabHorizontalSpace(true).setFillHorizontalAlignment(), index - 1);
                List<HitLocation> locations = mLocation.getOwningTable().getLocations();
                index--; // There is a non-item row before the list in the panel, so compensate for it
                locations.remove(index);
                locations.add(index - 1, mLocation);
                parent.adjustForReordering();
                mAdjustCallback.run();
            }
        });
        left.add(mMoveUpButton);
        mMoveDownButton = new FontIconButton(FontAwesome.ARROW_ALT_CIRCLE_DOWN, I18n.text("下移"), (b) -> {
            BodyTypePanel parent = (BodyTypePanel) getParent();
            int           index  = UIUtilities.getIndexOf(parent, this);
            if (index != -1 && index < parent.getComponentCount() - 1) {
                parent.remove(index);
                parent.add(this, new PrecisionLayoutData().setGrabHorizontalSpace(true).setFillHorizontalAlignment(), index + 1);
                List<HitLocation> locations = mLocation.getOwningTable().getLocations();
                index--; // There is a non-item row before the list in the panel, so compensate for it
                locations.remove(index);
                locations.add(index + 1, mLocation);
                parent.adjustForReordering();
                mAdjustCallback.run();
            }
        });
        left.add(mMoveDownButton);
        mAddSubTableButton = new FontIconButton(FontAwesome.PLUS_CIRCLE,
                I18n.text("Add Hit Location Sub-Table"), (b) -> addSubHitLocations());
        left.add(mAddSubTableButton);

        mCenter = new ContentPanel(new PrecisionLayout(), false);
        add(mCenter, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));

        ContentPanel wrapper = new ContentPanel(new PrecisionLayout().setColumns(8).setMargins(0), false);
        mIDField = addField(wrapper,
                I18n.text("ID"),
                I18n.text("命中位置的ID"),
                mLocation.getID(),
                null,
                FieldFactory.STRING,
                (f) -> {
                    String existingID = mLocation.getID();
                    String id         = ((String) f.getValue());
                    if (!existingID.equals(id)) {
                        id = ID.sanitize(id, null, false);
                        if (id.isEmpty()) {
                            f.setValue(existingID);
                        } else {
                            mLocation.setID(id);
                            f.setValue(id);
                            mAdjustCallback.run();
                        }
                    }
                });
        addField(wrapper,
                I18n.text("槽位"),
                I18n.text("这个命中位置在表格中填充连续槽位的数量"),
                Integer.valueOf(mLocation.getSlots()),
                Integer.valueOf(999),
                FieldFactory.POSINT6,
                (f) -> {
                    mLocation.setSlots(((Integer) f.getValue()).intValue());
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("命中惩罚"),
                I18n.text("命中这个位置的技能调整值"),
                Integer.valueOf(mLocation.getHitPenalty()),
                Integer.valueOf(-999),
                FieldFactory.INT6,
                (f) -> {
                    mLocation.setHitPenalty(((Integer) f.getValue()).intValue());
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("DR加成"),
                I18n.text("这个命中位置提供的DR加值（比如来自天然强韧）"),
                Integer.valueOf(mLocation.getDRBonus()),
                Integer.valueOf(999),
                FieldFactory.POSINT6,
                (f) -> {
                    mLocation.setDRBonus(((Integer) f.getValue()).intValue());
                    mAdjustCallback.run();
                });
        mCenter.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));

        wrapper = new ContentPanel(new PrecisionLayout().setColumns(4).setMargins(0), false);
        addField(wrapper,
                I18n.text("选项名"),
                I18n.text("这个命中位置在选项列表里的名称"),
                mLocation.getChoiceName(),
                null,
                FieldFactory.STRING,
                (f) -> {
                    mLocation.setChoiceName((String) f.getValue());
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("表名"),
                I18n.text("这个命中位置在命中位置表中的名称"),
                mLocation.getTableName(),
                null,
                FieldFactory.STRING,
                (f) -> {
                    mLocation.setTableName((String) f.getValue());
                    mAdjustCallback.run();
                });
        mCenter.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));

        wrapper = new ContentPanel(new PrecisionLayout().setColumns(2).setMargins(0), false);
        mDescriptionField = addTextArea(wrapper,
                I18n.text("描述"),
                I18n.text("击中这个位置效果的描述"),
                mLocation.getDescription());
        mCenter.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));

        if (mLocation.getSubTable() != null) {
            BodyTypePanel subTable = new BodyTypePanel(mLocation.getSubTable(), mAdjustCallback);
            mCenter.add(subTable, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));
        }

        ContentPanel right = new ContentPanel(new PrecisionLayout(), false);
        add(right, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        FontIconButton remove = new FontIconButton(FontAwesome.TRASH, I18n.text("移除"), (b) -> {
            getParent().remove(this);
            mLocation.getOwningTable().removeLocation(mLocation);
            mAdjustCallback.run();
        });
        right.add(remove);
    }

    private static EditorField addField(Container container, String title, String tooltip, Object value, Object protoValue, JFormattedTextField.AbstractFormatterFactory formatter, EditorField.ChangeListener listener) {
        EditorField         field      = new EditorField(formatter, listener, SwingConstants.LEFT, value, protoValue, tooltip);
        PrecisionLayoutData layoutData = new PrecisionLayoutData().setFillHorizontalAlignment();
        if (protoValue == null) {
            layoutData.setGrabHorizontalSpace(true);
        }
        container.add(new Label(title), new PrecisionLayoutData().setFillHorizontalAlignment());
        container.add(field, layoutData);
        return field;
    }

    private MultiLineTextField addTextArea(Container container, String title, String tooltip, String text) {
        MultiLineTextField area = new MultiLineTextField(text, tooltip, this);
        container.add(new Label(title), new PrecisionLayoutData().setFillHorizontalAlignment().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        container.add(area, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        return area;
    }

    public void focusIDField() {
        mIDField.requestFocusInWindow();
    }

    public void adjustButtons(boolean isFirst, boolean isLast) {
        mMoveUpButton.setEnabled(!isFirst);
        mMoveDownButton.setEnabled(!isLast);
        mAddSubTableButton.setEnabled(mLocation.getSubTable() == null);
    }

    public void addSubHitLocations() {
        HitLocationTable table = new HitLocationTable("id", I18n.text("名称"), new Dice(3));
        mLocation.setSubTable(table);
        BodyTypePanel subTable = new BodyTypePanel(table, mAdjustCallback);
        mCenter.add(subTable, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));
        mAdjustCallback.run();
        scrollRectToVisible(new Rectangle(0, getPreferredSize().height - 1, 1, 1));
        subTable.focusFirstField();
        subTable.adjustButtons();
    }

    private void adjustDescription() {
        mLocation.setDescription(mDescriptionField.getText());
        mAdjustCallback.run();
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        adjustDescription();
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        adjustDescription();
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        adjustDescription();
    }
}
