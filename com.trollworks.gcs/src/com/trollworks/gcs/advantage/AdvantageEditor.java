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

package com.trollworks.gcs.advantage;

import com.trollworks.gcs.ancestry.AncestryRef;
import com.trollworks.gcs.character.FieldFactory;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.feature.FeaturesPanel;
import com.trollworks.gcs.modifier.AdvantageModifier;
import com.trollworks.gcs.modifier.AdvantageModifierListEditor;
import com.trollworks.gcs.prereq.PrereqsPanel;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutAlignment;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.Checkbox;
import com.trollworks.gcs.ui.widget.EditorField;
import com.trollworks.gcs.ui.widget.MultiLineTextField;
import com.trollworks.gcs.ui.widget.Panel;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.ScrollContent;
import com.trollworks.gcs.ui.widget.outline.RowEditor;
import com.trollworks.gcs.utility.Filtered;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.IntegerFormatter;
import com.trollworks.gcs.weapon.MeleeWeaponListEditor;
import com.trollworks.gcs.weapon.RangedWeaponListEditor;
import com.trollworks.gcs.weapon.WeaponStats;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;

/** The detailed editor for {@link Advantage}s. */
public class AdvantageEditor extends RowEditor<Advantage> implements ActionListener, DocumentListener, EditorField.ChangeListener {
    private EditorField                           mNameField;
    private Checkbox                              mShouldRoundCostDown;
    private PopupMenu<Levels>                     mLevelTypePopup;
    private EditorField                           mBasePointsField;
    private EditorField                           mLevelField;
    private Checkbox                              mHalfLevel;
    private EditorField                           mLevelPointsField;
    private EditorField                           mPointsField;
    private MultiLineTextField                    mNotesField;
    private MultiLineTextField                    mVTTNotesField;
    private MultiLineTextField                    mUserDescField;
    private EditorField                           mCategoriesField;
    private EditorField                           mReferenceField;
    private PrereqsPanel                          mPrereqs;
    private FeaturesPanel                         mFeatures;
    private MeleeWeaponListEditor                 mMeleeWeapons;
    private RangedWeaponListEditor                mRangedWeapons;
    private AdvantageModifierListEditor           mModifiers;
    private int                                   mLastLevel;
    private int                                   mLastPointsPerLevel;
    private boolean                               mLastHalfLevel;
    private Checkbox                              mMentalType;
    private Checkbox                              mPhysicalType;
    private Checkbox                              mSocialType;
    private Checkbox                              mExoticType;
    private Checkbox                              mSupernaturalType;
    private Checkbox                              mEnabledCheckBox;
    private PopupMenu<AdvantageContainerType>     mContainerTypePopup;
    private PopupMenu<AncestryRef>                mAncestryPopup;
    private PopupMenu<SelfControlRoll>            mCRPopup;
    private PopupMenu<SelfControlRollAdjustments> mCRAdjPopup;
    private String                                mUserDesc;

    /**
     * Creates a new {@link Advantage} editor.
     *
     * @param advantage The {@link Advantage} to edit.
     */
    public AdvantageEditor(Advantage advantage) {
        super(advantage);
        addContent();
    }

