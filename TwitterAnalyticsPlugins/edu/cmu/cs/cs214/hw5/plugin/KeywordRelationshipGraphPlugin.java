package edu.cmu.cs.cs214.hw5.plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import edu.cmu.cs.cs214.hw5.PhraseLocation;
import edu.cmu.cs.cs214.hw5.framework.Framework;
import edu.cmu.cs.cs214.hw5.framework.TweetObject;
import edu.cmu.cs.cs214.hw5.framework.Tweetility;

public class KeywordRelationshipGraphPlugin implements Plugin{

	private final int MAX_KEYWORDS = 5;
	private final String title = "Keyword-Based Tweet Search";
	private Framework framework;
	private int myID;
	
	private List<String> keywords;
	private int numKeywords;		
	private int width, height;
	private JPanel myPanel;
	
	public KeywordRelationshipGraphPlugin (){
		keywords = new ArrayList<String>(MAX_KEYWORDS);
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

	@Override
	public void startAnalysis(Map<String, List<TweetObject>> data) {

		// remove all previous data
		myPanel.removeAll();
		keywords.clear();		
		keywords.addAll(framework.getKeywords());
		numKeywords = keywords.size();
		
		Map<Point, List<String>> overlapTweets = new HashMap<Point, List<String>>();	
		int numTweets = 0;
			
		for (int i = 0; i<numKeywords; i++){
			for (int j = 0; j<numKeywords; j++){
				
				String firstKey = keywords.get(i);
				String secondKey = chop(keywords.get(j));
				Point point = new Point(i,j);
				ArrayList<String> tweets = new ArrayList<String>();
				
				
				if (i == j){
					numTweets = data.get(firstKey).size();
					if (numTweets >0){
						List<TweetObject> objs = data.get(firstKey);						
						for (TweetObject obj: objs){
							tweets.add(obj.getTweet());
						}											
					}
				}
				else{
					
					Map<String, List<TweetObject>> filtered =Tweetility.getByPhrase(secondKey, PhraseLocation.ISPRESENT, data);				
					numTweets = filtered.get(firstKey).size();	
								
					if (numTweets >0){
						List<TweetObject> objs = filtered.get(firstKey);						
						for (TweetObject obj: objs){
							tweets.add(obj.getTweet());
						}											
					}				
					
				}					
				overlapTweets.put(point, tweets);
			}			
			
		}
		 
		// maps each Point(i,j) to a list of tweets containing both keywords(i) and keywords(j)
		Map<Point, List<String>> combinedOverlap = this.combineTweetMap(overlapTweets, keywords);
		
		GraphPanel graphPanel = new GraphPanel(width, height, numKeywords, keywords, combinedOverlap);
		graphPanel.setPreferredSize(new Dimension(width-10, height-10));
		graphPanel.setMinimumSize(new Dimension(width-10, height-10));
		myPanel.add(graphPanel);
		myPanel.setBackground(graphPanel.DARK_BLUE);
		framework.revalidatePluginPanel(myID);
	}
	
	@Override
	public void initializePluginPanel(JPanel content) {
		myPanel = content;
		this.width = myPanel.getWidth();
		this.height = myPanel.getHeight();
	}

	/**
	 * combines map of tweets containing a that also contains b with map of tweets containing b that also contains a
	 * to a single map that maps Point(a,b) to list of tweets containing word a and word b
	 * @param map
	 * @param keywords
	 * @return
	 */
	private Map<Point, List<String>> combineTweetMap(Map<Point, List<String>> map, List<String> keywords){
		int size = keywords.size();
		HashMap <Point, List<String>> combined = new HashMap <Point, List<String>>();
		for (int i = 0; i<size; i++){
			for (int j =0; j<=i; j++){
				List <String> newlist = new ArrayList<String>();
				List<String> forwardList = map.get(new Point(i,j));
				List<String> reverseList = map.get(new Point(j,i));			
				for (String item: forwardList){
					if (!newlist.contains(item)){
						newlist.add(item);
					}
				}
				for (String item: reverseList){
					if (!newlist.contains(item)){
						newlist.add(item);
					}
				}
				combined.put(new Point(i,j), newlist);
				combined.put(new Point(j,i), newlist);
				
			}
		}
		return combined;
	}
	
	/**
	 * 
	 * @param input
	 * @return the string without quotations
	 */
	private String chop(String input){
		if (input == null) return input;
		int length = input.length();
		if (input.length() == 1) return input;		
		if (input.charAt(0) == '"' && input.charAt(length-1) == '"'){			
			return input.substring( 1,length);					
		}
		else return input;
	}

}
