package edu.cmu.cs.cs214.hw5.plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.*;
import de.erichseifert.gral.plots.PiePlot.PieSliceRenderer;
import de.erichseifert.gral.plots.colors.LinearGradient;
import de.erichseifert.gral.ui.InteractivePanel;
import de.erichseifert.gral.util.Insets2D;

import edu.cmu.cs.cs214.hw5.framework.Framework;
import edu.cmu.cs.cs214.hw5.framework.TweetObject;
import edu.cmu.cs.cs214.hw5.framework.Tweetility;

public class HashTagCountPlugin implements Plugin {
	private final int MAX_KEYWORDS = 5;
	private final String title = "Pie chart of hashtag ratio";
	private Framework framework;
	private int myID;
	private List<String> keywords;
	private int numKeywords;
	private JPanel myPanel;

	private DataTable tweetCount;
	private DataTable reTweetCount;
	private DataTable totalCount;
	private int width;
	private int height;

/**
 * 
 *  Do analysis by counting how many hashtags each tweet associate with a keyword has
 *  Then turn the information into a pie chart.
 * 
 */
	@SuppressWarnings("unchecked")
	public HashTagCountPlugin() {
		keywords = new ArrayList<String>(MAX_KEYWORDS);
		tweetCount = new DataTable(Integer.class);
		reTweetCount = new DataTable(Integer.class);
		totalCount = new DataTable(Integer.class);
	}

	@Override
	public void registerFramework(Framework f, int pluginID) {
		this.framework = f;
		this.myID = pluginID;
	}

	@Override
	public String getPluginTitle() {
		return title;
	}

	/**
	 * Do parsing and use tweetility to count the amount of hashtags.
	 */
	@Override
	public void startAnalysis(Map<String, List<TweetObject>> data) {
		myPanel.removeAll();
		keywords.clear();
		this.reTweetCount.clear();
		this.tweetCount.clear();

		// Get new keywords.
		keywords.addAll(framework.getKeywords());
		numKeywords = keywords.size();
		Map<String, List<TweetObject>> tweetOnly;
		Map<String, List<TweetObject>> reTweetOnly;
		tweetOnly = Tweetility.getTweetsOnly(data);
		reTweetOnly = Tweetility.getRetweetsOnly(data);

		// do tweetOnly first
		for (int i = 0; i < numKeywords; i++) {
			List<TweetObject> person;
			String key = keywords.get(i);
			person = tweetOnly.get(key);
			int hashTagCount = 0;
			int total = 0;
			for (TweetObject tweet : person) {
				List<String> hashtag = tweet.getHashtags();
				hashTagCount += hashtag.size();
			}
			tweetCount.add(hashTagCount);
			total = hashTagCount;

			person = reTweetOnly.get(key);
			hashTagCount = 0;
			for (TweetObject tweet : person) {
				List<String> hashtag = tweet.getHashtags();
				hashTagCount += hashtag.size();
			}
			reTweetCount.add(hashTagCount);
			total += hashTagCount;

			totalCount.add(total);

		}
		
		LinearGradient colors = new LinearGradient(Color.RED, Color.BLUE);
		PiePlot tweetPie = makePie( tweetCount,  "Tweet Only", colors);
		PiePlot reTweetPie = makePie( reTweetCount,  "reTweet Only", colors);
		
		
		myPanel.add(new InteractivePanel(tweetPie));
		myPanel.add(new InteractivePanel(reTweetPie));

		
		// Communicate to framework that analysis is done
		// and JPanel content has been updated.
		framework.revalidatePluginPanel(myID);
		
		
	}

	@Override
	public void initializePluginPanel(JPanel content) {
		myPanel = content;
		myPanel.setLayout(new GridLayout(1,2));
		this.width = myPanel.getWidth();
		this.height = myPanel.getHeight();
		this.tweetCount.clear();
		this.reTweetCount.clear();
		this.totalCount.clear();
	}

/**
 *  helper method to create pie chart
 * @param sample data set to turn into pie chart
 * @param description of the chart
 * @param colors scheme
 * @return
 */
	private PiePlot makePie(DataTable sample, String description, LinearGradient colors) {
		PiePlot plot = new PiePlot(sample);
		
		plot.setSetting(PiePlot.TITLE, description);
		plot.setSetting(PiePlot.RADIUS, 0.9);
		plot.setSetting(PiePlot.LEGEND, false);
		
		plot.setInsets(new Insets2D.Double(10.0, 30.0, 30.0, 30.0));		
		plot.getPointRenderer(sample).setSetting(PieSliceRenderer.RADIUS_INNER,0.4);
		plot.getPointRenderer(sample).setSetting(PieSliceRenderer.GAP, 0.2);
		plot.getPointRenderer(sample).setSetting(PieSliceRenderer.COLOR, colors);
		plot.getPointRenderer(sample).setSetting(PieSliceRenderer.VALUE_DISPLAYED, true);
		plot.getPointRenderer(sample).setSetting(PieSliceRenderer.VALUE_COLOR,Color.WHITE);
		plot.getPointRenderer(sample).setSetting(PieSliceRenderer.VALUE_FONT,Font.decode(null).deriveFont(Font.BOLD));
		plot.getPointRenderer(sample).setSetting(PieSliceRenderer.VALUE_ALIGNMENT_Y, 5);
		return plot;
	}
}
