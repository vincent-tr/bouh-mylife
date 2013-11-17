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

import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.RegistrationCommand;
import org.mylife.home.net.hub.irc.UnregisteredEntity;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class Pass implements RegistrationCommand {
	public final void invoke(RegisteredEntity src, String[] params) {
		Util.sendAlreadyRegisteredError(src);
	}

	public final void invoke(final UnregisteredEntity src, String[] params) {
		src.setPass(params);
	}

	public String getName() {
		return "PASS";
	}

	public int getMinimumParameterCount() {
		return 1;
	}
}
