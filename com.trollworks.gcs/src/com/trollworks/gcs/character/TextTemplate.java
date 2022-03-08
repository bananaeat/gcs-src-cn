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

package com.trollworks.gcs.character;

import com.trollworks.gcs.advantage.Advantage;
import com.trollworks.gcs.advantage.AdvantageColumn;
import com.trollworks.gcs.attribute.Attribute;
import com.trollworks.gcs.attribute.AttributeDef;
import com.trollworks.gcs.attribute.AttributeType;
import com.trollworks.gcs.attribute.PoolThreshold;
import com.trollworks.gcs.body.HitLocation;
import com.trollworks.gcs.body.HitLocationTable;
import com.trollworks.gcs.equipment.Equipment;
import com.trollworks.gcs.equipment.EquipmentColumn;
import com.trollworks.gcs.feature.DRBonus;
import com.trollworks.gcs.feature.Feature;
import com.trollworks.gcs.modifier.AdvantageModifier;
import com.trollworks.gcs.modifier.EquipmentModifier;
import com.trollworks.gcs.notes.Note;
import com.trollworks.gcs.settings.Settings;
import com.trollworks.gcs.skill.Skill;
import com.trollworks.gcs.skill.SkillColumn;
import com.trollworks.gcs.spell.Spell;
import com.trollworks.gcs.spell.SpellColumn;
import com.trollworks.gcs.ui.Colors;
import com.trollworks.gcs.ui.ThemeColor;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.ui.widget.outline.Row;
import com.trollworks.gcs.ui.widget.outline.RowIterator;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.FilteredIterator;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Log;
import com.trollworks.gcs.utility.PathUtils;
import com.trollworks.gcs.utility.text.Numbers;
import com.trollworks.gcs.utility.text.NumericComparator;
import com.trollworks.gcs.weapon.MeleeWeaponStats;
import com.trollworks.gcs.weapon.RangedWeaponStats;
import com.trollworks.gcs.weapon.WeaponDisplayRow;
import com.trollworks.gcs.weapon.WeaponStats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/** Provides text template output. */
public class TextTemplate {
    private static final String  UNIDENTIFIED_KEY   = "Unidentified key: '%s'";
    private static final String  UNRESOLVABLE_TYPE  = "Unresolvable type for key: '%s'";
    private static final String  CURRENT            = "current";
    private static final String  GROUP              = "GROUP";
    private static final String  ITEM               = "ITEM";
    private static final String  ONE                = "1";
    private static final String  UNDERSCORE         = "_";
    private static final String  PARAGRAPH_START    = "<p>";
    private static final String  PARAGRAPH_END      = "</p>";
    private static final String  NEWLINE            = "\n";
    private static final String  COMMA_SEPARATOR    = ", ";
    private static final Pattern NOT_NUMBER_PATTERN = Pattern.compile("[^0-9]");

    private static final String KEY_ACCURACY                     = "ACCURACY";
    private static final String KEY_ADVANTAGE_POINTS             = "ADVANTAGE_POINTS";
    private static final String KEY_AGE                          = "AGE";
    private static final String KEY_AMMO                         = "AMMO";
    private static final String KEY_AMMO_TYPE                    = "AmmoType:";
    private static final String KEY_ATTRIBUTE_POINTS             = "ATTRIBUTE_POINTS";
    private static final String KEY_BASIC_LIFT                   = "BASIC_LIFT";
    private static final String KEY_BASIC_MOVE                   = "BASIC_MOVE";
    private static final String KEY_BASIC_MOVE_POINTS            = "BASIC_MOVE_POINTS";
    private static final String KEY_BASIC_SPEED                  = "BASIC_SPEED";
    private static final String KEY_BASIC_SPEED_POINTS           = "BASIC_SPEED_POINTS";
    private static final String KEY_BEST_CURRENT_BLOCK           = "BEST_CURRENT_BLOCK";
    private static final String KEY_BEST_CURRENT_PARRY           = "BEST_CURRENT_PARRY";
    private static final String KEY_BIRTHDAY                     = "BIRTHDAY";
    private static final String KEY_BLOCK                        = "BLOCK";
    private static final String KEY_BODY_TYPE                    = "BODY_TYPE";
    private static final String KEY_BULK                         = "BULK";
    private static final String KEY_CARRIED_STATUS               = "CARRIED_STATUS";
    private static final String KEY_CARRIED_VALUE                = "CARRIED_VALUE";
    private static final String KEY_CARRIED_WEIGHT               = "CARRIED_WEIGHT";
    private static final String KEY_CARRY_ON_BACK                = "CARRY_ON_BACK";
    private static final String KEY_CATEGORIES                   = "CATEGORIES";
    private static final String KEY_CLASS                        = "CLASS";
    private static final String KEY_COLLEGE                      = "COLLEGE";
    private static final String KEY_COLOR_PREFIX                 = "COLOR_";
    private static final String KEY_COMBINED_NAME                = "COMBINED_NAME";
    private static final String KEY_CONTINUE_ID                  = "CONTINUE_ID";
    private static final String KEY_COST                         = "COST";
    private static final String KEY_COST_SUMMARY                 = "COST_SUMMARY";
    private static final String KEY_CREATED_ON                   = "CREATED_ON";
    private static final String KEY_CURRENT                      = "CURRENT";
    private static final String KEY_CURRENT_DODGE                = "CURRENT_DODGE";
    private static final String KEY_CURRENT_MARKER               = "CURRENT_MARKER";
    private static final String KEY_CURRENT_MARKER_1             = "CURRENT_MARKER_1";
    private static final String KEY_CURRENT_MARKER_BULLET        = "CURRENT_MARKER_BULLET";
    private static final String KEY_CURRENT_MOVE                 = "CURRENT_MOVE";
    private static final String KEY_DAMAGE                       = "DAMAGE";
    private static final String KEY_DESCRIPTION                  = "DESCRIPTION";
    private static final String KEY_DESCRIPTION_MODIFIER_NOTES   = "DESCRIPTION_MODIFIER_NOTES";
    private static final String KEY_DESCRIPTION_NOTES            = "DESCRIPTION_NOTES";
    private static final String KEY_DESCRIPTION_PRIMARY          = "DESCRIPTION_PRIMARY";
    private static final String KEY_DESCRIPTION_USER             = "DESCRIPTION_USER";
    private static final String KEY_DESCRIPTION_USER_FORMATTED   = "DESCRIPTION_USER_FORMATTED";
    private static final String KEY_DIFFICULTY                   = "DIFFICULTY";
    private static final String KEY_DISADVANTAGE_POINTS          = "DISADVANTAGE_POINTS";
    private static final String KEY_DODGE                        = "DODGE";
    private static final String KEY_DR                           = "DR";
    private static final String KEY_DR_TOOLTIP                   = "DR_TOOLTIP";
    private static final String KEY_DURATION                     = "DURATION";
    private static final String KEY_DX                           = "DX";
    private static final String KEY_DX_POINTS                    = "DX_POINTS";
    private static final String KEY_ENCODING_OFF                 = "ENCODING_OFF";
    private static final String KEY_ENHANCED_KEY_PARSING         = "ENHANCED_KEY_PARSING";
    private static final String KEY_LOCATION_EQUIPMENT           = "EQUIPMENT";
    private static final String KEY_LOCATION_EQUIPMENT_FORMATTED = "EQUIPMENT_FORMATTED";
    private static final String KEY_EQUIPPED                     = "EQUIPPED";
    private static final String KEY_EQUIPPED_FONT_AWESOME        = "EQUIPPED_FA";
    private static final String KEY_EQUIPPED_NUM                 = "EQUIPPED_NUM";
    private static final String KEY_EXCLUDE_CATEGORIES           = "EXCLUDE_CATEGORIES_";
    private static final String KEY_EYES                         = "EYES";
    private static final String KEY_FRIGHT_CHECK                 = "FRIGHT_CHECK";
    private static final String KEY_GENDER                       = "GENDER";
    private static final String KEY_GENERAL_DR                   = "GENERAL_DR";
    private static final String KEY_GRID_TEMPLATE                = "GRID_TEMPLATE";
    private static final String KEY_HAIR                         = "HAIR";
    private static final String KEY_HAND                         = "HAND";
    private static final String KEY_HEARING                      = "HEARING";
    private static final String KEY_HEIGHT                       = "HEIGHT";
    private static final String KEY_HT                           = "HT";
    private static final String KEY_HT_POINTS                    = "HT_POINTS";
    private static final String KEY_ID                           = "ID";
    private static final String KEY_IQ                           = "IQ";
    private static final String KEY_IQ_POINTS                    = "IQ_POINTS";
    private static final String KEY_LEGALITY_CLASS               = "LEGALITY_CLASS";
    private static final String KEY_LEVEL                        = "LEVEL";
    private static final String KEY_LEVEL_NO_MARKER              = "LEVEL_NO_MARKER";
    private static final String KEY_LEVEL_ONLY                   = "LEVEL_ONLY";
    private static final String KEY_LOCATION                     = "LOCATION";
    private static final String KEY_FULL_NAME                    = "FULL_NAME";
    private static final String KEY_MANA_CAST                    = "MANA_CAST";
    private static final String KEY_MANA_MAINTAIN                = "MANA_MAINTAIN";
    private static final String KEY_MAX_LOAD                     = "MAX_LOAD";
    private static final String KEY_MAX_USES                     = "MAX_USES";
    private static final String KEY_MAXIMUM                      = "MAXIMUM";
    private static final String KEY_MODIFIED_ON                  = "MODIFIED_ON";
    private static final String KEY_MODIFIER                     = "MODIFIER";
    private static final String KEY_MODIFIER_NOTES_FOR           = "MODIFIER_NOTES_FOR_";
    private static final String KEY_MOVE                         = "MOVE";
    private static final String KEY_NAME                         = "NAME";
    private static final String KEY_NOTE                         = "NOTE";
    private static final String KEY_NOTE_FORMATTED               = "NOTE_FORMATTED";
    private static final String KEY_ALL_NOTES_COMBINED           = "NOTES";
    private static final String KEY_ONE_HANDED_LIFT              = "ONE_HANDED_LIFT";
    private static final String KEY_ONLY_CATEGORIES              = "ONLY_CATEGORIES_";
    private static final String KEY_ORGANIZATION                 = "ORGANIZATION";
    private static final String KEY_OTHER_VALUE                  = "OTHER_EQUIPMENT_VALUE";
    private static final String KEY_PARENT_ID                    = "PARENT_ID";
    private static final String KEY_PARRY                        = "PARRY";
    private static final String KEY_PENALTY                      = "PENALTY";
    private static final String KEY_PERCEPTION                   = "PERCEPTION";
    private static final String KEY_PERCEPTION_POINTS            = "PERCEPTION_POINTS";
    private static final String KEY_PLAYER                       = "PLAYER";
    private static final String KEY_POINTS                       = "POINTS";
    private static final String KEY_PORTRAIT                     = "PORTRAIT";
    private static final String KEY_PORTRAIT_EMBEDDED            = "PORTRAIT_EMBEDDED";
    private static final String KEY_PREFIX_DEPTH                 = "DEPTHx";
    private static final String KEY_QTY                          = "QTY";
    private static final String KEY_QUIRK_POINTS                 = "QUIRK_POINTS";
    private static final String KEY_RACE_POINTS                  = "RACE_POINTS";
    private static final String KEY_RANGE                        = "RANGE";
    private static final String KEY_REACH                        = "REACH";
    private static final String KEY_RECOIL                       = "RECOIL";
    private static final String KEY_REF                          = "REF";
    private static final String KEY_RELIGION                     = "RELIGION";
    private static final String KEY_RESIST                       = "RESIST";
    private static final String KEY_ROF                          = "ROF";
    private static final String KEY_ROLL                         = "ROLL";
    private static final String KEY_RSL                          = "RSL";
    private static final String KEY_RUNNING_SHOVE                = "RUNNING_SHOVE";
    private static final String KEY_SATISFIED                    = "SATISFIED";
    private static final String KEY_SHIFT_SLIGHTLY               = "SHIFT_SLIGHTLY";
    private static final String KEY_SHOTS                        = "SHOTS";
    private static final String KEY_SHOVE                        = "SHOVE";
    private static final String KEY_SITUATION                    = "SITUATION";
    private static final String KEY_SIZE                         = "SIZE";
    private static final String KEY_SKILL_POINTS                 = "SKILL_POINTS";
    private static final String KEY_SKIN                         = "SKIN";
    private static final String KEY_SL                           = "SL";
    private static final String KEY_SPELL_POINTS                 = "SPELL_POINTS";
    private static final String KEY_ST                           = "ST";
    private static final String KEY_ST_POINTS                    = "ST_POINTS";
    private static final String KEY_STATE                        = "STATE";
    private static final String KEY_STYLE_INDENT_WARNING         = "STYLE_INDENT_WARNING";
    private static final String KEY_SWING                        = "SWING";
    private static final String KEY_TASTE_SMELL                  = "TASTE_SMELL";
    private static final String KEY_THRUST                       = "THRUST";
    private static final String KEY_TIME_CAST                    = "TIME_CAST";
    private static final String KEY_TITLE                        = "TITLE";
    private static final String KEY_TL                           = "TL";
    private static final String KEY_TOTAL_POINTS                 = "TOTAL_POINTS";
    private static final String KEY_TOUCH                        = "TOUCH";
    private static final String KEY_TWO_HANDED_LIFT              = "TWO_HANDED_LIFT";
    private static final String KEY_TYPE                         = "TYPE";
    private static final String KEY_UNMODIFIED_DAMAGE            = "UNMODIFIED_DAMAGE";
    private static final String KEY_UNSPENT_POINTS               = "UNSPENT_POINTS";
    private static final String KEY_USAGE                        = "USAGE";
    private static final String KEY_USES                         = "USES";
    private static final String KEY_USES_AMMO_TYPE               = "UsesAmmoType:";
    private static final String KEY_VALUE                        = "VALUE";
    private static final String KEY_VISION                       = "VISION";
    private static final String KEY_WEAPON_STRENGTH              = "STRENGTH";
    private static final String KEY_WEAPON_STRENGTH_NUM          = "WEAPON_STRENGTH";
    private static final String KEY_WEIGHT                       = "WEIGHT";
    private static final String KEY_WEIGHT_RAW                   = "WEIGHT_RAW";
    private static final String KEY_WEIGHT_SUMMARY               = "WEIGHT_SUMMARY";
    private static final String KEY_WHERE                        = "WHERE";
    private static final String KEY_WILL                         = "WILL";
    private static final String KEY_WILL_POINTS                  = "WILL_POINTS";

