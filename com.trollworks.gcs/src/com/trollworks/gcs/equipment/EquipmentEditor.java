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

package com.trollworks.gcs.equipment;

import com.trollworks.gcs.character.FieldFactory;
import com.trollworks.gcs.datafile.PageRefCell;
import com.trollworks.gcs.feature.FeaturesPanel;
import com.trollworks.gcs.modifier.EquipmentModifier;
import com.trollworks.gcs.modifier.EquipmentModifierListEditor;
import com.trollworks.gcs.prereq.PrereqsPanel;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.Checkbox;
import com.trollworks.gcs.ui.widget.EditorField;
import com.trollworks.gcs.ui.widget.MultiLineTextField;
import com.trollworks.gcs.ui.widget.Panel;
import com.trollworks.gcs.ui.widget.ScrollContent;
import com.trollworks.gcs.ui.widget.outline.RowEditor;
import com.trollworks.gcs.utility.Filtered;
import com.trollworks.gcs.utility.Fixed6;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Numbers;
import com.trollworks.gcs.utility.text.Text;
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.utility.units.WeightValue;
import com.trollworks.gcs.weapon.MeleeWeaponListEditor;
import com.trollworks.gcs.weapon.RangedWeaponListEditor;
import com.trollworks.gcs.weapon.WeaponStats;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** The detailed editor for {@link Equipment}s. */
public class EquipmentEditor extends RowEditor<Equipment> implements DocumentListener {
    private Checkbox                    mEquippedCheckBox;
    private Checkbox                    mIgnoreWeightForSkillsCheckBox;
    private EditorField                 mDescriptionField;
    private EditorField                 mTechLevelField;
    private EditorField                 mLegalityClassField;
    private EditorField                 mQtyField;
    private EditorField                 mUsesField;
    private EditorField                 mMaxUsesField;
    private EditorField                 mValueField;
    private EditorField                 mExtValueField;
    private EditorField                 mWeightField;
    private EditorField                 mExtWeightField;
    private MultiLineTextField          mNotesField;
    private MultiLineTextField          mVTTNotesField;
    private EditorField                 mCategoriesField;
    private EditorField                 mReferenceField;
    private PrereqsPanel                mPrereqs;
    private FeaturesPanel               mFeatures;
    private MeleeWeaponListEditor       mMeleeWeapons;
    private RangedWeaponListEditor      mRangedWeapons;
    private EquipmentModifierListEditor mModifiers;
    private Fixed6                      mContainedValue;
    private WeightValue                 mContainedWeight;
    private boolean                     mCarried;

    /**
     * Creates a new {@link Equipment} editor.
     *
     * @param equipment The {@link Equipment} to edit.
     * @param carried   {@code true} for the carried equipment, {@code false} for the other
     *                  equipment.
     */
    public EquipmentEditor(Equipment equipment, boolean carried) {
        super(equipment);
        mCarried = carried;
        addContent();
    }

