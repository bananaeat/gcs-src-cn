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

package com.trollworks.gcs.feature;

import com.trollworks.gcs.equipment.Equipment;
import com.trollworks.gcs.modifier.EquipmentModifier;
import com.trollworks.gcs.ui.widget.outline.ListRow;
import com.trollworks.gcs.utility.I18n;

/** The type of feature. */
public enum FeatureType {
    ATTRIBUTE_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予属性加成");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof AttributeBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new AttributeBonusEditor(row, (AttributeBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new AttributeBonus();
        }
    },
    DR_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予DR加成");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof DRBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new DRBonusEditor(row, (DRBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new DRBonus();
        }
    },
    REACTION_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予反应调整值");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof ReactionBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new ReactionBonusEditor(row, (ReactionBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new ReactionBonus();
        }
    },
    CONDITIONAL_MODIFIER {
        @Override
        public String toString() {
            return I18n.text("给予条件调整值");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof ConditionalModifier;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new ConditionalModifierEditor(row, (ConditionalModifier) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new ConditionalModifier();
        }
    },
    SKILL_LEVEL_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予技能等级加成");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SkillBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new SkillBonusEditor(row, (SkillBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new SkillBonus();
        }
    },
    SKILL_POINT_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予技能点加成");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SkillPointBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new SkillPointBonusEditor(row, (SkillPointBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new SkillPointBonus();
        }
    },
    SPELL_LEVEL_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予法术等级加成");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SpellBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new SpellBonusEditor(row, (SpellBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new SpellBonus();
        }
    },
    SPELL_POINT_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予法术点数加成");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof SpellPointBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new SpellPointBonusEditor(row, (SpellPointBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new SpellPointBonus();
        }
    },
    WEAPON_DAMAGE_BONUS {
        @Override
        public String toString() {
            return I18n.text("给予武器伤害加成");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof WeaponDamageBonus;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new WeaponDamageBonusEditor(row, (WeaponDamageBonus) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new WeaponDamageBonus();
        }
    },
    REDUCE_ATTRIBUTE_COST {
        @Override
        public String toString() {
            return I18n.text("减少属性消耗");
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof CostReduction;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new CostReductionEditor(row, (CostReduction) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new CostReduction();
        }
    },
    REDUCE_CONTAINED_WEIGHT {
        @Override
        public String toString() {
            return I18n.text("减少权重");
        }

        @Override
        public boolean validRow(ListRow row) {
            return row instanceof Equipment || row instanceof EquipmentModifier;
        }

        @Override
        public boolean matches(Feature feature) {
            return feature instanceof ContainedWeightReduction;
        }

        @Override
        public FeatureEditor createFeatureEditor(ListRow row, Feature feature) {
            if (matches(feature)) {
                return new ContainedWeightReductionEditor(row, (ContainedWeightReduction) feature);
            }
            return null;
        }

        @Override
        public Feature createFeature() {
            return new ContainedWeightReduction();
        }
    };

    /**
     * @param row The row to check.
     * @return {@code true} if the row can be used with this feature type.
     */
    public boolean validRow(ListRow row) {
        return true;
    }

    /**
     * @param feature The feature to check.
     * @return {@code true} if the feature matches this feature type.
     */
    public abstract boolean matches(Feature feature);

    /**
     * @param row     The row to edit.
     * @param feature The feature to edit.
     * @return An editor for the feature.
     */
    public abstract FeatureEditor createFeatureEditor(ListRow row, Feature feature);

    /** @return A new feature of this type. */
    public abstract Feature createFeature();
}