    // Loop key prefixes
    private static final String KEY_ADVANTAGES_ALL         = "ADVANTAGES_ALL";
    private static final String KEY_ADVANTAGES             = "ADVANTAGES";
    private static final String KEY_ADVANTAGES_ONLY        = "ADVANTAGES_ONLY";
    private static final String KEY_ATTACK_MODES           = "ATTACK_MODES";
    private static final String KEY_CONDITIONAL_MODIFIERS  = "CONDITIONAL_MODIFIERS";
    private static final String KEY_CULTURAL_FAMILIARITIES = "CULTURAL_FAMILIARITIES";
    private static final String KEY_DISADVANTAGES_ALL      = "DISADVANTAGES_ALL";
    private static final String KEY_DISADVANTAGES          = "DISADVANTAGES";
    private static final String KEY_ENCUMBRANCE            = "ENCUMBRANCE";
    private static final String KEY_EQUIPMENT              = "EQUIPMENT";
    private static final String KEY_HIERARCHICAL_MELEE     = "HIERARCHICAL_MELEE";
    private static final String KEY_HIERARCHICAL_RANGED    = "HIERARCHICAL_RANGED";
    private static final String KEY_HIT_LOCATION           = "HIT_LOCATION";
    private static final String KEY_LANGUAGES              = "LANGUAGES";
    private static final String KEY_MELEE                  = "MELEE";
    private static final String KEY_NOTES                  = "NOTES";
    private static final String KEY_OTHER_EQUIPMENT        = "OTHER_EQUIPMENT";
    private static final String KEY_PERKS                  = "PERKS";
    private static final String KEY_POINT_POOL             = "POINT_POOL";
    private static final String KEY_PRIMARY_ATTRIBUTE      = "PRIMARY_ATTRIBUTE";
    private static final String KEY_QUIRKS                 = "QUIRKS";
    private static final String KEY_RANGED                 = "RANGED";
    private static final String KEY_REACTION               = "REACTION";
    private static final String KEY_SECONDARY_ATTRIBUTE    = "SECONDARY_ATTRIBUTE";
    private static final String KEY_SKILLS                 = "SKILLS";
    private static final String KEY_SPELLS                 = "SPELLS";

    private static final String LOOP_START = "_LOOP_START";
    private static final String LOOP_END   = "_LOOP_END";
    private static final String LOOP_COUNT = "_LOOP_COUNT";

    private static final String KEY_SUFFIX_BRACKET = "_BRACKET";
    private static final String KEY_SUFFIX_CURLY   = "_CURLY";
    private static final String KEY_SUFFIX_PAREN   = "_PAREN";

    // TODO: Eliminate these deprecated keys after a suitable waiting period; added May 30, 2020
    private static final String KEY_EARNED_POINTS_DEPRECATED = "EARNED_POINTS";
    private static final String KEY_CAMPAIGN_DEPRECATED      = "CAMPAIGN";
    private static final String KEY_RACE_DEPRECATED          = "RACE";

    // TODO: Eliminate these deprecated keys after a suitable waiting period; added April 15, 2021
    private static final String KEY_BASIC_FP_DEPRECATED      = "BASIC_FP";
    private static final String KEY_BASIC_HP_DEPRECATED      = "BASIC_HP";
    private static final String KEY_DEAD_DEPRECATED          = "DEAD";
    private static final String KEY_DEATH_CHECK_1_DEPRECATED = "DEATH_CHECK_1";
    private static final String KEY_DEATH_CHECK_2_DEPRECATED = "DEATH_CHECK_2";
    private static final String KEY_DEATH_CHECK_3_DEPRECATED = "DEATH_CHECK_3";
    private static final String KEY_DEATH_CHECK_4_DEPRECATED = "DEATH_CHECK_4";
    private static final String KEY_FP_DEPRECATED            = "FP";
    private static final String KEY_FP_COLLAPSE_DEPRECATED   = "FP_COLLAPSE";
    private static final String KEY_FP_POINTS_DEPRECATED     = "FP_POINTS";
    private static final String KEY_HP_DEPRECATED            = "HP";
    private static final String KEY_HP_COLLAPSE_DEPRECATED   = "HP_COLLAPSE";
    private static final String KEY_HP_POINTS_DEPRECATED     = "HP_POINTS";
    private static final String KEY_REELING_DEPRECATED       = "REELING";
    private static final String KEY_TIRED_DEPRECATED         = "TIRED";
    private static final String KEY_UNCONSCIOUS_DEPRECATED   = "UNCONSCIOUS";

    // TODO: Eliminate these deprecated keys after a suitable waiting period; added May 12, 2021
    private static final String KEY_OPTIONS_CODE_DEPRECATED = "OPTIONS_CODE";

    private CharacterSheet mSheet;
    private boolean        mEncodeText         = true;
    private boolean        mEnhancedKeyParsing;
    private Set<String>    mOnlyCategories     = new HashSet<>();
    private Set<String>    mExcludedCategories = new HashSet<>();

    public TextTemplate(CharacterSheet sheet) {
        mSheet = sheet;
    }

    /**
     * @param exportTo The path to save to.
     * @param template The template to use.
     * @return {@code true} on success.
     */
    public boolean export(Path exportTo, Path template) {
        try {
            char[]        buffer           = new char[1];
            boolean       lookForKeyMarker = true;
            StringBuilder keyBuffer        = new StringBuilder();
            try (BufferedReader in = Files.newBufferedReader(template, StandardCharsets.UTF_8)) {
                try (BufferedWriter out = Files.newBufferedWriter(exportTo, StandardCharsets.UTF_8)) {
                    while (in.read(buffer) != -1) {
                        char ch = buffer[0];
                        if (lookForKeyMarker) {
                            if (ch == '@') {
                                lookForKeyMarker = false;
                                in.mark(1);
                            } else {
                                out.append(ch);
                            }
                        } else {
                            if (ch == '_' || Character.isLetterOrDigit(ch)) {
                                keyBuffer.append(ch);
                                in.mark(1);
                            } else {
                                if (!mEnhancedKeyParsing || ch != '@') {
                                    in.reset();        // Allow KEYs to be surrounded by @KEY@
                                }
                                emitKey(in, out, keyBuffer.toString(), exportTo);
                                keyBuffer.setLength(0);
                                lookForKeyMarker = true;
                            }
                        }
                    }
                    if (!keyBuffer.isEmpty()) {
                        emitKey(in, out, keyBuffer.toString(), exportTo);
                    }
                }
            }
            return true;
        } catch (Exception exception) {
            Log.error(exception);
            return false;
        }
    }

