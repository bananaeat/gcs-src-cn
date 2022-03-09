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

import com.trollworks.gcs.character.FieldFactory;
import com.trollworks.gcs.menu.file.ExportToGCalcCommand;
import com.trollworks.gcs.pageref.PDFViewer;
import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.FontAwesome;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.layout.PrecisionLayout;
import com.trollworks.gcs.ui.layout.PrecisionLayoutData;
import com.trollworks.gcs.ui.scale.Scales;
import com.trollworks.gcs.ui.widget.Button;
import com.trollworks.gcs.ui.widget.Checkbox;
import com.trollworks.gcs.ui.widget.EditorField;
import com.trollworks.gcs.ui.widget.FontIconButton;
import com.trollworks.gcs.ui.widget.Label;
import com.trollworks.gcs.ui.widget.LayoutConstants;
import com.trollworks.gcs.ui.widget.Panel;
import com.trollworks.gcs.ui.widget.PopupMenu;
import com.trollworks.gcs.ui.widget.Wrapper;
import com.trollworks.gcs.utility.Dirs;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Log;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import javax.swing.SwingConstants;

public final class GeneralSettingsWindow extends SettingsWindow<GeneralSettings> {
    private static GeneralSettingsWindow INSTANCE;

    private EditorField            mPlayerName;
    private EditorField            mTechLevel;
    private EditorField            mInitialPoints;
    private Checkbox               mAutoFillProfile;
    private PopupMenu<Scales>      mInitialScale;
    private EditorField            mToolTipInitialDelayMilliseconds;
    private EditorField            mToolTipDismissDelaySeconds;
    private EditorField            mToolTipReshowDelayMilliseconds;
    private EditorField            mImageResolution;
    private Checkbox               mIncludeUnspentPointsInTotal;
    private EditorField            mGCalcKey;
    private PopupMenu<CalendarRef> mCalendar;
    private PopupMenu<PDFViewer>   mPDFViewer;
    private Label                  mPDFInstall;
    private Label                  mPDFLink;
    private Button                 mResetButton;

