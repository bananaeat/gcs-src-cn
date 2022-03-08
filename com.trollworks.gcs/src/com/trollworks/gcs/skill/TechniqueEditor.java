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

package com.trollworks.gcs.skill;

import com.trollworks.gcs.attribute.AttributeChoice;
import com.trollworks.gcs.character.FieldFactory;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.PageRefCell;
import com.trollworks.gcs.feature.FeaturesPanel;
import com.trollworks.gcs.prereq.PrereqsPanel;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.Checkbox;
import com.trollworks.gcs.ui.widget.Commitable;
import com.trollworks.gcs.ui.widget.EditorField;
import com.trollworks.gcs.ui.widget.Label;
import com.trollworks.gcs.ui.widget.MultiLineTextField;
import com.trollworks.gcs.ui.widget.Panel;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.ScrollContent;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.ui.widget.outline.RowEditor;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Text;
import com.trollworks.gcs.weapon.MeleeWeaponListEditor;
import com.trollworks.gcs.weapon.RangedWeaponListEditor;
import com.trollworks.gcs.weapon.WeaponStats;

import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/** The detailed editor for {@link Technique}s. */
public class TechniqueEditor extends RowEditor<Technique> implements DocumentListener {
    private EditorField                mNameField;
    private MultiLineTextField         mNotesField;
    private MultiLineTextField         mVTTNotesField;
    private EditorField                mCategoriesField;
    private EditorField                mReferenceField;
    private PopupMenu<SkillDifficulty> mDifficultyPopup;
    private EditorField                mPointsField;
    private EditorField                mLevelField;
    private Panel                      mDefaultPanel;
    private PopupMenu<AttributeChoice> mDefaultTypePopup;
    private EditorField                mDefaultNameField;
    private EditorField                mDefaultSpecializationField;
    private EditorField                mDefaultModifierField;
    private Checkbox                   mLimitCheckbox;
    private EditorField                mLimitField;
    private PrereqsPanel               mPrereqs;
    private FeaturesPanel              mFeatures;
    private String                     mLastDefaultType;
    private MeleeWeaponListEditor      mMeleeWeapons;
    private RangedWeaponListEditor     mRangedWeapons;

    /**
     * Creates a new {@link Technique} editor.
     *
     * @param technique The {@link Technique} to edit.
     */
    public TechniqueEditor(Technique technique) {
        super(technique);
        addContent();
    }

