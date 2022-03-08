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
import com.trollworks.gcs.ui.widget.Checkbox;
import com.trollworks.gcs.ui.widget.ContentPanel;
import com.trollworks.gcs.ui.widget.EditorField;
import com.trollworks.gcs.ui.widget.FontIconButton;
import com.trollworks.gcs.ui.widget.Label;
import com.trollworks.gcs.ui.widget.MultiLineTextField;
import com.trollworks.gcs.utility.I18n;

import java.awt.Container;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ThresholdPanel extends ContentPanel implements DocumentListener {
    private List<PoolThreshold> mThresholds;
    private PoolThreshold       mThreshold;
    private Runnable            mAdjustCallback;
    private FontIconButton      mMoveUpButton;
    private FontIconButton      mMoveDownButton;
    private EditorField         mStateField;
    private EditorField         mDivisorField;
    private MultiLineTextField  mExplanationField;

    public ThresholdPanel(List<PoolThreshold> thresholds, PoolThreshold threshold, Runnable adjustCallback) {
        super(new PrecisionLayout().setColumns(3).setMargins(0));
        setOpaque(false);
        mThresholds = thresholds;
        mThreshold = threshold;
        mAdjustCallback = adjustCallback;

        ContentPanel left = new ContentPanel(new PrecisionLayout(), false);
        add(left, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        mMoveUpButton = new FontIconButton(FontAwesome.ARROW_ALT_CIRCLE_UP, I18n.text("向上"), (b) -> {
            ThresholdListPanel parent = (ThresholdListPanel) getParent();
            int                index  = UIUtilities.getIndexOf(parent, this);
            if (index > 0) {
                parent.remove(index);
                mThresholds.remove(index);
                parent.add(this, new PrecisionLayoutData().setGrabHorizontalSpace(true).setFillHorizontalAlignment(), index - 1);
                mThresholds.add(index - 1, mThreshold);
                mAdjustCallback.run();
            }
        });
        left.add(mMoveUpButton);
        mMoveDownButton = new FontIconButton(FontAwesome.ARROW_ALT_CIRCLE_DOWN, I18n.text("向下"), (b) -> {
            ThresholdListPanel parent = (ThresholdListPanel) getParent();
            int                index  = UIUtilities.getIndexOf(parent, this);
            if (index != -1 && index < parent.getComponentCount() - 1) {
                parent.remove(index);
                mThresholds.remove(index);
                parent.add(this, new PrecisionLayoutData().setGrabHorizontalSpace(true).setFillHorizontalAlignment(), index + 1);
                mThresholds.add(index + 1, mThreshold);
                mAdjustCallback.run();
            }
        });
        left.add(mMoveDownButton);

        ContentPanel center = new ContentPanel(new PrecisionLayout(), false);
        add(center, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));

        ContentPanel wrapper = new ContentPanel(new PrecisionLayout().setColumns(8).setMargins(0), false);
        wrapper.setOpaque(false);
        mStateField = addField(wrapper,
                I18n.text("状态"),
                I18n.text("当前阈值状态的简短描述"),
                mThreshold.getState(),
                null,
                FieldFactory.STRING,
                (f) -> {
                    mThreshold.setState((String) f.getValue());
                    mAdjustCallback.run();
                });
        addField(wrapper,
                I18n.text("乘数"),
                I18n.text("在决定阈值时首先应用的乘数"),
                Integer.valueOf(mThreshold.getMultiplier()),
                Integer.valueOf(-999999),
                FieldFactory.INT6,
                (f) -> {
                    mThreshold.setMultiplier(((Integer) f.getValue()).intValue());
                    mAdjustCallback.run();
                });
        mDivisorField = addField(wrapper,
                I18n.text("除数"),
                I18n.text("在决定阈值时第二应用的除数"),
                Integer.valueOf(mThreshold.getDivisor()),
                Integer.valueOf(-999999),
                FieldFactory.INT6,
                (f) -> {
                    int value = ((Integer) f.getValue()).intValue();
                    if (value == 0) {
                        mDivisorField.setValue(Integer.valueOf(1));
                    } else {
                        mThreshold.setDivisor(value);
                        mAdjustCallback.run();
                    }
                });
        addField(wrapper,
                I18n.text("加数"),
                I18n.text("在决定阈值时第三应用的加数"),
                Integer.valueOf(mThreshold.getAddition()),
                Integer.valueOf(-999999),
                FieldFactory.INT6,
                (f) -> {
                    mThreshold.setAddition(((Integer) f.getValue()).intValue());
                    mAdjustCallback.run();
                });
        center.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));

        ThresholdOps[] opValues = ThresholdOps.values();
        wrapper = new ContentPanel(new PrecisionLayout().setColumns(opValues.length - 1).setMargins(0), false);
        for (ThresholdOps op : opValues) {
            if (op != ThresholdOps.UNKNOWN) {
                addCheckbox(wrapper, op);
            }
        }
        center.add(wrapper, new PrecisionLayoutData().setHorizontalAlignment(PrecisionLayoutAlignment.END).setGrabHorizontalSpace(true).setMargins(0));

        wrapper = new ContentPanel(new PrecisionLayout().setColumns(2).setMargins(0), false);
        mExplanationField = addTextArea(wrapper,
                I18n.text("解释"),
                I18n.text("当前阈值状态效果的解释"),
                mThreshold.getExplanation());
        center.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setMargins(0));

        ContentPanel right = new ContentPanel(new PrecisionLayout(), false);
        add(right, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        FontIconButton remove = new FontIconButton(FontAwesome.TRASH, I18n.text("移除"), (b) -> {
            ThresholdListPanel parent = (ThresholdListPanel) getParent();
            int                index  = UIUtilities.getIndexOf(parent, this);
            if (index != -1) {
                parent.remove(this);
                mThresholds.remove(index);
                mAdjustCallback.run();
            }
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

    private void addCheckbox(Container container, ThresholdOps op) {
        Checkbox checkbox = new Checkbox(op.title(), mThreshold.getOps().contains(op), (b) -> {
            List<ThresholdOps> ops = mThreshold.getOps();
            if (b.isChecked()) {
                ops.add(op);
            } else {
                ops.remove(op);
            }
            mAdjustCallback.run();
        });
        checkbox.setToolTipText(op.toString());
        container.add(checkbox);
    }

    public void adjustButtons(boolean isFirst, boolean isLast) {
        mMoveUpButton.setEnabled(!isFirst);
        mMoveDownButton.setEnabled(!isLast);
    }

    private void adjustExplanation() {
        mThreshold.setExplanation(mExplanationField.getText());
        mAdjustCallback.run();
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        adjustExplanation();
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        adjustExplanation();
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        adjustExplanation();
    }

    public void focusStateField() {
        mStateField.requestFocusInWindow();
    }
}
