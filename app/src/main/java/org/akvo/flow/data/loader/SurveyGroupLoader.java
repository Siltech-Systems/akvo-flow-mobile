/*
 * Copyright (C) 2010-2017 Stichting Akvo (Akvo Foundation)
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

package org.akvo.flow.data.loader;

import android.content.Context;
import android.database.Cursor;

import org.akvo.flow.data.loader.base.DataLoader;
import org.akvo.flow.database.SurveyDbAdapter;

public class SurveyGroupLoader extends DataLoader<Cursor> {

    public SurveyGroupLoader(Context context, SurveyDbAdapter db) {
        super(context, db);
    }

    @Override
    protected Cursor loadData(SurveyDbAdapter database) {
        return database.getSurveyGroups();
    }

}
