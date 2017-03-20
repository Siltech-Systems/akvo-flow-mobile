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

package org.akvo.flow.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.flow.R;
import org.akvo.flow.app.FlowApp;
import org.akvo.flow.domain.SurveyGroup;
import org.akvo.flow.util.PlatformUtil;

import java.util.ArrayList;
import java.util.List;

public class DrawerAdapter extends BaseExpandableListAdapter implements
         ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
    private List<SurveyGroup> mSurveys;

    LayoutInflater mInflater;

     @ColorInt
     private final int mHighlightColor;

     public DrawerAdapter(Context context) {
         mInflater = LayoutInflater.from(context);
         mSurveys = new ArrayList<>();
         mHighlightColor = ContextCompat.getColor(context, R.color.orange_main);
     }

     @Override
     public int getGroupCount() {
         return 2;
     }

     @Override
     public int getChildrenCount(int groupPosition) {
         switch (groupPosition) {
             case DrawerFragment.GROUP_SURVEYS:
                 return mSurveys.size();
             default:
                 return 0;
         }
     }

     @Override
     public Object getGroup(int groupPosition) {
         return null;
     }

     @Override
     public Object getChild(int groupPosition, int childPosition) {
         return null;
     }

     @Override
     public long getGroupId(int groupPosition) {
         return 0;
     }

     @Override
     public long getChildId(int groupPosition, int childPosition) {
         return 0;
     }

     @Override
     public boolean hasStableIds() {
         return false;
     }

     @Override
     public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
             ViewGroup parent) {
         View v = convertView;
         if (v == null) {
             v = mInflater.inflate(R.layout.drawer_item, null);
         }
         View divider = v.findViewById(R.id.divider);
         TextView tv = (TextView) v.findViewById(R.id.item_txt);
         ImageView img = (ImageView) v.findViewById(R.id.item_img);
         ImageView dropdown = (ImageView) v.findViewById(R.id.dropdown);

         switch (groupPosition) {
             case 0:
                 divider.setMinimumHeight((int) PlatformUtil
                         .dp2Pixel(parent.getContext(), 3));
                 tv.setTextSize(DrawerFragment.ITEM_TEXT_SIZE);
                 tv.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.black_disabled));
                 tv.setText(R.string.surveys);
                 img.setVisibility(View.GONE);
                 dropdown.setVisibility(View.GONE);
                 break;
             case 1:
                 divider.setMinimumHeight((int) PlatformUtil.dp2Pixel(parent.getContext(), 1));
                 tv.setTextSize(DrawerFragment.ITEM_TEXT_SIZE);
                 tv.setTextColor(Color.BLACK);
                 tv.setText(R.string.settingslabel);
                 img.setVisibility(View.GONE);
                 dropdown.setVisibility(View.GONE);
                 break;
         }

         return v;
     }

     @Override
     public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
             View convertView, ViewGroup parent) {
         View v = convertView;
         if (v == null) {
             v = mInflater.inflate(android.R.layout.simple_list_item_1, null);
         }
         TextView tv = (TextView) v.findViewById(android.R.id.text1);
         v.setPadding((int) PlatformUtil.dp2Pixel(parent.getContext(), 30), 0, 0, 0);

         tv.setTextSize(DrawerFragment.ITEM_TEXT_SIZE);
         tv.setTextColor(Color.BLACK);
         v.setBackgroundColor(Color.TRANSPARENT);

         switch (groupPosition) {
             case DrawerFragment.GROUP_SURVEYS:
                 SurveyGroup sg = mSurveys.get(childPosition);
                 tv.setText(sg.getName());
                 if (sg.getId() == FlowApp.getApp().getSurveyGroupId()) {
                     tv.setTextColor(mHighlightColor);
                     v.setBackgroundColor(ContextCompat.getColor(parent.getContext(), R.color.background_alternate));
                 }
                 v.setTag(sg);
                 break;
         }

         return v;
     }

     @Override
     public boolean isChildSelectable(int groupPosition, int childPosition) {
         return groupPosition == DrawerFragment.GROUP_SURVEYS;
     }

     @Override
     public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
         switch (groupPosition) {
             case DrawerFragment.GROUP_SURVEYS:
                 return true; // This way the expander cannot be collapsed
             case DrawerFragment.GROUP_SETTINGS:
//                 drawerFragment
//                         .startActivity(new Intent(drawerFragment.getActivity(), SettingsActivity.class));
                 return true;
             default:
                 return false;
         }
     }

     @Override
     public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
             int childPosition, long id) {
         switch (groupPosition) {
             case DrawerFragment.GROUP_SURVEYS:
                 SurveyGroup sg = (SurveyGroup) v.getTag();
//                 drawerFragment.mListener.onSurveySelected(sg);
                 return true;
         }
         return false;
     }
 }
