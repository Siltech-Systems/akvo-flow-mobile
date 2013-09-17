/*
 *  Copyright (C) 2013 Stichting Akvo (Akvo Foundation)
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

package com.gallatinsystems.survey.device.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.util.Log;

/**
 * OpenStack/Swift uploader. This version uses HTTP Basic Authentication <br>
 * TODO:
 * <ul>
 * <li>Add MIME type to objects</li>
 * <li>Discuss container security options (public/private)</li>
 * </ul>
 */
public class Swift {

    /**
     * Interface that can be used to be notified of upload progress
     */
    public interface UploadListener {
        public void uploadProgress(long bytesUploaded, long totalBytes);
    }

    private static final String TAG = Swift.class.getSimpleName();
    private static final int BUFFER_SIZE = 8192;

    private String mApiUrl;
    private String mUsername;
    private String mPassword;

    public Swift(String apiUrl, String username, String password) {
        mApiUrl = apiUrl;
        mUsername = username;
        mPassword = password;
    }

    public boolean uploadFile(String container, String name, File file,
            UploadListener listener){
        Log.i(TAG, "uploading file: " + name);
        try {
            return put(container, name, file, listener);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    private boolean put(String container, String name, File file, UploadListener listener)
            throws IOException {
        InputStream in = null;
        OutputStream out = null;
        HttpURLConnection conn = null;
        boolean ok = false;

        try {
            URL url = new URL(mApiUrl + "/" + container + "/" + name);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty(Header.AUTH, getAuthHeader());
            conn.setRequestProperty(Header.ETAG, FileUtil.getMD5Checksum(file));

            in = new BufferedInputStream(new FileInputStream(file));
            out = new BufferedOutputStream(conn.getOutputStream());

            final long totalBytes = file.length();
            long bytesWritten = 0;

            byte[] b = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
                bytesWritten += read;
                if (listener != null) {
                    listener.uploadProgress(bytesWritten, totalBytes);
                }
            }
            out.flush();

            int status = 0;
            try {
                status = conn.getResponseCode();
            } catch (IOException e) {
                // HttpUrlConnection will throw an IOException if any 4XX
                // response is sent. If we request the status again, this
                // time the internal status will be properly set, and we'll be
                // able to retrieve it.
                status = conn.getResponseCode();
            }
            ok = (status == HttpStatus.SC_CREATED);
            if (!ok) {
                Log.e(TAG, "Status Code: " + status + ". Expected: 201 - Created");
            }

            return ok;
        } finally {
            if (conn != null)
                conn.disconnect();
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {}
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception ignored) {}
            }
        }
    }
	
	private String getAuthHeader() {
		final String userPassword = mUsername + ":" + mPassword;
        final String auth = Base64.encodeBytes(userPassword.getBytes());
		return "Basic " + auth;
	}

    interface Header {
        static final String AUTH = "Authorization";
        static final String ETAG = "ETag";
    }
}
