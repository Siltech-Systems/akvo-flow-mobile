/*
 * Copyright (C) 2017 Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Flow.
 *
 * Akvo Flow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Flow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Flow.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.akvo.flow.ui.model.caddisfly;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;

public class CaddisflyJsonMapper {

    private final Gson gson = new Gson();

    public CaddisflyJsonMapper() {
    }

    @NonNull
    public List<CaddisflyTestResult> transform(@Nullable String result) {
        if (result != null) {
            CaddisflyResult caddisflyResult = gson.fromJson(result, CaddisflyResult.class);
            if (caddisflyResult != null && caddisflyResult.getResults() != null) {
                return caddisflyResult.getResults();
            }
        }
        return Collections.emptyList();
    }
}
