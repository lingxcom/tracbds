package com.tracbds.server;

import com.tracbds.server.swing.TracbdsFrame;

public class AppUI {
	public static boolean isAppUI=false;
	public static void main(String[] args) {
		AppUI.isAppUI=true;
		TracbdsFrame.configFile="spring.xml";
		TracbdsFrame.main(args);
	}

}