    @Override
    protected void addContentSelf(ScrollContent outer) {
        outer.add(createTopSection(), new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        mPrereqs = new PrereqsPanel(mRow, mRow.getPrereqs());
        addSection(outer, mPrereqs);
        mFeatures = new FeaturesPanel(mRow, mRow.getFeatures());
        addSection(outer, mFeatures);
        mModifiers = EquipmentModifierListEditor.createEditor(mRow);
        mModifiers.addActionListener((evt) -> {
            valueChanged();
            weightChanged();
        });
        addSection(outer, mModifiers);
        List<WeaponStats> weapons = mRow.getWeapons();
        mMeleeWeapons = new MeleeWeaponListEditor(mRow, weapons);
        addSection(outer, mMeleeWeapons);
        mRangedWeapons = new RangedWeaponListEditor(mRow, weapons);
        addSection(outer, mRangedWeapons);
    }

    private Panel createTopSection() {
        Panel panel = new Panel(new PrecisionLayout().setMargins(0).setColumns(2));
        addLabel(panel, I18n.text("名称"));
        mDescriptionField = createCorrectableField(panel, mRow.getDescription(),
                I18n.text("装备的名称/描述，不带任何备注"));
        createSecondLineFields(panel);
        createValueAndWeightFields(panel);
        mNotesField = new MultiLineTextField(mRow.getNotes(),
                I18n.text("你想要在列表中和此装备一起显示的备注"), this);
        addLabel(panel, I18n.text("备注")).setBeginningVerticalAlignment().setTopMargin(2);
        panel.add(mNotesField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        mVTTNotesField = addVTTNotesField(panel, this);

        addLabel(panel, I18n.text("类型"));
        mCategoriesField = createField(panel, mRow.getCategoriesAsString(),
                I18n.text("这件装备所属的类型(用英文括号隔开多个类型)"), 0);

        boolean forCharacterOrTemplate = mRow.getCharacter() != null || mRow.getTemplate() != null;
        Panel   wrapper                = new Panel(new PrecisionLayout().setMargins(0).setColumns(forCharacterOrTemplate ? 5 : 3));
        if (forCharacterOrTemplate) {
            addLabel(panel, I18n.text("使用次数"));
            mUsesField = createIntegerNumberField(wrapper, mRow.getUses(),
                    I18n.text("此装备的使用次数"), 99999, null);
            addInteriorLabel(wrapper, I18n.text("最多使用次数"));
        } else {
            addLabel(panel, I18n.text("最多使用次数"));
        }
        mMaxUsesField = createIntegerNumberField(wrapper, mRow.getMaxUses(),
                I18n.text("这件装备的最多使用次数"), 99999, null);
        addInteriorLabel(wrapper, I18n.text("页面引用"));
        mReferenceField = createField(wrapper, mRow.getReference(),
                PageRefCell.getStdToolTip(I18n.text("装备")), 0);
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        return panel;
    }

    private void createSecondLineFields(Container parent) {
        boolean isContainer = mRow.canHaveChildren();
        Panel   wrapper     = new Panel(new PrecisionLayout().setMargins(0).setColumns((isContainer ? 4 : 6) + (showEquipmentState() ? 1 : 0)));
        if (isContainer) {
            addLabel(parent, I18n.text("科技水平(TL)"));
        } else {
            addLabel(parent, I18n.text("数量"));
            mQtyField = createIntegerNumberField(wrapper, mRow.getQuantity(),
                    I18n.text("这件装备的数量"), 999999999, (f) -> {
                        valueChanged();
                        weightChanged();
                    });
            addInteriorLabel(wrapper, I18n.text("科技水平(TL)"));
        }
        mTechLevelField = createField(wrapper, mRow.getTechLevel(),
                I18n.text("这件装备最低可用的科技水平"), 3);
        addInteriorLabel(wrapper, I18n.text("合法等级"));
        mLegalityClassField = createField(wrapper, mRow.getLegalityClass(),
                I18n.text("这件装备的合法等级"), 3);
        if (showEquipmentState()) {
            mEquippedCheckBox = new Checkbox(I18n.text("装备上"), mRow.isEquipped(), null);
            mEquippedCheckBox.setEnabled(mIsEditable);
            mEquippedCheckBox.setToolTipText(I18n.text("未装备的物品不会像装备上那样对角色加成。"));
            wrapper.add(mEquippedCheckBox, new PrecisionLayoutData().setLeftMargin(4));
        }
        parent.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
    }

    private boolean showEquipmentState() {
        return mCarried && mRow.getCharacter() != null;
    }

    private void createValueAndWeightFields(Container parent) {
        Panel wrapper = new Panel(new PrecisionLayout().setMargins(0).setColumns(3));
        mContainedValue = mRow.getExtendedValue().sub(mRow.getAdjustedValue().mul(new Fixed6(mRow.getQuantity())));
        Fixed6 protoValue = new Fixed6("9999999.999999", false);
        addLabel(parent, I18n.text("价格"));
        mValueField = createValueField(wrapper, mRow.getValue(), protoValue,
                I18n.text("修正前这些装备中一件的基础价格"),
                (f) -> valueChanged());
        addInteriorLabel(wrapper, I18n.text("扩展"));
        mExtValueField = createValueField(wrapper, mRow.getExtendedValue(), protoValue,
                I18n.text("这些装备和它们容纳的装备的总价值"),
                null);
        mExtValueField.setEnabled(false);
        parent.add(wrapper);

        wrapper = new Panel(new PrecisionLayout().setMargins(0).setColumns(4));
        mContainedWeight = new WeightValue(mRow.getExtendedWeight(false));
        WeightValue weight = new WeightValue(mRow.getAdjustedWeight(false));
        weight.setValue(weight.getValue().mul(new Fixed6(mRow.getQuantity())));
        mContainedWeight.subtract(weight);
        WeightValue weightProto = new WeightValue(protoValue, WeightUnits.LB);
        addLabel(parent, I18n.text("重量"));
        mWeightField = createWeightField(wrapper, mRow.getWeight(), weightProto,
                I18n.text("这些装备中一件的重量"), (f) -> weightChanged());
        addInteriorLabel(wrapper, I18n.text("扩展"));
        mExtWeightField = createWeightField(wrapper, mRow.getExtendedWeight(false), weightProto,
                I18n.text("这些装备和它们容纳的装备的总重量"),
                null);
        mExtWeightField.setEnabled(false);
        mIgnoreWeightForSkillsCheckBox = new Checkbox(I18n.text("忽略技能"), mRow.isWeightIgnoredForSkills(), null);
        mIgnoreWeightForSkillsCheckBox.setEnabled(mIsEditable);
        mIgnoreWeightForSkillsCheckBox.setToolTipText(I18n.text("如果选择，这件装备的重量将不会在计算技能的负重减值时包括在内"));
        wrapper.add(mIgnoreWeightForSkillsCheckBox, new PrecisionLayoutData().setLeftMargin(4));
        parent.add(wrapper);
    }

    private EditorField createCorrectableField(Container parent, String text, String tooltip) {
        EditorField field = new EditorField(FieldFactory.STRING, null, SwingConstants.LEFT, text, tooltip);
        field.setEnabled(mIsEditable);
        field.getDocument().addDocumentListener(this);
        parent.add(field, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        return field;
    }

    private EditorField createField(Container parent, String text, String tooltip, int maxChars) {
        EditorField field = new EditorField(FieldFactory.STRING, null, SwingConstants.LEFT, text, maxChars > 0 ? Text.makeFiller(maxChars, 'M') : null, tooltip);
        field.setEnabled(mIsEditable);
        PrecisionLayoutData ld = new PrecisionLayoutData().setFillHorizontalAlignment();
        if (maxChars == 0) {
            ld.setGrabHorizontalSpace(true);
        }
        parent.add(field, ld);
        return field;
    }

    private EditorField createIntegerNumberField(Container parent, int value, String tooltip, int maxValue, EditorField.ChangeListener listener) {
        EditorField field = new EditorField(maxValue == 99999 ? FieldFactory.POSINT5 : FieldFactory.POSINT9, listener, SwingConstants.LEFT, Integer.valueOf(value), Integer.valueOf(maxValue), tooltip);
        field.setEnabled(mIsEditable);
        parent.add(field, new PrecisionLayoutData().setFillHorizontalAlignment());
        return field;
    }

    private EditorField createValueField(Container parent, Fixed6 value, Fixed6 protoValue, String tooltip, EditorField.ChangeListener listener) {
        EditorField field = new EditorField(FieldFactory.FIXED6, listener, SwingConstants.LEFT, value, protoValue, tooltip);
        field.setEnabled(mIsEditable);
        parent.add(field, new PrecisionLayoutData().setFillHorizontalAlignment());
        return field;
    }

    private EditorField createWeightField(Container parent, WeightValue value, WeightValue protoValue, String tooltip, EditorField.ChangeListener listener) {
        EditorField field = new EditorField(FieldFactory.WEIGHT, listener, SwingConstants.LEFT, value, protoValue, tooltip);
        field.setEnabled(mIsEditable);
        parent.add(field, new PrecisionLayoutData().setFillHorizontalAlignment());
        return field;
    }

    @Override
    public boolean applyChangesSelf() {
        boolean modified = mRow.setDescription(mDescriptionField.getText());
        modified |= mRow.setReference(mReferenceField.getText());
        modified |= mRow.setTechLevel(mTechLevelField.getText());
        modified |= mRow.setLegalityClass(mLegalityClassField.getText());
        modified |= mRow.setQuantity(getQty());
        modified |= mRow.setValue(new Fixed6(mValueField.getText(), Fixed6.ZERO, true));
        modified |= mRow.setWeight(WeightValue.extract(mWeightField.getText(), true));
        modified |= mRow.setWeightIgnoredForSkills(mIgnoreWeightForSkillsCheckBox.isChecked());
        modified |= mRow.setMaxUses(Numbers.extractInteger(mMaxUsesField.getText(), 0, true));
        modified |= mUsesField != null ? mRow.setUses(Numbers.extractInteger(mUsesField.getText(), 0, true)) : mRow.setUses(mRow.getMaxUses());
        if (showEquipmentState()) {
            modified |= mRow.setEquipped(mEquippedCheckBox.isChecked());
        }
        modified |= mRow.setNotes(mNotesField.getText());
        modified |= mRow.setVTTNotes(mVTTNotesField.getText());
        modified |= mRow.setCategories(mCategoriesField.getText());
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
        if (mModifiers.wasModified()) {
            modified = true;
            mRow.setModifiers(mModifiers.getModifiers());
        }
        return modified;
    }

    private int getQty() {
        if (mQtyField != null) {
            return Numbers.extractInteger(mQtyField.getText(), 0, true);
        }
        return 1;
    }

    private void valueChanged() {
        int    qty   = getQty();
        Fixed6 value = qty < 1 ? Fixed6.ZERO : new Fixed6(qty).mul(Equipment.getValueAdjustedForModifiers(new Fixed6(mValueField.getText(), Fixed6.ZERO, true), Filtered.list(mModifiers.getAllModifiers(), EquipmentModifier.class))).add(mContainedValue);
        mExtValueField.setText(value.toLocalizedString());
    }

    private void weightChanged() {
        int         qty    = getQty();
        WeightValue weight = mRow.getWeightAdjustedForModifiers(WeightValue.extract(qty < 1 ? "0" : mWeightField.getText(), true), Filtered.list(mModifiers.getAllModifiers(), EquipmentModifier.class));
        if (qty > 0) {
            weight.setValue(weight.getValue().mul(new Fixed6(qty)));
            weight.add(mContainedWeight);
        } else {
            weight.setValue(Fixed6.ZERO);
        }
        mExtWeightField.setText(weight.toString());
    }

    private void docChanged(DocumentEvent event) {
        if (mDescriptionField.getDocument() == event.getDocument()) {
            mDescriptionField.setErrorMessage(mDescriptionField.getText().trim().isEmpty() ? I18n.text("名称不得为空") : null);
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
}
