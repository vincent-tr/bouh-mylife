package org.mylife.home.common;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

public final class NetUtils {
	
	/**
	 * Pas d'instance
	 */
	private NetUtils() {
	}

	public static InetAddress getPublicAddress() throws IOException {
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface netint : Collections.list(nets)) {
        	if(netint.isLoopback())
        		continue;
        	
        	if(netint.isVirtual())
        		continue;
        	
        	Enumeration<InetAddress> addresses = netint.getInetAddresses();
        	for(InetAddress address : Collections.list(addresses)) {
        		if(address instanceof Inet4Address)
        			return address;
        	}
        }
        
        return null;
	}
}