    /** Displays the general settings window. */
    public static void display() {
        if (!UIUtilities.inModalState()) {
            GeneralSettingsWindow wnd;
            synchronized (GeneralSettingsWindow.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GeneralSettingsWindow();
                }
                wnd = INSTANCE;
            }
            wnd.setVisible(true);
        }
    }

    private GeneralSettingsWindow() {
        super(I18n.text("一般设置"));
        fill();
    }

    @Override
    protected void preDispose() {
        synchronized (GeneralSettingsWindow.class) {
            INSTANCE = null;
        }
    }

    @Override
    protected Panel createContent() {
        GeneralSettings settings = Settings.getInstance().getGeneralSettings();
        Panel           panel    = new Panel(new PrecisionLayout().setColumns(3).setMargins(LayoutConstants.WINDOW_BORDER_INSET));

        // First row
        mPlayerName = new EditorField(FieldFactory.STRING, (f) -> {
            Settings.getInstance().getGeneralSettings().setDefaultPlayerName(f.getText().trim());
            adjustResetButton();
        }, SwingConstants.LEFT, settings.getDefaultPlayerName(),
                I18n.text("当新任务卡创建时使用的人物名"));
        panel.add(new Label(I18n.text("玩家")), new PrecisionLayoutData().setEndHorizontalAlignment());
        panel.add(mPlayerName, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));

        mAutoFillProfile = new Checkbox(I18n.text("输入起始描述"),
                settings.autoFillProfile(), (b) -> {
            Settings.getInstance().getGeneralSettings().setAutoFillProfile(b.isChecked());
            adjustResetButton();
        });
        mAutoFillProfile.setToolTipText(I18n.text("自动用随机生成选项填入人物的身份和描述信息"));
        mAutoFillProfile.setOpaque(false);
        panel.add(mAutoFillProfile, new PrecisionLayoutData().setLeftMargin(10));

        // Second row
        mTechLevel = new EditorField(FieldFactory.STRING, (f) -> {
            Settings.getInstance().getGeneralSettings().setDefaultTechLevel(f.getText().trim());
            adjustResetButton();
        }, SwingConstants.RIGHT, settings.getDefaultTechLevel(), "99+99^", getTechLevelTooltip());
        panel.add(new Label(I18n.text("科技等级(TL)")),
                new PrecisionLayoutData().setEndHorizontalAlignment());
        Wrapper wrapper = new Wrapper(new PrecisionLayout().setMargins(0).setColumns(3));
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        wrapper.add(mTechLevel, new PrecisionLayoutData().setFillHorizontalAlignment());

        mInitialPoints = new EditorField(FieldFactory.POSINT6, (f) -> {
            Settings.getInstance().getGeneralSettings().setInitialPoints(((Integer) f.getValue()).intValue());
            adjustResetButton();
        }, SwingConstants.RIGHT, Integer.valueOf(settings.getInitialPoints()), Integer.valueOf(999999),
                I18n.text("起始的人物点数"));
        wrapper.add(new Label(I18n.text("起始点数")),
                new PrecisionLayoutData().setFillHorizontalAlignment().setLeftMargin(10));
        wrapper.add(mInitialPoints, new PrecisionLayoutData().setFillHorizontalAlignment());

        mIncludeUnspentPointsInTotal = new Checkbox(I18n.text("在总点数里包括未消耗的点数"),
                settings.includeUnspentPointsInTotal(), (b) -> {
            Settings s = Settings.getInstance();
            s.getGeneralSettings().setIncludeUnspentPointsInTotal(b.isChecked(), s);
            adjustResetButton();
        });
        mIncludeUnspentPointsInTotal.setToolTipText(I18n.text("在人物总点数里包括未消耗的点数"));
        mIncludeUnspentPointsInTotal.setOpaque(false);
        panel.add(mIncludeUnspentPointsInTotal, new PrecisionLayoutData().setLeftMargin(10));

        // Third row
        mCalendar = new PopupMenu<>(CalendarRef.choices(), (p) -> {
            Settings.getInstance().getGeneralSettings().setCalendarRef(p.getSelectedItem().name());
            adjustResetButton();
        });
        mCalendar.setSelectedItem(CalendarRef.current(), false);
        panel.add(new Label(I18n.text("日历")),
                new PrecisionLayoutData().setEndHorizontalAlignment());
        panel.add(mCalendar, new PrecisionLayoutData().setHorizontalSpan(2));

        // Fourth row
        mInitialScale = new PopupMenu<>(Scales.values(), (p) -> {
            Settings.getInstance().getGeneralSettings().setInitialUIScale(p.getSelectedItem());
            adjustResetButton();
        });
        mInitialScale.setSelectedItem(settings.getInitialUIScale(), false);
        panel.add(new Label(I18n.text("起始尺寸")),
                new PrecisionLayoutData().setEndHorizontalAlignment());
        wrapper = new Wrapper(new PrecisionLayout().setMargins(0).setColumns(4));
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().
                setGrabHorizontalSpace(true).setHorizontalSpan(2));
        wrapper.add(mInitialScale);

        mImageResolution = new EditorField(FieldFactory.OUTPUT_DPI, (f) -> {
            Settings.getInstance().getGeneralSettings().setImageResolution(((Integer) f.getValue()).intValue());
            adjustResetButton();
        }, SwingConstants.RIGHT, Integer.valueOf(settings.getImageResolution()),
                FieldFactory.getMaxValue(FieldFactory.OUTPUT_DPI),
                I18n.text("分辨率，单位为dpi，用来将人物卡保存为PNG文件"));
        wrapper.add(new Label(I18n.text("图片分辨率")),
                new PrecisionLayoutData().setFillHorizontalAlignment().setLeftMargin(10));
        wrapper.add(mImageResolution, new PrecisionLayoutData().setFillHorizontalAlignment());
        wrapper.add(new Label(I18n.text("dpi")));

        // Fifth row
        panel.add(new Label(I18n.text("提示弹窗初始延迟")),
                new PrecisionLayoutData().setEndHorizontalAlignment());
        wrapper = new Wrapper(new PrecisionLayout().setMargins(0).setColumns(2));
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().
                setGrabHorizontalSpace(true).setHorizontalSpan(2));
        mToolTipInitialDelayMilliseconds = new EditorField(FieldFactory.TOOLTIP_MILLIS_TIMEOUT, (f) -> {
            Settings.getInstance().getGeneralSettings().setToolTipInitialDelayMilliseconds(((Integer) f.getValue()).intValue());
            adjustResetButton();
        }, SwingConstants.RIGHT, Integer.valueOf(settings.getToolTipInitialDelayMilliseconds()),
                FieldFactory.getMaxValue(FieldFactory.TOOLTIP_MILLIS_TIMEOUT),
                I18n.text("在鼠标停留后需要多少毫秒才弹出提示弹窗"));
        wrapper.add(mToolTipInitialDelayMilliseconds, new PrecisionLayoutData().setFillHorizontalAlignment());
        wrapper.add(new Label(I18n.text("毫秒")));

        panel.add(new Label(I18n.text("提示弹窗消失延迟")),
                new PrecisionLayoutData().setEndHorizontalAlignment());
        wrapper = new Wrapper(new PrecisionLayout().setMargins(0).setColumns(2));
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().
                setGrabHorizontalSpace(true).setHorizontalSpan(2));
        mToolTipDismissDelaySeconds = new EditorField(FieldFactory.TOOLTIP_TIMEOUT, (f) -> {
            Settings.getInstance().getGeneralSettings().setToolTipDismissDelaySeconds(((Integer) f.getValue()).intValue());
            adjustResetButton();
        }, SwingConstants.RIGHT, Integer.valueOf(settings.getToolTipDismissDelaySeconds()),
                FieldFactory.getMaxValue(FieldFactory.TOOLTIP_TIMEOUT),
                I18n.text("在鼠标离开后需要多少秒提示弹窗才消失"));
        wrapper.add(mToolTipDismissDelaySeconds, new PrecisionLayoutData().setFillHorizontalAlignment());
        wrapper.add(new Label(I18n.text("秒")));

        panel.add(new Label(I18n.text("提示弹窗重新显示延迟")),
                new PrecisionLayoutData().setEndHorizontalAlignment());
        wrapper = new Wrapper(new PrecisionLayout().setMargins(0).setColumns(2));
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().
                setGrabHorizontalSpace(true).setHorizontalSpan(2));
        mToolTipReshowDelayMilliseconds = new EditorField(FieldFactory.TOOLTIP_MILLIS_TIMEOUT, (f) -> {
            Settings.getInstance().getGeneralSettings().setToolTipReshowDelayMilliseconds(((Integer) f.getValue()).intValue());
            adjustResetButton();
        }, SwingConstants.RIGHT, Integer.valueOf(settings.getToolTipReshowDelayMilliseconds()),
                FieldFactory.getMaxValue(FieldFactory.TOOLTIP_MILLIS_TIMEOUT),
                I18n.text("在等待初始延迟之前，用户需要额外等待多少毫秒让提示弹窗出现。如果提示弹窗刚消失，且用户把在此延迟内将鼠标移动进同样的窗口成分，提示弹窗将会立刻显示。否则，如果用户在此延迟外移动鼠标进此窗口成分，则需额外等待起始延迟使提示弹窗弹出。"));
        wrapper.add(mToolTipReshowDelayMilliseconds, new PrecisionLayoutData().setFillHorizontalAlignment());
        wrapper.add(new Label(I18n.text("毫秒")));

        // Sixth row
        mPDFViewer = new PopupMenu<>(PDFViewer.valuesForPlatform(), (p) -> {
            PDFViewer pdfViewer = p.getSelectedItem();
            if (pdfViewer != null) {
                Settings.getInstance().getGeneralSettings().setPDFViewer(pdfViewer);
                updatePDFLinks(pdfViewer);
                adjustResetButton();
            }
        });
        PDFViewer pdfViewer = settings.getPDFViewer();
        mPDFViewer.setSelectedItem(pdfViewer, false);
        panel.add(new Label(I18n.text("PDF 浏览器")), new PrecisionLayoutData().setEndHorizontalAlignment());
        wrapper = new Wrapper(new PrecisionLayout().setMargins(0).setColumns(3));
        wrapper.add(mPDFViewer);
        mPDFInstall = new Label("");
        wrapper.add(mPDFInstall, new PrecisionLayoutData().setLeftMargin(10));
        mPDFLink = new Label("");
        mPDFLink.setForeground(Colors.ICON_BUTTON_PRESSED);
        mPDFLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mPDFLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                String text = mPDFLink.getText();
                if (!text.isBlank()) {
                    try {
                        Desktop.getDesktop().browse(new URI(text));
                    } catch (Exception ex) {
                        Log.error(ex);
                    }
                }
            }
        });
        wrapper.add(mPDFLink, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setHorizontalSpan(2));
        updatePDFLinks(pdfViewer);

        // Seventh row
        wrapper = new Wrapper(new PrecisionLayout().setMargins(0).setColumns(3));
        panel.add(wrapper, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true).setHorizontalSpan(3));
        mGCalcKey = new EditorField(FieldFactory.STRING, (f) -> {
            Settings.getInstance().getGeneralSettings().setGCalcKey(f.getText().trim());
            adjustResetButton();
        }, SwingConstants.LEFT, settings.getGCalcKey(), null);
        wrapper.add(new Label(I18n.text("GURPS 计算器密钥")), new PrecisionLayoutData().setEndHorizontalAlignment());
        wrapper.add(mGCalcKey, new PrecisionLayoutData().setFillHorizontalAlignment().setGrabHorizontalSpace(true));
        wrapper.add(new FontIconButton(FontAwesome.SEARCH,
                I18n.text("在GURPS计算器网站上寻找你的计算器密钥"),
                ExportToGCalcCommand::openBrowserToFindKey));

        return panel;
    }

    public static String getTechLevelTooltip() {
        return I18n.text("""
                TL0: 石器时代 (史前时代开始)
                TL1: 青铜时代 (3500 B.C.+)
                TL2: 铁器时代 (1200 B.C.+)
                TL3: 中世纪 (600 A.D.+)
                TL4: 航海时代 (1450+)
                TL5: 工业革命 (1730+)
                TL6: 机械化时代 (1880+)
                TL7: 核子时代 (1940+)
                TL8: 数字时代 (1980+)
                TL9: 微科技时代 (2025+?)
                TL10: 机器人时代 (2070+?)
                TL11: 超材料时代
                TL12: 由GM命名！""");
    }

    @Override
    public void establishSizing() {
        setResizable(false);
    }

    @Override
    protected boolean shouldResetBeEnabled() {
        return !Settings.getInstance().getGeneralSettings().equals(new GeneralSettings());
    }

    @Override
    protected GeneralSettings getResetData() {
        return new GeneralSettings();
    }

    @Override
    protected void doResetTo(GeneralSettings data) {
        GeneralSettings settings = Settings.getInstance().getGeneralSettings();
        settings.copyFrom(data);
        mPlayerName.setValue(settings.getDefaultPlayerName());
        mTechLevel.setValue(settings.getDefaultTechLevel());
        mInitialPoints.setValue(Integer.valueOf(settings.getInitialPoints()));
        mAutoFillProfile.setChecked(settings.autoFillProfile());
        mCalendar.setSelectedItem(CalendarRef.get(settings.calendarRef()), true);
        mInitialScale.setSelectedItem(settings.getInitialUIScale(), true);
        mToolTipInitialDelayMilliseconds.setValue(Integer.valueOf(settings.getToolTipInitialDelayMilliseconds()));
        mToolTipDismissDelaySeconds.setValue(Integer.valueOf(settings.getToolTipDismissDelaySeconds()));
        mToolTipReshowDelayMilliseconds.setValue(Integer.valueOf(settings.getToolTipReshowDelayMilliseconds()));
        mImageResolution.setValue(Integer.valueOf(settings.getImageResolution()));
        mIncludeUnspentPointsInTotal.setChecked(settings.includeUnspentPointsInTotal());
        mGCalcKey.setValue(settings.getGCalcKey());
        PDFViewer pdfViewer = settings.getPDFViewer();
        mPDFViewer.setSelectedItem(pdfViewer, true);
        updatePDFLinks(pdfViewer);
        settings.updateToolTipTiming();
    }

    private static String getInstallFromText() {
        return I18n.text("从这里安装：");
    }

    private void updatePDFLinks(PDFViewer viewer) {
        String from = viewer.installFrom();
        mPDFInstall.setText(from.isBlank() ? "" : getInstallFromText());
        mPDFInstall.revalidate();
        mPDFInstall.repaint();
        mPDFLink.setText(from);
        mPDFLink.revalidate();
        mPDFLink.repaint();
    }

    @Override
    protected Dirs getDir() {
        return Dirs.SETTINGS;
    }

    @Override
    protected FileType getFileType() {
        return FileType.GENERAL_SETTINGS;
    }

    @Override
    protected GeneralSettings createSettingsFrom(Path path) throws IOException {
        return new GeneralSettings(path);
    }

    @Override
    protected void exportSettingsTo(Path path) throws IOException {
        Settings.getInstance().getGeneralSettings().save(path);
    }
}