    @Override
    protected void addContentSelf(ScrollContent outer) {
        Panel panel = new Panel(new PrecisionLayout().setMargins(0).setColumns(2));

        mNameField = createCorrectableField(panel, panel, I18n.text("名称"), mRow.getName(),
                I18n.text("技法的基础名称，不包含分支和备注"),
                null);
        mNotesField = new MultiLineTextField(mRow.getNotes(),
                I18n.text("你想要在列表中与该技法一同显示的备注"),
                this);
        addLabelPinnedToTop(panel, I18n.text("备注"));
        panel.add(mNotesField, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        mVTTNotesField = addVTTNotesField(panel, this);
        mCategoriesField = createField(panel, panel, I18n.text("类别"),
                mRow.getCategoriesAsString(),
                I18n.text("技法所属的类别（将多个类别用逗号隔开）"),
                0, null);
        createDefaults(panel);
        createLimits(panel);
        createDifficultyPopups(panel);
        mReferenceField = createField(panel, panel, I18n.text("页面引用"), mRow.getReference(),
                PageRefCell.getStdToolTip(I18n.text("技法")), 6, null);
        outer.add(panel, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mPrereqs = new PrereqsPanel(mRow, mRow.getPrereqs());
        addSection(outer, mPrereqs);
        mFeatures = new FeaturesPanel(mRow, mRow.getFeatures());
        addSection(outer, mFeatures);
        List<WeaponStats> weapons = mRow.getWeapons();
        mMeleeWeapons = new MeleeWeaponListEditor(mRow, weapons);
        addSection(outer, mMeleeWeapons);
        mRangedWeapons = new RangedWeaponListEditor(mRow, weapons);
        addSection(outer, mRangedWeapons);
    }

    private void createDefaults(Container parent) {
        mDefaultPanel = new Panel(new PrecisionLayout().setMargins(0));
        mDefaultTypePopup = SkillDefaultType.createPopup(mDefaultPanel, mRow.getDataFile(),
                mRow.getDefault().getType(), (p) -> {
                    if (!mLastDefaultType.equals(getDefaultType())) {
                        rebuildDefaultPanel();
                    }
                    recalculateLevel();
                }, mIsEditable);
        addLabel(parent, I18n.text("默认值为"));
        parent.add(mDefaultPanel, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        rebuildDefaultPanel();
    }

    private String getDefaultType() {
        AttributeChoice choice = mDefaultTypePopup.getSelectedItem();
        if (choice != null) {
            return choice.getAttribute();
        }
        return "skill";
    }

    private String getSpecialization() {
        if (SkillDefaultType.isSkillBased(getDefaultType())) {
            StringBuilder builder = new StringBuilder();
            builder.append(mDefaultNameField.getText());
            String specialization = mDefaultSpecializationField.getText();
            if (!specialization.isEmpty()) {
                builder.append(" (");
                builder.append(specialization);
                builder.append(')');
            }
            return builder.toString();
        }
        return "";
    }

    private void rebuildDefaultPanel() {
        SkillDefault def = mRow.getDefault();
        boolean      skillBased;

        mLastDefaultType = getDefaultType();
        skillBased = SkillDefaultType.isSkillBased(mLastDefaultType);
        Commitable.sendCommitToFocusOwner();
        while (mDefaultPanel.getComponentCount() > 1) {
            mDefaultPanel.remove(1);
        }
        if (skillBased) {
            mDefaultNameField = createCorrectableField(null, mDefaultPanel, I18n.text("默认值为"),
                    def.getName(), I18n.text("这个技法默认值基于的技能名称"),
                    (f) -> recalculateLevel());
            mDefaultSpecializationField = createField(null, mDefaultPanel, null, def.getSpecialization(),
                    I18n.text("这个技法基于的技能分支名称，如果有的话"),
                    0, (f) -> recalculateLevel());
        }
        mDefaultModifierField = createSInt2NumberField(mDefaultPanel,
                I18n.text("默认技能等级的调整值"), def.getModifier());
        ((PrecisionLayout) mDefaultPanel.getLayout()).setColumns(mDefaultPanel.getComponentCount());
        mDefaultPanel.revalidate();
    }

    private void createLimits(Container parent) {
        Panel wrapper = new Panel(new PrecisionLayout().setMargins(0).setColumns(2));

        mLimitCheckbox = new Checkbox(I18n.text("最多无法多出默认技能等级的级数"),
                mRow.isLimited(), (b) -> {
            mLimitField.setEnabled(mLimitCheckbox.isChecked());
            recalculateLevel();
        });
        mLimitCheckbox.setToolTipText(I18n.text("是否要限制可达到的最大等级"));

        mLimitField = createSInt2NumberField(wrapper,
                I18n.text("在默认技能等级之上，此技法最多多多少等级"),
                mRow.getLimitModifier());
        mLimitField.setEnabled(mLimitCheckbox.isChecked());

        wrapper.add(mLimitCheckbox);
        wrapper.add(mLimitField);
        parent.add(new Label(""));
        parent.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
    }

    private EditorField createCorrectableField(Container labelParent, Container fieldParent, String title, String text, String tooltip, EditorField.ChangeListener listener) {
        EditorField field = new EditorField(FieldFactory.STRING, listener, SwingConstants.LEFT, text, tooltip);
        field.getDocument().addDocumentListener(this);
        if (labelParent != null) {
            addLabel(labelParent, title);
        }
        fieldParent.add(field, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        return field;
    }

    private static EditorField createField(Container labelParent, Container fieldParent, String title, String text, String tooltip, int maxChars, EditorField.ChangeListener listener) {
        EditorField field = new EditorField(FieldFactory.STRING, listener, SwingConstants.LEFT, text,
                maxChars > 0 ? Text.makeFiller(maxChars, 'M') : null, tooltip);
        if (labelParent != null) {
            addLabel(labelParent, title);
        }
        PrecisionLayoutData ld = new PrecisionLayoutData().setFillHorizontalAlignment();
        if (maxChars == 0) {
            ld.setGrabHorizontalSpace(true);
        }
        fieldParent.add(field, ld);
        return field;
    }

    private EditorField createSInt2NumberField(Container fieldParent, String tooltip, int value) {
        EditorField field = new EditorField(FieldFactory.SINT2, (f) -> recalculateLevel(),
                SwingConstants.LEFT, Integer.valueOf(value), Integer.valueOf(-99), tooltip);
        fieldParent.add(field, new PrecisionLayoutData().setFillHorizontalAlignment());
        return field;
    }

    private static String editorLevelTooltip() {
        return I18n.text("用于检定的技能等级和相对技能等级\n");
    }

    private void createPointsFields(Container parent, boolean forCharacter) {
        mPointsField = new EditorField(FieldFactory.POSINT3, (f) -> recalculateLevel(),
                SwingConstants.LEFT, Integer.valueOf(mRow.getRawPoints()), Integer.valueOf(999),
                I18n.text("在此技法上花费的点数"));
        addLabel(parent, I18n.text("点数"));
        parent.add(mPointsField, new PrecisionLayoutData().setFillHorizontalAlignment());
        if (forCharacter) {
            mLevelField = createField(parent, parent, I18n.text("等级"),
                    Technique.getTechniqueDisplayLevel(mRow.getLevel(), mRow.getRelativeLevel(),
                            mRow.getDefault().getModifier()),
                    editorLevelTooltip() + mRow.getLevelToolTip(), 6, null);
            mLevelField.setEnabled(false);
        }
    }

    private void createDifficultyPopups(Container parent) {
        GURPSCharacter character = mRow.getCharacter();
        Panel          wrapper   = new Panel(new PrecisionLayout().setMargins(0).setColumns(1 + (character != null ? 4 : 2)));
        mDifficultyPopup = new PopupMenu<>(new SkillDifficulty[]{SkillDifficulty.A, SkillDifficulty.H},
                (p) -> recalculateLevel());
        mDifficultyPopup.setToolTipText(I18n.text("学习此技法的相对难度"));
        mDifficultyPopup.setSelectedItem(mRow.getDifficulty(), false);
        wrapper.add(mDifficultyPopup);
        if (character != null || mRow.getTemplate() != null) {
            createPointsFields(wrapper, character != null);
        }
        addLabel(parent, I18n.text("难度"));
        parent.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
    }

    private void recalculateLevel() {
        if (mLevelField != null) {
            SkillLevel level = Technique.calculateTechniqueLevel(mRow.getCharacter(),
                    mNameField.getText(), getSpecialization(),
                    ListRow.createCategoriesList(mCategoriesField.getText()), createNewDefault(),
                    getSkillDifficulty(), getAdjustedSkillPoints(), true, mLimitCheckbox.isChecked(),
                    getLimitModifier());
            mLevelField.setText(Technique.getTechniqueDisplayLevel(level.mLevel,
                    level.mRelativeLevel, getDefaultModifier()));
            mLevelField.setToolTipText(editorLevelTooltip() + level.getToolTip());
        }
    }

    private SkillDefault createNewDefault() {
        String type = getDefaultType();
        if (SkillDefaultType.isSkillBased(type)) {
            return new SkillDefault(type, mDefaultNameField.getText(), mDefaultSpecializationField.getText(), getDefaultModifier());
        }
        return new SkillDefault(type, null, null, getDefaultModifier());
    }

    private SkillDifficulty getSkillDifficulty() {
        return mDifficultyPopup.getSelectedItem();
    }

    private int getPoints() {
        return ((Integer) mPointsField.getValue()).intValue();
    }

    private int getAdjustedSkillPoints() {
        int            points    = getPoints();
        GURPSCharacter character = mRow.getCharacter();
        if (character != null) {
            String name           = mNameField.getText();
            String specialization = "";
            if (SkillDefaultType.isSkillBased(getDefaultType())) {
                specialization = mDefaultSpecializationField.getText();
            }
            points += character.getSkillPointComparedIntegerBonusFor(Skill.ID_POINTS + "*", name, specialization, ListRow.createCategoriesList(mCategoriesField.getText()));
            points += character.getIntegerBonusFor(Skill.ID_POINTS + "/" + name.toLowerCase());
            if (points < 0) {
                points = 0;
            }
        }
        return points;
    }

    private int getDefaultModifier() {
        return ((Integer) mDefaultModifierField.getValue()).intValue();
    }

    private int getLimitModifier() {
        return ((Integer) mLimitField.getValue()).intValue();
    }

    @Override
    public boolean applyChangesSelf() {
        boolean modified = mRow.setName(mNameField.getText());
        modified |= mRow.setDefault(createNewDefault());
        modified |= mRow.setReference(mReferenceField.getText());
        modified |= mRow.setNotes(mNotesField.getText());
        modified |= mRow.setVTTNotes(mVTTNotesField.getText());
        modified |= mRow.setCategories(mCategoriesField.getText());
        if (mPointsField != null) {
            modified |= mRow.setRawPoints(getPoints());
        }
        modified |= mRow.setLimited(mLimitCheckbox.isChecked());
        modified |= mRow.setLimitModifier(getLimitModifier());
        modified |= mRow.setDifficulty(getSkillDifficulty());
        modified |= mRow.setPrereqs(mPrereqs.getPrereqList());
        modified |= mRow.setFeatures(mFeatures.getFeatures());
        List<WeaponStats> list = new ArrayList<>(mMeleeWeapons.getWeapons());
        list.addAll(mRangedWeapons.getWeapons());
        modified |= mRow.setWeapons(list);
        return modified;
    }

    private void docChanged(DocumentEvent event) {
        Document doc = event.getDocument();
        if (doc == mNameField.getDocument()) {
            mNameField.setErrorMessage(mNameField.getText().trim().isEmpty() ? I18n.text("名称不能为空") : null);
        } else if (SkillDefaultType.isSkillBased(getDefaultType()) && doc == mDefaultNameField.getDocument()) {
            mDefaultNameField.setErrorMessage(mDefaultNameField.getText().trim().isEmpty() ? I18n.text("默认名称不能为空") : null);
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
