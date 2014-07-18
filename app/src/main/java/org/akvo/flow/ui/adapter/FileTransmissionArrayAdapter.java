/*
 *  Copyright (C) 2010-2012 Stichting Akvo (Akvo Foundation)
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

package org.akvo.flow.ui.adapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.akvo.flow.R;
import org.akvo.flow.domain.FileTransmission;
import org.akvo.flow.dao.SurveyDbAdapter.TransmissionStatus;

/**
 * Adapter that converts FileTransmission objects for display in a list view.
 * 
 * @author Christopher Fagiani
 */
public class FileTransmissionArrayAdapter extends ArrayAdapter<FileTransmission> {
    private DateFormat dateFormat;
    private int layoutId;

    public FileTransmissionArrayAdapter(Context context, int resourceId,
            List<FileTransmission> objects) {
        super(context, resourceId, objects);
        layoutId = resourceId;
        // TODO:  US-style  date should  not be  hardcoded...
        dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    }

    private void bindView(View view, FileTransmission trans) {
        ImageView imageView = (ImageView) view.findViewById(R.id.statusicon);

        String statusText = "Unknown";//TODO: Hey buddy, you can do this way better...
        switch (trans.getStatus()) {
            case TransmissionStatus.QUEUED:
                statusText = "Queued";
                imageView.setImageResource(R.drawable.yellowcircle);
                break;
            case TransmissionStatus.IN_PROGRESS:
                statusText = "In Progress";
                imageView.setImageResource(R.drawable.blueuparrow);
                break;
            case TransmissionStatus.SYNCED:
                statusText = "Synced";
                imageView.setImageResource(R.drawable.greencircle);
                break;
            case TransmissionStatus.FAILED:
                statusText = "Failed";
                imageView.setImageResource(R.drawable.redcircle);
                break;
        }

        ((TextView)view.findViewById(R.id.statustext)).setText(statusText);
        TextView startDate = (TextView) view.findViewById(R.id.startdate);
        if (trans.getStartDate() != null) {
            startDate.setText(dateFormat.format(trans.getStartDate()));
        }
        TextView endDate = (TextView) view.findViewById(R.id.enddate);
        if (trans.getEndDate() != null) {
            endDate.setText(dateFormat.format(trans.getEndDate()));
        }

        TextView fileName = (TextView) view.findViewById(R.id.filename);
        fileName.setText(trans.getFileName());
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Context ctx = getContext();
        LayoutInflater inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layoutId, null);
        bindView(view, getItem(position));
        return view;
    }

}