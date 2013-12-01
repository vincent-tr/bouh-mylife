/*
 * jIRCd - Java Internet Relay Chat Daemon
 * Copyright 2003 Tyrel L. Haveman <tyrel@haveman.net>
 *
 * This file is part of jIRCd.
 *
 * jIRCd is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * jIRCd is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with jIRCd; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.mylife.home.net.hub.irc.commands;

import java.util.logging.Logger;

import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.RegistrationCommand;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.UnregisteredEntity;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class ServerCommand implements RegistrationCommand {
    private static final Logger logger = Logger.getLogger(Error.class.getName());
    protected final IrcServerMBean jIRCd;
    
    public ServerCommand(IrcServerMBean jircd) {
        this.jIRCd = jircd;
    }
    public void invoke(RegisteredEntity src, String[] params) {
        String name = params[0];
        int hopcount = Integer.parseInt(params[1]);
        int token = Integer.parseInt(params[2]);
        String desc = params[3];
        /*Server server = */new Server(name, hopcount, token, desc, (Server) src);
    }
    public final void invoke(final UnregisteredEntity src, String[] params) {
        Connection.Handler handler = src.getHandler();
        final Connection connection = handler.getConnection();
        if(checkPass(connection, src.getPass())) {
            login(src, params);
        } else {
            logger.warning("Invalid password");
            src.disconnect("Invalid password");
        }
    }
    private boolean checkPass(Connection connection, String[] passParams) {
    	/*
        IrcLinkAccept configLink = jIRCd.findLinkAccept(connection.getRemoteAddress(), connection.getLocalPort());
        String expectedPassword = configLink.getPassword();
        if(expectedPassword != null) {
            String password = (passParams != null && passParams.length > 0) ? passParams[0] : null;
            return expectedPassword.equals(password);
        } else {
            return false;
        }*/
    	return true;
    }
    protected void login(final UnregisteredEntity src, String[] params) {
        Connection.Handler handler = src.getHandler();
        //final Connection connection = handler.getConnection();
        String name = params[0];
        int hopcount = Integer.parseInt(params[1]);
        if(hopcount != 1)
            Util.sendError(src, "The hop count must be 1 for a peer server: "+hopcount);
        int token = Integer.parseInt(params[2]);
        String desc = params[3];
        src.setName(name);
        Server server = new Server(src, token, desc);
        handler.login(server);
        /*
        IrcLinkConnect configLink = jIRCd.findLinkConnect(connection.getRemoteAddress(), connection.getRemotePort());
        String linkPassword = configLink.getPassword();
        */
        if(src.getParameters() == null) {
            //Util.sendPass(server, linkPassword);
            Util.sendServer(server);
        }
        Util.sendNetSync(server);
    }
    public String getName() {
        return "SERVER";
    }
    public int getMinimumParameterCount() {
        return 4;
    }
}
