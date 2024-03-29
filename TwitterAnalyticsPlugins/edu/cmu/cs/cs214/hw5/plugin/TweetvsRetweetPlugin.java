package edu.cmu.cs.cs214.hw5.plugin;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import edu.cmu.cs.cs214.hw5.framework.Framework;
import edu.cmu.cs.cs214.hw5.framework.TweetObject;
import edu.cmu.cs.cs214.hw5.framework.Tweetility;

/**
 * Creates and displays a bar graph showing the relative popularity
 * of each keyword at certain times of day.
 * @author 
 *
 */
public class TweetvsRetweetPlugin implements Plugin {
	
	private final int MAX_KEYWORDS = 5;
	private final String title = "Bar Graph of Number of Tweets and Retweets by Keyword";
	private Framework framework;
	private int myID;
	private List<String> keywords;
	private JPanel myPanel;
	
	private DefaultCategoryDataset chartData;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	
	private int width, height;
	

	public TweetvsRetweetPlugin() {
		keywords = new ArrayList<String>(MAX_KEYWORDS);
		chartData = new DefaultCategoryDataset();
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
	 * Cleans up data from last analysis, and re-populates internal data structures
	 * with new information from the framework. Sets up a Stacked Bar Graph (courtesy
	 * of JFreeChart - a 3rd party library) using quantities extracted from this information.
	 * Calls framework to refresh the newly updated JPanel.
	 * @param data
	 */
	@Override
	public void startAnalysis(Map<String, List<TweetObject>> data) {
		// Clear data from previous analysis.
		myPanel.removeAll();
		chartData.clear();
		keywords.clear();
		
		// Get new keywords.
		keywords.addAll(framework.getKeywords());
		int numKeywords = keywords.size();
		
		Map <String, List <TweetObject>> retweet = Tweetility.getRetweetsOnly(data);
		Map <String, List <TweetObject>> tweet = Tweetility.getTweetsOnly(data);
		
		for (int i = 0; i< keywords.size(); i++){
			String key = keywords.get(i);
			int retweetSize = retweet.get(key).size();
			int tweetSize = tweet.get(key).size();
			int total = data.get(key).size();
			
			chartData.addValue(retweetSize, "retweets", key);
			chartData.addValue(tweetSize, "tweets", key);
		    
		}
		
		// Create the bar graph, and add it to my JPanel.
		chart = makeChart(chartData);
		chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(width-10, height-10));
		chartPanel.setMinimumSize(new Dimension(width-10, height-10));
		chartPanel.setBackground(Color.WHITE);
		myPanel.add(chartPanel);
		
		// Communicate to framework that analysis is done
		// and JPanel content has been updated.
		framework.revalidatePluginPanel(myID);
	}

	@Override
	public void initializePluginPanel(JPanel content) {
		myPanel = content;
		this.width = myPanel.getWidth();
		this.height = myPanel.getHeight();
	}
	
	/**
	 * Uses methods from JFreeChart library to create and render a stacked bar graph.
	 * Some parts of each bar segment have been customized, like the fill color and 
	 * the presence of individual labels.
	 * @param dataset
	 * @return a new bar graph
	 */
	private JFreeChart makeChart(CategoryDataset dataset) {
		/*
		 * Title: "Which keywords are popular when?"
		 * Horizontal (Domain) Axis Label: "Time of Day"
		 * Vertical (Range) Axis Label: "Number of Times Each Keyword Appears"
		 * Data to plot: dataset
		 * Chart Orientation: Vertical Bars
		 * Make a legend: Yes
		 * Add tooltips: No
		 * Add URLS: No
		 */
		JFreeChart chart = ChartFactory.createStackedBarChart("Distribution of Tweets and Retweets for Each Keyword",
				"Keyword", "Number of Tweets and Retweets for Each Keyword", dataset,
				PlotOrientation.VERTICAL, true, false, false);
		
		// Access internal chart renderer in order to customize appearance.
		final CategoryPlot plot = chart.getCategoryPlot();
		CategoryItemRenderer r = plot.getRenderer();
		
		// Change colors from default set, and add individual labels.
		r.setSeriesPaint(0, Color.RED);
		r.setSeriesPaint(1, new Color(0,135,15)); // Green
	//	r.setSeriesPaint(2, new Color(115,15,115)); // Purple
	//	r.setSeriesPaint(3, Color.BLUE);
	//	r.setSeriesPaint(4, Color.MAGENTA);
		
		r.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		r.setBaseItemLabelPaint(Color.WHITE);
		r.setBaseItemLabelsVisible(true);
		return chart;
	}
}
