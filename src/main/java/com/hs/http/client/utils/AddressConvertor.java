package com.hs.http.client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class AddressConvertor {	
	public static List<String> getLocalIPList() throws IOException {
		List<String> ipList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			NetworkInterface networkInterface = null;
			Enumeration<InetAddress> inetAddresses = null;
			InetAddress inetAddress = null;
			while (networkInterfaces.hasMoreElements()) {
				networkInterface = networkInterfaces.nextElement();
				inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					inetAddress = inetAddresses.nextElement();
					if (inetAddress != null && inetAddress instanceof Inet4Address) {
						if (!inetAddress.isLoopbackAddress() 
			                     && inetAddress.getHostAddress().indexOf(":") == -1) {
							if (ping(inetAddress.getHostAddress())) {
								ipList.add(inetAddress.getHostAddress());
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			return Collections.emptyList();
		}
		
		return ipList;
	}
	
	private static Boolean ping(String host) throws IOException {
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			process = runtime.exec("ping " + host);
			is = process.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
 
			String line;
			int counter = 0;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				if (counter > 1) {
					break;
				}
				
				sb.append(line);
				counter ++;
			}

			if (sb.toString().indexOf("timeout") != -1) {
				return Boolean.FALSE;
			}
			
			return Boolean.TRUE;
		} catch (Exception e) {
			return Boolean.FALSE;
		} finally {
			if (is != null) {
				is.close();
			}
			
			if (isr != null) {
				isr.close();
			}
			
			if (br != null) {
				br.close();
			}
		}
	}
}
