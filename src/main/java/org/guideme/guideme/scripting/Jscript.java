package org.guideme.guideme.scripting;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.guideme.guideme.model.Guide;
import org.guideme.guideme.settings.AppSettings;
import org.guideme.guideme.settings.ComonFunctions;
import org.guideme.guideme.settings.GuideSettings;
import org.guideme.guideme.settings.UserSettings;
import org.guideme.guideme.ui.MainShell;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.debugger.Main;

public class Jscript  implements Runnable
{
	private static Guide guide;
	private static GuideSettings guideSettings;
	private static UserSettings userSettings;
	private static AppSettings appSettings;
	private static OverRide overRide;
	private static Logger logger = LogManager.getLogger();
	private static final Marker JSCRIPT_MARKER = MarkerManager.getMarker("JSCRIPT");
	private static Boolean inPrefGuide;
	private String javaScriptText;
	private String javaFunction;
	private boolean pageloading;
	private boolean running;
	
	public Jscript(Guide Iguide, UserSettings IuserSettings, AppSettings IappSettings, Boolean IinPrefGuide, MainShell ImainShell, OverRide IoverRide, String ijavaScriptText, String ijavaFunction, boolean ipageloading) 
	{
		super();
		guide = Iguide;
		guideSettings = Iguide.getSettings();
		userSettings = IuserSettings;
		appSettings = IappSettings;
		inPrefGuide = IinPrefGuide;
		guide.setMainshell(ImainShell);
		overRide = IoverRide;
		javaScriptText = ijavaScriptText;
		javaFunction = ijavaFunction;
		pageloading = ipageloading;
		running = true;
	}


	public void changePreSettings(GuideSettings IguideSettings) {
		guideSettings = IguideSettings;
	}

	public static void jscriptLog(String strMessage) {
		logger.info(JSCRIPT_MARKER, strMessage);
		guide.updateJConsole(strMessage);
	}

