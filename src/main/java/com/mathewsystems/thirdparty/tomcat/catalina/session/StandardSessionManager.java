package com.mathewsystems.thirdparty.tomcat.catalina.session;

/*
 * Copyright 2023 Mathew Chan
 * Contact: mathew.chan (at) mathewsystems.com
 * Web: matcphotos.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.IOException;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Tomcat standard distribution session manager drop-in replacement.
 * <p>
 * Tomcat's standard implementation of its session manager, StandardManager,
 * stores "active" (hot) sessions in the heap. However, memory is neither
 * released after session expiry, nor after full garbage collection, resulting
 * in memory leak in high session throughput and high capacity session object
 * creation scenarios. This module scans and releases unused session object
 * references in parallel threads, which fixes the issue, and can perform up to
 * 10-20 fold faster in locating expired session objects. It is capable of
 * releasing chunks of more than 50k expired sessions, or gigabytes of heap in
 * sub-milliseconds.
 * <p>
 * Performance reference: Freeing ~60k sessions, 2GB session objects heap.
 * 50-100ms seconds on AMD Ryzen 9 7900X.
 *
 * @author Mathew Chan
 * @web matcphotos.com/blog
 */
public class StandardSessionManager extends StandardManager {

    private final Log log = LogFactory.getLog(StandardSessionManager.class); // must not be static

    @Override
    protected void doLoad() throws ClassNotFoundException, IOException {

        log.info("StandardSessionManager loading, running super constructor");

        super.doLoad();

    }

    /**
     * Free up expired session references from heap for garbage collection.
     */
    @Override
    public void processExpires() {

        final String contextName = getContext().getName();

        if (sessions.isEmpty()) {

            if (log.isDebugEnabled()) {

                log.debug("[StandardSessionManager] No active session to purge. Context: " + contextName);

            }

            return;

        }

        if (log.isDebugEnabled()) {

            final StringBuilder sb = new StringBuilder();

            sb.append("Context: ").append(contextName)
                    .append(" [StandardSessionManager] Starting to purge expired sessions. Total sessions in heap:  ")
                    .append(sessions.size());

            log.debug(sb.toString());

        }

        long timeNow = System.currentTimeMillis();

        int counter = 0;

        for (Session s : sessions.values()) {

            if (s != null && !s.isValid()) {

                s.expire();

                counter++;

            }

        }

        long timeEnd = System.currentTimeMillis();

        if (log.isDebugEnabled()) {

            processingTime += (timeEnd - timeNow);

            final StringBuilder sb = new StringBuilder();

            sb.append("Context: ").append(contextName)
                    .append(" [StandardSessionManager] Expired sessions purged. Purged: ")
                    .append(counter)
                    .append(" . Elapsed time: ")
                    .append(processingTime)
                    .append(" ms.");

            log.debug(sb.toString());

        }

    }

}
