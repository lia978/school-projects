package edu.cmu.cs.cs214.hw5.framework;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.cs214.hw5.plugin.Plugin;

public interface Framework {
	
	/**
	 * Allows the framework and plugin to communicate.
	 * @param p : The plugin to be registered.
	 */
	public void registerPlugin(Plugin p);
	
	/**
	 * Gets the set of keywords, which can be specified by the user
	 * (or as default chosen by the framework),that the framework 
	 * is currently querying for on Twitter.
	 * @return The current set of keywords.
	 */
	public Set<String> getKeywords();
	
	/**
	 * Allows plugins to access the entire current dataset.
	 * Made redundant by startAnalysis method in Plugin interface,
	 * which supplies this dataset as an argument.
	 * @return A mapping of keyword to list of TweetObjects.
	 */
	public Map<String, List<TweetObject>> getAll();
	
	/**
	 * Alerts the framework when a plugin has updated
	 * its visual content. Refreshes the affected JPanel.
	 * @param pluginID : The plugin's identifier.
	 */
	public void revalidatePluginPanel(int pluginID);
	
}
