package com.lbi.swansonb.games.xo.server;

import javax.websocket.server.ServerEndpointConfig;

public class XOServerConfigurator extends ServerEndpointConfig.Configurator {

    public static final XOServer xoServer = new XOServer();

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        if (endpointClass.equals(XOServer.class)) {
            return (T) xoServer;
        }

        throw new InstantiationException();
    }
}
