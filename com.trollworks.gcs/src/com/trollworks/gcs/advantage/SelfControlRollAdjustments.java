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

import com.trollworks.gcs.criteria.StringCompareType;
import com.trollworks.gcs.criteria.StringCriteria;
import com.trollworks.gcs.feature.Bonus;
import com.trollworks.gcs.feature.LeveledAmount;
import com.trollworks.gcs.feature.SkillBonus;
import com.trollworks.gcs.utility.I18n;
import com.trollworks.gcs.utility.text.Numbers;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The possible adjustments for self-control rolls. */
public enum SelfControlRollAdjustments {
    /** None. */
    NONE {
        @Override
        public String toString() {
            return I18n.text("无");
        }

        @Override
        public String getDescription(SelfControlRoll cr) {
            return "";
        }

        @Override
        public int getAdjustment(SelfControlRoll cr) {
            return 0;
        }
    },
    /** General action penalty. */
    ACTION_PENALTY {
        @Override
        public String toString() {
            return I18n.text("包括失败时的动作减值");
        }

        @Override
        public String getDescription(SelfControlRoll cr) {
            if (cr == SelfControlRoll.NONE_REQUIRED) {
                return "";
            }
            return MessageFormat.format(I18n.text("{0}动作减值"), Numbers.formatWithForcedSign(getAdjustment(cr)));
        }

        @Override
        public int getAdjustment(SelfControlRoll cr) {
            return cr.ordinal() - SelfControlRoll.NONE_REQUIRED.ordinal();
        }
    },
    /** Reaction penalty. */
    REACTION_PENALTY {
        @Override
        public String toString() {
            return I18n.text("包括失败时的反应减值");
        }

        @Override
        public String getDescription(SelfControlRoll cr) {
            if (cr == SelfControlRoll.NONE_REQUIRED) {
                return "";
            }
            return MessageFormat.format(I18n.text("{0} 反应减值"), Numbers.formatWithForcedSign(getAdjustment(cr)));
        }

        @Override
        public int getAdjustment(SelfControlRoll cr) {
            return cr.ordinal() - SelfControlRoll.NONE_REQUIRED.ordinal();
        }
    },
    /** Fright Check penalty. */
    FRIGHT_CHECK_PENALTY {
        @Override
        public String toString() {
            return I18n.text("包括恐惧检定减值");
        }

        @Override
        public String getDescription(SelfControlRoll cr) {
            if (cr == SelfControlRoll.NONE_REQUIRED) {
                return "";
            }
            return MessageFormat.format(I18n.text("{0} 恐惧检定减值"), Numbers.formatWithForcedSign(getAdjustment(cr)));
        }

        @Override
        public int getAdjustment(SelfControlRoll cr) {
            return cr.ordinal() - SelfControlRoll.NONE_REQUIRED.ordinal();
        }
    },
    /** Fright Check bonus. */
    FRIGHT_CHECK_BONUS {
        @Override
        public String toString() {
            return I18n.text("包括恐惧检定加值");
        }

        @Override
        public String getDescription(SelfControlRoll cr) {
            if (cr == SelfControlRoll.NONE_REQUIRED) {
                return "";
            }
            return MessageFormat.format(I18n.text("{0} 恐惧检定加值"), Numbers.formatWithForcedSign(getAdjustment(cr)));
        }

        @Override
        public int getAdjustment(SelfControlRoll cr) {
            return SelfControlRoll.NONE_REQUIRED.ordinal() - cr.ordinal();
        }
    },
    /** Minor cost of living increase. */
    MINOR_COST_OF_LIVING_INCREASE {
        @Override
        public String toString() {
            return I18n.text("包括生活费用小幅度提升");
        }

        @Override
        public String getDescription(SelfControlRoll cr) {
            if (cr == SelfControlRoll.NONE_REQUIRED) {
                return "";
            }
            return MessageFormat.format(I18n.text("{0}% 生活费用提升"), Numbers.formatWithForcedSign(getAdjustment(cr)));
        }

        @Override
        public int getAdjustment(SelfControlRoll cr) {
            return 5 * (SelfControlRoll.NONE_REQUIRED.ordinal() - cr.ordinal());
        }
    },
    /** Major cost of living increase plus merchant penalty. */
    MAJOR_COST_OF_LIVING_INCREASE {
        @Override
        public String toString() {
            return I18n.text("包括生活费用大幅度提升及商人技能减值");
        }

        @Override
        public String getDescription(SelfControlRoll cr) {
            if (cr == SelfControlRoll.NONE_REQUIRED) {
                return "";
            }
            return MessageFormat.format(I18n.text("{0}% 生活费用提升"), Numbers.formatWithForcedSign(getAdjustment(cr)));
        }

        @Override
        public int getAdjustment(SelfControlRoll cr) {
            return switch (cr) {
                case CR6 -> 80;
                case CR9 -> 40;
                case CR12 -> 20;
                case CR15 -> 10;
                default -> 0;
            };
        }

        @Override
        public List<Bonus> getBonuses(SelfControlRoll cr) {
            List<Bonus>    list     = new ArrayList<>();
            SkillBonus     bonus    = new SkillBonus();
            StringCriteria criteria = bonus.getNameCriteria();
            criteria.setType(StringCompareType.IS);
            criteria.setQualifier("Merchant");
            criteria = bonus.getSpecializationCriteria();
            criteria.setType(StringCompareType.ANY);
            LeveledAmount amount = bonus.getAmount();
            amount.setDecimal(false);
            amount.setPerLevel(false);
            amount.setAmount(cr.ordinal() - SelfControlRoll.NONE_REQUIRED.ordinal());
            list.add(bonus);
            return list;
        }
    };

    /**
     * @param cr The {@link SelfControlRoll} being adjusted.
     * @return The short description.
     */
    public abstract String getDescription(SelfControlRoll cr);

    /**
     * @param cr The {@link SelfControlRoll} being adjusted.
     * @return The adjustment value.
     */
    public abstract int getAdjustment(SelfControlRoll cr);

    /**
     * @param cr The {@link SelfControlRoll} being adjusted.
     * @return The set of bonuses that this adjustment provides.
     */
    public List<Bonus> getBonuses(SelfControlRoll cr) {
        return Collections.emptyList();
    }
}
