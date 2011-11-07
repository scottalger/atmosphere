/*
 * Copyright 2011 Jeanfrancois Arcand
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.atmosphere.samples.pubsub;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.HeaderConfig;
import org.atmosphere.cpr.Meteor;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.atmosphere.websocket.WebSocketProtocol;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple PubSub resource that demonstrate many functionality supported by
 * Atmosphere JQuery Plugin and WebSocketProtocol extension.  You can compare that implementation
 * with the MeteorPubSub, AtmosphereHandlerPubSub and the JQueryPubsub sample
 * <p/>
 * This sample support out of the box WebSocket ONLY
 *
 * @author Jeanfrancois Arcand
 */
public class WebSocketPubSub implements WebSocketProtocol {

    private AtmosphereResource<HttpServletRequest, HttpServletResponse> r;

    @Override
    public void configure(AtmosphereServlet.AtmosphereConfig config) {
    }

    @Override
    public HttpServletRequest onMessage(WebSocket webSocket, String message) {
        Broadcaster b = lookupBroadcaster(r.getRequest().getPathInfo());

        if (message != null && message.indexOf("message") != -1) {
            b.broadcast(message.substring("message=".length()));
        }

        //Do not dispatch to another Container
        return null;
    }

    @Override
    public HttpServletRequest onMessage(WebSocket webSocket, byte[] data, int offset, int length) {
        //Do not dispatch to another Container
        return null;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        // Accept the handshake by suspending the response.
        r = (AtmosphereResource<HttpServletRequest, HttpServletResponse>) webSocket.atmosphereResource();
        Broadcaster b = lookupBroadcaster(r.getRequest().getPathInfo());
        r.setBroadcaster(b);
        r.addEventListener(new WebSocketEventListenerAdapter());

        r.suspend(-1);
    }

    @Override
    public void onClose(WebSocket webSocket) {
        // Tell Atmosphere to
        webSocket.atmosphereResource().resume();
    }

    /**
     * Retrieve the {@link Broadcaster} based on the request's path info.
     *
     * @param pathInfo
     * @return the {@link Broadcaster} based on the request's path info.
     */
    Broadcaster lookupBroadcaster(String pathInfo) {
        String[] decodedPath = pathInfo.split("/");
        Broadcaster b = BroadcasterFactory.getDefault().lookup(decodedPath[decodedPath.length - 1], true);
        return b;
    }

}