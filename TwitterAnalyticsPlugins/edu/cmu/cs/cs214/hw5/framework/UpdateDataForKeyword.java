package edu.cmu.cs.cs214.hw5.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import twitter4j.HashtagEntity;
import twitter4j.Status;

/**
 * UpdateDataForKeyword : is a Runnable that is launched
 * on a background thread different from the Event Dispatch Thread
 * so that the GUI doesn't freeze when the framework processes
 * the data it received from the Twitter website, i.e. it
 * wraps the list of statuses into programmer-friendly TweetObjects
 * that may be used by the Plugin programmer for analysis.
 * Multiple updates to data can happen in different threads for
 * different keywords, and so we use a ConcurrentHashMap to 
 * keep track of data in the framework. 
 * 
 * @author Esha Uboweja (euboweja) & Samantha Traiman(straiman)
 *
 */
public class UpdateDataForKeyword implements Runnable {

	private final String keyword;
	private List<Status> tweets;
	private TwitterFramework ownerTF;
	
	/**
	 * Create an Update Task
	 * @param keyword: String
	 * 		the keyword for which the data is being wrapped, used to
	 * 		query and update the data in the owner framework
	 * @param results: List<Status>
	 * 		the List of Status objects obtained via the Search Task
	 * 		for 'keyword'
	 * @param tw: TwitterFramework
	 * 		the framework that wants to use this update data task
	 * 		to update its data
	 */
	public UpdateDataForKeyword(String keyword, List<Status> results, TwitterFramework tw) {
		this.keyword = keyword;
		this.ownerTF = tw;
		this.tweets = results;
	}
	

	/**
	 * do an update to the list of TweetObject(s) in the 'data' tracked
	 * by the framework
	 */
	@Override
	public void run() {
		// Collect information from the tweets
		List<TweetObject> newData = new ArrayList<TweetObject>();
		for (Status tweet : tweets){
			// ignore bad data... i.e. null users/statuses
			if (tweet != null && tweet.getUser() != null){
				
				TweetObject twObj = new TweetObject();
				
				if (tweet.isRetweet() && tweet.getRetweetedStatus() != null && tweet.getRetweetedStatus().getUser() != null){
					twObj.setIsRetweet(tweet.isRetweet());
					Status retweeted = tweet.getRetweetedStatus();
					twObj.setUserName(retweeted.getUser().getScreenName());
					twObj.setTweetText(retweeted.getText());
					HashtagEntity[] hashtagEnts = retweeted.getHashtagEntities();
					List<String> hashtags = new ArrayList<String>();
					for (HashtagEntity hashtag : hashtagEnts){
						hashtags.add(hashtag.getText());
					}
					twObj.setTweetHashes(hashtags);
					twObj.setPostDate(retweeted.getCreatedAt());
					twObj.setRetweeterName(tweet.getUser().getScreenName());
					twObj.setRetweetPostDate(tweet.getCreatedAt());
					newData.add(twObj);
				}
				else if (!tweet.isRetweet()){
					twObj.setUserName(tweet.getUser().getScreenName());
					twObj.setTweetText(tweet.getText());
					HashtagEntity[] hashtagEnts = tweet.getHashtagEntities();
					List<String> hashtags = new ArrayList<String>();
					for (HashtagEntity hashtag : hashtagEnts){
						hashtags.add(hashtag.getText());
					}
					twObj.setTweetHashes(hashtags);
					twObj.setPostDate(tweet.getCreatedAt());
					// isn't a retweet, so default values are OK
					newData.add(twObj);
				}
			}
		}

		final List<TweetObject> addingData = newData;
		
		// update data 
		// NOTE : we can read and update the hashmap and not have
		// race conditions for modifying the same keyword because 
		// only one thread is forced to update a single keyword at
		// a time.
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				List<TweetObject> curData = ownerTF.data.putIfAbsent(keyword,addingData);
				if (curData != null){
					curData.addAll(addingData);
					ownerTF.data.put(keyword, curData);
					
				}
				if (keyword.equals(ownerTF.lastKeyword)){
					ownerTF.updating = false;
					ownerTF.dataForPlugins = new HashMap<String, List<TweetObject>>(ownerTF.data);
					
					ownerTF.done = false;
					ownerTF.removeProgressBar();
					// notify all plugins that the data has been updated
					ownerTF.notifyAllPlugins();
				}
			}
		});
	}

}
