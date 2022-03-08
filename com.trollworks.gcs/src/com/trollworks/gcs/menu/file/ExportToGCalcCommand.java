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

package com.trollworks.gcs.menu.file;

import com.trollworks.gcs.character.CharacterSheet;
import com.trollworks.gcs.character.GURPSCharacter;
import com.trollworks.gcs.character.SheetDockable;
import com.trollworks.gcs.character.TextTemplate;
import com.trollworks.gcs.menu.Command;
import com.trollworks.gcs.settings.QuickExport;
import com.trollworks.gcs.settings.Settings;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.widget.MessageType;
import com.trollworks.gcs.ui.widget.Modal;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Log;
import com.trollworks.gcs.utility.SaveType;
import com.trollworks.gcs.utility.json.JsonWriter;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public final class ExportToGCalcCommand extends Command {
    public static final  ExportToGCalcCommand INSTANCE     = new ExportToGCalcCommand();
    private static final String               BASE_URL     = "http://www.gurpscalculator.com";
    private static final Pattern              UUID_PATTERN = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}");

    public static void openBrowserToFindKey(Component parent) {
        String uri = BASE_URL + "/Character/ImportGCS";
        try {
            Desktop.getDesktop().browse(new URI(uri));
        } catch (Exception exception) {
            Modal.showError(parent, MessageFormat.format(I18n.text("无法打开{0}"), uri));
        }
    }

    private ExportToGCalcCommand() {
        super(I18n.text("GURPS计算器……"), "gCalcExport", KeyEvent.VK_L);
    }

    @Override
    public void adjust() {
        setEnabled(!UIUtilities.inModalState() && Command.getTarget(SheetDockable.class) != null);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        performExport(getTarget(SheetDockable.class));
    }

    public static void performExport(SheetDockable dockable) {
        if (dockable != null) {
            CharacterSheet sheet     = dockable.getSheet();
            GURPSCharacter character = sheet.getCharacter();
            String         key       = Settings.getInstance().getGeneralSettings().getGCalcKey();
            try {
                if ("true".equals(get(String.format("api/GetCharacterExists/%s/%s", character.getID(), key)))) {
                    Modal dialog = Modal.prepareToShowMessage(KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner(),
                            I18n.text("人物已经存在。"), MessageType.WARNING,
                            I18n.text("""
                                    这个人物已经在GURPS计算器中存在。
                                    你想要替换它吗？

                                    如果你选择新建，你应该
                                    之后保存你的人物。"""));
                    dialog.addButton(I18n.text("替换"), (btn) -> {
                        export(dockable);
                        dialog.setVisible(false);
                    });
                    dialog.addButton(I18n.text("新建"), (btn) -> {
                        character.generateNewID();
                        character.setModified(true);
                        export(dockable);
                        dialog.setVisible(false);
                    });
                    dialog.addCancelButton();
                    dialog.presentToUser();
                } else {
                    export(dockable);
                }
            } catch (Exception exception) {
                Log.error(exception);
                showResult(false);
            }
        }
    }

    private static void export(SheetDockable dockable) {
        CharacterSheet sheet = dockable.getSheet();
        try {
            File templateFile = File.createTempFile("gcalcTemplate", ".html");
            try {
                try (PrintWriter out = new PrintWriter(templateFile, StandardCharsets.UTF_8)) {
                    out.print(get("api/GetOutputTemplate"));
                }
                File outputFile = File.createTempFile("gcalcOutput", ".html");
                try {
                    if (new TextTemplate(sheet).export(outputFile.toPath(), templateFile.toPath())) {
                        String result = null;
                        try (Scanner scanner = new Scanner(outputFile, StandardCharsets.UTF_8)) {
                            result = scanner.useDelimiter("\\A").next();
                        } catch (FileNotFoundException exception) {
                            Log.error(exception);
                        }
                        GURPSCharacter character = sheet.getCharacter();
                        UUID           id        = character.getID();
                        String         key       = Settings.getInstance().getGeneralSettings().getGCalcKey();
                        String         path      = String.format("api/SaveCharacter/%s/%s", id, key);
                        result = post(path, result);
                        if (!result.isEmpty()) {
                            throw new IOException("Bad response from the web server for template write");
                        }
                        File image = File.createTempFile("gcalcImage", ".png");
                        try {
                            ImageIO.write(character.getProfile().getPortraitWithFallback().getRetina(), "png", image);
                            path = String.format("api/SaveCharacterImage/%s/%s", id, key);
                            result = post(path, Files.readAllBytes(image.toPath()));
                            if (!result.isEmpty()) {
                                throw new IOException("Bad response from the web server for image write");
                            }
                        } finally {
                            image.delete();
                        }
                        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                            try (JsonWriter w = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), "\t")) {
                                character.save(w, SaveType.NORMAL, false);
                            }
                            path = String.format("api/SaveCharacterRawFileGCS/%s/%s", id, key);
                            result = post(path, out.toByteArray());
                            if (!result.isEmpty()) {
                                throw new IOException("Bad response from the web server for GCS file write");
                            }
                        }
                        dockable.recordQuickExport(new QuickExport());
                        showResult(true);
                    } else {
                        showResult(false);
                    }
                } finally {
                    outputFile.delete();
                }
            } finally {
                templateFile.delete();
            }
        } catch (Exception exception) {
            Log.error(exception);
            showResult(false);
        }
    }

    private static void showResult(boolean success) {
        if (success) {
            Modal.showMessage(Command.getFocusOwner(), I18n.text("成功"), MessageType.NONE,
                    I18n.text("Export to GURPS Calculator was successful."));
        } else {
            String key = Settings.getInstance().getGeneralSettings().getGCalcKey();
            String message;
            if (key == null || !UUID_PATTERN.matcher(key).matches()) {
                message = I18n.text("你必须先在一般设置里设置GURPS计算器键值。");
            } else {
                message = I18n.text("导出到GURPS计算器的过程中发生了一个错误。\n请稍后重试。");
            }
            Modal.showError(Command.getFocusOwner(), message);
        }
    }

    public static String get(String path) throws IOException {
        URLConnection connection = prepare(path);
        return retrieveResponse(connection);
    }

    public static String post(String path, String body) throws IOException {
        URLConnection connection = prepare(path);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(body);
        }
        return retrieveResponse(connection);
    }

    public static String post(String path, byte[] body) throws IOException {
        URLConnection connection = prepare(path);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        try (DataOutputStream writer = new DataOutputStream(connection.getOutputStream())) {
            writer.write(body);
        }
        return retrieveResponse(connection);
    }

    private static URLConnection prepare(String path) throws IOException {
        URLConnection connection = new URL(BASE_URL + "/" + path + "/").openConnection();
        connection.setRequestProperty("Accept-Charset", StandardCharsets.UTF_8.toString());
        return connection;
    }

    private static String retrieveResponse(URLConnection connection) throws IOException {
        try (InputStream stream = connection.getInputStream()) {
            try (Scanner s = new Scanner(stream, StandardCharsets.UTF_8)) {
                s.useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            }
        }
    }
}