    private void emitKey(BufferedReader in, BufferedWriter out, String key, Path base) throws IOException {
        GURPSCharacter gurpsCharacter = mSheet.getCharacter();
        Profile        description    = gurpsCharacter.getProfile();
        switch (key) {
            case KEY_GRID_TEMPLATE:
                out.write(mSheet.getHTMLGridTemplate());
                break;
            case KEY_ENCODING_OFF:
                mEncodeText = false;
                break;
            case KEY_ENHANCED_KEY_PARSING:      // Turn on the ability to enclose a KEY with @.
                mEnhancedKeyParsing = true;     // ex: @KEY@. Useful for when output needs to
                break;                          // be embedded. ex: "<HTML@KEY@TAG>"
            case KEY_PORTRAIT:
                String fileName = PathUtils.enforceExtension(PathUtils.getLeafName(base, false), FileType.PNG.getExtension());
                ImageIO.write(description.getPortraitWithFallback().getRetina(), "png", base.resolveSibling(fileName).toFile());
                out.write(URLEncoder.encode(fileName, StandardCharsets.UTF_8));
                break;
            case KEY_PORTRAIT_EMBEDDED:
                out.write("data:image/png;base64,");
                ByteArrayOutputStream imgBuffer = new ByteArrayOutputStream();
                OutputStream wrapped = Base64.getEncoder().wrap(imgBuffer);
                ImageIO.write(description.getPortraitWithFallback().getRetina(), "png", wrapped);
                wrapped.close();
                out.write(imgBuffer.toString(StandardCharsets.UTF_8));
                break;
            case KEY_NAME:
                writeEncodedText(out, description.getName());
                break;
            case KEY_TITLE:
                writeEncodedText(out, description.getTitle());
                break;
            case KEY_ORGANIZATION:
                writeEncodedText(out, description.getOrganization());
                break;
            case KEY_RELIGION:
                writeEncodedText(out, description.getReligion());
                break;
            case KEY_PLAYER:
                writeEncodedText(out, description.getPlayerName());
                break;
            case KEY_CREATED_ON:
                writeEncodedText(out, Numbers.formatDateTime(Numbers.DATE_AT_TIME_FORMAT, gurpsCharacter.getCreatedOn() * FieldFactory.TIMESTAMP_FACTOR));
                break;
            case KEY_MODIFIED_ON:
                writeEncodedText(out, Numbers.formatDateTime(Numbers.DATE_AT_TIME_FORMAT, gurpsCharacter.getModifiedOn() * FieldFactory.TIMESTAMP_FACTOR));
                break;
            case KEY_TOTAL_POINTS:
                writeEncodedText(out, Numbers.format(Settings.getInstance().getGeneralSettings().includeUnspentPointsInTotal() ? gurpsCharacter.getTotalPoints() : gurpsCharacter.getSpentPoints()));
                break;
            case KEY_ATTRIBUTE_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributePoints()));
                break;
            case KEY_ST_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("st")));
                break;
            case KEY_DX_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("dx")));
                break;
            case KEY_IQ_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("iq")));
                break;
            case KEY_HT_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("ht")));
                break;
            case KEY_PERCEPTION_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("per")));
                break;
            case KEY_WILL_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("will")));
                break;
            case KEY_FP_POINTS_DEPRECATED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("fp")));
                break;
            case KEY_HP_POINTS_DEPRECATED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("hp")));
                break;
            case KEY_BASIC_SPEED_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("basic_speed")));
                break;
            case KEY_BASIC_MOVE_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCost("basic_move")));
                break;
            case KEY_ADVANTAGE_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAdvantagePoints()));
                break;
            case KEY_DISADVANTAGE_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getDisadvantagePoints()));
                break;
            case KEY_QUIRK_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getQuirkPoints()));
                break;
            case KEY_SKILL_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getSkillPoints()));
                break;
            case KEY_SPELL_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getSpellPoints()));
                break;
            case KEY_RACE_POINTS:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getRacePoints()));
                break;
            case KEY_UNSPENT_POINTS:
            case KEY_EARNED_POINTS_DEPRECATED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getUnspentPoints()));
                break;
            case KEY_HEIGHT:
                writeEncodedText(out, description.getHeight().toString());
                break;
            case KEY_HAIR:
                writeEncodedText(out, description.getHair());
                break;
            case KEY_GENDER:
                writeEncodedText(out, description.getGender());
                break;
            case KEY_WEIGHT:
                writeEncodedText(out, EquipmentColumn.getDisplayWeight(gurpsCharacter, description.getWeight()));
                break;
            case KEY_EYES:
                writeEncodedText(out, description.getEyeColor());
                break;
            case KEY_AGE:
                writeEncodedText(out, description.getAge());
                break;
            case KEY_SIZE:
                writeEncodedText(out, Numbers.formatWithForcedSign(description.getSizeModifier()));
                break;
            case KEY_SKIN:
                writeEncodedText(out, description.getSkinColor());
                break;
            case KEY_BIRTHDAY:
                writeEncodedText(out, description.getBirthday());
                break;
            case KEY_TL:
                writeEncodedText(out, description.getTechLevel());
                break;
            case KEY_HAND:
                writeEncodedText(out, description.getHandedness());
                break;
            case KEY_ST:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("st")));
                break;
            case KEY_DX:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("dx")));
                break;
            case KEY_IQ:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("iq")));
                break;
            case KEY_HT:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("ht")));
                break;
            case KEY_WILL:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("will")));
                break;
            case KEY_FRIGHT_CHECK:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("fright_check")));
                break;
            case KEY_BASIC_SPEED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeDoubleValue("basic_speed")));
                break;
            case KEY_BASIC_MOVE:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("basic_move")));
                break;
            case KEY_PERCEPTION:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("per")));
                break;
            case KEY_VISION:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("vision")));
                break;
            case KEY_HEARING:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("hearing")));
                break;
            case KEY_TASTE_SMELL:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("taste_smell")));
                break;
            case KEY_TOUCH:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("touch")));
                break;
            case KEY_THRUST:
                writeEncodedText(out, gurpsCharacter.getThrust().toString());
                break;
            case KEY_SWING:
                writeEncodedText(out, gurpsCharacter.getSwing().toString());
                break;
            case KEY_GENERAL_DR:
                int torsoDR = 0;
                HitLocation torsoLocation = gurpsCharacter.getSheetSettings().getHitLocations().lookupLocationByID("torso");
                if (torsoLocation != null) {
                    Map<String, Integer> dr  = torsoLocation.getDR(gurpsCharacter, null, null);
                    Integer              all = dr.get(DRBonus.ALL_SPECIALIZATION);
                    torsoDR = all == null ? 0 : all.intValue();
                }
                writeEncodedText(out, Numbers.format(torsoDR));
                break;
            case KEY_CURRENT_DODGE:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getDodge(gurpsCharacter.getEncumbranceLevel(false))));
                break;
            case KEY_CURRENT_MOVE:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getMove(gurpsCharacter.getEncumbranceLevel(false))));
                break;
            case KEY_BEST_CURRENT_PARRY:
                writeBestWeaponDefense(out, MeleeWeaponStats::getResolvedParryNoToolTip);
                break;
            case KEY_BEST_CURRENT_BLOCK:
                writeBestWeaponDefense(out, MeleeWeaponStats::getResolvedBlockNoToolTip);
                break;
            case KEY_FP_DEPRECATED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCurrentIntValue("fp")));
                break;
            case KEY_BASIC_FP_DEPRECATED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("fp")));
                break;
            case KEY_TIRED_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "fp", I18n.text("疲惫"));
                break;
            case KEY_FP_COLLAPSE_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "fp", I18n.text("累到倒下"));
                break;
            case KEY_UNCONSCIOUS_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "fp", I18n.text("失去意识"));
                break;
            case KEY_HP_DEPRECATED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeCurrentIntValue("hp")));
                break;
            case KEY_BASIC_HP_DEPRECATED:
                writeEncodedText(out, Numbers.format(gurpsCharacter.getAttributeIntValue("hp")));
                break;
            case KEY_REELING_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "hp", I18n.text("蹒跚"));
                break;
            case KEY_HP_COLLAPSE_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "hp", I18n.text("受伤倒下"));
                break;
            case KEY_DEATH_CHECK_1_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "hp", String.format(I18n.text("濒死 #%d"), Integer.valueOf(1)));
                break;
            case KEY_DEATH_CHECK_2_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "hp", String.format(I18n.text("濒死 #%d"), Integer.valueOf(2)));
                break;
            case KEY_DEATH_CHECK_3_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "hp", String.format(I18n.text("濒死 #%d"), Integer.valueOf(3)));
                break;
            case KEY_DEATH_CHECK_4_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "hp", String.format(I18n.text("濒死 #%d"), Integer.valueOf(4)));
                break;
            case KEY_DEAD_DEPRECATED:
                deprecatedWritePointPoolThreshold(out, gurpsCharacter, "hp", I18n.text("死亡"));
                break;
            case KEY_BASIC_LIFT:
                writeEncodedText(out, gurpsCharacter.getBasicLift().toString());
                break;
            case KEY_ONE_HANDED_LIFT:
                writeEncodedText(out, gurpsCharacter.getOneHandedLift().toString());
                break;
            case KEY_TWO_HANDED_LIFT:
                writeEncodedText(out, gurpsCharacter.getTwoHandedLift().toString());
                break;
            case KEY_SHOVE:
                writeEncodedText(out, gurpsCharacter.getShoveAndKnockOver().toString());
                break;
            case KEY_RUNNING_SHOVE:
                writeEncodedText(out, gurpsCharacter.getRunningShoveAndKnockOver().toString());
                break;
            case KEY_CARRY_ON_BACK:
                writeEncodedText(out, gurpsCharacter.getCarryOnBack().toString());
                break;
            case KEY_SHIFT_SLIGHTLY:
                writeEncodedText(out, gurpsCharacter.getShiftSlightly().toString());
                break;
            case KEY_CARRIED_WEIGHT:
                writeEncodedText(out, EquipmentColumn.getDisplayWeight(gurpsCharacter, gurpsCharacter.getWeightCarried(false)));
                break;
            case KEY_CARRIED_VALUE:
                writeEncodedText(out, "$" + gurpsCharacter.getWealthCarried().toLocalizedString());
                break;
            case KEY_OTHER_VALUE:
                writeEncodedText(out, "$" + gurpsCharacter.getWealthNotCarried().toLocalizedString());
                break;
            case KEY_ALL_NOTES_COMBINED:
                StringBuilder buffer = new StringBuilder();
                for (Note note : gurpsCharacter.getNotesIterator()) {
                    if (!buffer.isEmpty()) {
                        buffer.append("\n\n");
                    }
                    buffer.append(note.getDescription());
                }
                writeEncodedText(out, buffer.toString());
                break;
            case KEY_CONTINUE_ID: // No-op... here so that old files don't choke
                break;
            case KEY_RACE_DEPRECATED:
            case KEY_CAMPAIGN_DEPRECATED:
            case KEY_OPTIONS_CODE_DEPRECATED:
                break;
            case KEY_BODY_TYPE:
                writeEncodedText(out, gurpsCharacter.getSheetSettings().getHitLocations().getName());
                break;
            default:
                if (!checkForLoopKeys(in, out, key)) {
                    if (key.startsWith(KEY_ONLY_CATEGORIES)) {
                        setOnlyCategories(key);
                    } else if (key.startsWith(KEY_EXCLUDE_CATEGORIES)) {
                        setExcludeCategories(key);
                    } else if (key.startsWith(KEY_COLOR_PREFIX)) {
                        String colorKey = key.substring(KEY_COLOR_PREFIX.length()).toLowerCase();
                        for (ThemeColor one : Colors.ALL) {
                            if (colorKey.equals(one.getKey())) {
                                out.write(Colors.encodeToHex(one));
                            }
                        }
                    } else if (!processAttributeKeys(out, gurpsCharacter, key)) {
                        writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                    }
                }
                break;
        }
    }

    private boolean checkForLoopKeys(BufferedReader in, BufferedWriter out, String key) throws IOException {
        if (key.startsWith(KEY_ENCUMBRANCE + LOOP_START)) {
            processEncumbranceLoop(out, extractUpToMarker(in, KEY_ENCUMBRANCE + LOOP_END));
        } else if (key.startsWith(KEY_ENCUMBRANCE + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(Encumbrance.values().length));
        } else if (key.startsWith(KEY_HIT_LOCATION + LOOP_START)) {
            processHitLocationLoop(out, extractUpToMarker(in, KEY_HIT_LOCATION + LOOP_END));
        } else if (key.startsWith(KEY_HIT_LOCATION + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(mSheet.getCharacter().getSheetSettings().getHitLocations().getLocations().size()));
        } else if (key.startsWith(KEY_ADVANTAGES + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_ADVANTAGES + LOOP_END), AdvantagesLoopType.ALL);
        } else if (key.startsWith(KEY_ADVANTAGES + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.ALL);
        } else if (key.startsWith(KEY_ADVANTAGES_ALL + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_ADVANTAGES_ALL + LOOP_END), AdvantagesLoopType.ADS_ALL);
        } else if (key.startsWith(KEY_ADVANTAGES_ALL + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.ADS_ALL);
        } else if (key.startsWith(KEY_ADVANTAGES_ONLY + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_ADVANTAGES_ONLY + LOOP_END), AdvantagesLoopType.ADS);
        } else if (key.startsWith(KEY_ADVANTAGES_ONLY + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.ADS);
        } else if (key.startsWith(KEY_DISADVANTAGES + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_DISADVANTAGES + LOOP_END), AdvantagesLoopType.DISADS);
        } else if (key.startsWith(KEY_DISADVANTAGES + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.DISADS);
        } else if (key.startsWith(KEY_DISADVANTAGES_ALL + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_DISADVANTAGES_ALL + LOOP_END), AdvantagesLoopType.DISADS_ALL);
        } else if (key.startsWith(KEY_DISADVANTAGES_ALL + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.DISADS_ALL);
        } else if (key.startsWith(KEY_QUIRKS + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_QUIRKS + LOOP_END), AdvantagesLoopType.QUIRKS);
        } else if (key.startsWith(KEY_QUIRKS + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.QUIRKS);
        } else if (key.startsWith(KEY_PERKS + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_PERKS + LOOP_END), AdvantagesLoopType.PERKS);
        } else if (key.startsWith(KEY_PERKS + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.PERKS);
        } else if (key.startsWith(KEY_LANGUAGES + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_LANGUAGES + LOOP_END), AdvantagesLoopType.LANGUAGES);
        } else if (key.startsWith(KEY_LANGUAGES + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.LANGUAGES);
        } else if (key.startsWith(KEY_CULTURAL_FAMILIARITIES + LOOP_START)) {
            processAdvantagesLoop(out, extractUpToMarker(in, KEY_CULTURAL_FAMILIARITIES + LOOP_END), AdvantagesLoopType.CULTURAL_FAMILIARITIES);
        } else if (key.startsWith(KEY_CULTURAL_FAMILIARITIES + LOOP_COUNT)) {
            writeAdvantagesLoopCount(out, AdvantagesLoopType.CULTURAL_FAMILIARITIES);
        } else if (key.startsWith(KEY_SKILLS + LOOP_START)) {
            processSkillsLoop(out, extractUpToMarker(in, KEY_SKILLS + LOOP_END));
        } else if (key.startsWith(KEY_SKILLS + LOOP_COUNT)) {
            int counter = 0;
            for (Skill ignored : mSheet.getCharacter().getSkillsIterator()) {
                counter++;
            }
            writeEncodedText(out, Integer.toString(counter));
        } else if (key.startsWith(KEY_SPELLS + LOOP_START)) {
            processSpellsLoop(out, extractUpToMarker(in, KEY_SPELLS + LOOP_END));
        } else if (key.startsWith(KEY_SPELLS + LOOP_COUNT)) {
            int counter = 0;
            for (Spell ignored : mSheet.getCharacter().getSpellsIterator()) {
                counter++;
            }
            writeEncodedText(out, Integer.toString(counter));
        } else if (key.startsWith(KEY_MELEE + LOOP_START)) {
            processMeleeLoop(out, extractUpToMarker(in, KEY_MELEE + LOOP_END));
        } else if (key.startsWith(KEY_MELEE + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(mSheet.getMeleeWeaponOutline().getModel().getRows().size()));
        } else if (key.startsWith(KEY_HIERARCHICAL_MELEE + LOOP_START)) {
            processHierarchicalMeleeLoop(out, extractUpToMarker(in, KEY_HIERARCHICAL_MELEE + LOOP_END));
        } else if (key.startsWith(KEY_HIERARCHICAL_MELEE + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(mSheet.getMeleeWeaponOutline().getModel().getRows().size()));
        } else if (key.startsWith(KEY_RANGED + LOOP_START)) {
            processRangedLoop(out, extractUpToMarker(in, KEY_RANGED + LOOP_END));
        } else if (key.startsWith(KEY_RANGED + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(mSheet.getRangedWeaponOutline().getModel().getRows().size()));
        } else if (key.startsWith(KEY_HIERARCHICAL_RANGED + LOOP_START)) {
            processHierarchicalRangedLoop(out, extractUpToMarker(in, KEY_HIERARCHICAL_RANGED + LOOP_END));
        } else if (key.startsWith(KEY_HIERARCHICAL_RANGED + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(mSheet.getRangedWeaponOutline().getModel().getRows().size()));
        } else if (key.startsWith(KEY_EQUIPMENT + LOOP_START)) {
            processEquipmentLoop(out, extractUpToMarker(in, KEY_EQUIPMENT + LOOP_END), true);
        } else if (key.startsWith(KEY_EQUIPMENT + LOOP_COUNT)) {
            writeEquipmentLoopCount(out, true);
        } else if (key.startsWith(KEY_OTHER_EQUIPMENT + LOOP_START)) {
            processEquipmentLoop(out, extractUpToMarker(in, KEY_OTHER_EQUIPMENT + LOOP_END), false);
        } else if (key.startsWith(KEY_OTHER_EQUIPMENT + LOOP_COUNT)) {
            writeEquipmentLoopCount(out, false);
        } else if (key.startsWith(KEY_NOTES + LOOP_START)) {
            processNotesLoop(out, extractUpToMarker(in, KEY_NOTES + LOOP_END));
        } else if (key.startsWith(KEY_NOTES + LOOP_COUNT)) {
            int counter = 0;
            for (Note ignored : mSheet.getCharacter().getNotesIterator()) {
                counter++;
            }
            writeEncodedText(out, Integer.toString(counter));
        } else if (key.startsWith(KEY_REACTION + LOOP_START)) {
            processReactionLoop(out, extractUpToMarker(in, KEY_REACTION + LOOP_END));
        } else if (key.startsWith(KEY_REACTION + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(mSheet.collectReactions().size()));
        } else if (key.startsWith(KEY_CONDITIONAL_MODIFIERS + LOOP_START)) {
            processConditionalModifiersLoop(out, extractUpToMarker(in, KEY_CONDITIONAL_MODIFIERS + LOOP_END));
        } else if (key.startsWith(KEY_CONDITIONAL_MODIFIERS + LOOP_COUNT)) {
            writeEncodedText(out, Integer.toString(mSheet.collectConditionalModifiers().size()));
        } else if (key.startsWith(KEY_PRIMARY_ATTRIBUTE + LOOP_START)) {
            processAttributeLoop(out, extractUpToMarker(in, KEY_PRIMARY_ATTRIBUTE + LOOP_END), true);
        } else if (key.startsWith(KEY_PRIMARY_ATTRIBUTE + LOOP_COUNT)) {
            int            counter = 0;
            GURPSCharacter gch     = mSheet.getCharacter();
            for (AttributeDef def : AttributeDef.getOrdered(gch.getSheetSettings().getAttributes())) {
                if (def.getType() != AttributeType.POOL && def.isPrimary()) {
                    Attribute attr = gch.getAttributes().get(def.getID());
                    if (attr != null) {
                        counter++;
                    }
                }
            }
            writeEncodedText(out, Integer.toString(counter));
        } else if (key.startsWith(KEY_SECONDARY_ATTRIBUTE + LOOP_START)) {
            processAttributeLoop(out, extractUpToMarker(in, KEY_SECONDARY_ATTRIBUTE + LOOP_END), false);
        } else if (key.startsWith(KEY_SECONDARY_ATTRIBUTE + LOOP_COUNT)) {
            int            counter = 0;
            GURPSCharacter gch     = mSheet.getCharacter();
            for (AttributeDef def : AttributeDef.getOrdered(gch.getSheetSettings().getAttributes())) {
                if (def.getType() != AttributeType.POOL && !def.isPrimary()) {
                    Attribute attr = gch.getAttributes().get(def.getID());
                    if (attr != null) {
                        counter++;
                    }
                }
            }
            writeEncodedText(out, Integer.toString(counter));
        } else if (key.startsWith(KEY_POINT_POOL + LOOP_START)) {
            processPointPoolLoop(out, extractUpToMarker(in, KEY_POINT_POOL + LOOP_END));
        } else if (key.startsWith(KEY_POINT_POOL + LOOP_COUNT)) {
            int            counter = 0;
            GURPSCharacter gch     = mSheet.getCharacter();
            for (AttributeDef def : AttributeDef.getOrdered(gch.getSheetSettings().getAttributes())) {
                if (def.getType() == AttributeType.POOL) {
                    Attribute attr = gch.getAttributes().get(def.getID());
                    if (attr != null) {
                        counter++;
                    }
                }
            }
            writeEncodedText(out, Integer.toString(counter));
        } else {
            return false;
        }
        return true;
    }

    private boolean processAttributeKeys(BufferedWriter out, GURPSCharacter gch, String key) throws IOException {
        key = key.toLowerCase();
        Attribute attr = gch.getAttributes().get(key);
        if (attr != null) {
            AttributeDef attrDef = attr.getAttrDef(gch);
            if (attrDef == null) {
                return false;
            }
            switch (attrDef.getType()) {
                case INTEGER:
                case POOL:
                    writeEncodedText(out, Numbers.format(attr.getIntValue(gch)));
                    return true;
                case DECIMAL:
                    writeEncodedText(out, Numbers.format(attr.getDoubleValue(gch)));
                    return true;
                default:
                    return false;
            }
        }
        if (key.endsWith("_" + KEY_NAME.toLowerCase())) {
            attr = gch.getAttributes().get(key.substring(0, key.length() - (KEY_NAME.length() + 1)));
            if (attr != null) {
                AttributeDef attrDef = attr.getAttrDef(gch);
                if (attrDef == null) {
                    return false;
                }
                writeEncodedText(out, attrDef.getName());
                return true;
            }
        } else if (key.endsWith("_" + KEY_FULL_NAME.toLowerCase())) {
            attr = gch.getAttributes().get(key.substring(0, key.length() - (KEY_FULL_NAME.length() + 1)));
            if (attr != null) {
                AttributeDef attrDef = attr.getAttrDef(gch);
                if (attrDef == null) {
                    return false;
                }
                writeEncodedText(out, attrDef.getFullName());
                return true;
            }
        } else if (key.endsWith("_" + KEY_COMBINED_NAME.toLowerCase())) {
            attr = gch.getAttributes().get(key.substring(0, key.length() - (KEY_COMBINED_NAME.length() + 1)));
            if (attr != null) {
                AttributeDef attrDef = attr.getAttrDef(gch);
                if (attrDef == null) {
                    return false;
                }
                writeEncodedText(out, attrDef.getCombinedName());
                return true;
            }
        } else if (key.endsWith("_" + KEY_POINTS.toLowerCase())) {
            attr = gch.getAttributes().get(key.substring(0, key.length() - (KEY_POINTS.length() + 1)));
            if (attr != null) {
                writeEncodedText(out, Numbers.format(attr.getPointCost(gch)));
                return true;
            }
        } else if (key.endsWith("_" + KEY_CURRENT.toLowerCase())) {
            attr = gch.getAttributes().get(key.substring(0, key.length() - (KEY_CURRENT.length() + 1)));
            if (attr != null) {
                AttributeDef attrDef = attr.getAttrDef(gch);
                if (attrDef == null || attrDef.getType() != AttributeType.POOL) {
                    return false;
                }
                writeEncodedText(out, Numbers.format(attr.getCurrentIntValue(gch)));
                return true;
            }
        }
        return false;
    }

    private void deprecatedWritePointPoolThreshold(BufferedWriter out, GURPSCharacter gch, String poolID, String match) throws IOException {
        Attribute attr = gch.getAttributes().get(poolID);
        if (attr != null) {
            AttributeDef def = attr.getAttrDef(gch);
            if (def != null) {
                List<PoolThreshold> thresholds = def.getThresholds();
                if (thresholds != null) {
                    for (PoolThreshold threshold : thresholds) {
                        if (match.equals(threshold.getState())) {
                            writeEncodedText(out, Numbers.format(threshold.threshold(attr.getIntValue(gch))));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void setOnlyCategories(String key) {
        String[] categories = key.substring(KEY_ONLY_CATEGORIES.length()).split(UNDERSCORE);
        mOnlyCategories.addAll(Arrays.asList(categories));
    }

    private void setExcludeCategories(String key) {
        String[] categories = key.substring(KEY_EXCLUDE_CATEGORIES.length()).split(UNDERSCORE);
        mExcludedCategories.addAll(Arrays.asList(categories));
    }

    private void writeBestWeaponDefense(BufferedWriter out, Function<MeleeWeaponStats, String> resolver) throws IOException {
        String best      = "-";
        int    bestValue = Integer.MIN_VALUE;
        for (WeaponDisplayRow row : new FilteredIterator<>(mSheet.getMeleeWeaponOutline().getModel().getRows(), WeaponDisplayRow.class)) {
            MeleeWeaponStats weapon = (MeleeWeaponStats) row.getWeapon();
            String           result = resolver.apply(weapon).trim();
            if (!result.isEmpty() && !"No".equals(result)) {
                int value = Numbers.extractInteger(result, 0, false);
                if (value > bestValue) {
                    bestValue = value;
                    best = result;
                }
            }
        }
        writeEncodedText(out, best);
    }

    private void writeEncodedText(BufferedWriter out, String text) throws IOException {
        if (mEncodeText) {
            StringBuilder buffer = new StringBuilder();
            int           length = text.length();
            for (int i = 0; i < length; i++) {
                char ch = text.charAt(i);
                if (ch == '<') {
                    buffer.append("&lt;");
                } else if (ch == '>') {
                    buffer.append("&gt;");
                } else if (ch == '&') {
                    buffer.append("&amp;");
                } else if (ch == '"') {
                    buffer.append("&quot;");
                } else if (ch >= ' ' && ch <= '~') {
                    buffer.append(ch);
                } else if (ch == '\n') {
                    buffer.append("<br>");
                } else {
                    buffer.append("&#");
                    buffer.append((int) ch);
                    buffer.append(';');
                }
            }
            text = buffer.toString();
        }
        out.write(text);
    }

    private String extractUpToMarker(BufferedReader in, String marker) throws IOException {
        char[]        buffer           = new char[1];
        StringBuilder keyBuffer        = new StringBuilder();
        StringBuilder extraction       = new StringBuilder();
        boolean       lookForKeyMarker = true;
        while (in.read(buffer) != -1) {
            char ch = buffer[0];
            if (lookForKeyMarker) {
                if (ch == '@') {
                    lookForKeyMarker = false;
                    in.mark(1);
                } else {
                    extraction.append(ch);
                }
            } else {
                if (ch == '_' || Character.isLetterOrDigit(ch)) {
                    keyBuffer.append(ch);
                    in.mark(1);
                } else {
                    if (!mEnhancedKeyParsing || ch != '@') {
                        in.reset();        // Allow KEYs to be surrounded by @KEY@
                    }
                    String key = keyBuffer.toString();
                    in.reset();
                    if (key.equals(marker)) {
                        return extraction.toString();
                    }
                    extraction.append('@');
                    extraction.append(key);
                    keyBuffer.setLength(0);
                    lookForKeyMarker = true;
                }
            }
        }
        return extraction.toString();
    }

    private void processEncumbranceLoop(BufferedWriter out, String contents) throws IOException {
        GURPSCharacter gurpsCharacter   = mSheet.getCharacter();
        int            length           = contents.length();
        StringBuilder  keyBuffer        = new StringBuilder();
        boolean        lookForKeyMarker = true;
        for (Encumbrance encumbrance : Encumbrance.values()) {
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        switch (key) {
                            case KEY_CURRENT_MARKER:
                                if (encumbrance == gurpsCharacter.getEncumbranceLevel(false)) {
                                    out.write(CURRENT);
                                }
                                break;
                            case KEY_CURRENT_MARKER_1:
                                if (encumbrance == gurpsCharacter.getEncumbranceLevel(false)) {
                                    out.write(ONE);
                                }
                                break;
                            case KEY_CURRENT_MARKER_BULLET:
                                if (encumbrance == gurpsCharacter.getEncumbranceLevel(false)) {
                                    out.write("•");
                                }
                                break;
                            case KEY_LEVEL:
                                writeEncodedText(out, MessageFormat.format(encumbrance == gurpsCharacter.getEncumbranceLevel(false) ? "• {0} ({1})" : "{0} ({1})", encumbrance, Numbers.format(-encumbrance.getEncumbrancePenalty())));
                                break;
                            case KEY_LEVEL_NO_MARKER:
                                writeEncodedText(out, MessageFormat.format("{0} ({1})", encumbrance, Numbers.format(-encumbrance.getEncumbrancePenalty())));
                                break;
                            case KEY_LEVEL_ONLY:
                                writeEncodedText(out, Numbers.format(-encumbrance.getEncumbrancePenalty()));
                                break;
                            case KEY_MAX_LOAD:
                                writeEncodedText(out, gurpsCharacter.getMaximumCarry(encumbrance).toString());
                                break;
                            case KEY_MOVE:
                                writeEncodedText(out, Numbers.format(gurpsCharacter.getMove(encumbrance)));
                                break;
                            case KEY_DODGE:
                                writeEncodedText(out, Numbers.format(gurpsCharacter.getDodge(encumbrance)));
                                break;
                            default:
                                writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                break;
                        }
                    }
                }
            }
        }
    }

    private void processHitLocationLoop(BufferedWriter out, String contents) throws IOException {
        GURPSCharacter   gurpsCharacter   = mSheet.getCharacter();
        int              length           = contents.length();
        StringBuilder    keyBuffer        = new StringBuilder();
        boolean          lookForKeyMarker = true;
        int              currentID        = 0;
        HitLocationTable table            = gurpsCharacter.getSheetSettings().getHitLocations();
        for (HitLocation location : table.getLocations()) {
            currentID++;
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        switch (key) {
                            case KEY_ROLL -> writeEncodedText(out, location.getRollRange());
                            case KEY_WHERE -> writeEncodedText(out, location.getTableName());
                            case KEY_PENALTY -> writeEncodedText(out, Numbers.format(location.getHitPenalty()));
                            case KEY_DR -> writeEncodedText(out, location.getDisplayDR(gurpsCharacter, null));
                            case KEY_DR_TOOLTIP -> {
                                StringBuilder tooltip = new StringBuilder();
                                location.getDisplayDR(gurpsCharacter, tooltip);
                                writeEncodedText(out, tooltip.toString());
                            }
                            case KEY_ID -> writeEncodedText(out, Integer.toString(currentID));
                            // Show the equipment that is providing the DR bonus
                            case KEY_LOCATION_EQUIPMENT -> writeEncodedText(out, hitLocationEquipment(location).replace(NEWLINE, COMMA_SEPARATOR));
                            case KEY_LOCATION_EQUIPMENT_FORMATTED -> {
                                String loc = hitLocationEquipment(location);
                                if (!loc.isEmpty()) {
                                    writeEncodedText(out, PARAGRAPH_START + loc.replace(NEWLINE, PARAGRAPH_END + NEWLINE + PARAGRAPH_START) + PARAGRAPH_END);
                                }
                            }
                            default -> writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                        }
                    }
                }
            }
        }
    }

    // TODO: Revisit this method, as the custom hit locations are different...

    /* A kludgy method to relate a hitlocation to the armor that is providing the DR for that hit location. */
    private String hitLocationEquipment(HitLocation location) {
        StringBuilder sb    = new StringBuilder();
        boolean       first = true;
        for (Equipment equipment : mSheet.getCharacter().getEquipmentIterator()) {
            if (equipment.isEquipped()) {
                for (Feature feature : equipment.getFeatures()) {
                    if (feature instanceof DRBonus) {
                        if (location.getID().equals(((DRBonus) feature).getLocation())) {
                            // HUGE Kludge. Only way I could equate the 2
                            // different HitLocations. I know that one is derived
                            // from the other, so this check will ALWAYS work.
                            if (!first) {
                                sb.append("\n");
                            }
                            sb.append(equipment.getDescription());
                            first = false;
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    private void writeAdvantagesLoopCount(BufferedWriter out, AdvantagesLoopType loopType) throws IOException {
        int counter = 0;
        for (Advantage advantage : mSheet.getCharacter().getAdvantagesIterator(false)) {
            if (loopType.shouldInclude(advantage, mOnlyCategories, mExcludedCategories)) {
                counter++;
            }
        }
        writeEncodedText(out, Integer.toString(counter));
    }

    private void processAdvantagesLoop(BufferedWriter out, String contents, AdvantagesLoopType loopType) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        for (Advantage advantage : mSheet.getCharacter().getAdvantagesIterator(false)) {
            if (loopType.shouldInclude(advantage, mOnlyCategories, mExcludedCategories)) {
                for (int i = 0; i < length; i++) {
                    char ch = contents.charAt(i);
                    if (lookForKeyMarker) {
                        if (ch == '@') {
                            lookForKeyMarker = false;
                        } else {
                            out.append(ch);
                        }
                    } else {
                        if (ch == '_' || Character.isLetterOrDigit(ch)) {
                            keyBuffer.append(ch);
                        } else {
                            String key = keyBuffer.toString();
                            i--;
                            if (mEnhancedKeyParsing && ch == '@') {
                                i++;        // Allow KEYs to be surrounded with @, e.g. @KEY@
                            }
                            keyBuffer.setLength(0);
                            lookForKeyMarker = true;
                            if (!processStyleIndentWarning(key, out, advantage)) {
                                if (!processDescription(key, out, advantage)) {
                                    switch (key) {
                                        case KEY_POINTS:
                                            writeEncodedText(out, AdvantageColumn.POINTS.getDataAsText(advantage));
                                            break;
                                        case KEY_REF:
                                            writeEncodedText(out, AdvantageColumn.REFERENCE.getDataAsText(advantage));
                                            break;
                                        case KEY_ID:
                                            writeEncodedText(out, advantage.getID().toString());
                                            break;
                                        case KEY_PARENT_ID:
                                            ListRow parent = (ListRow) advantage.getParent();
                                            if (parent != null) {
                                                out.write(parent.getID().toString());
                                            }
                                            break;
                                        case KEY_TYPE:
                                            writeEncodedText(out, advantage.canHaveChildren() ? advantage.getContainerType().name() : ITEM);
                                            break;
                                        case KEY_DESCRIPTION_USER:
                                            writeEncodedText(out, advantage.getUserDesc());
                                            break;
                                        case KEY_DESCRIPTION_USER_FORMATTED:
                                            if (!advantage.getUserDesc().isEmpty()) {
                                                writeEncodedText(out, PARAGRAPH_START + advantage.getUserDesc().replace(NEWLINE, PARAGRAPH_END + NEWLINE + PARAGRAPH_START) + PARAGRAPH_END);
                                            }
                                            break;
                                        default:
                                            /* Allows the access to notes on modifiers.  Currently only used in the 'Language' loop.
                                             * e.g. Advantage:Language, Modifier:Spoken -> Note:Native, Advantage:Language, Modifier:Written -> Note:Accented
                                             */
                                            if (key.startsWith(KEY_MODIFIER_NOTES_FOR)) {
                                                AdvantageModifier m = advantage.getActiveModifierFor(key.substring(KEY_MODIFIER_NOTES_FOR.length()));
                                                if (m != null) {
                                                    writeEncodedText(out, m.getNotes());
                                                }
                                            } else {
                                                writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        mOnlyCategories.clear();
        mExcludedCategories.clear();
    }

    private boolean processDescription(String key, BufferedWriter out, ListRow row) throws IOException {
        if (key.equals(KEY_DESCRIPTION)) {
            writeEncodedText(out, row.toString());
            writeNote(out, row.getModifierNotes());
            writeNote(out, row.getNotes());
            if (row instanceof Spell) {
                writeNote(out, ((Spell) row).getRituals());
            }
        } else if (key.equals(KEY_DESCRIPTION_PRIMARY)) {
            writeEncodedText(out, row.toString());
        } else if (key.startsWith(KEY_DESCRIPTION_MODIFIER_NOTES)) {
            writeXMLTextWithOptionalParens(key, out, row.getModifierNotes());
        } else if (key.startsWith(KEY_DESCRIPTION_NOTES)) {
            String notes = row.getNotes();
            if (row instanceof Spell) {
                if (!notes.isEmpty()) {
                    notes += "; ";
                }
                notes += ((Spell) row).getRituals();
            }
            writeXMLTextWithOptionalParens(key, out, notes);
        } else {
            return false;
        }
        return true;
    }

    private void writeXMLTextWithOptionalParens(String key, BufferedWriter out, String text) throws IOException {
        if (!text.isEmpty()) {
            String pre  = "";
            String post = "";
            if (key.endsWith(KEY_SUFFIX_PAREN)) {
                pre = " (";
                post = ")";
            } else if (key.endsWith(KEY_SUFFIX_BRACKET)) {
                pre = " [";
                post = "]";
            } else if (key.endsWith(KEY_SUFFIX_CURLY)) {
                pre = " {";
                post = "}";
            }
            out.write(pre);
            writeEncodedText(out, text);
            out.write(post);
        }
    }

    private void writeNote(BufferedWriter out, String notes) throws IOException {
        if (!notes.isEmpty()) {
            out.write("<div class=\"note\">");
            writeEncodedText(out, notes);
            out.write("</div>");
        }
    }

    private void processSkillsLoop(BufferedWriter out, String contents) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        for (Skill skill : mSheet.getCharacter().getSkillsIterator()) {
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        if (!processStyleIndentWarning(key, out, skill)) {
                            if (!processDescription(key, out, skill)) {
                                switch (key) {
                                    case KEY_SL -> writeEncodedText(out, SkillColumn.LEVEL.getDataAsText(skill));
                                    case KEY_RSL -> writeEncodedText(out, SkillColumn.RELATIVE_LEVEL.getDataAsText(skill));
                                    case KEY_DIFFICULTY -> writeEncodedText(out, SkillColumn.DIFFICULTY.getDataAsText(skill));
                                    case KEY_POINTS -> writeEncodedText(out, SkillColumn.POINTS.getDataAsText(skill));
                                    case KEY_REF -> writeEncodedText(out, SkillColumn.REFERENCE.getDataAsText(skill));
                                    case KEY_ID -> writeEncodedText(out, skill.getID().toString());
                                    case KEY_PARENT_ID -> {
                                        ListRow parent = (ListRow) skill.getParent();
                                        if (parent != null) {
                                            out.write(parent.getID().toString());
                                        }
                                    }
                                    case KEY_TYPE -> writeEncodedText(out, skill.canHaveChildren() ? GROUP : ITEM);
                                    default -> writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean processStyleIndentWarning(String key, BufferedWriter out, ListRow row) throws IOException {
        if (key.equals(KEY_STYLE_INDENT_WARNING)) {
            StringBuilder style = new StringBuilder();
            int           depth = row.getDepth();
            if (depth > 0) {
                style.append(" style=\"padding-left: ");
                style.append(depth * 12);
                style.append("px;");
            }
            if (!row.isSatisfied()) {
                if (style.isEmpty()) {
                    style.append(" style=\"");
                }
                style.append(" color: red;");
            }
            if (!style.isEmpty()) {
                style.append("\" ");
                out.write(style.toString());
            }
        } else if (key.startsWith(KEY_PREFIX_DEPTH)) {
            int amt = Numbers.extractInteger(key.substring(6), 1, false);
            out.write(Integer.toString(amt * row.getDepth()));
        } else if (key.equals(KEY_SATISFIED)) {
            out.write(row.isSatisfied() ? "Y" : "N");
        } else {
            return false;
        }
        return true;
    }

    private void processSpellsLoop(BufferedWriter out, String contents) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        for (Spell spell : mSheet.getCharacter().getSpellsIterator()) {
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        if (!processStyleIndentWarning(key, out, spell)) {
                            if (!processDescription(key, out, spell)) {
                                switch (key) {
                                    case KEY_CLASS -> writeEncodedText(out, spell.getSpellClass());
                                    case KEY_COLLEGE -> writeEncodedText(out, String.join(", ", spell.getColleges()));
                                    case KEY_MANA_CAST -> writeEncodedText(out, spell.getCastingCost());
                                    case KEY_MANA_MAINTAIN -> writeEncodedText(out, spell.getMaintenance());
                                    case KEY_TIME_CAST -> writeEncodedText(out, spell.getCastingTime());
                                    case KEY_DURATION -> writeEncodedText(out, spell.getDuration());
                                    case KEY_RESIST -> writeEncodedText(out, spell.getResist());
                                    case KEY_SL -> writeEncodedText(out, SpellColumn.LEVEL.getDataAsText(spell));
                                    case KEY_RSL -> writeEncodedText(out, SpellColumn.RELATIVE_LEVEL.getDataAsText(spell));
                                    case KEY_DIFFICULTY -> writeEncodedText(out, spell.getDifficultyAsText());
                                    case KEY_POINTS -> writeEncodedText(out, SpellColumn.POINTS.getDataAsText(spell));
                                    case KEY_REF -> writeEncodedText(out, SpellColumn.REFERENCE.getDataAsText(spell));
                                    case KEY_ID -> writeEncodedText(out, spell.getID().toString());
                                    case KEY_PARENT_ID -> {
                                        ListRow parent = (ListRow) spell.getParent();
                                        if (parent != null) {
                                            out.write(parent.getID().toString());
                                        }
                                    }
                                    case KEY_TYPE -> writeEncodedText(out, spell.canHaveChildren() ? GROUP : ITEM);
                                    default -> writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processMeleeLoop(BufferedWriter out, String contents) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        int           currentID        = 0;
        for (WeaponDisplayRow row : new FilteredIterator<>(mSheet.getMeleeWeaponOutline().getModel().getRows(), WeaponDisplayRow.class)) {
            currentID++;
            MeleeWeaponStats weapon = (MeleeWeaponStats) row.getWeapon();
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        i = processMeleeWeaponKeys(out, key, currentID, weapon, i, contents, null);
                    }
                }
            }
        }
    }

    // Handle keys specific to MeleeWeaponStats. If "attackModes" is NOT NULL, then we could allow
    // processing of a hierarchical loop.
    private int processMeleeWeaponKeys(BufferedWriter out, String key, int counter, MeleeWeaponStats weapon, int index, String contents, List<MeleeWeaponStats> attackModes) throws IOException {
        switch (key) {
            case KEY_PARRY -> {
                writeEncodedText(out, weapon.getResolvedParryNoToolTip());
                return index;
            }
            case KEY_BLOCK -> {
                writeEncodedText(out, weapon.getResolvedBlockNoToolTip());
                return index;
            }
            case KEY_REACH -> {
                writeEncodedText(out, weapon.getReach());
                return index;
            }
            default -> {
                if (attackModes != null && key.startsWith(KEY_ATTACK_MODES + LOOP_START)) {
                    int endIndex = contents.indexOf(KEY_ATTACK_MODES + LOOP_END);
                    if (endIndex > 0) {
                        String subContents = contents.substring(index + 1, endIndex - 1);
                        processMeleeAttackModes(out, subContents, attackModes);
                        return endIndex + (KEY_ATTACK_MODES + LOOP_END).length();
                    }
                }
                if (key.startsWith(KEY_ATTACK_MODES + LOOP_COUNT)) {
                    writeEncodedText(out, Integer.toString(attackModes != null ? attackModes.size() : 0));
                    return index;
                }
                return processWeaponKeys(out, key, counter, weapon, index);
            }
        }
    }

    // Handle keys specific to RangedWeaponStats. If "attackModes" is NOT NULL, then we could allow
    // processing of a hierarchical loop.
    private int processRangedWeaponKeys(BufferedWriter out, String key, int counter, RangedWeaponStats weapon, int index, String contents, List<RangedWeaponStats> attackModes) throws IOException {
        switch (key) {
            case KEY_BULK -> {
                writeEncodedText(out, weapon.getBulk());
                return index;
            }
            case KEY_ACCURACY -> {
                writeEncodedText(out, weapon.getAccuracy());
                return index;
            }
            case KEY_RANGE -> {
                writeEncodedText(out, weapon.getRange());
                return index;
            }
            case KEY_ROF -> {
                writeEncodedText(out, weapon.getRateOfFire());
                return index;
            }
            case KEY_SHOTS -> {
                writeEncodedText(out, weapon.getShots());
                return index;
            }
            case KEY_RECOIL -> {
                writeEncodedText(out, weapon.getRecoil());
                return index;
            }
            default -> {
                if (attackModes != null && key.startsWith(KEY_ATTACK_MODES + LOOP_START)) {
                    int endIndex = contents.indexOf(KEY_ATTACK_MODES + LOOP_END);
                    if (endIndex > 0) {
                        String subContents = contents.substring(index + 1, endIndex - 1);
                        processRangedAttackModes(out, subContents, attackModes);
                        return endIndex + (KEY_ATTACK_MODES + LOOP_END).length();
                    }
                }
                if (key.startsWith(KEY_ATTACK_MODES + LOOP_COUNT)) {
                    writeEncodedText(out, Integer.toString(attackModes != null ? attackModes.size() : 0));
                    return index;
                }
                return processWeaponKeys(out, key, counter, weapon, index);
            }
        }
    }

    /* Break out handling of general weapons information. Anything known by WeaponStats or the equipment.  */
    private int processWeaponKeys(BufferedWriter out, String key, int counter, WeaponStats weapon, int index) throws IOException {
        Equipment equipment = null;
        if (weapon.getOwner() instanceof Equipment) {
            equipment = (Equipment) weapon.getOwner();
        }
        if (!processDescription(key, out, weapon)) {
            switch (key) {
                case KEY_USAGE:
                    writeEncodedText(out, weapon.getUsage());
                    break;
                case KEY_LEVEL:
                    writeEncodedText(out, Numbers.format(weapon.getSkillLevel()));
                    break;
                case KEY_DAMAGE:
                    writeEncodedText(out, weapon.getDamage().getResolvedDamage());
                    break;
                case KEY_UNMODIFIED_DAMAGE:
                    writeEncodedText(out, weapon.getDamage().toString());
                    break;
                case KEY_WEAPON_STRENGTH:
                    writeEncodedText(out, weapon.getStrength());
                    break;
                case KEY_WEAPON_STRENGTH_NUM:
                    writeEncodedText(out, NOT_NUMBER_PATTERN.matcher(weapon.getStrength()).replaceAll(""));
                    break;
                case KEY_ID:
                    writeEncodedText(out, Integer.toString(counter));
                    break;
                case KEY_COST:
                    if (equipment != null) {
                        writeEncodedText(out, equipment.getAdjustedValue().toLocalizedString());
                    }
                    break;
                case KEY_LEGALITY_CLASS:
                    if (equipment != null) {
                        writeEncodedText(out, equipment.getLegalityClass());
                    }
                    break;
                case KEY_TL:
                    if (equipment != null) {
                        writeEncodedText(out, equipment.getTechLevel());
                    }
                    break;
                case KEY_WEIGHT:
                    if (equipment != null) {
                        writeEncodedText(out, EquipmentColumn.getDisplayWeight(equipment.getDataFile(), equipment.getAdjustedWeight(false)));
                    }
                    break;
                case KEY_AMMO:
                    if (equipment != null) {
                        writeEncodedText(out, Numbers.format(ammoFor(equipment)));
                    }
                    break;
                default:
                    writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                    break;
            }
        }
        return index;
    }

    /* Process the weapons in a hierarchical format. One time for each weapon with a unique name,
     * and then possibly one time for each different "attack mode" that the weapon can support.
     * e.g. Weapon Name: Spear, attack modes "1 Handed" and "2 Handed"
     */
    private void processHierarchicalMeleeLoop(BufferedWriter out, String contents) throws IOException {
        int                                      length           = contents.length();
        StringBuilder                            keyBuffer        = new StringBuilder();
        boolean                                  lookForKeyMarker = true;
        int                                      currentID        = 0;
        Map<String, ArrayList<MeleeWeaponStats>> weaponsMap       = new HashMap<>();
        Map<String, MeleeWeaponStats>            weapons          = new HashMap<>();
        for (WeaponDisplayRow row : new FilteredIterator<>(mSheet.getMeleeWeaponOutline().getModel().getRows(), WeaponDisplayRow.class)) {
            MeleeWeaponStats weapon      = (MeleeWeaponStats) row.getWeapon();
            String           description = weapon.getDescription();
            weapons.put(description, weapon);
            weaponsMap.computeIfAbsent(description, k -> new ArrayList<>()).add(weapon);
        }
        List<MeleeWeaponStats> sorted = new ArrayList<>(weapons.values());
        sorted.sort((o1, o2) -> {
            int result = NumericComparator.CASELESS_COMPARATOR.compare(o1.getDescription(), o2.getDescription());
            if (result == 0) {
                result = NumericComparator.CASELESS_COMPARATOR.compare(o1.getUsage(), o2.getUsage());
            }
            return result;
        });
        for (MeleeWeaponStats weapon : sorted) {
            currentID++;
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        i = processMeleeWeaponKeys(out, key, currentID, weapon, i, contents, weaponsMap.get(weapon.getDescription()));
                    }
                }
            }
        }
    }

    /* Process the weapons in a hierarchical format.   One time for each weapon with a unique name,
     * and then possibly one time for each different "attack mode" that the weapon can support.
     * e.g. Weapon Name: Atlatl, attack modes "Shoot Dart" and "Shoot Javelin"
     */
    private void processHierarchicalRangedLoop(BufferedWriter out, String contents) throws IOException {
        int                                       length           = contents.length();
        StringBuilder                             keyBuffer        = new StringBuilder();
        boolean                                   lookForKeyMarker = true;
        int                                       currentID        = 0;
        Map<String, ArrayList<RangedWeaponStats>> weaponsMap       = new HashMap<>();
        Map<String, RangedWeaponStats>            weapons          = new HashMap<>();
        for (WeaponDisplayRow row : new FilteredIterator<>(mSheet.getRangedWeaponOutline().getModel().getRows(), WeaponDisplayRow.class)) {
            RangedWeaponStats weapon      = (RangedWeaponStats) row.getWeapon();
            String            description = weapon.getDescription();
            weapons.put(description, weapon);
            weaponsMap.computeIfAbsent(description, k -> new ArrayList<>()).add(weapon);
        }
        List<RangedWeaponStats> sorted = new ArrayList<>(weapons.values());
        sorted.sort((o1, o2) -> {
            int result = NumericComparator.CASELESS_COMPARATOR.compare(o1.getDescription(), o2.getDescription());
            if (result == 0) {
                result = NumericComparator.CASELESS_COMPARATOR.compare(o1.getUsage(), o2.getUsage());
            }
            return result;
        });
        for (RangedWeaponStats weapon : sorted) {
            currentID++;
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        i = processRangedWeaponKeys(out, key, currentID, weapon, i, contents, weaponsMap.get(weapon.getDescription()));
                    }
                }
            }
        }
    }

    /* Loop through all of the attackModes for a particular weapon.   We need to make melee/ranged specific
     * versions of this method because they must call the correct "processXXWeaponKeys" method.
     */
    private void processMeleeAttackModes(BufferedWriter out, String contents, List<MeleeWeaponStats> attackModes) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        int           counter          = 0;
        for (MeleeWeaponStats weapon : attackModes) {
            counter++;
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        i = processMeleeWeaponKeys(out, key, counter, weapon, i, contents, null);
                    }
                }
            }
        }
    }

    /* Loop through all of the attackModes for a particular weapon.   We need to make melee/ranged specific
     * versions of this method because they must call the correct "processXXWeaponKeys" method.
     */
    private void processRangedAttackModes(BufferedWriter out, String contents, List<RangedWeaponStats> attackModes) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        int           counter          = 0;
        for (RangedWeaponStats weapon : attackModes) {
            counter++;
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        i = processRangedWeaponKeys(out, key, counter, weapon, i, contents, null);
                    }
                }
            }
        }
    }

    /* report the quantity of ammo used by this weapon, if any */
    private int ammoFor(Equipment weapon) {
        String usesAmmoType = null;
        String ammoType     = null;
        for (String category : weapon.getCategories()) {
            if (category.startsWith(KEY_USES_AMMO_TYPE)) {
                usesAmmoType = category.substring(KEY_USES_AMMO_TYPE.length()).trim();
            }
        }
        if (usesAmmoType == null) {
            return 0;
        }
        for (Equipment equipment : mSheet.getCharacter().getEquipmentIterator()) {
            if (equipment.isEquipped()) {
                for (String category : equipment.getCategories()) {
                    if (category.startsWith(KEY_AMMO_TYPE)) {
                        ammoType = category.substring(KEY_AMMO_TYPE.length()).trim();
                    }
                }
                if (usesAmmoType.equalsIgnoreCase(ammoType)) {
                    return equipment.getQuantity();
                }
            }
        }
        return 0;
    }

    private boolean processDescription(String key, BufferedWriter out, WeaponStats stats) throws IOException {
        if (key.equals(KEY_DESCRIPTION)) {
            writeEncodedText(out, stats.toString());
            writeNote(out, stats.getNotes());
        } else if (key.equals(KEY_DESCRIPTION_PRIMARY)) {
            writeEncodedText(out, stats.toString());
        } else if (key.startsWith(KEY_DESCRIPTION_NOTES)) {
            writeXMLTextWithOptionalParens(key, out, stats.getNotes());
        } else {
            return false;
        }
        return true;
    }

    private void processRangedLoop(BufferedWriter out, String contents) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        int           currentID        = 0;
        for (WeaponDisplayRow row : new FilteredIterator<>(mSheet.getRangedWeaponOutline().getModel().getRows(), WeaponDisplayRow.class)) {
            currentID++;
            RangedWeaponStats weapon = (RangedWeaponStats) row.getWeapon();
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        i = processRangedWeaponKeys(out, key, currentID, weapon, i, contents, null);
                    }
                }
            }
        }
    }

    private void writeEquipmentLoopCount(BufferedWriter out, boolean carried) throws IOException {
        int                    counter = 0;
        RowIterator<Equipment> iter    = carried ? mSheet.getCharacter().getEquipmentIterator() : mSheet.getCharacter().getOtherEquipmentIterator();
        for (Equipment equipment : iter) {
            if (shouldInclude(equipment)) {   // Allows category filtering
                counter++;
            }
        }
        writeEncodedText(out, Integer.toString(counter));
    }

    private void processEquipmentLoop(BufferedWriter out, String contents, boolean carried) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        // Create child-to-parent maps to determine where items are being stored.
        // Used by KEY_LOCATION
        List<List<Row>>        children      = new ArrayList<>();
        List<Equipment>        parents       = new ArrayList<>();
        List<Equipment>        equipmentList = new ArrayList<>();
        RowIterator<Equipment> iter          = carried ? mSheet.getCharacter().getEquipmentIterator() : mSheet.getCharacter().getOtherEquipmentIterator();
        for (Equipment equipment : iter) {
            if (shouldInclude(equipment)) {   // Allows category filtering
                equipmentList.add(equipment);
                if (equipment.hasChildren()) {
                    children.add(equipment.getChildren());
                    parents.add(equipment);
                }
            }
        }
        for (Equipment equipment : equipmentList) {
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        if (!processStyleIndentWarning(key, out, equipment)) {
                            if (!processDescription(key, out, equipment)) {
                                switch (key) {
                                    case KEY_STATE:
                                        if (carried) {
                                            out.write(equipment.isEquipped() ? "E" : "C");
                                        } else {
                                            out.write("-");
                                        }
                                        break;
                                    case KEY_EQUIPPED:
                                        if (carried && equipment.isEquipped()) {
                                            out.write("✓");
                                        }
                                        break;
                                    case KEY_EQUIPPED_FONT_AWESOME:
                                        if (carried && equipment.isEquipped()) {
                                            out.write("<i class=\"fas fa-check-circle\"></i>");
                                        }
                                        break;
                                    case KEY_EQUIPPED_NUM:
                                        out.write(carried && equipment.isEquipped() ? '1' : '0');
                                        break;
                                    case KEY_CARRIED_STATUS:
                                        if (carried) {
                                            out.write(equipment.isEquipped() ? '2' : '1');
                                        } else {
                                            out.write('0');
                                        }
                                        break;
                                    case KEY_QTY:
                                        writeEncodedText(out, Numbers.format(equipment.getQuantity()));
                                        break;
                                    case KEY_COST:
                                        writeEncodedText(out, equipment.getAdjustedValue().toLocalizedString());
                                        break;
                                    case KEY_WEIGHT:
                                        writeEncodedText(out, EquipmentColumn.getDisplayWeight(equipment.getDataFile(), equipment.getAdjustedWeight(false)));
                                        break;
                                    case KEY_COST_SUMMARY:
                                        writeEncodedText(out, equipment.getExtendedValue().toLocalizedString());
                                        break;
                                    case KEY_WEIGHT_SUMMARY:
                                        writeEncodedText(out, EquipmentColumn.getDisplayWeight(equipment.getDataFile(), equipment.getExtendedWeight(false)));
                                        break;
                                    case KEY_WEIGHT_RAW:
                                        writeEncodedText(out, equipment.getAdjustedWeight(false).getNormalizedValue().toLocalizedString());
                                        break;
                                    case KEY_REF:
                                        writeEncodedText(out, equipment.getReference());
                                        break;
                                    case KEY_ID:
                                        writeEncodedText(out, equipment.getID().toString());
                                        break;
                                    case KEY_PARENT_ID:
                                        ListRow parent = (ListRow) equipment.getParent();
                                        if (parent != null) {
                                            out.write(parent.getID().toString());
                                        }
                                        break;
                                    case KEY_TYPE:
                                        writeEncodedText(out, equipment.canHaveChildren() ? GROUP : ITEM);
                                        break;
                                    case KEY_TL:
                                        writeEncodedText(out, equipment.getTechLevel());
                                        break;
                                    case KEY_LEGALITY_CLASS:
                                        writeEncodedText(out, equipment.getDisplayLegalityClass());
                                        break;
                                    case KEY_CATEGORIES:
                                        writeEncodedText(out, equipment.getCategoriesAsString());
                                        break;
                                    case KEY_LOCATION:
                                        for (int j = 0; j < children.size(); j++) {
                                            if (children.get(j).contains(equipment)) {
                                                writeEncodedText(out, parents.get(j).getDescription());
                                            }
                                        }
                                        break;
                                    case KEY_USES:
                                        writeEncodedText(out, Integer.valueOf(equipment.getUses()).toString());
                                        break;
                                    case KEY_MAX_USES:
                                        writeEncodedText(out, Integer.valueOf(equipment.getMaxUses()).toString());
                                        break;
                                    default:
                                        if (key.startsWith(KEY_MODIFIER_NOTES_FOR)) {
                                            EquipmentModifier m = equipment.getActiveModifierFor(key.substring(KEY_MODIFIER_NOTES_FOR.length()));
                                            if (m != null) {
                                                writeEncodedText(out, m.getNotes());
                                            }
                                        } else {
                                            writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                        }
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
        mOnlyCategories.clear();
        mExcludedCategories.clear();
    }

    private boolean shouldInclude(Equipment equipment) {
        for (String cat : mOnlyCategories) {
            if (equipment.hasCategory(cat)) {
                return true;
            }
        }
        if (!mOnlyCategories.isEmpty()) {  // If 'only' categories were provided, and none matched,
            // then false
            return false;
        }
        for (String cat : mExcludedCategories) {
            if (equipment.hasCategory(cat)) {
                return false;
            }
        }
        return true;
    }

    private void processNotesLoop(BufferedWriter out, String contents) throws IOException {
        int           length           = contents.length();
        StringBuilder keyBuffer        = new StringBuilder();
        boolean       lookForKeyMarker = true;
        for (Note note : mSheet.getCharacter().getNotesIterator()) {
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        if (!processStyleIndentWarning(key, out, note)) {
                            switch (key) {
                                case KEY_NOTE:
                                    writeEncodedText(out, note.getDescription());
                                    break;
                                case KEY_NOTE_FORMATTED:
                                    if (!note.getDescription().isEmpty()) {
                                        writeEncodedText(out, PARAGRAPH_START + note.getDescription().replace(NEWLINE, PARAGRAPH_END + NEWLINE + PARAGRAPH_START) + PARAGRAPH_END);
                                    }
                                    break;
                                case KEY_ID:
                                    writeEncodedText(out, note.getID().toString());
                                    break;
                                case KEY_PARENT_ID:
                                    ListRow parent = (ListRow) note.getParent();
                                    if (parent != null) {
                                        out.write(parent.getID().toString());
                                    }
                                    break;
                                case KEY_TYPE:
                                    writeEncodedText(out, note.canHaveChildren() ? GROUP : ITEM);
                                    break;
                                case KEY_REF:
                                    writeEncodedText(out, note.getReference());
                                    break;
                                default:
                                    writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                    break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void processReactionLoop(BufferedWriter out, String contents) throws IOException {
        int               length           = contents.length();
        StringBuilder     keyBuffer        = new StringBuilder();
        boolean           lookForKeyMarker = true;
        int               currentID        = 0;
        List<ReactionRow> reactions        = mSheet.collectReactions();
        for (ReactionRow reaction : reactions) {
            currentID++;
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        switch (key) {
                            case KEY_MODIFIER -> writeEncodedText(out, Numbers.formatWithForcedSign(reaction.getTotalAmount()));
                            case KEY_SITUATION -> writeEncodedText(out, reaction.getFrom());
                            case KEY_ID -> writeEncodedText(out, Integer.toString(currentID));
                            default -> writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                        }
                    }
                }
            }
        }
    }

    private void processConditionalModifiersLoop(BufferedWriter out, String contents) throws IOException {
        int                          length           = contents.length();
        StringBuilder                keyBuffer        = new StringBuilder();
        boolean                      lookForKeyMarker = true;
        int                          currentID        = 0;
        List<ConditionalModifierRow> cms              = mSheet.collectConditionalModifiers();
        for (ConditionalModifierRow cm : cms) {
            currentID++;
            for (int i = 0; i < length; i++) {
                char ch = contents.charAt(i);
                if (lookForKeyMarker) {
                    if (ch == '@') {
                        lookForKeyMarker = false;
                    } else {
                        out.append(ch);
                    }
                } else {
                    if (ch == '_' || Character.isLetterOrDigit(ch)) {
                        keyBuffer.append(ch);
                    } else {
                        String key = keyBuffer.toString();
                        i--;
                        if (mEnhancedKeyParsing && ch == '@') {
                            i++;        // Allow KEYs to be surrounded by @KEY@
                        }
                        keyBuffer.setLength(0);
                        lookForKeyMarker = true;
                        switch (key) {
                            case KEY_MODIFIER -> writeEncodedText(out, Numbers.formatWithForcedSign(cm.getTotalAmount()));
                            case KEY_SITUATION -> writeEncodedText(out, cm.getFrom());
                            case KEY_ID -> writeEncodedText(out, Integer.toString(currentID));
                            default -> writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                        }
                    }
                }
            }
        }
    }

    private void processAttributeLoop(BufferedWriter out, String contents, boolean primary) throws IOException {
        int                length           = contents.length();
        StringBuilder      keyBuffer        = new StringBuilder();
        boolean            lookForKeyMarker = true;
        GURPSCharacter     gch              = mSheet.getCharacter();
        List<AttributeDef> defs             = AttributeDef.getOrdered(gch.getSheetSettings().getAttributes());
        for (AttributeDef def : defs) {
            if (def.getType() != AttributeType.POOL && def.isPrimary() == primary) {
                Attribute attr = gch.getAttributes().get(def.getID());
                if (attr != null) {
                    for (int i = 0; i < length; i++) {
                        char ch = contents.charAt(i);
                        if (lookForKeyMarker) {
                            if (ch == '@') {
                                lookForKeyMarker = false;
                            } else {
                                out.append(ch);
                            }
                        } else {
                            if (ch == '_' || Character.isLetterOrDigit(ch)) {
                                keyBuffer.append(ch);
                            } else {
                                String key = keyBuffer.toString();
                                i--;
                                if (mEnhancedKeyParsing && ch == '@') {
                                    i++;        // Allow KEYs to be surrounded by @KEY@
                                }
                                keyBuffer.setLength(0);
                                lookForKeyMarker = true;
                                switch (key) {
                                    case KEY_ID -> writeEncodedText(out, def.getID());
                                    case KEY_NAME -> writeEncodedText(out, def.getName());
                                    case KEY_FULL_NAME -> writeEncodedText(out, def.getFullName());
                                    case KEY_COMBINED_NAME -> writeEncodedText(out, def.getCombinedName());
                                    case KEY_VALUE -> {
                                        if (def.getType() == AttributeType.DECIMAL) {
                                            writeEncodedText(out, Numbers.format(attr.getDoubleValue(gch)));
                                        } else {
                                            writeEncodedText(out, Numbers.format(attr.getIntValue(gch)));
                                        }
                                    }
                                    case KEY_POINTS -> writeEncodedText(out, Numbers.format(attr.getPointCost(gch)));
                                    default -> writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processPointPoolLoop(BufferedWriter out, String contents) throws IOException {
        int            length           = contents.length();
        StringBuilder  keyBuffer        = new StringBuilder();
        boolean        lookForKeyMarker = true;
        GURPSCharacter gch              = mSheet.getCharacter();
        for (AttributeDef def : AttributeDef.getOrdered(gch.getSheetSettings().getAttributes())) {
            if (def.getType() == AttributeType.POOL) {
                Attribute attr = gch.getAttributes().get(def.getID());
                if (attr != null) {
                    for (int i = 0; i < length; i++) {
                        char ch = contents.charAt(i);
                        if (lookForKeyMarker) {
                            if (ch == '@') {
                                lookForKeyMarker = false;
                            } else {
                                out.append(ch);
                            }
                        } else {
                            if (ch == '_' || Character.isLetterOrDigit(ch)) {
                                keyBuffer.append(ch);
                            } else {
                                String key = keyBuffer.toString();
                                i--;
                                if (mEnhancedKeyParsing && ch == '@') {
                                    i++;        // Allow KEYs to be surrounded by @KEY@
                                }
                                keyBuffer.setLength(0);
                                lookForKeyMarker = true;
                                switch (key) {
                                    case KEY_ID -> writeEncodedText(out, def.getID());
                                    case KEY_NAME -> writeEncodedText(out, def.getName());
                                    case KEY_FULL_NAME -> writeEncodedText(out, def.getFullName());
                                    case KEY_COMBINED_NAME -> writeEncodedText(out, def.getCombinedName());
                                    case KEY_CURRENT -> writeEncodedText(out, Numbers.format(attr.getCurrentIntValue(gch)));
                                    case KEY_MAXIMUM -> writeEncodedText(out, Numbers.format(attr.getIntValue(gch)));
                                    case KEY_POINTS -> writeEncodedText(out, Numbers.format(attr.getPointCost(gch)));
                                    default -> writeEncodedText(out, String.format(UNIDENTIFIED_KEY, key));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private enum AdvantagesLoopType {
        ALL {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return includeByCategories(advantage, included, excluded);
            }

        },
        ADS {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getAdjustedPoints() > 1 && includeByCategories(advantage, included, excluded);
            }

        },
        ADS_ALL {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getAdjustedPoints() > 0 && includeByCategories(advantage, included, excluded);
            }

        },
        DISADS {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getAdjustedPoints() < -1 && includeByCategories(advantage, included, excluded);
            }

        },
        DISADS_ALL {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getAdjustedPoints() < 0 && includeByCategories(advantage, included, excluded);
            }

        },
        PERKS {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getAdjustedPoints() == 1 && includeByCategories(advantage, included, excluded);
            }

        },
        QUIRKS {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getAdjustedPoints() == -1 && includeByCategories(advantage, included, excluded);
            }

        },
        LANGUAGES {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getCategories().contains("Language") && includeByCategories(advantage, included, excluded);
            }

        },
        CULTURAL_FAMILIARITIES {
            @Override
            public boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded) {
                return advantage.getName().startsWith("Cultural Familiarity (") && includeByCategories(advantage, included, excluded);
            }

        };

        public abstract boolean shouldInclude(Advantage advantage, Set<String> included, Set<String> excluded);

        private static boolean includeByCategories(Advantage advantage, Set<String> included, Set<String> excluded) {
            for (String cat : included) {
                if (advantage.hasCategory(cat)) {
                    return true;
                }
            }
            if (!included.isEmpty()) {
                return false;
            }
            for (String cat : excluded) {
                if (advantage.hasCategory(cat)) {
                    return false;
                }
            }
            return true;
        }
    }
}
