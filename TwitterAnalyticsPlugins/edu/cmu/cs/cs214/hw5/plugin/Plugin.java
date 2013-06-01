package edu.cmu.cs.cs214.hw5.plugin;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import edu.cmu.cs.cs214.hw5.framework.Framework;
import edu.cmu.cs.cs214.hw5.framework.TweetObject;

public interface Plugin {

	/**
	 * Establishes a line of communication between the plugin
	 * and the framework.
	 * @param f : The framework to be registered.
	 */
	public void registerFramework(Framework f, int pluginID);
	
	/**
	 * Get the title of the plugin when it registers with
	 * the framework
	 * @return pluginTitle : String
	 */
	public String getPluginTitle();
	
	/**
	 * Alerts the plugin that the dataset has been updated.
	 * Triggers a new round of analysis.
	 * @param data : the new updated data from the framework
	 */
	public void startAnalysis(Map<String,List<TweetObject>> data);
	
	/**
	 * Gives the plugin direct access to its own JPanel, which it
	 * can customize.
	 * @param content : The plugin's JPanel, allotted by the framework.
	 */
	public void initializePluginPanel(JPanel content);
}
