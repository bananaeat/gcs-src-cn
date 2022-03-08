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

package com.trollworks.gcs.attribute;

import com.trollworks.gcs.utility.I18n;

public enum ThresholdOps {
    UNKNOWN {
        @Override
        public String title() {
            return toString();
        }

        @Override
        public String toString() {
            return I18n.text("未知");
        }
    },
    HALVE_MOVE {
        @Override
        public String title() {
            return I18n.text("减半移动");
        }

        @Override
        public String toString() {
            return I18n.text("减半移动（向上取整）");
        }
    },
    HALVE_DODGE {
        @Override
        public String title() {
            return I18n.text("减半闪避");
        }

        @Override
        public String toString() {
            return I18n.text("减半闪避（向上取整）");
        }
    },
    HALVE_ST {
        @Override
        public String title() {
            return I18n.text("减半ST");
        }

        @Override
        public String toString() {
            return I18n.text("减半ST (向上取整；不影响HP和伤害)");
        }
    };

    abstract String title();
}
