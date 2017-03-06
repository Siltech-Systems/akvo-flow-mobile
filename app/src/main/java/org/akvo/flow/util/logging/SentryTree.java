/*
 * Copyright (C) 2010-2016 Stichting Akvo (Akvo Foundation)
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

package org.akvo.flow.util.logging;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.joshdholtz.sentry.Sentry;

import timber.log.Timber;

class SentryTree extends Timber.Tree {

    @Override
    protected void log(int priority, @Nullable String tag, @Nullable String message,
            @Nullable Throwable t) {

        if (!shouldSendLog(priority) || isThrowableExcluded(t)) {
            return;
        }

        if (t != null) {
            //We will only send stacktraces for now
            Sentry.captureException(t);
        }
    }

    /**
     * Some exceptions are not useful to be sent to sentry, this method will filter them out
     * @param t
     * @return
     */
    private boolean isThrowableExcluded(Throwable t) {
        return t instanceof java.net.ConnectException
                || t instanceof javax.net.ssl.SSLHandshakeException
                || t instanceof java.security.cert.CertificateNotYetValidException
                || t instanceof javax.net.ssl.SSLProtocolException
                || t instanceof java.net.SocketTimeoutException
                || containsFilteredMessage(t);
    }

    private boolean containsFilteredMessage(Throwable t) {
        return !TextUtils.isEmpty(t.getMessage()) && t.getMessage()
                .contains("Connection timed out");
    }

    /**
     * Configure which level should be sent
     * @param priority
     * @return
     */
    private boolean shouldSendLog(int priority) {
        return priority == Log.ERROR || priority == Log.ASSERT;
    }
}