	public void run() {
		try {
			String javaScriptToRun = javaScriptText + guide.getGlobaljScript();
			logger.info(JSCRIPT_MARKER, "Chapter: " + guideSettings.getChapter());
			logger.info(JSCRIPT_MARKER, "Page: " + guideSettings.getCurrPage());
			logger.info(JSCRIPT_MARKER, "javaFunction: " + javaFunction);
			logger.info(JSCRIPT_MARKER, "pageloading: " + pageloading);
			logger.info(JSCRIPT_MARKER, "javaScriptText: " + javaScriptText);
			if (!guideSettings.isGlobalScriptLogged()) {
				logger.info(JSCRIPT_MARKER, "globalJavaScriptText: " + guide.getGlobaljScript());
				guideSettings.setGlobalScriptLogged(true);
			}
			ComonFunctions comonFunctions = ComonFunctions.getComonFunctions();
			HashMap<String, Object> scriptVars;
			scriptVars = guideSettings.getScriptVariables();
			ContextFactory cntxFact = new ContextFactory();
			Main dbg = null;
			if (appSettings.getJsDebug())
			{
				dbg = new Main("GuideMe");
				dbg.attachTo(cntxFact);
			}
			
			Context cntx = cntxFact.enterContext();
			cntx.getWrapFactory().setJavaPrimitiveWrap(false);
			Scriptable scope = cntx.initStandardObjects();
			if (! inPrefGuide) {
				UserSettings cloneUS = userSettings.clone();
				ScriptableObject.putProperty(scope, "userSettings", cloneUS);
			} else {
				ScriptableObject.putProperty(scope, "userSettings", userSettings);
			}
			@SuppressWarnings("rawtypes")
			Class[] cArg = new Class[1];
			cArg[0] = String.class;
			java.lang.reflect.Method tjlog = Jscript.class.getMethod("jscriptLog", cArg);
			FunctionObject jlog = new FunctionObject("jscriptLog", tjlog, scope);
			//Deprecated should use guide now
			ScriptableObject.putProperty(scope, "guideSettings", guideSettings);
			//Deprecated should use guide now
			ScriptableObject.putProperty(scope, "comonFunctions", comonFunctions);
			ScriptableObject.putProperty(scope, "scriptVars", scriptVars);
			ScriptableObject.putProperty(scope, "guide", guide);
			ScriptableObject.putProperty(scope, "mediaDir", appSettings.getDataDirectory());
			ScriptableObject.putProperty(scope, "fileSeparator", java.lang.System.getProperty("file.separator"));
			ScriptableObject.putProperty(scope, "jscriptLog", jlog);
			logger.info(JSCRIPT_MARKER, "Starting ScriptVariables: " + scriptVars);
			logger.info(JSCRIPT_MARKER, "Starting Flags {" + guideSettings.getFlags() + "}");
			
			if (pageloading) {
				ScriptableObject.putProperty(scope, "overRide", overRide);
			}
			
			try {

				if (appSettings.getJsDebug())
				{
				    dbg.setBreakOnExceptions(appSettings.getJsDebugError());
				    dbg.setBreakOnEnter(appSettings.getJsDebugEnter());
				    dbg.setBreakOnReturn(appSettings.getJsDebugExit());
				    dbg.setScope(scope);
				    dbg.setSize(appSettings.getJsDebugWidth(), appSettings.getJsDebugHeight());
				    dbg.setVisible(true);
					JFrame jfWindow = dbg.getDebugFrame();
					if (appSettings.getMainMonitor() == 1)
					{
						showOnScreen(1, jfWindow);
					}
					else
					{
						showOnScreen(0, jfWindow);
					}
				    dbg.setExitAction(new DontExitOnClose());
				}
			    
			    cntx.evaluateString(scope, javaScriptToRun, "script", 1, null);
			    
				int argStart;
				int argEnd;
				String argstring = "";
				String[] argArray;
				argStart = javaFunction.indexOf("(");
				argEnd = javaFunction.indexOf(")");
				if (argStart > -1) {
					argstring = javaFunction.substring(argStart + 1, argEnd);
					javaFunction = javaFunction.substring(0, argStart);
				}
				Object fObj = scope.get(javaFunction, scope);
				if ((fObj instanceof Function)) {
					Object args[] = { "" };
					if (argstring.length() > 0) {
						argArray = argstring.split(",");
						args = argArray;
					}
					Function fct = (Function)fObj;
					fct.call(cntx, scope, scope, args);
				} else {
					logger.error(JSCRIPT_MARKER, " Couldn't find function " + javaFunction);
					guide.updateJConsole("Couldn't find function " + javaFunction);
				}
			}
			catch (EvaluatorException ex) {
				logger.error(JSCRIPT_MARKER, "JavaScriptError line " + ex.lineNumber() + " column " + ex.columnNumber() + " Source " + ex.lineSource() + " error " + ex.getLocalizedMessage());
				guide.updateJConsole("JavaScriptError line " + ex.lineNumber() + " column " + ex.columnNumber() + " Source " + ex.lineSource() + " error " + ex.getLocalizedMessage());
			}
			catch (Exception ex) {
				logger.error(JSCRIPT_MARKER, " FileRunScript " + ex.getLocalizedMessage(), ex);
				guide.updateJConsole("FileRunScript " + ex.getLocalizedMessage());
				logger.error(" FileRunScript " + ex.getLocalizedMessage(), ex);
			}
			logger.info(JSCRIPT_MARKER, "Ending ScriptVariables: " + scriptVars);
			logger.info(JSCRIPT_MARKER, "Ending Flags {" + guideSettings.getFlags() + "}");
			Context.exit();

			if (appSettings.getJsDebug())
			{
			    dbg.detach();
			    dbg.dispose();
			}		    

			guideSettings.setFlags(comonFunctions.GetFlags(guide.getFlags()));
			guideSettings.saveSettings();
			if (inPrefGuide) {
				userSettings.saveUserSettings();
			}
			cArg = null;
			jlog= null;
		}
		catch (Exception ex) {
			logger.error(" FileRunScript " + ex.getLocalizedMessage(), ex);
		}
    	running = false;		
	}

	public boolean isRunning() {
		return running;
	}
	
	public void setOverRide(OverRide overRide) {
		Jscript.overRide = overRide;
	}
	private static class DontExitOnClose implements Runnable {
	@Override
		public void run() {
			//System.exit(0);
		}
	}	

	public static void showOnScreen( int screen, JFrame frame ) {
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    GraphicsDevice[] gd = ge.getScreenDevices();
	    if( screen > -1 && screen < gd.length ) {
	        frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x, frame.getY());
	    } else if( gd.length > 0 ) {
	        frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x, frame.getY());
	    } else {
	        throw new RuntimeException( "No Screens Found" );
	    }
	}
}

