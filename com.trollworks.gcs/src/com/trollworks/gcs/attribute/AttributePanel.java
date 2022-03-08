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

package com.trollworks.gcs.attribute;

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
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.ID;
import com.trollworks.gcs.utility.text.Text;

import java.awt.Container;
import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.SwingConstants;

public class AttributePanel extends ContentPanel {
    private   Map<String, AttributeDef> mAttributes;
    protected AttributeDef              mAttrDef;
    private   Runnable                  mAdjustCallback;
    private   EditorField               mIDField;
    private   FontIconButton            mMoveUpButton;
    private   FontIconButton            mMoveDownButton;
    private   ContentPanel              mCenter;
    private   FontIconButton            mAddThresholdButton;
    private   ThresholdListPanel        mThresholdListPanel;

    public AttributePanel(Map<String, AttributeDef> attributes, AttributeDef attrDef, Runnable adjustCallback) {
        super(new PrecisionLayout().setColumns(3).setMargins(0), false);
        mAttributes = attributes;
        mAttrDef = attrDef;
        mAdjustCallback = adjustCallback;

        ContentPanel left = new ContentPanel(new PrecisionLayout(), false);
        add(left, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        mMoveUpButton = new FontIconButton(FontAwesome.ARROW_ALT_CIRCLE_UP, I18n.text("向上"), (b) -> {
            AttributeListPanel parent = (AttributeListPanel) getParent();
            int                index  = UIUtilities.getIndexOf(parent, this);
            if (index > 0) {
                parent.remove(index);
                parent.add(this, new PrecisionLayoutData().setGrabHorizontalSpace(true).setFillHorizontalAlignment(), index - 1);
                parent.renumber();
                mAdjustCallback.run();
            }
        });
        left.add(mMoveUpButton);
        mMoveDownButton = new FontIconButton(FontAwesome.ARROW_ALT_CIRCLE_DOWN, I18n.text("向下"), (b) -> {
            AttributeListPanel parent = (AttributeListPanel) getParent();
            int                index  = UIUtilities.getIndexOf(parent, this);
            if (index != -1 && index < parent.getComponentCount() - 1) {
                parent.remove(index);
                parent.add(this, new PrecisionLayoutData().setGrabHorizontalSpace(true).setFillHorizontalAlignment(), index + 1);
                parent.renumber();
                mAdjustCallback.run();
            }
        });
        left.add(mMoveDownButton);
        mAddThresholdButton = new FontIconButton(FontAwesome.PLUS_CIRCLE, I18n.text("添加池阈值"), (b) -> addThreshold());
        left.add(mAddThresholdButton);

        mCenter = new ContentPanel(new PrecisionLayout(), false);
        add(mCenter, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));

        ContentPanel wrapper = new ContentPanel(new PrecisionLayout().setColumns(6).setMargins(0), false);
        mIDField = addField(wrapper,
                I18n.text("ID"),
                I18n.text("属性的唯一ID"),
                attrDef.getID(),
                Text.makeFiller(7, 'm'),
                FieldFactory.STRING,
                (f) -> {
                    String existingID = mAttrDef.getID();
                    String id         = ((String) f.getValue());
                    if (!existingID.equals(id)) {
                        id = ID.sanitize(id, AttributeDef.RESERVED, false);
                        if (id.isEmpty() || mAttributes.containsKey(id)) {
                            f.setValue(existingID);
                        } else {
                            mAttributes.remove(existingID);
                            mAttrDef.setID(id);
                            id = mAttrDef.getID();
                            mAttributes.put(id, mAttrDef);
                            f.setValue(id);
                            mAdjustCallback.run();
                        }
                    }
                });
        addField(wrapper,
                I18n.text("名称"),
                I18n.text("属性的名称，通常是别名"),
                attrDef.getName(),
                Text.makeFiller(8, 'm'),
                FieldFactory.STRING,
                (f) -> {
                    mAttrDef.setName((String) f.getValue());
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("全名"),
                I18n.text("这个属性的全名（可以省略）"),
                attrDef.getFullName(),
                null,
                FieldFactory.STRING,
                (f) -> {
                    mAttrDef.setFullName((String) f.getValue());
                    mAdjustCallback.run();
                });
        mCenter.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));

