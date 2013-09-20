package org.guideme.guideme.settings;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppSettings {
	private Logger logger = LogManager.getLogger();
	private int FontSize = 20;
	private int HtmlFontSize = 20;
	private boolean Debug = false;
	private String DataDirectory;
	private int[] sash1Weights = new int[2];
	private int[] sash2Weights = new int[2];
	private Properties appSettingsProperties = new Properties();
	private String settingsLocation = "settings.properties";
	private String userDir;
	private String userHome;
	private String userName;
	private String fileSeparator;
	private static AppSettings appSettings;

	public static synchronized AppSettings getAppSettings() {
		if (appSettings == null) {
			appSettings = new AppSettings();
		}
		return appSettings;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	private AppSettings() {
		super();
		Properties properties = java.lang.System.getProperties();
		userDir = String.valueOf(properties.get("user.dir"));
		userHome = String.valueOf(properties.get("user.home"));
		userName = String.valueOf(properties.get("user.name"));
		fileSeparator = String.valueOf(properties.get("file.separator"));
		try {
			try {
				appSettingsProperties.loadFromXML(new FileInputStream(settingsLocation));
			}
			catch (IOException ex) {
				//failed to load file so just carry on
				logger.error(ex.getLocalizedMessage(), ex);
			}
			FontSize = Integer.parseInt(appSettingsProperties.getProperty("FontSize", "20"));
			HtmlFontSize = Integer.parseInt(appSettingsProperties.getProperty("HtmlFontSize", "20"));
			Debug = Boolean.parseBoolean(appSettingsProperties.getProperty("Debug", "false"));
			DataDirectory = appSettingsProperties.getProperty("DataDirectory", userDir);
			sash1Weights[0] = Integer.parseInt(appSettingsProperties.getProperty("sash1Weights0", "350"));
			sash1Weights[1] = Integer.parseInt(appSettingsProperties.getProperty("sash1Weights1", "350"));
			sash2Weights[0] = Integer.parseInt(appSettingsProperties.getProperty("sash2Weights0", "800"));
			sash2Weights[1] = Integer.parseInt(appSettingsProperties.getProperty("sash2Weights1", "200"));
		}
		catch (Exception ex) {
			logger.error(ex.getLocalizedMessage(), ex);
		}
		saveSettings();
	}

	public int getFontSize() {
		return FontSize;
	}

	public void setFontSize(int fontSize) {
		FontSize = fontSize;
	}

	public boolean getDebug() {
		return Debug;
	}

	public void setDebug(boolean debug) {
		Debug = debug;
	}

	public String getDataDirectory() {
		return DataDirectory;
	}

	public void setDataDirectory(String dataDirectory) {
		DataDirectory = dataDirectory;
	}

	public int[] getSash1Weights() {
		return sash1Weights;
	}

	public void setSash1Weights(int[] sash1Weights) {
		this.sash1Weights = sash1Weights;
	}

	public int[] getSash2Weights() {
		return sash2Weights;
	}

	public void setSash2Weights(int[] sash2Weights) {
		this.sash2Weights = sash2Weights;
	}

	public void saveSettings() {
		try {
			appSettingsProperties.setProperty("FontSize", String.valueOf(FontSize));
			appSettingsProperties.setProperty("HtmlFontSize", String.valueOf(HtmlFontSize));
			appSettingsProperties.setProperty("Debug", String.valueOf(Debug));
			appSettingsProperties.setProperty("DataDirectory", DataDirectory);
			appSettingsProperties.setProperty("sash1Weights0", String.valueOf(sash1Weights[0]));
			appSettingsProperties.setProperty("sash1Weights1", String.valueOf(sash1Weights[1]));
			appSettingsProperties.setProperty("sash2Weights0", String.valueOf(sash2Weights[0]));
			appSettingsProperties.setProperty("sash2Weights1", String.valueOf(sash2Weights[1]));
			appSettingsProperties.storeToXML(new FileOutputStream(settingsLocation), null);
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	public int getHtmlFontSize() {
		return HtmlFontSize;
	}

	public void setHtmlFontSize(int htmlFontSize) {
		HtmlFontSize = htmlFontSize;
	}

	public String getUserDir() {
		return userDir;
	}

	public String getUserHome() {
		return userHome;
	}

	public String getUserName() {
		return userName;
	}

	public String getFileSeparator() {
		return fileSeparator;
	}

}
