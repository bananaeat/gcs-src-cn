/*
 * Copyright ©1998-2021 by Richard A. Wilkes. All rights reserved.
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
import com.trollworks.gcs.character.names.USCensusNames;
import com.trollworks.gcs.feature.BonusAttributeType;
import com.trollworks.gcs.preferences.Preferences;
import com.trollworks.gcs.ui.RetinaIcon;
import com.trollworks.gcs.ui.image.Images;
import com.trollworks.gcs.ui.image.Img;
import com.trollworks.gcs.utility.FileType;
import com.trollworks.gcs.utility.Fixed6;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.Log;
import com.trollworks.gcs.utility.json.JsonMap;
import com.trollworks.gcs.utility.json.JsonWriter;
import com.trollworks.gcs.utility.text.Numbers;
import com.trollworks.gcs.utility.units.LengthUnits;
import com.trollworks.gcs.utility.units.LengthValue;
import com.trollworks.gcs.utility.units.WeightUnits;
import com.trollworks.gcs.utility.units.WeightValue;
import static java.time.format.TextStyle.FULL;
import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;

/** Holds the character profile. */
public class Profile {
    /** The root XML tag. */
    public static final  String            TAG_ROOT             = "profile";
    private static final String            KEY_SM               = "SM";
    /** The prefix used in front of all IDs for profile. */
    public static final  String            PROFILE_PREFIX       = GURPSCharacter.CHARACTER_PREFIX + "pi.";
    /** The field ID for portrait changes. */
    public static final  String            ID_PORTRAIT          = PROFILE_PREFIX + "Portrait";
    /** The field ID for name changes. */
    public static final  String            ID_NAME              = PROFILE_PREFIX + "Name";
    /** The field ID for title changes. */
    public static final  String            ID_TITLE             = PROFILE_PREFIX + "Title";
    /** The field ID for age changes. */
    public static final  String            ID_AGE               = PROFILE_PREFIX + "Age";
    /** The field ID for birthday changes. */
    public static final  String            ID_BIRTHDAY          = PROFILE_PREFIX + "Birthday";
    /** The field ID for eye color changes. */
    public static final  String            ID_EYE_COLOR         = PROFILE_PREFIX + "EyeColor";
    /** The field ID for hair color changes. */
    public static final  String            ID_HAIR              = PROFILE_PREFIX + "Hair";
    /** The field ID for skin color changes. */
    public static final  String            ID_SKIN_COLOR        = PROFILE_PREFIX + "SkinColor";
    /** The field ID for handedness changes. */
    public static final  String            ID_HANDEDNESS        = PROFILE_PREFIX + "Handedness";
    /** The field ID for height changes. */
    public static final  String            ID_HEIGHT            = PROFILE_PREFIX + "Height";
    /** The field ID for weight changes. */
    public static final  String            ID_WEIGHT            = PROFILE_PREFIX + "Weight";
    /** The field ID for gender changes. */
    public static final  String            ID_GENDER            = PROFILE_PREFIX + "Gender";
    /** The field ID for religion changes. */
    public static final  String            ID_RELIGION          = PROFILE_PREFIX + "Religion";
    /** The field ID for player name changes. */
    public static final  String            ID_PLAYER_NAME       = PROFILE_PREFIX + "PlayerName";
    /** The field ID for tech level changes. */
    public static final  String            ID_TECH_LEVEL        = PROFILE_PREFIX + "TechLevel";
    /** The field ID for size modifier changes. */
    public static final  String            ID_SIZE_MODIFIER     = PROFILE_PREFIX + BonusAttributeType.SM.name();
    /** The field ID for body type changes. */
    public static final  String            ID_BODY_TYPE         = PROFILE_PREFIX + "BodyType";
    /** The height, in 1/72nds of an inch, of the portrait. */
    public static final  int               PORTRAIT_HEIGHT      = 96;
    /** The width, in 1/72nds of an inch, of the portrait. */
    public static final  int               PORTRAIT_WIDTH       = 3 * PORTRAIT_HEIGHT / 4;
    private static final DateTimeFormatter MONTH_AND_DAY_FORMAT = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient().appendText(MONTH_OF_YEAR, FULL).appendLiteral(' ').appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE).toFormatter();
    private static final String            TAG_PLAYER_NAME      = "player_name";
    private static final String            TAG_NAME             = "name";
    private static final String            TAG_TITLE            = "title";
    private static final String            TAG_AGE              = "age";
    private static final String            TAG_BIRTHDAY         = "birthday";
    private static final String            TAG_EYES             = "eyes";
    private static final String            TAG_HAIR             = "hair";
    private static final String            TAG_SKIN             = "skin";
    private static final String            TAG_HANDEDNESS       = "handedness";
    private static final String            TAG_HEIGHT           = "height";
    private static final String            TAG_WEIGHT           = "weight";
    private static final String            TAG_GENDER           = "gender";
    private static final String            TAG_TECH_LEVEL       = "tech_level";
    private static final String            TAG_RELIGION         = "religion";
    private static final String            TAG_PORTRAIT         = "portrait";
    private static final String            TAG_BODY_TYPE        = "body_type";
    private static final Random            RANDOM               = new Random();
    private              GURPSCharacter    mCharacter;
    private              boolean           mCustomPortrait;
    private              RetinaIcon        mPortrait;
    private              String            mName;
    private              String            mTitle;
    private              String            mAge;
    private              String            mBirthday;
    private              String            mEyeColor;
    private              String            mHair;
    private              String            mSkinColor;
    private              String            mHandedness;
    private              LengthValue       mHeight;
    private              WeightValue       mWeight;
    private              int               mSizeModifier;
    private              int               mSizeModifierBonus;
    private              String            mGender;
    private              String            mReligion;
    private              String            mPlayerName;
    private              String            mTechLevel;
    private              HitLocationTable  mHitLocationTable;

    Profile(GURPSCharacter character, boolean full) {
        mCharacter = character;
        mCustomPortrait = false;
        mPortrait = null;
        mTitle = "";
        mReligion = "";
        mHitLocationTable = HitLocationTable.HUMANOID;
        Preferences prefs = Preferences.getInstance();
        mPortrait = createPortrait(getPortraitFromPortraitPath(prefs.getDefaultPortraitPath()));
        if (full) {
            mAge = Numbers.format(getRandomAge());
            mBirthday = getRandomMonthAndDay();
            mEyeColor = getRandomEyeColor("");
            mHair = getRandomHair("");
            mSkinColor = getRandomSkinColor("");
            mHandedness = getRandomHandedness();
            mHeight = getRandomHeight(mCharacter.getStrength(), getSizeModifier());
            mWeight = getRandomWeight(mCharacter.getStrength(), getSizeModifier(), Fixed6.ONE);
            mGender = getRandomGender();
            mName = USCensusNames.INSTANCE.getFullName(I18n.Text("Male").equals(mGender));
            mTechLevel = prefs.getDefaultTechLevel();
            mPlayerName = prefs.getDefaultPlayerName();
        } else {
            mAge = "";
            mBirthday = "";
            mEyeColor = "";
            mHair = "";
            mSkinColor = "";
            mHandedness = "";
            Settings settings = mCharacter.getSettings();
            mHeight = new LengthValue(Fixed6.ZERO, settings.defaultLengthUnits());
            mWeight = new WeightValue(Fixed6.ZERO, settings.defaultWeightUnits());
            mGender = "";
            mName = "";
            mTechLevel = "";
            mPlayerName = "";
        }
    }

    void load(JsonMap m) {
        mPlayerName = m.getString(TAG_PLAYER_NAME);
        mName = m.getString(TAG_NAME);
        mTitle = m.getString(TAG_TITLE);
        mAge = m.getString(TAG_AGE);
        mBirthday = m.getString(TAG_BIRTHDAY);
        mEyeColor = m.getString(TAG_EYES);
        mHair = m.getString(TAG_HAIR);
        mSkinColor = m.getString(TAG_SKIN);
        mHandedness = m.getString(TAG_HANDEDNESS);
        mHeight = LengthValue.extract(m.getString(TAG_HEIGHT), false);
        mWeight = WeightValue.extract(m.getString(TAG_WEIGHT), false);
        mSizeModifier = m.getInt(KEY_SM);
        mGender = m.getString(TAG_GENDER);
        mHitLocationTable = HitLocationTable.MAP.get(m.getString(TAG_BODY_TYPE));
        if (mHitLocationTable == null) {
            mHitLocationTable = HitLocationTable.HUMANOID;
        }
        mTechLevel = m.getString(TAG_TECH_LEVEL);
        mReligion = m.getString(TAG_RELIGION);
        if (m.has(TAG_PORTRAIT)) {
            try {
                mPortrait = createPortrait(Img.create(new ByteArrayInputStream(Base64.getDecoder().decode(m.getString(TAG_PORTRAIT)))));
                mCustomPortrait = true;
            } catch (Exception imageException) {
                Log.warn(imageException);
            }
        }
    }

    void save(JsonWriter w) throws IOException {
        w.startMap();
        w.keyValueNot(TAG_PLAYER_NAME, mPlayerName, "");
        w.keyValueNot(TAG_NAME, mName, "");
        w.keyValueNot(TAG_TITLE, mTitle, "");
        w.keyValueNot(TAG_AGE, mAge, "");
        w.keyValueNot(TAG_BIRTHDAY, mBirthday, "");
        w.keyValueNot(TAG_EYES, mEyeColor, "");
        w.keyValueNot(TAG_HAIR, mHair, "");
        w.keyValueNot(TAG_SKIN, mSkinColor, "");
        w.keyValueNot(TAG_HANDEDNESS, mHandedness, "");
        if (!mHeight.getNormalizedValue().equals(Fixed6.ZERO)) {
            w.keyValue(TAG_HEIGHT, mHeight.toString(false));
        }
        if (!mWeight.getNormalizedValue().equals(Fixed6.ZERO)) {
            w.keyValue(TAG_WEIGHT, mWeight.toString(false));
        }
        w.keyValueNot(KEY_SM, mSizeModifier, 0);
        w.keyValueNot(TAG_GENDER, mGender, "");
        w.keyValue(TAG_BODY_TYPE, mHitLocationTable.getKey());
        w.keyValueNot(TAG_TECH_LEVEL, mTechLevel, "");
        w.keyValueNot(TAG_RELIGION, mReligion, "");
        if (mCustomPortrait && mPortrait != null) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(mPortrait.getRetina(), FileType.PNG.getExtension(), baos);
                w.keyValue(TAG_PORTRAIT, Base64.getEncoder().encodeToString(baos.toByteArray()));
            } catch (Exception imageException) {
                Log.warn(imageException);
            }
        }
        w.endMap();
    }

    void update() {
        setSizeModifierBonus(mCharacter.getIntegerBonusFor(GURPSCharacter.ATTRIBUTES_PREFIX + BonusAttributeType.SM.name()));
    }

    /** @return The portrait. */
    public RetinaIcon getPortrait() {
        return mPortrait;
    }

    /**
     * Sets the portrait.
     *
     * @param portrait The new portrait.
     */
    public void setPortrait(Img portrait) {
        if (portrait == null ? mPortrait != null : mPortrait.getRetina() != portrait) {
            mCustomPortrait = true;
            RetinaIcon newPortrait = portrait != null ? createPortrait(portrait) : null;
            mCharacter.postUndoFieldEdit(I18n.Text("Portrait Change"), (c, v) -> c.getProfile().setPortrait(v != null ? ((RetinaIcon) v).getRetina() : null), mPortrait, newPortrait);
            mPortrait = newPortrait;
            mCharacter.notifyOfChange();
        }
    }

    public static RetinaIcon createPortrait(Img image) {
        if (image == null) {
            return null;
        }
        Img normal;
        Img retina;
        int width  = image.getWidth();
        int height = image.getHeight();
        if (width == PORTRAIT_WIDTH * 2 && height == PORTRAIT_HEIGHT * 2) {
            retina = image;
            normal = retina.scale(PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
        } else if (width == PORTRAIT_WIDTH && height == PORTRAIT_HEIGHT) {
            normal = image;
            retina = normal.scale(PORTRAIT_WIDTH * 2, PORTRAIT_HEIGHT * 2);
        } else {
            int    dw = PORTRAIT_WIDTH * 2;
            int    dh = PORTRAIT_HEIGHT * 2;
            double r  = Math.min(dw / (double) width, dh / (double) height);
            int    w  = (int) (width * r);
            int    h  = (int) (height * r);
            retina = Img.create(dw, dh, Transparency.TRANSLUCENT);
            Graphics2D gc = retina.getGraphics();
            gc.drawImage(image.scale(w, h), (dw - w) / 2, (dh - h) / 2, null);
            gc.dispose();
            normal = retina.scale(PORTRAIT_WIDTH, PORTRAIT_HEIGHT);
        }
        return new RetinaIcon(normal, retina);
    }

    /** @return The name. */
    public String getName() {
        return mName;
    }

    /**
     * Sets the name.
     *
     * @param name The new name.
     */
    public void setName(String name) {
        if (!mName.equals(name)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Name Change"), (c, v) -> c.getProfile().setName((String) v), mName, name);
            mName = name;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The gender. */
    public String getGender() {
        return mGender;
    }

    /**
     * Sets the gender.
     *
     * @param gender The new gender.
     */
    public void setGender(String gender) {
        if (!mGender.equals(gender)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Gender Change"), (c, v) -> c.getProfile().setGender((String) v), mGender, gender);
            mGender = gender;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The religion. */
    public String getReligion() {
        return mReligion;
    }

    /**
     * Sets the religion.
     *
     * @param religion The new religion.
     */
    public void setReligion(String religion) {
        if (!mReligion.equals(religion)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Religion Change"), (c, v) -> c.getProfile().setReligion((String) v), mReligion, religion);
            mReligion = religion;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The player's name. */
    public String getPlayerName() {
        return mPlayerName;
    }

    /**
     * Sets the player's name.
     *
     * @param player The new player's name.
     */
    public void setPlayerName(String player) {
        if (!mPlayerName.equals(player)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Player Name Change"), (c, v) -> c.getProfile().setPlayerName((String) v), mPlayerName, player);
            mPlayerName = player;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The tech level. */
    public String getTechLevel() {
        return mTechLevel;
    }

    /**
     * Sets the tech level.
     *
     * @param techLevel The new tech level.
     */
    public void setTechLevel(String techLevel) {
        if (!mTechLevel.equals(techLevel)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Tech Level Change"), (c, v) -> c.getProfile().setTechLevel((String) v), mTechLevel, techLevel);
            mTechLevel = techLevel;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The title. */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Sets the title.
     *
     * @param title The new title.
     */
    public void setTitle(String title) {
        if (!mTitle.equals(title)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Title Change"), (c, v) -> c.getProfile().setTitle((String) v), mTitle, title);
            mTitle = title;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The age. */
    public String getAge() {
        return mAge;
    }

    /**
     * Sets the age.
     *
     * @param age The new age.
     */
    public void setAge(String age) {
        if (!mAge.equals(age)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Age Change"), (c, v) -> c.getProfile().setAge((String) v), mAge, age);
            mAge = age;
            mCharacter.notifyOfChange();
        }
    }

    /** @return A random age. */
    public int getRandomAge() {
        if (mCharacter.getAdvantageNamed("Unaging") != null) {
            return 18 + RANDOM.nextInt(7);
        }

        int mod = 7;
        if (RANDOM.nextInt(3) == 1) {
            mod += 7;
            if (RANDOM.nextInt(4) == 1) {
                mod += 13;
            }
        }

        int       base     = 16;
        int       levels;
        Advantage lifespan = mCharacter.getAdvantageNamed("Short Lifespan");
        if (lifespan != null) {
            levels = lifespan.getLevels();
            base >>= levels;
            mod >>= levels;
        } else {
            lifespan = mCharacter.getAdvantageNamed("Extended Lifespan");
            if (lifespan != null) {
                levels = lifespan.getLevels();
                base <<= levels;
                mod <<= levels;
            }
        }
        if (mod < 1) {
            mod = 1;
        }

        return base + RANDOM.nextInt(mod);
    }

    /** @return The date of birth. */
    public String getBirthday() {
        return mBirthday;
    }

    /**
     * Sets the date of birth.
     *
     * @param birthday The new date of birth.
     */
    public void setBirthday(String birthday) {
        if (!mBirthday.equals(birthday)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Birthday Change"), (c, v) -> c.getProfile().setBirthday((String) v), mBirthday, birthday);
            mBirthday = birthday;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The eye color. */
    public String getEyeColor() {
        return mEyeColor;
    }

    /**
     * Sets the eye color.
     *
     * @param eyeColor The new eye color.
     */
    public void setEyeColor(String eyeColor) {
        if (!mEyeColor.equals(eyeColor)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Eye Color Change"), (c, v) -> c.getProfile().setEyeColor((String) v), mEyeColor, eyeColor);
            mEyeColor = eyeColor;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The hair. */
    public String getHair() {
        return mHair;
    }

    /**
     * Sets the hair.
     *
     * @param hair The new hair.
     */
    public void setHair(String hair) {
        if (!mHair.equals(hair)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Hair Change"), (c, v) -> c.getProfile().setHair((String) v), mHair, hair);
            mHair = hair;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The skin color. */
    public String getSkinColor() {
        return mSkinColor;
    }

    /**
     * Sets the skin color.
     *
     * @param skinColor The new skin color.
     */
    public void setSkinColor(String skinColor) {
        if (!mSkinColor.equals(skinColor)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Skin Color Change"), (c, v) -> c.getProfile().setSkinColor((String) v), mSkinColor, skinColor);
            mSkinColor = skinColor;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The handedness. */
    public String getHandedness() {
        return mHandedness;
    }

    /**
     * Sets the handedness.
     *
     * @param handedness The new handedness.
     */
    public void setHandedness(String handedness) {
        if (!mHandedness.equals(handedness)) {
            mCharacter.postUndoFieldEdit(I18n.Text("Handedness Change"), (c, v) -> c.getProfile().setHandedness((String) v), mHandedness, handedness);
            mHandedness = handedness;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The height. */
    public LengthValue getHeight() {
        return mHeight;
    }

    /**
     * Sets the height.
     *
     * @param height The new height.
     */
    public void setHeight(LengthValue height) {
        if (!mHeight.equals(height)) {
            height = new LengthValue(height);
            mCharacter.postUndoFieldEdit(I18n.Text("Height Change"), (c, v) -> c.getProfile().setHeight((LengthValue) v), new LengthValue(mHeight), height);
            mHeight = height;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The weight. */
    public WeightValue getWeight() {
        return mWeight;
    }

    /**
     * Sets the weight.
     *
     * @param weight The new weight.
     */
    public void setWeight(WeightValue weight) {
        if (!mWeight.equals(weight)) {
            weight = new WeightValue(weight);
            mCharacter.postUndoFieldEdit(I18n.Text("Weight Change"), (c, v) -> c.getProfile().setWeight((WeightValue) v), new WeightValue(mWeight), weight);
            mWeight = weight;
            mCharacter.notifyOfChange();
        }
    }

    /** @return The multiplier compared to average weight for this character. */
    public Fixed6 getWeightMultiplier() {
        if (mCharacter.hasAdvantageNamed("Very Fat")) {
            return new Fixed6(2);
        } else if (mCharacter.hasAdvantageNamed("Fat")) {
            return new Fixed6("1.5", Fixed6.ZERO, false);
        } else if (mCharacter.hasAdvantageNamed("Overweight")) {
            return new Fixed6("1.3", Fixed6.ZERO, false);
        } else if (mCharacter.hasAdvantageNamed("Skinny")) {
            return new Fixed6("0.67", Fixed6.ZERO, false);
        }
        return Fixed6.ONE;
    }

    /** @return The size modifier. */
    public int getSizeModifier() {
        return mSizeModifier + mSizeModifierBonus;
    }

    /** @return The size modifier bonus. */
    public int getSizeModifierBonus() {
        return mSizeModifierBonus;
    }

    /** @param size The new size modifier. */
    public void setSizeModifier(int size) {
        int totalSizeModifier = getSizeModifier();

        if (totalSizeModifier != size) {
            Integer value = Integer.valueOf(size);

            mCharacter.postUndoFieldEdit(I18n.Text("Size Modifier Change"), (c, v) -> c.getProfile().setSizeModifier(((Integer) v).intValue()), Integer.valueOf(totalSizeModifier), value);
            mSizeModifier = size - mSizeModifierBonus;
            mCharacter.notifyOfChange();
        }
    }

    /** @param bonus The new size modifier bonus. */
    public void setSizeModifierBonus(int bonus) {
        if (mSizeModifierBonus != bonus) {
            mSizeModifierBonus = bonus;
            mCharacter.notifyOfChange();
        }
    }

    /** @return A random hair color, style & length. */
    public static String getRandomHair(String not) {
        String result;
        do {
            if (RANDOM.nextInt(7) == 0) {
                result = I18n.Text("Bald");
            } else {
                String color = switch (RANDOM.nextInt(9)) {
                    case 0, 1, 2 -> I18n.Text("Black");
                    case 3, 4 -> I18n.Text("Blond");
                    case 5 -> I18n.Text("Redhead");
                    default -> I18n.Text("Brown");
                };
                String style = switch (RANDOM.nextInt(3)) {
                    case 0 -> I18n.Text("Curly");
                    case 1 -> I18n.Text("Wavy");
                    default -> I18n.Text("Straight");
                };
                String length = switch (RANDOM.nextInt(3)) {
                    case 0 -> I18n.Text("Short");
                    case 1 -> I18n.Text("Long");
                    default -> I18n.Text("Medium");
                };
                result = MessageFormat.format("{0}, {1}, {2}", color, style, length);
            }
        } while (result.equals(not));
        return result;
    }

    /** @return A random eye color. */
    public static String getRandomEyeColor(String not) {
        String result;
        do {
            result = switch (RANDOM.nextInt(8)) {
                case 0, 1 -> I18n.Text("Blue");
                case 2 -> I18n.Text("Green");
                case 3 -> I18n.Text("Grey");
                case 4 -> I18n.Text("Violet");
                default -> I18n.Text("Brown");
            };
        } while (result.equals(not));
        return result;
    }

    /** @return A random sking color. */
    public static String getRandomSkinColor(String not) {
        String result;
        do {
            result = switch (RANDOM.nextInt(8)) {
                case 0 -> I18n.Text("Freckled");
                case 1 -> I18n.Text("Light Tan");
                case 2 -> I18n.Text("Dark Tan");
                case 3 -> I18n.Text("Brown");
                case 4 -> I18n.Text("Light Brown");
                case 5 -> I18n.Text("Dark Brown");
                case 6 -> I18n.Text("Pale");
                default -> I18n.Text("Tan");
            };
        } while (result.equals(not));
        return result;
    }

    /** @return A random handedness. */
    public static String getRandomHandedness() {
        if (RANDOM.nextInt(4) == 0) {
            return I18n.Text("Left");
        }
        return I18n.Text("Right");
    }

    /** @return A random gender. */
    public static String getRandomGender() {
        if (RANDOM.nextInt(2) == 0) {
            return I18n.Text("Female");
        }
        return I18n.Text("Male");
    }

    /** @return A random month and day. */
    public static String getRandomMonthAndDay() {
        return Numbers.formatDateTime(MONTH_AND_DAY_FORMAT, RANDOM.nextLong());
    }

    /**
     * @param strength The strength to base the height on.
     * @param sm       The size modifier to use.
     * @return A random height.
     */
    public LengthValue getRandomHeight(int strength, int sm) {
        Fixed6 base;
        if (strength < 7) {
            base = new Fixed6(52);
        } else if (strength < 10) {
            base = new Fixed6(55 + (strength - 7) * 3);
        } else if (strength == 10) {
            base = new Fixed6(63);
        } else if (strength < 14) {
            base = new Fixed6(65 + (strength - 11) * 3);
        } else {
            base = new Fixed6(74);
        }
        Settings settings  = mCharacter.getSettings();
        boolean  useMetric = settings.defaultWeightUnits().isMetric();
        if (useMetric) {
            base = LengthUnits.CM.convert(LengthUnits.FT_IN, base).round().add(new Fixed6(RANDOM.nextInt(16)));
        } else {
            base = base.add(new Fixed6(RANDOM.nextInt(11)));
        }
        if (sm != 0) {
            base = base.mul(new Fixed6(Math.pow(10.0, sm / 6.0))).round();
            if (base.lessThan(Fixed6.ONE)) {
                base = Fixed6.ONE;
            }
        }
        LengthUnits calcUnits    = useMetric ? LengthUnits.CM : LengthUnits.FT_IN;
        LengthUnits desiredUnits = settings.defaultLengthUnits();
        return new LengthValue(desiredUnits.convert(calcUnits, base), desiredUnits);
    }

    /**
     * @param strength   The strength to base the weight on.
     * @param sm         The size modifier to use.
     * @param multiplier The weight multiplier for being under- or overweight.
     * @return A random weight.
     */
    public WeightValue getRandomWeight(int strength, int sm, Fixed6 multiplier) {
        Fixed6 base;
        Fixed6 range;
        if (strength < 7) {
            base = new Fixed6(60);
            range = new Fixed6(61);
        } else if (strength < 10) {
            base = new Fixed6(75 + (strength - 7) * 15);
            range = new Fixed6(61);
        } else if (strength == 10) {
            base = new Fixed6(115);
            range = new Fixed6(61);
        } else if (strength < 14) {
            base = new Fixed6(125 + (strength - 11) * 15);
            range = new Fixed6(71 + (strength - 11) * 10);
        } else {
            base = new Fixed6(170);
            range = new Fixed6(101);
        }
        Settings settings  = mCharacter.getSettings();
        boolean  useMetric = settings.defaultWeightUnits().isMetric();
        if (useMetric) {
            base = WeightUnits.KG.convert(WeightUnits.LB, base).round();
            range = WeightUnits.KG.convert(WeightUnits.LB, range.sub(Fixed6.ONE)).round().add(Fixed6.ONE);
        }
        base = base.add(new Fixed6(RANDOM.nextInt((int) range.asLong())));
        if (sm != 0) {
            base = base.mul(new Fixed6(Math.pow(1000.0, sm / 6.0))).round();
        }
        base = base.mul(multiplier).round();
        if (base.lessThan(Fixed6.ONE)) {
            base = Fixed6.ONE;
        }
        WeightUnits calcUnits    = useMetric ? WeightUnits.KG : WeightUnits.LB;
        WeightUnits desiredUnits = settings.defaultWeightUnits();
        return new WeightValue(desiredUnits.convert(calcUnits, base), desiredUnits);
    }

    /**
     * @param path The path to load.
     * @return The portrait.
     */
    public static Img getPortraitFromPortraitPath(String path) {
        if (Preferences.DEFAULT_DEFAULT_PORTRAIT_PATH.equals(path)) {
            return Images.DEFAULT_PORTRAIT;
        }
        try {
            return Img.create(new File(path));
        } catch (IOException exception) {
            return null;
        }
    }

    /** @return The hit location table. */
    public HitLocationTable getHitLocationTable() {
        return mHitLocationTable;
    }

    /** @param table The hit location table. */
    public void setHitLocationTable(HitLocationTable table) {
        if (mHitLocationTable != table) {
            mCharacter.postUndoFieldEdit(I18n.Text("Body Type Change"), (c, v) -> c.getProfile().setHitLocationTable((HitLocationTable) v), mHitLocationTable, table);
            mHitLocationTable = table;
            mCharacter.notifyOfChange();
        }
    }
}