    @Override
    protected void addContentSelf(ScrollContent outer) {
        outer.add(createTopSection(), new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        boolean isContainer = mRow.canHaveChildren();
        if (!isContainer) {
            mPrereqs = new PrereqsPanel(mRow, mRow.getPrereqs());
            addSection(outer, mPrereqs);
            mFeatures = new FeaturesPanel(mRow, mRow.getFeatures());
            addSection(outer, mFeatures);
        }
        mModifiers = AdvantageModifierListEditor.createEditor(mRow);
        mModifiers.addActionListener(this);
        addSection(outer, mModifiers);
        if (!isContainer) {
            List<WeaponStats> weapons = mRow.getWeapons();
            mMeleeWeapons = new MeleeWeaponListEditor(mRow, weapons);
            addSection(outer, mMeleeWeapons);
            mRangedWeapons = new RangedWeaponListEditor(mRow, weapons);
            addSection(outer, mRangedWeapons);
            updatePoints();
        }
    }

    private Panel createTopSection() {
        Panel panel = new Panel(new PrecisionLayout().setMargins(0).setColumns(2));
        addPrimaryCommonFields(panel);
        if (mRow.canHaveChildren()) {
            addSecondaryCommonFields(panel);
            addContainerTypeFields(panel);
        } else {
            addPointFields(panel);
            addSecondaryCommonFields(panel);
            addTypeFields(panel);
        }
        return panel;
    }

    private void addPrimaryCommonFields(Container parent) {
        mNameField = createField(mRow.getName(), null, I18n.text("优势的名称，不带任何备注"));
        mNameField.getDocument().addDocumentListener(this);
        addLabel(parent, I18n.text("名称"));
        Panel wrapper = new Panel(new PrecisionLayout().setColumns(2).setMargins(0).setHorizontalSpacing(8));
        wrapper.add(mNameField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        mEnabledCheckBox = new Checkbox(I18n.text("启用"), mRow.isSelfEnabled(), (b) -> updatePoints());
        mEnabledCheckBox.setToolTipText(I18n.text("如果选中，此优势正常计算。如果未选中，此优势被视为不存在。"));
        wrapper.add(mEnabledCheckBox);
        parent.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
    }

    private void addPointFields(Container parent) {
        mLastLevel = mRow.getLevels();
        mLastHalfLevel = mRow.hasHalfLevel();
        mLastPointsPerLevel = mRow.getPointsPerLevel();
        if (mLastLevel < 0) {
            mLastLevel = 1;
            mLastHalfLevel = false;
        }

        mPointsField = createField(-9999999, 9999999, mRow.getAdjustedPoints(), I18n.text("花费在此优势上的总点数。"));
        mPointsField.setEnabled(false);
        addLabel(parent, I18n.text("点数花费"));
        Panel wrapper = new Panel(new PrecisionLayout().setColumns(10).setMargins(0));
        wrapper.add(mPointsField, new PrecisionLayoutData().setFillHorizontalAlignment());

        mBasePointsField = createField(-9999, 9999, mRow.getPoints(), I18n.text("花费在此优势上的基本点数。"));
        addInteriorLabel(wrapper, I18n.text("基本"));
        wrapper.add(mBasePointsField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mLevelTypePopup = new PopupMenu<>(Levels.values(), (p) -> levelTypeChanged());
        Levels levels = mRow.allowHalfLevels() ? Levels.HAS_HALF_LEVELS : Levels.HAS_LEVELS;
        mLevelTypePopup.setSelectedItem(mRow.isLeveled() ? levels : Levels.NO_LEVELS, false);
        wrapper.add(mLevelTypePopup, new PrecisionLayoutData().setLeftMargin(4));

        mLevelField = createField(0, 9999, mLastLevel, I18n.text("此优势的等级"));
        addInteriorLabel(wrapper, I18n.text("等级"));
        wrapper.add(mLevelField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mHalfLevel = new Checkbox("+½", mLastHalfLevel, (b) -> updatePoints());
        mHalfLevel.setToolTipText(I18n.text("添加半级"));
        mHalfLevel.setEnabled(mRow.allowHalfLevels());
        wrapper.add(mHalfLevel, new PrecisionLayoutData().setLeftMargin(4));

        mLevelPointsField = createField(-9999, 9999, mLastPointsPerLevel,
                I18n.text("此优势每等级的点数消耗。如果这个值被设置为0，且优势等级不为0，那么点数花费=基本点数"));
        addInteriorLabel(wrapper, I18n.text("每级")).setLeftMargin(8);
        wrapper.add(mLevelPointsField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mShouldRoundCostDown = new Checkbox(I18n.text("向下取整"), mRow.shouldRoundCostDown(),
                (b) -> updatePoints());
        mShouldRoundCostDown.setToolTipText(I18n.text("选中则向下取整点数消耗，否则向上取整（GURPS里的大部分东西都向上取整）"));
        wrapper.add(mShouldRoundCostDown, new PrecisionLayoutData().setLeftMargin(4));

        parent.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        if (!mRow.isLeveled()) {
            mLevelField.setText("");
            mLevelField.setEnabled(false);
            mLevelPointsField.setText("");
            mLevelPointsField.setEnabled(false);
        }
    }

    private void addSecondaryCommonFields(Container parent) {
        mNotesField = new MultiLineTextField(mRow.getNotes(),
                I18n.text("你想要在优势栏里与此优势一起显示的备注"), this);
        addLabel(parent, I18n.text("备注")).setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING).setTopMargin(2);
        parent.add(mNotesField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        mVTTNotesField = addVTTNotesField(parent, this);

        if (mRow.getDataFile() instanceof GURPSCharacter) {
            mUserDesc = mRow.getUserDesc();
            mUserDescField = new MultiLineTextField(mUserDesc,
                    I18n.text("为你自己提供参考的额外备注。这些只会存在于人物卡中，且在转移至数据列表或模板时会被移除。"), this);
            addLabel(parent, I18n.text("用户描述")).setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING).setTopMargin(2);
            parent.add(mUserDescField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        }

        mCategoriesField = createField(mRow.getCategoriesAsString(), null,
                I18n.text("优势所属的类型（用英文逗号隔开多个类型）"));
        addLabel(parent, I18n.text("类型"));
        parent.add(mCategoriesField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mCRPopup = new PopupMenu<>(SelfControlRoll.values(), (p) -> {
            SelfControlRoll cr = getCR();
            if (cr == SelfControlRoll.NONE_REQUIRED) {
                mCRAdjPopup.setSelectedItem(SelfControlRollAdjustments.NONE, true);
                mCRAdjPopup.setEnabled(false);
            } else {
                mCRAdjPopup.setEnabled(mIsEditable);
            }
            updatePoints();
        });
        mCRPopup.setSelectedIndex(mRow.getCR().ordinal(), false);
        addLabel(parent, I18n.text("自控检定"));
        Panel wrapper = new Panel(new PrecisionLayout().setColumns(2).setMargins(0));
        wrapper.add(mCRPopup);
        mCRAdjPopup = new PopupMenu<>(SelfControlRollAdjustments.values(), null);
        mCRAdjPopup.setToolTipText(I18n.text("由于自控检定限制应用的调整值"));
        mCRAdjPopup.setSelectedIndex(mRow.getCRAdj().ordinal(), false);
        mCRAdjPopup.setEnabled(mRow.getCR() != SelfControlRoll.NONE_REQUIRED);
        wrapper.add(mCRAdjPopup, new PrecisionLayoutData().setLeftMargin(4));
        parent.add(wrapper);
    }

    private void addTypeFields(Container parent) {
        addLabel(parent, I18n.text("页面引用"));
        Panel wrapper = new Panel(new PrecisionLayout().setColumns(6).setMargins(0).setHorizontalSpacing(8));
        mReferenceField = createField(mRow.getReference(), null, null);
        wrapper.add(mReferenceField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mMentalType = new Checkbox(I18n.text("心理"), (mRow.getType() & Advantage.TYPE_MASK_MENTAL) == Advantage.TYPE_MASK_MENTAL, null);
        wrapper.add(mMentalType);

        mPhysicalType = new Checkbox(I18n.text("生理"), (mRow.getType() & Advantage.TYPE_MASK_PHYSICAL) == Advantage.TYPE_MASK_PHYSICAL, null);
        wrapper.add(mPhysicalType);

        mSocialType = new Checkbox(I18n.text("社会"), (mRow.getType() & Advantage.TYPE_MASK_SOCIAL) == Advantage.TYPE_MASK_SOCIAL, null);
        wrapper.add(mSocialType);

        mExoticType = new Checkbox(I18n.text("异种"), (mRow.getType() & Advantage.TYPE_MASK_EXOTIC) == Advantage.TYPE_MASK_EXOTIC, null);
        wrapper.add(mExoticType);

        mSupernaturalType = new Checkbox(I18n.text("超自然"), (mRow.getType() & Advantage.TYPE_MASK_SUPERNATURAL) == Advantage.TYPE_MASK_SUPERNATURAL, null);
        wrapper.add(mSupernaturalType);

        parent.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
    }

    private void addContainerTypeFields(Container parent) {
        mContainerTypePopup = new PopupMenu<>(AdvantageContainerType.values(), (popup) -> mAncestryPopup.setEnabled(popup.getSelectedItem() == AdvantageContainerType.RACE));
        mContainerTypePopup.setSelectedItem(mRow.getContainerType(), false);
        mContainerTypePopup.setToolTipText(I18n.text("这个容器的容器类型"));
        addLabel(parent, I18n.text("容器类型"));
        Panel wrapper = new Panel(new PrecisionLayout().setColumns(3).setMargins(0));
        wrapper.add(mContainerTypePopup);
        mReferenceField = createField(mRow.getReference(), null, I18n.text("页面引用"));
        addLabel(wrapper, I18n.text("页面引用e")).setLeftMargin(10);
        wrapper.add(mReferenceField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        parent.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mAncestryPopup = new PopupMenu<>(AncestryRef.choices(), null);
        mAncestryPopup.setSelectedItem(mRow.getAncestryRef(), false);
        mAncestryPopup.setToolTipText(I18n.text("控制人物卡中的随机生成部分在容器类型变为种族时如何被处理"));
        mAncestryPopup.setEnabled(mRow.getContainerType() == AdvantageContainerType.RACE);
        addLabel(parent, I18n.text("Ancestry"));
        parent.add(mAncestryPopup, new PrecisionLayoutData().setGrabHorizontalSpace(true));
    }

    private EditorField createField(String text, String prototype, String tooltip) {
        return new EditorField(FieldFactory.STRING, this, SwingConstants.LEFT, text, prototype, tooltip);
    }

    private EditorField createField(int min, int max, int value, String tooltip) {
        int proto = Math.max(Math.abs(min), Math.abs(max));
        if (min < 0 || max < 0) {
            proto = -proto;
        }
        return new EditorField(new DefaultFormatterFactory(new IntegerFormatter(min, max, false)), this, SwingConstants.LEFT, Integer.valueOf(value), Integer.valueOf(proto), tooltip);
    }

    @Override
    public boolean applyChangesSelf() {
        boolean modified = mRow.setName((String) mNameField.getValue());
        modified |= mRow.setEnabled(enabled());
        if (mRow.canHaveChildren()) {
            modified |= mRow.setContainerType(mContainerTypePopup.getSelectedItem());
            modified |= mRow.setAncestryRef(mAncestryPopup.getSelectedItem());
        } else {
            int type = 0;

            if (mMentalType.isChecked()) {
                type |= Advantage.TYPE_MASK_MENTAL;
            }
            if (mPhysicalType.isChecked()) {
                type |= Advantage.TYPE_MASK_PHYSICAL;
            }
            if (mSocialType.isChecked()) {
                type |= Advantage.TYPE_MASK_SOCIAL;
            }
            if (mExoticType.isChecked()) {
                type |= Advantage.TYPE_MASK_EXOTIC;
            }
            if (mSupernaturalType.isChecked()) {
                type |= Advantage.TYPE_MASK_SUPERNATURAL;
            }
            modified |= mRow.setType(type);
            modified |= mRow.setShouldRoundCostDown(shouldRoundCostDown());
            modified |= mRow.setAllowHalfLevels(allowHalfLevels());
            modified |= mRow.setPoints(getBasePoints());
            if (isLeveled()) {
                modified |= mRow.setPointsPerLevel(getPointsPerLevel());
                modified |= mRow.setLevels(getLevels());
            } else {
                modified |= mRow.setPointsPerLevel(0);
                modified |= mRow.setLevels(-1);
            }
            modified |= isLeveled() && allowHalfLevels() ? mRow.setHalfLevel(getHalfLevel()) : mRow.setHalfLevel(false);
            if (mPrereqs != null) {
                modified |= mRow.setPrereqs(mPrereqs.getPrereqList());
            }
            if (mFeatures != null) {
                modified |= mRow.setFeatures(mFeatures.getFeatures());
            }
            if (mMeleeWeapons != null) {
                List<WeaponStats> list = new ArrayList<>(mMeleeWeapons.getWeapons());
                list.addAll(mRangedWeapons.getWeapons());
                modified |= mRow.setWeapons(list);
            }
        }
        modified |= mRow.setCR(getCR());
        modified |= mRow.setCRAdj(getCRAdj());
        if (mModifiers.wasModified()) {
            modified = true;
            mRow.setModifiers(mModifiers.getModifiers());
        }
        modified |= mRow.setReference((String) mReferenceField.getValue());
        modified |= mRow.setNotes(mNotesField.getText());
        modified |= mRow.setVTTNotes(mVTTNotesField.getText());
        modified |= mRow.setCategories((String) mCategoriesField.getValue());
        if (mUserDesc != null) {
            modified |= mRow.setUserDesc(mUserDesc);
        }
        return modified;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Object src = event.getSource();
        if (src == mModifiers) {
            updatePoints();
        }
    }

    private boolean isLeveled() {
        return mLevelTypePopup.getSelectedItem() != Levels.NO_LEVELS;
    }

    private boolean allowHalfLevels() {
        return mLevelTypePopup.getSelectedItem() == Levels.HAS_HALF_LEVELS;
    }

    private void levelTypeChanged() {
        boolean isLeveled       = isLeveled();
        boolean allowHalfLevels = allowHalfLevels();

        if (isLeveled) {
            mLevelField.setValue(Integer.valueOf(mLastLevel));
            mLevelPointsField.setValue(Integer.valueOf(mLastPointsPerLevel));
            mHalfLevel.setChecked(mLastHalfLevel && allowHalfLevels);
        } else {
            mLastLevel = getLevels();
            mLastHalfLevel = getHalfLevel();
            mLastPointsPerLevel = getPointsPerLevel();
            mLevelField.setText("");
            mHalfLevel.setChecked(false);
            mLevelPointsField.setText("");
        }
        mLevelField.setEnabled(isLeveled);
        mLevelPointsField.setEnabled(isLeveled);
        mHalfLevel.setEnabled(isLeveled && allowHalfLevels);
        updatePoints();
    }

    private SelfControlRoll getCR() {
        return mCRPopup.getSelectedItem();
    }

    private SelfControlRollAdjustments getCRAdj() {
        return mCRAdjPopup.getSelectedItem();
    }

    private int getLevels() {
        return ((Integer) mLevelField.getValue()).intValue();
    }

    private boolean getHalfLevel() {
        return mHalfLevel.isChecked();
    }

    private int getPointsPerLevel() {
        return ((Integer) mLevelPointsField.getValue()).intValue();
    }

    private int getBasePoints() {
        return ((Integer) mBasePointsField.getValue()).intValue();
    }

    private int getPoints() {
        if (mModifiers == null || !enabled()) {
            return 0;
        }
        List<AdvantageModifier> modifiers = Filtered.list(mModifiers.getAllModifiers(), AdvantageModifier.class);
        return mRow.getAdjustedPoints(getBasePoints(), isLeveled() ? getLevels() : 0, allowHalfLevels() && getHalfLevel(), getPointsPerLevel(), getCR(), modifiers, shouldRoundCostDown());
    }

    private void updatePoints() {
        if (mPointsField != null) {
            mPointsField.setValue(Integer.valueOf(getPoints()));
        }
    }

    private boolean shouldRoundCostDown() {
        return mShouldRoundCostDown.isChecked();
    }

    private boolean enabled() {
        return mEnabledCheckBox.isChecked();
    }

    private void docChanged(DocumentEvent event) {
        Document doc = event.getDocument();
        if (mNameField.getDocument() == doc) {
            mNameField.setErrorMessage(mNameField.getText().trim().isEmpty() ? I18n.text("名称不得为空") : null);
        } else if (mUserDescField != null && mUserDescField.getDocument() == doc) {
            mUserDesc = mUserDescField.getText();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        docChanged(event);
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        docChanged(event);
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        docChanged(event);
    }

    @Override
    public void editorFieldChanged(EditorField field) {
        if (field == mLevelField || field == mLevelPointsField || field == mBasePointsField) {
            updatePoints();
        }
    }
}
