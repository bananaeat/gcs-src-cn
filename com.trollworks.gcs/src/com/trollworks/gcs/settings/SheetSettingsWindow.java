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

package com.trollworks.gcs.settings;

import com.trollworks.gcs.character.DisplayOption;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.datafile.DataChangeListener;
import com.trollworks.gcs.page.PageSettingsEditor;
import com.trollworks.gcs.ui.Fonts;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutAlignment;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.widget.Checkbox;
import com.trollworks.gcs.ui.widget.Label;
import com.trollworks.gcs.ui.widget.LayoutConstants;
import com.trollworks.gcs.ui.widget.MultiLineTextField;
import com.trollworks.gcs.ui.widget.Panel;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.Separator;
import com.trollworks.gcs.utility.Dirs;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.units.LengthUnits;
import com.trollworks.gcs.utility.units.WeightUnits;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class SheetSettingsWindow extends SettingsWindow<SheetSettings> implements DocumentListener, DataChangeListener {
    private static final Map<UUID, SheetSettingsWindow> INSTANCES = new HashMap<>();

    private GURPSCharacter               mCharacter;
    private SheetSettings                mSheetSettings;
    private Checkbox                     mUseMultiplicativeModifiers;
    private Checkbox                     mUseModifyingDicePlusAdds;
    private Checkbox                     mUseSimpleMetricConversions;
    private Checkbox                     mShowCollegeInSpells;
    private Checkbox                     mShowDifficulty;
    private Checkbox                     mShowAdvantageModifierAdj;
    private Checkbox                     mShowEquipmentModifierAdj;
    private Checkbox                     mShowSpellAdj;
    private Checkbox                     mShowTitleInsteadOfNameInPageFooter;
    private PopupMenu<DamageProgression> mDamageProgressionPopup;
    private PopupMenu<LengthUnits>       mLengthUnitsPopup;
    private PopupMenu<WeightUnits>       mWeightUnitsPopup;
    private PopupMenu<DisplayOption>     mUserDescriptionDisplayPopup;
    private PopupMenu<DisplayOption>     mModifiersDisplayPopup;
    private PopupMenu<DisplayOption>     mNotesDisplayPopup;
    private PopupMenu<DisplayOption>     mSkillLevelAdjustmentsPopup;
    private MultiLineTextField           mBlockLayoutField;
    private PageSettingsEditor           mPageSettingsEditor;
    private boolean                      mUpdatePending;

    /** Displays the sheet settings window. */
    public static void display(GURPSCharacter gchar) {
        if (!UIUtilities.inModalState()) {
            SheetSettingsWindow wnd;
            synchronized (INSTANCES) {
                UUID key = gchar == null ? null : gchar.getID();
                wnd = INSTANCES.get(key);
                if (wnd == null) {
                    wnd = new SheetSettingsWindow(gchar);
                    INSTANCES.put(key, wnd);
                }
            }
            wnd.setVisible(true);
        }
    }

    /** Closes the SheetSettingsWindow for the given character if it is open. */
    public static void closeFor(GURPSCharacter gchar) {
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window instanceof SheetSettingsWindow wnd) {
                if (wnd.mCharacter == gchar) {
                    wnd.attemptClose();
                }
            }
        }
    }

    private static String createTitle(GURPSCharacter character) {
        return character == null ? I18n.text("人物卡设置") : String.format(I18n.text("人物卡设置：%s"), character.getProfile().getName());
    }

    private SheetSettingsWindow(GURPSCharacter character) {
        super(createTitle(character));
        mCharacter = character;
        mSheetSettings = SheetSettings.get(mCharacter);
        if (mCharacter != null) {
            mCharacter.addChangeListener(this);
            Settings.getInstance().addChangeListener(this);
        }
        fill();
    }

    @Override
    protected void preDispose() {
        synchronized (INSTANCES) {
            INSTANCES.remove(mCharacter == null ? null : mCharacter.getID());
        }
        if (mCharacter != null) {
            mCharacter.removeChangeListener(this);
            Settings.getInstance().removeChangeListener(this);
        }
    }

    @Override
    protected Panel createContent() {
        Panel left = new Panel(new PrecisionLayout().setMargins(0).
                setVerticalSpacing(LayoutConstants.WINDOW_BORDER_INSET));
        left.add(createDamageProgressionPanel());
        left.add(createCheckBoxGroupPanel());
        left.add(createBlockLayoutPanel(), new PrecisionLayoutData().setFillAlignment().setGrabSpace(true));

        Panel right = new Panel(new PrecisionLayout().setMargins(0).
                setVerticalSpacing(LayoutConstants.WINDOW_BORDER_INSET));
        right.add(createUnitsOfMeasurePanel(), new PrecisionLayoutData().
                setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        right.add(createWhereToDisplayPanel(), new PrecisionLayoutData().
                setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        mPageSettingsEditor = new PageSettingsEditor(mSheetSettings.getPageSettings(), this::adjustResetButton);
        right.add(mPageSettingsEditor, new PrecisionLayoutData().setFillHorizontalAlignment().
                setGrabHorizontalSpace(true));

        Panel panel = new Panel(new PrecisionLayout().setColumns(2).
                setMargins(LayoutConstants.WINDOW_BORDER_INSET).
                setHorizontalSpacing(LayoutConstants.WINDOW_BORDER_INSET));
        panel.add(left, new PrecisionLayoutData().setFillVerticalAlignment().setGrabVerticalSpace(true));
        panel.add(right, new PrecisionLayoutData().setVerticalAlignment(PrecisionLayoutAlignment.BEGINNING));
        panel.setSize(panel.getPreferredSize());
        return panel;
    }

    private static <E> PopupMenu<E> addPopupMenu(Panel panel, E[] values, E choice, String title, String tooltip, PopupMenu.SelectionListener<E> listener) {
        PopupMenu<E> popup = new PopupMenu<>(values, listener);
        popup.setToolTipText(tooltip);
        popup.setSelectedItem(choice, false);
        panel.add(new Label(title), new PrecisionLayoutData().setFillHorizontalAlignment());
        panel.add(popup);
        return popup;
    }

    private static Checkbox addCheckbox(Panel panel, String title, String tooltip, boolean checked, Checkbox.ClickFunction clickFunction) {
        Checkbox checkbox = new Checkbox(title, checked, clickFunction);
        checkbox.setToolTipText(tooltip);
        panel.add(checkbox, new PrecisionLayoutData().setHorizontalSpan(2));
        return checkbox;
    }

    private Panel createDamageProgressionPanel() {
        Panel             panel                    = new Panel(new PrecisionLayout().setColumns(2).setMargins(0));
        DamageProgression currentDamageProgression = mSheetSettings.getDamageProgression();
        mDamageProgressionPopup = addPopupMenu(panel, DamageProgression.values(),
                currentDamageProgression, I18n.text("伤害递进"),
                currentDamageProgression.getTooltip(), (p) -> {
                    DamageProgression progression = mDamageProgressionPopup.getSelectedItem();
                    if (progression != null) {
                        mDamageProgressionPopup.setToolTipText(progression.getTooltip());
                        mSheetSettings.setDamageProgression(progression);
                        adjustResetButton();
                    }
                });
        return panel;
    }

    private Panel createCheckBoxGroupPanel() {
        Panel panel = new Panel(new PrecisionLayout().setMargins(0));
        mShowCollegeInSpells = addCheckbox(panel, I18n.text("显示学院栏"), null,
                mSheetSettings.showCollegeInSpells(), (b) -> {
                    mSheetSettings.setShowCollegeInSpells(b.isChecked());
                    adjustResetButton();
                });
        mShowDifficulty = addCheckbox(panel, I18n.text("显示难度栏"), null,
                mSheetSettings.showDifficulty(), (b) -> {
                    mSheetSettings.setShowDifficulty(b.isChecked());
                    adjustResetButton();
                });
        mShowAdvantageModifierAdj = addCheckbox(panel,
                I18n.text("显示优势修正因子花费栏"), null,
                mSheetSettings.showAdvantageModifierAdj(), (b) -> {
                    mSheetSettings.setShowAdvantageModifierAdj(b.isChecked());
                    adjustResetButton();
                });
        mShowEquipmentModifierAdj = addCheckbox(panel,
                I18n.text("显示装备修正因子花费栏以及重量调整"), null,
                mSheetSettings.showEquipmentModifierAdj(), (b) -> {
                    mSheetSettings.setShowEquipmentModifierAdj(b.isChecked());
                    adjustResetButton();
                });
        mShowSpellAdj = addCheckbox(panel, I18n.text("显示法术仪式，花费以及时间调整"),
                null, mSheetSettings.showSpellAdj(), (b) -> {
                    mSheetSettings.setShowSpellAdj(b.isChecked());
                    adjustResetButton();
                });
        mShowTitleInsteadOfNameInPageFooter = addCheckbox(panel,
                I18n.text("在脚注显示头衔而非姓名"), null,
                mSheetSettings.useTitleInFooter(), (b) -> {
                    mSheetSettings.setUseTitleInFooter(b.isChecked());
                    adjustResetButton();
                });
        mUseMultiplicativeModifiers = addCheckbox(panel,
                I18n.text("使修正因子相乘 (PW102；改变点数价值)"), null,
                mSheetSettings.useMultiplicativeModifiers(), (b) -> {
                    mSheetSettings.setUseMultiplicativeModifiers(b.isChecked());
                    adjustResetButton();
                });
        mUseModifyingDicePlusAdds = addCheckbox(panel, I18n.text("使用调整值骰子+价值 (B269)"),
                null, mSheetSettings.useModifyingDicePlusAdds(), (b) -> {
                    mSheetSettings.setUseModifyingDicePlusAdds(b.isChecked());
                    adjustResetButton();
                });
        return panel;
    }

    private Panel createBlockLayoutPanel() {
        Panel panel  = new Panel(new PrecisionLayout().setMargins(0));
        Label header = new Label(I18n.text("块布局"));
        header.setThemeFont(Fonts.HEADER);
        panel.add(header);
        mBlockLayoutField = new MultiLineTextField(Settings.linesToString(mSheetSettings.blockLayout()),
                I18n.text("指定人物卡上数据块的布局"), this);
        panel.add(mBlockLayoutField, new PrecisionLayoutData().setFillAlignment().setGrabSpace(true));
        return panel;
    }

    private Panel createUnitsOfMeasurePanel() {
        Panel panel  = new Panel(new PrecisionLayout().setColumns(2).setMargins(0));
        Label header = new Label(I18n.text("测量单位"));
        header.setThemeFont(Fonts.HEADER);
        panel.add(header, new PrecisionLayoutData().setHorizontalSpan(2));
        panel.add(new Separator(), new PrecisionLayoutData().setFillHorizontalAlignment().
                setGrabHorizontalSpace(true).setHorizontalSpan(2).
                setBottomMargin(LayoutConstants.TOOLBAR_VERTICAL_INSET / 2));
        mLengthUnitsPopup = addPopupMenu(panel, LengthUnits.values(),
                mSheetSettings.defaultLengthUnits(), I18n.text("长度单位"),
                I18n.text("生成长度显示的单位"), (p) -> {
                    mSheetSettings.setDefaultLengthUnits(mLengthUnitsPopup.getSelectedItem());
                    adjustResetButton();
                });
        mWeightUnitsPopup = addPopupMenu(panel, WeightUnits.values(),
                mSheetSettings.defaultWeightUnits(), I18n.text("重量单位"),
                I18n.text("生成重量显示的单位"), (p) -> {
                    mSheetSettings.setDefaultWeightUnits(mWeightUnitsPopup.getSelectedItem());
                    adjustResetButton();
                });
        mUseSimpleMetricConversions = addCheckbox(panel,
                I18n.text("使用简单的公制转化规则 (B9)"), null,
                mSheetSettings.useSimpleMetricConversions(), (b) -> {
                    mSheetSettings.setUseSimpleMetricConversions(b.isChecked());
                    adjustResetButton();
                });
        return panel;
    }

    private Panel createWhereToDisplayPanel() {
        Panel panel  = new Panel(new PrecisionLayout().setColumns(2).setMargins(0));
        Label header = new Label(I18n.text("在哪里显示……"));
        header.setThemeFont(Fonts.HEADER);
        panel.add(header, new PrecisionLayoutData().setHorizontalSpan(2));
        panel.add(new Separator(), new PrecisionLayoutData().setFillHorizontalAlignment().
                setGrabHorizontalSpace(true).setHorizontalSpan(2).
                setBottomMargin(LayoutConstants.TOOLBAR_VERTICAL_INSET / 2));
        String tooltip = I18n.text("在哪里显示信息");
        mUserDescriptionDisplayPopup = addPopupMenu(panel, DisplayOption.values(),
                mSheetSettings.userDescriptionDisplay(), I18n.text("用户描述"),
                tooltip, (p) -> {
                    mSheetSettings.setUserDescriptionDisplay(mUserDescriptionDisplayPopup.getSelectedItem());
                    adjustResetButton();
                });
        mModifiersDisplayPopup = addPopupMenu(panel, DisplayOption.values(),
                mSheetSettings.modifiersDisplay(), I18n.text("修正值"), tooltip, (p) -> {
                    mSheetSettings.setModifiersDisplay(mModifiersDisplayPopup.getSelectedItem());
                    adjustResetButton();
                });
        mNotesDisplayPopup = addPopupMenu(panel, DisplayOption.values(), mSheetSettings.notesDisplay(),
                I18n.text("备注"), tooltip, (p) -> {
                    mSheetSettings.setNotesDisplay(mNotesDisplayPopup.getSelectedItem());
                    adjustResetButton();
                });
        mSkillLevelAdjustmentsPopup = addPopupMenu(panel, DisplayOption.values(), mSheetSettings.skillLevelAdjustmentsDisplay(),
                I18n.text("技能等级修正"), tooltip, (p) -> {
                    mSheetSettings.setSkillLevelAdjustmentsDisplay(mSkillLevelAdjustmentsPopup.getSelectedItem());
                    adjustResetButton();
                });
        return panel;
    }

    @Override
    public void establishSizing() {
        pack();
        int width = getSize().width;
        setMinimumSize(new Dimension(width, 200));
        setMaximumSize(new Dimension(width, 10000));
    }

    @Override
    protected boolean shouldResetBeEnabled() {
        SheetSettings defaults   = new SheetSettings(mCharacter);
        boolean       atDefaults = mUseModifyingDicePlusAdds.isChecked() == defaults.useModifyingDicePlusAdds();
        atDefaults = atDefaults && mShowCollegeInSpells.isChecked() == defaults.showCollegeInSpells();
        atDefaults = atDefaults && mShowDifficulty.isChecked() == defaults.showDifficulty();
        atDefaults = atDefaults && mShowAdvantageModifierAdj.isChecked() == defaults.showAdvantageModifierAdj();
        atDefaults = atDefaults && mShowEquipmentModifierAdj.isChecked() == defaults.showEquipmentModifierAdj();
        atDefaults = atDefaults && mShowSpellAdj.isChecked() == defaults.showSpellAdj();
        atDefaults = atDefaults && mShowTitleInsteadOfNameInPageFooter.isChecked() == defaults.useTitleInFooter();
        atDefaults = atDefaults && mUseMultiplicativeModifiers.isChecked() == defaults.useMultiplicativeModifiers();
        atDefaults = atDefaults && mDamageProgressionPopup.getSelectedItem() == defaults.getDamageProgression();
        atDefaults = atDefaults && mUseSimpleMetricConversions.isChecked() == defaults.useSimpleMetricConversions();
        atDefaults = atDefaults && mLengthUnitsPopup.getSelectedItem() == defaults.defaultLengthUnits();
        atDefaults = atDefaults && mWeightUnitsPopup.getSelectedItem() == defaults.defaultWeightUnits();
        atDefaults = atDefaults && mUserDescriptionDisplayPopup.getSelectedItem() == defaults.userDescriptionDisplay();
        atDefaults = atDefaults && mModifiersDisplayPopup.getSelectedItem() == defaults.modifiersDisplay();
        atDefaults = atDefaults && mNotesDisplayPopup.getSelectedItem() == defaults.notesDisplay();
        atDefaults = atDefaults && mSkillLevelAdjustmentsPopup.getSelectedItem() == defaults.skillLevelAdjustmentsDisplay();
        atDefaults = atDefaults && mBlockLayoutField.getText().equals(Settings.linesToString(defaults.blockLayout()));
        atDefaults = atDefaults && mSheetSettings.getPageSettings().equals(defaults.getPageSettings());
        return !atDefaults;
    }

    @Override
    protected String getResetButtonTooltip() {
        return mCharacter == null ? super.getResetButtonTooltip() : I18n.text("重设到全局默认值");
    }

    @Override
    protected SheetSettings getResetData() {
        return new SheetSettings(mCharacter);
    }

    @Override
    protected void doResetTo(SheetSettings data) {
        mSheetSettings.copyFrom(data);
        mUseModifyingDicePlusAdds.setChecked(data.useModifyingDicePlusAdds());
        mShowCollegeInSpells.setChecked(data.showCollegeInSpells());
        mShowDifficulty.setChecked(data.showDifficulty());
        mShowAdvantageModifierAdj.setChecked(data.showAdvantageModifierAdj());
        mShowEquipmentModifierAdj.setChecked(data.showEquipmentModifierAdj());
        mShowSpellAdj.setChecked(data.showSpellAdj());
        mShowTitleInsteadOfNameInPageFooter.setChecked(data.useTitleInFooter());
        mUseMultiplicativeModifiers.setChecked(data.useMultiplicativeModifiers());
        DamageProgression progression = data.getDamageProgression();
        mDamageProgressionPopup.setSelectedItem(progression, true);
        mUseSimpleMetricConversions.setChecked(data.useSimpleMetricConversions());
        mLengthUnitsPopup.setSelectedItem(data.defaultLengthUnits(), true);
        mWeightUnitsPopup.setSelectedItem(data.defaultWeightUnits(), true);
        mUserDescriptionDisplayPopup.setSelectedItem(data.userDescriptionDisplay(), true);
        mModifiersDisplayPopup.setSelectedItem(data.modifiersDisplay(), true);
        mNotesDisplayPopup.setSelectedItem(data.notesDisplay(), true);
        mSkillLevelAdjustmentsPopup.setSelectedItem(data.skillLevelAdjustmentsDisplay(), true);
        mBlockLayoutField.setText(Settings.linesToString(data.blockLayout()));
        mPageSettingsEditor.resetTo(data.getPageSettings());
    }

    @Override
    public void dataWasChanged() {
        if (!mUpdatePending) {
            mUpdatePending = true;
            EventQueue.invokeLater(() -> {
                setTitle(createTitle(mCharacter));
                adjustResetButton();
                mUpdatePending = false;
            });
        }
    }

    @Override
    public void insertUpdate(DocumentEvent event) {
        changedUpdate(event);
    }

    @Override
    public void removeUpdate(DocumentEvent event) {
        changedUpdate(event);
    }

    @Override
    public void changedUpdate(DocumentEvent event) {
        mSheetSettings.setBlockLayout(List.of(mBlockLayoutField.getText().split("\n")));
        adjustResetButton();
    }

    @Override
    protected Dirs getDir() {
        return Dirs.SETTINGS;
    }

    @Override
    protected FileType getFileType() {
        return FileType.SHEET_SETTINGS;
    }

    @Override
    protected SheetSettings createSettingsFrom(Path path) throws IOException {
        return new SheetSettings(path);
    }

    @Override
    protected void exportSettingsTo(Path path) throws IOException {
        mSheetSettings.save(path);
    }
}
