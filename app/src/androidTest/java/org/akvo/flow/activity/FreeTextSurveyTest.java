/*
 *  Copyright (C) 2017 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Flow.
 *
 *  Akvo Flow is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Akvo Flow is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Akvo Flow.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.flow.activity;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.rule.ActivityTestRule;

import org.akvo.flow.R;
import org.akvo.flow.domain.Survey;
import org.akvo.flow.testhelper.SurveyInstaller;
import org.akvo.flow.util.FileUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.akvo.flow.tests.R.raw.freetextsurvey;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.not;

@Ignore("These tests are broken for now")
public class FreeTextSurveyTest {

    @Rule
    public ActivityTestRule<SurveyActivity> rule = new ActivityTestRule<>(SurveyActivity.class);

    private Context runtimeContext;
    private SurveyInstaller installer;

    @Before
    public void init() {
        runtimeContext = InstrumentationRegistry.getContext();
        installer      = new SurveyInstaller(rule.getActivity().getApplicationContext());
    }

    @Test
    public void canFillFreeTextQuestion() throws IOException {
        fillFreeTextQuestion(freetextsurvey, "This is an answer to your question");
        onView(allOf(withClassName(endsWith("Button")), withText(R.string.submitbutton))).check(matches((isEnabled())));
    }

    @Test
    public void ensureCantSubmitEmptyFreeText() throws IOException {
        fillFreeTextQuestion(freetextsurvey, "");
        onView(allOf(withClassName(endsWith("Button")), withText(R.string.submitbutton))).check(matches(not(isEnabled())));
    }

    private Survey fillFreeTextQuestion(int surveyResId, String text) throws IOException {
        Survey survey = getSurvey(surveyResId);

        openDrawer();
        onView(withText(survey.getName())).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.new_datapoint)).perform(click());
        onView(withId(R.id.question_tv)).check(matches(isDisplayed()));
        onView(withId(R.id.input_et)).perform(typeText(text));
        onView(withId(R.id.next_btn)).perform(click());

        return survey;
    }

    private Survey getSurvey(int resId) throws IOException {
        InputStream input = runtimeContext.getResources().openRawResource(resId);
        return installer.persistSurvey(FileUtil.readText(input));
    }

    private void openDrawer() {
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
    }
}
