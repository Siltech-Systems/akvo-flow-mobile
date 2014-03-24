/*
 *  Copyright (C) 2010-2014 Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo FLOW.
 *
 *  Akvo FLOW is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo FLOW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.flow.ui.view;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import org.akvo.flow.R;
import org.akvo.flow.domain.AltText;
import org.akvo.flow.domain.Dependency;
import org.akvo.flow.domain.Question;
import org.akvo.flow.domain.QuestionHelp;
import org.akvo.flow.domain.QuestionResponse;
import org.akvo.flow.event.QuestionInteractionEvent;
import org.akvo.flow.event.QuestionInteractionListener;
import org.akvo.flow.util.ConstantUtil;
import org.akvo.flow.util.ViewUtil;

public abstract class QuestionView extends LinearLayout implements QuestionInteractionListener {
    protected static String[] sColors = null;

    protected Question mQuestion;
    private QuestionResponse mResponse;

    private List<QuestionInteractionListener> mListeners;
    protected String[] mLangs = null;
    protected String mDefaultLang;
    protected boolean mReadOnly;

    private TextView mQuestionText;
    private ImageButton mTipImage;

    public QuestionView(final Context context, Question q, String defaultLangauge, String[] langs,
            boolean readOnly) {
        super(context);
        mQuestion = q;
        mDefaultLang = defaultLangauge;
        mReadOnly = readOnly;
        mLangs = langs;
        if (sColors == null) {
            // must have enough colors for all enabled languages
            sColors = context.getResources().getStringArray(R.array.colors);
        }
    }

    protected void setupQuestion() {
        mQuestionText = (TextView)findViewById(R.id.question_tv);
        mTipImage = (ImageButton)findViewById(R.id.tip_ib);

        if (mQuestionText == null || mTipImage == null) {
            throw new RuntimeException(
                    "Subclasses must inflate the common question header before calling this method.");
        }

        mQuestionText.setText(formText(), BufferType.SPANNABLE);

        // if there is a tip for this question, construct an alert dialog box with the data
        final int tips = mQuestion.getHelpTypeCount();
        if (tips > 0) {
            mTipImage.setVisibility(View.VISIBLE);// GONE by default
            mTipImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tips > 1) {
                        displayHelpChoices();
                    } else {
                        if (mQuestion.getHelpByType(ConstantUtil.TIP_HELP_TYPE)
                                .size() > 0) {
                            displayHelp(ConstantUtil.TIP_HELP_TYPE);
                        } else if (mQuestion.getHelpByType(
                                ConstantUtil.VIDEO_HELP_TYPE).size() > 0) {
                            displayHelp(ConstantUtil.VIDEO_HELP_TYPE);
                        } else if (mQuestion.getHelpByType(
                                ConstantUtil.IMAGE_HELP_TYPE).size() > 0) {
                            displayHelp(ConstantUtil.IMAGE_HELP_TYPE);
                        }
                    }
                }
            });
        }

        if (!mReadOnly) {
            mQuestionText.setLongClickable(true);
            mQuestionText.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    ViewUtil.showConfirmDialog(R.string.clearquestion,
                            R.string.clearquestiondesc, getContext(), true,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    resetQuestion(true);
                                }
                            });
                    return true;
                }
            });
        }

        // if this question has 1 or more dependencies, then it needs to be invisible initially
        if (mQuestion.getDependencies() != null && mQuestion.getDependencies().size() > 0) {
            setVisibility(View.GONE);
        }
    }

    /**
     * forms the question text based on the selected languages
     *
     * @return
     */
    private Spanned formText() {
        boolean isFirst = true;
        StringBuilder text = new StringBuilder();
        if (mQuestion.isMandatory()) {
            text.append("<i><b>");
        }
        for (int i = 0; i < mLangs.length; i++) {
            if (mDefaultLang.equalsIgnoreCase(mLangs[i])) {
                if (!isFirst) {
                    text.append(" / ");
                } else {
                    isFirst = false;
                }
                text.append(mQuestion.getText());
            } else {
                AltText txt = mQuestion.getAltText(mLangs[i]);
                if (txt != null) {
                    if (!isFirst) {
                        text.append(" / ");
                    } else {
                        isFirst = false;
                    }
                    text.append("<font color='").append(sColors[i]).append("'>")
                            .append(txt.getText()).append("</font>");
                }
            }
        }
        if (mQuestion.isMandatory()) {
            text = text.append("*</b></i>");
        }
        return Html.fromHtml(text.toString());
    }

    /**
     * updates the question's visible languages
     *
     * @param languageCodes
     */
    public void updateSelectedLanguages(String[] languageCodes) {
        mLangs = languageCodes;
        mQuestionText.setText(formText());
    }

    /**
     * displays a dialog box with options for each of the help types that have
     * been initialized for this particular question.
     */
    @SuppressWarnings("rawtypes")
    private void displayHelpChoices() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.helpheading);
        final CharSequence[] items = new CharSequence[mQuestion
                .getHelpTypeCount()];
        final Resources resources = getResources();
        int itemIndex = 0;
        ArrayList tempList = mQuestion
                .getHelpByType(ConstantUtil.IMAGE_HELP_TYPE);

        if (tempList != null && tempList.size() > 0) {
            items[itemIndex++] = resources.getString(R.string.photohelpoption);
        }
        tempList = mQuestion.getHelpByType(ConstantUtil.VIDEO_HELP_TYPE);
        if (tempList != null && tempList.size() > 0) {
            items[itemIndex++] = resources.getString(R.string.videohelpoption);
        }
        tempList = mQuestion.getHelpByType(ConstantUtil.TIP_HELP_TYPE);
        if (tempList != null && tempList.size() > 0) {
            items[itemIndex++] = resources.getString(R.string.texthelpoption);
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String val = items[id].toString();
                if (resources.getString(R.string.texthelpoption).equals(val)) {
                    displayHelp(ConstantUtil.TIP_HELP_TYPE);
                } else if (resources.getString(R.string.videohelpoption)
                        .equals(val)) {
                    displayHelp(ConstantUtil.VIDEO_HELP_TYPE);
                } else if (resources.getString(R.string.photohelpoption)
                        .equals(val)) {
                    displayHelp(ConstantUtil.IMAGE_HELP_TYPE);
                }
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * displays the selected help type
     *
     * @param type
     */
    private void displayHelp(String type) {
        if (ConstantUtil.VIDEO_HELP_TYPE.equals(type)) {
            notifyQuestionListeners(QuestionInteractionEvent.VIDEO_TIP_VIEW);
        } else if (ConstantUtil.TIP_HELP_TYPE.equals(type)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            TextView tipText = new TextView(getContext());
            StringBuilder textBuilder = new StringBuilder();
            ArrayList<QuestionHelp> helpItems = mQuestion.getHelpByType(type);
            boolean isFirst = true;
            if (helpItems != null) {
                for (int i = 0; i < helpItems.size(); i++) {
                    if (i > 0) {
                        textBuilder.append("<br>");
                    }

                    for (int j = 0; j < mLangs.length; j++) {
                        if (mDefaultLang.equalsIgnoreCase(mLangs[j])) {
                            textBuilder.append(helpItems.get(i).getText());
                            isFirst = false;
                        }

                        AltText aText = helpItems.get(i).getAltText(mLangs[j]);
                        if (aText != null) {
                            if (!isFirst) {
                                textBuilder.append(" / ");
                            } else {
                                isFirst = false;
                            }

                            textBuilder.append("<font color='").append(sColors[j]).append("'>")
                                    .append(aText.getText()).append("</font>");
                        }
                    }
                }
            }
            tipText.setText(Html.fromHtml(textBuilder.toString()));
            builder.setView(tipText);
            builder.setPositiveButton(R.string.okbutton,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        } else if (ConstantUtil.IMAGE_HELP_TYPE.equals(type)) {
            notifyQuestionListeners(QuestionInteractionEvent.PHOTO_TIP_VIEW);
        } else {
            notifyQuestionListeners(QuestionInteractionEvent.ACTIVITY_TIP_VIEW);
        }
    }

    /**
     * adds a listener to the internal list of clients to be notified on an
     * event
     *
     * @param listener
     */
    public void addQuestionInteractionListener(
            QuestionInteractionListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<QuestionInteractionListener>();
        }
        if (listener != null && !mListeners.contains(listener) && listener != this) {
            mListeners.add(listener);
        }
    }

    /**
     * notifies each QuestionInteractionListener registered with this question.
     * This is done serially on the calling thread.
     *
     * @param type
     */
    protected void notifyQuestionListeners(String type) {
        if (mListeners != null) {
            QuestionInteractionEvent event = new QuestionInteractionEvent(type, this);
            for (int i = 0; i < mListeners.size(); i++) {
                mListeners.get(i).onQuestionInteraction(event);
            }
        }
    }

    /**
     * method that can be overridden by sub classes if they want to have some
     * sort of visual response to a question interaction.
     */
    public void questionComplete(Bundle data) {
        // do nothing
    }

    /**
     * method that should be overridden by sub classes to clear current value
     */
    public void resetQuestion(boolean fireEvent) {
        setResponse(null, false);
        highlight(false);
        if (fireEvent) {
            notifyQuestionListeners(QuestionInteractionEvent.QUESTION_CLEAR_EVENT);
        }
    }

    @Override
    public void onQuestionInteraction(QuestionInteractionEvent event) {
        if (QuestionInteractionEvent.QUESTION_ANSWER_EVENT.equals(event.getEventType())) {
            // if this question is dependent, see if it has been satisfied
            List<Dependency> dependencies = mQuestion.getDependencies();
            if (dependencies != null) {
                for (int i = 0; i < dependencies.size(); i++) {
                    Dependency d = dependencies.get(i);
                    if (d.getQuestion().equalsIgnoreCase(
                            event.getSource().getQuestion().getId())) {
                        if (handleDependencyParentResponse(d, event.getSource().getResponse(true))) {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * updates the state of this question view based on the value in the
     * dependency parent response. This method returns true if there is a value
     * match and false otherwise.
     *
     * @param dep
     * @param resp
     * @return
     */
    public boolean handleDependencyParentResponse(Dependency dep, QuestionResponse resp) {
        boolean isMatch = false;
        if (dep.getAnswer() != null
                && resp != null
                && dep.isMatch(resp.getValue())
                && resp.getIncludeFlag()) {
            isMatch = true;
        } else if (dep.getAnswer() != null
                && resp != null
                && resp.getIncludeFlag()) {
            if (resp.getValue() != null) {
                StringTokenizer strTok = new StringTokenizer(resp.getValue(),
                        "|");
                while (strTok.hasMoreTokens()) {
                    if (dep.isMatch(strTok.nextToken().trim())) {
                        isMatch = true;
                    }
                }
            }
        }

        boolean setVisible = false;
        // if we're here, then the question on which we depend
        // has been answered. Check the value to see if it's the
        // one we are looking for
        if (isMatch) {
            setVisibility(View.VISIBLE);
            if (mResponse != null) {
                mResponse.setIncludeFlag(true);
            }
            setVisible = true;
        } else {
            if (mResponse != null) {
                mResponse.setIncludeFlag(false);
            }
            setVisibility(View.GONE);
        }

        // now notify our own listeners to make sure we correctly toggle
        // nested dependencies (i.e. if A -> B -> C and C changes, A needs to
        // know too).
        notifyQuestionListeners(QuestionInteractionEvent.QUESTION_ANSWER_EVENT);

        return setVisible;
    }

    /**
     * this method should be overridden by subclasses so they can record input
     * in a QuestionResponse object
     */
    public void captureResponse() {
        // NO OP
    }

    /**
     * this method should be overridden by subclasses so they can record input
     * in a QuestionResponse object
     */
    public void captureResponse(boolean suppressListeners) {
        // NO OP
    }

    /**
     * this method should be overridden by subclasses so they can manage the UI
     * changes when resetting the value
     *
     * @param resp
     */
    public void rehydrate(QuestionResponse resp) {
        setResponse(resp, true);
    }

    /**
     * Release any heavy resource associated with this view. This method will
     * likely be overridden by subclasses. This callback should ALWAYS be called
     * when the Activity is about to become invisible (paused, stopped,...) and
     * this View's responses have been successfully cached. Any resource that
     * can cause a memory leak or prevent this View from being GC should be
     * freed/notified
     */
    public void releaseResources() {
    }

    public QuestionResponse getResponse(boolean suppressListeners) {
        if (mResponse == null
                || (ConstantUtil.VALUE_RESPONSE_TYPE.equals(mResponse.getType()) && (mResponse
                .getValue() == null || mResponse.getValue().trim()
                .length() == 0))) {
            captureResponse(suppressListeners);
        }
        return mResponse;
    }

    public QuestionResponse getResponse() {
        return getResponse(false);
    }

    public void setResponse(QuestionResponse response) {
        setResponse(response, false);
    }

    public void setResponse(QuestionResponse response, boolean suppressListeners) {
        if (response != null) {
            if (mQuestion != null) {
                response.setScoredValue(mQuestion.getResponseScore(response
                        .getValue()));
            }
            if (this.mResponse == null) {
                this.mResponse = response;
            } else {
                // we need to preserve the ID so we don't get duplicates in the
                // db
                this.mResponse.setType(response.getType());
                this.mResponse.setValue(response.getValue());
            }
        } else {
            this.mResponse = response;
        }
        if (!suppressListeners) {
            notifyQuestionListeners(QuestionInteractionEvent.QUESTION_ANSWER_EVENT);
        }
    }

    public Question getQuestion() {
        return mQuestion;
    }

    public void setTextSize(float size) {
        mQuestionText.setTextSize(size);
    }

    /**
     * hides or shows the tips button
     *
     * @param isSuppress
     */
    public void suppressHelp(boolean isSuppress) {
        mTipImage.setVisibility(isSuppress ? View.GONE : View.VISIBLE);
    }

    public String getDefaultLang() {
        return mDefaultLang;
    }

    public void setDefaultLang(String defaultLang) {
        mDefaultLang = defaultLang;
    }

    /**
     * turns highlighting on/off
     *
     * @param useHighlight
     */
    public void highlight(boolean useHighlight) {
        if (useHighlight) {
            mQuestionText.setBackgroundColor(0x55CC99CC);
        } else {
            mQuestionText.setBackgroundColor(Color.TRANSPARENT);
        }
    }

}

