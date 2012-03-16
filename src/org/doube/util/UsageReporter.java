package org.doube.util;

import ij.IJ;
import ij.Prefs;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Random;

import org.bonej.Help;

public class UsageReporter {
	public static final UsageReporter INSTANCE = new UsageReporter();

	private static final String ga = "http://www.google-analytics.com/__utm.gif?";
	private static final String utmwv = "utmwv=5.2.5&";
	private static final String utmhn = "utmhn=bonej.org&";
	private static final String utmcs = "utmcs=" + Charset.defaultCharset()
			+ "&";
	private static final String utmac = "utmac=UA-366405-8&";
	private static final String utmdt = "utmdt=bonej.org%20Usage%20Statistics&";
	private static final String utmt = "utmt=event&";
	private static final String utmul = "utmul=" + getLocaleString() + "&";
	private static final String utmje = "utmje=0&";
	private static final String utmfl = "utmfl=11.1%20r102&";
	private static final String utmr = "utmr=-&";
	private static final String utmp = "utmp=%2Fstats&";

	private static String bonejSession;
	private static String utme;
	private static String utmn;
	private static String utms;
	private static String utmsr;
	private static String utmvp;
	private static String utmsc;
	private static int session;
	private static String utmcc;
	private static String cookie;
	private static String cookie2;
	private static String firstTime;
	private static long lastTime = 0;
	private static long thisTime = 0;

	private static Random random;

	private static String utmhid;

	private UsageReporter() {
		random = new Random();
		bonejSession = Prefs.get(ReporterOptions.SESSIONKEY,
				Integer.toString(new Random().nextInt(1000)));
		int inc = Integer.parseInt(bonejSession);
		inc++;
		bonejSession = Integer.toString(inc);
		Prefs.set(ReporterOptions.SESSIONKEY, inc);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		GraphicsEnvironment ge;
		ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		int width = 0;
		int height = 0;
		if (!ge.isHeadlessInstance()) {
			GraphicsDevice[] screens = ge.getScreenDevices();
			for (int i = 0; i < screens.length; i++) {
				GraphicsConfiguration[] gc = screens[i].getConfigurations();
				for (GraphicsConfiguration g : gc) {
					width = Math.max(g.getBounds().x + g.getBounds().width,
							width);
					height = Math.max(g.getBounds().y + g.getBounds().height,
							height);
				}
			}
		}

		utmsr = "utmsr=" + screenSize.width + "x" + screenSize.height + "&";
		utmvp = "utmvp=" + width + "x" + height + "&";
		utmsc = "utmsc=24-bit&";
	}

	public static UsageReporter reportEvent(String category, String action,
			String label, Integer value) {
		utms = "utms=" + session + "&";
		session++;
		String val = (value == null) ? "" : "(" + value.toString() + ")";
		utme = "utme=5(" + category + "*" + action + "*" + label + ")" + val
				+ "&";
		utmn = "utmn=" + random.nextInt(Integer.MAX_VALUE) + "&";
		utmhid = "utmhid=" + random.nextInt(Integer.MAX_VALUE) + "&";

		final long time = System.currentTimeMillis() / 1000;
		lastTime = thisTime;
		if (lastTime == 0)
			lastTime = time;
		thisTime = time;
		utmcc = getCookieString();
		return INSTANCE;
	}

	public static UsageReporter reportEvent(Object o) {
		return reportEvent("Plugin Usage", o.getClass().getName(),
				Help.bonejVersion, null);
	}

	private static String getCookieString() {
		// seems to be a bug in Prefs.getInt, so are Strings wrapped in
		// Integer.toString()
		cookie = Prefs.get(ReporterOptions.COOKIE,
				Integer.toString(random.nextInt(Integer.MAX_VALUE)));
		cookie2 = Prefs.get(ReporterOptions.COOKIE2,
				Integer.toString(random.nextInt(Integer.MAX_VALUE)));
		firstTime = Prefs.get(ReporterOptions.FIRSTTIMEKEY,
				Integer.toString(random.nextInt(Integer.MAX_VALUE)));
		String cc = "utmcc=__utma%3D"
				+ cookie
				+ "."
				+ cookie2
				+ "."
				+ firstTime
				+ "."
				+ lastTime
				+ "."
				+ thisTime
				+ "."
				+ bonejSession
				+ "%3B%2B__utmz%3D"
				+ cookie
				+ "."
				+ thisTime
				+ ".79.42.utmcsr%3Dgoogle%7Cutmccn%3D(organic)%7Cutmcmd%3Dorganic%7Cutmctr%3Dbonej%3B";
		return cc;
	}

	public void send() {
		if (!isAllowed())
			return;
		try {
			URL url = new URL(ga + utmwv + utms + utmn + utmhn + utmt + utme
					+ utmcs + utmsr + utmvp + utmsc + utmul + utmje + utmfl
					+ utmdt + utmhid + utmr + utmp + utmac + utmcc);
			IJ.log(url.toString());
			URLConnection uc = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					uc.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				// IJ.log(inputLine);
				inputLine.length();
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getLocaleString() {
		String locale = Locale.getDefault().toString();
		locale = locale.replace("_", "-");
		locale = locale.toLowerCase();
		return locale;
	}

	private boolean isAllowed() {
		if (!Prefs.get(ReporterOptions.OPTOUTSET, false))
			new ReporterOptions().run("");
		return Prefs.get(ReporterOptions.OPTOUTKEY, true);
	}
}
