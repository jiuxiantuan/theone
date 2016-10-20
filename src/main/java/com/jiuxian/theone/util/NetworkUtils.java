/**
 * Copyright 2015-2020 jiuxian.com.
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
package com.jiuxian.theone.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.google.common.base.Throwables;

public final class NetworkUtils {

	public static String getLocalIp() {
		try {
			InetAddress localAddress = InetAddress.getLocalHost();
			if (localAddress != null && !localAddress.isAnyLocalAddress() && !localAddress.isLoopbackAddress()) {
				return localAddress.getHostAddress();
			}
		} catch (Exception e1) {
			throw Throwables.propagate(e1);
		}
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface current = interfaces.nextElement();
				if (!current.isUp() || current.isLoopback() || current.isVirtual())
					continue;
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr.isLoopbackAddress())
						continue;
					if (addr instanceof Inet4Address) {
						return addr.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			throw Throwables.propagate(e);
		}
		throw new RuntimeException("Cannot get local ip.");
	}

	public static void main(String[] args) {
		System.out.println(getLocalIp());
	}

}
