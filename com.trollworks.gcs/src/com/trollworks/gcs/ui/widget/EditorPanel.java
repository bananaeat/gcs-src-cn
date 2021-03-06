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

package com.trollworks.gcs.ui.widget;

import com.trollworks.gcs.attribute.AttributeChoice;
import com.trollworks.gcs.attribute.AttributeDef;
import com.trollworks.gcs.criteria.DoubleCriteria;
import com.trollworks.gcs.criteria.IntegerCriteria;
import com.trollworks.gcs.criteria.NumericCompareType;
import com.trollworks.gcs.criteria.NumericCriteria;
import com.trollworks.gcs.criteria.StringCompareType;
import com.trollworks.gcs.criteria.StringCriteria;
import com.trollworks.gcs.criteria.WeightCriteria;
import com.trollworks.gcs.datafile.DataFile;
import com.trollworks.gcs.ui.UIUtilities;
import com.trollworks.gcs.ui.border.EmptyBorder;
import com.trollworks.gcs.utility.text.IntegerFormatter;
import com.trollworks.gcs.utility.text.WeightFormatter;
import com.trollworks.gcs.utility.units.WeightValue;

import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

/** A generic editor panel. */
public abstract class EditorPanel extends ActionPanel implements EditorField.ChangeListener {
    private static final int GAP = 4;

    /** Creates a new EditorPanel. */
    protected EditorPanel() {
        this(0);
    }

    /**
     * Creates a new EditorPanel.
     *
     * @param indent The amount of indent to apply to the left side, if any.
     */
    protected EditorPanel(int indent) {
        setOpaque(false);
        setBorder(new EmptyBorder(GAP, GAP + indent, GAP, GAP));
    }

    protected PopupMenu<AttributeChoice> addAttributePopup(DataFile dataFile, String format, String attribute, boolean includeBlank, PopupMenu.SelectionListener<AttributeChoice> listener) {
        List<AttributeChoice> list = new ArrayList<>();
        if (includeBlank) {
            list.add(new AttributeChoice(" ", format, " "));
        }
        for (AttributeDef def : AttributeDef.getOrdered(dataFile.getSheetSettings().getAttributes())) {
            list.add(new AttributeChoice(def.getID(), format, def.getName()));
        }
        list.add(new AttributeChoice("sm", format, "Size Modifier"));
        list.add(new AttributeChoice("dodge", format, "Dodge"));
        list.add(new AttributeChoice("parry", format, "Parry"));
        list.add(new AttributeChoice("block", format, "Block"));
        AttributeChoice current = null;
        for (AttributeChoice attributeChoice : list) {
            if (attributeChoice.getAttribute().equals(attribute)) {
                current = attributeChoice;
                break;
            }
        }
        if (current == null) {
            list.add(new AttributeChoice(attribute, format, attribute));
            current = list.get(list.size() - 1);
        }
        PopupMenu<AttributeChoice> popup = new PopupMenu<>(list, listener);
        popup.setSelectedItem(current, false);
        add(popup);
        return popup;
    }

    protected PopupMenu<String> addStringComparePopup(StringCriteria compare, String extra) {
        List<String> list      = new ArrayList<>();
        String       selection = null;
        for (StringCompareType type : StringCompareType.values()) {
            if (extra == null) {
                list.add(type.toString());
            } else {
                String title = extra + type;
                list.add(title);
                if (type == compare.getType()) {
                    selection = title;
                }
            }
        }
        if (extra == null) {
            selection = compare.getType().toString();
        }
        PopupMenu<String> popup = new PopupMenu<>(list, (p) -> {
            compare.setType(StringCompareType.values()[p.getSelectedIndex()]);
            notifyActionListeners();
        });
        popup.setSelectedItem(selection, false);
        add(popup);
        return popup;
    }

    /**
     * @param compare The current string compare object.
     * @return The field that allows a string comparison to be changed.
     */
    protected EditorField addStringCompareField(StringCriteria compare) {
        DefaultFormatter formatter = new DefaultFormatter();
        formatter.setOverwriteMode(false);
        EditorField field = new EditorField(new DefaultFormatterFactory(formatter), this, SwingConstants.LEFT, compare.getQualifier(), null);
        field.putClientProperty(StringCriteria.class, compare);
        add(field);
        return field;
    }

    protected PopupMenu<String> addNumericComparePopup(NumericCriteria compare, String extra) {
        String       selection = null;
        List<String> list      = new ArrayList<>();
        for (NumericCompareType type : NumericCompareType.values()) {
            String title = extra == null ? type.toString() : extra + type.getDescription();
            list.add(title);
            if (type == compare.getType()) {
                selection = title;
            }
        }
        PopupMenu<String> popup = new PopupMenu<>(list, (p) -> {
            compare.setType(NumericCompareType.values()[p.getSelectedIndex()]);
            notifyActionListeners();
        });
        popup.setSelectedItem(selection, false);
        add(popup);
        return popup;
    }

    /**
     * @param compare   The current compare object.
     * @param min       The minimum value to allow.
     * @param max       The maximum value to allow.
     * @param forceSign Whether to force the sign to be visible.
     * @return The {@link EditorField} that allows an integer comparison to be changed.
     */
    protected EditorField addNumericCompareField(IntegerCriteria compare, int min, int max, boolean forceSign) {
        EditorField field = new EditorField(new DefaultFormatterFactory(new IntegerFormatter(min, max, forceSign)), this, SwingConstants.LEFT, Integer.valueOf(compare.getQualifier()), Integer.valueOf(max), null);
        field.putClientProperty(IntegerCriteria.class, compare);
        UIUtilities.setToPreferredSizeOnly(field);
        add(field);
        return field;
    }

    /**
     * @param compare The current compare object.
     * @return The {@link EditorField} that allows a weight comparison to be changed.
     */
    protected EditorField addWeightCompareField(WeightCriteria compare) {
        EditorField field = new EditorField(new DefaultFormatterFactory(new WeightFormatter(true)), this, SwingConstants.LEFT, compare.getQualifier(), null, null);
        field.putClientProperty(WeightCriteria.class, compare);
        add(field);
        return field;
    }

    @Override
    public void editorFieldChanged(EditorField field) {
        StringCriteria criteria = (StringCriteria) field.getClientProperty(StringCriteria.class);
        if (criteria != null) {
            criteria.setQualifier((String) field.getValue());
            notifyActionListeners();
        } else {
            IntegerCriteria integerCriteria = (IntegerCriteria) field.getClientProperty(IntegerCriteria.class);
            if (integerCriteria != null) {
                integerCriteria.setQualifier(((Integer) field.getValue()).intValue());
                notifyActionListeners();
            } else {
                DoubleCriteria doubleCriteria = (DoubleCriteria) field.getClientProperty(DoubleCriteria.class);
                if (doubleCriteria != null) {
                    doubleCriteria.setQualifier(((Double) field.getValue()).doubleValue());
                    notifyActionListeners();
                } else {
                    WeightCriteria weightCriteria = (WeightCriteria) field.getClientProperty(WeightCriteria.class);
                    if (weightCriteria != null) {
                        weightCriteria.setQualifier((WeightValue) field.getValue());
                        notifyActionListeners();
                    }
                }
            }
        }
    }
}