        wrapper = new ContentPanel(new PrecisionLayout().setColumns(7).setMargins(0), false);
        addAttributeTypePopup(wrapper,
                attrDef.getType(),
                (p) -> {
                    mAttrDef.setType(p.getSelectedItem());
                    if (mAttrDef.getType() == AttributeType.POOL) {
                        if (mThresholdListPanel == null) {
                            mThresholdListPanel = new ThresholdListPanel(mAttrDef, mAdjustCallback);
                            mCenter.add(mThresholdListPanel, new PrecisionLayoutData().setHorizontalSpan(7).setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));
                            mThresholdListPanel.revalidate();
                        }
                    } else if (mThresholdListPanel != null) {
                        mCenter.remove(mThresholdListPanel);
                        mCenter.revalidate();
                    }
                    AttributeListPanel owner = UIUtilities.getAncestorOfType(this, AttributeListPanel.class);
                    if (owner != null) {
                        owner.adjustButtons();
                    }
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("基础"),
                I18n.text("基础值，可以是数字或公式"),
                attrDef.getAttributeBase(),
                null,
                FieldFactory.STRING,
                (f) -> {
                    mAttrDef.setAttributeBase((String) f.getValue());
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("花费"),
                I18n.text("和基础值每偏离一级花费（得到）的点数"),
                Integer.valueOf(attrDef.getCostPerPoint()),
                Integer.valueOf(999999),
                FieldFactory.POSINT6,
                (f) -> {
                    mAttrDef.setCostPerPoint(((Integer) f.getValue()).intValue());
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("SM减值"),
                I18n.text("每级大于0的SM带来的点数费用减值（百分比）"),
                Integer.valueOf(attrDef.getCostAdjPercentPerSM()),
                Integer.valueOf(80),
                FieldFactory.PERCENT_REDUCTION,
                (f) -> {
                    mAttrDef.setCostAdjPercentPerSM(((Integer) f.getValue()).intValue());
                    mAdjustCallback.run();
                });
        mCenter.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));

        if (mAttrDef.getType() == AttributeType.POOL) {
            mThresholdListPanel = new ThresholdListPanel(mAttrDef, mAdjustCallback);
            mCenter.add(mThresholdListPanel, new PrecisionLayoutData().setHorizontalSpan(7).setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));
        }

        ContentPanel right = new ContentPanel(new PrecisionLayout(), false);
        add(right, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        FontIconButton remove = new FontIconButton(FontAwesome.TRASH, I18n.text("移除"), (b) -> {
            getParent().remove(this);
            mAttributes.remove(mAttrDef.getID());
            mAdjustCallback.run();
        });
        right.add(remove);
    }

    private static EditorField addField(Container container, String title, String tooltip, Object value, Object protoValue, AbstractFormatterFactory formatter, EditorField.ChangeListener listener) {
        EditorField         field      = new EditorField(formatter, listener, SwingConstants.LEFT, value, protoValue, tooltip);
        PrecisionLayoutData layoutData = new PrecisionLayoutData().setFillHorizontalAlignment();
        if (protoValue == null) {
            layoutData.setGrabHorizontalSpace(true);
        }
        container.add(new Label(title), new PrecisionLayoutData().setFillHorizontalAlignment());
        container.add(field, layoutData);
        return field;
    }

    private static void addAttributeTypePopup(Container container, AttributeType value, PopupMenu.SelectionListener<AttributeType> listener) {
        PopupMenu<AttributeType> popup = new PopupMenu<>(AttributeType.values(), listener);
        popup.setSelectedItem(value, false);
        container.add(popup);
    }

    public void focusIDField() {
        mIDField.requestFocusInWindow();
    }

    public void adjustButtons(boolean isFirst, boolean isLast) {
        mMoveUpButton.setEnabled(!isFirst);
        mMoveDownButton.setEnabled(!isLast);
        mAddThresholdButton.setEnabled(mAttrDef.getType() == AttributeType.POOL);
        if (mThresholdListPanel != null) {
            mThresholdListPanel.adjustButtons();
        }
    }

    public void addThreshold() {
        List<PoolThreshold> thresholds = mAttrDef.getThresholds();
        PoolThreshold       threshold  = new PoolThreshold(1, 1, 0, I18n.text("状态"), "", null);
        thresholds.add(threshold);
        ThresholdPanel panel = new ThresholdPanel(thresholds, threshold, mAdjustCallback);
        mThresholdListPanel.add(panel, new PrecisionLayoutData().setGrabHorizontalSpace(true).setFillHorizontalAlignment());
        mAdjustCallback.run();
        scrollRectToVisible(new Rectangle(0, getPreferredSize().height - 1, 1, 1));
        panel.focusStateField();
        mThresholdListPanel.adjustButtons();
    }
}
