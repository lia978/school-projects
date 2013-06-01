package edu.cmu.cs.cs214.hw5.framework;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.cs.cs214.hw5.PhraseLocation;
import edu.cmu.cs.cs214.hw5.TimeOfDay;

/**
 * Tweetility : is a utility class that provides plugin-programmer
 * friendly static helper functions for filtering a Map
 * of (keywords-> List<TweetObject>) with a certain property
 * as listed below.
 * 
 * @author Esha Uboweja (euboweja) & Samantha Traiman(straiman)
 *
 */
public final class Tweetility {
	
	private Tweetility() {}
	
	/**
	 * Get a Map data structure that maps the keywords to their respective list of TweetObject(s) 
	 * that have been created with the query string in the tweet text at the phrase location
	 * as specified by the user
	 * @param query:
	 * 		the string that the user enforces should be in every TweetObject's tweet text in
	 * 		the resulting map
	 * @param pl:
	 * 		the location of the query string in every tweet
	 * @param data:
	 * 		the map data structure of keyword(s) -> TweetObject(s) on which the filter is applied
	 * @return filterData : Map<String,List<TweetObject>>:
	 * 		the tweetObjects that have tweets with the query string only at location pl in the 
	 * 		text of the tweet
	 * @throws IllegalArgumentException
	 */
	public static Map<String, List<TweetObject>> getByPhrase(String query, PhraseLocation pl, Map<String, List<TweetObject>> data) 
			throws IllegalArgumentException{
		if (data == null || data.size() == 0){
			throw new IllegalArgumentException("Bad Data for input , there is nothing in \"data\"!");
		}
		if (pl == null){
			throw new IllegalArgumentException("Bad PhraseLocation for search, there is nothing in \"pl\"!");
		}
		if (query == null || query.length() == 0){
			throw new IllegalArgumentException("Bad Phrase for searching, there is nothing in \"query\"!");
		}
		Map<String, List<TweetObject>> filteredData = new HashMap<String, List<TweetObject>>();
		for (String keyword : data.keySet()){
			List<TweetObject> keysTweets = data.get(keyword);
			List<TweetObject> filteredTweets = new ArrayList<TweetObject>();
			for (TweetObject tweet: keysTweets){
				if (tweet != null){
					String tweetText = tweet.getTweet();
					if (PhraseLocation.getPhraseLocation(query, tweetText) == pl){
						filteredTweets.add(tweet);
					}
				}
			}
			filteredData.put(keyword, filteredTweets);
		}
		return filteredData;
	}

	/**
	 * Get a Map data structure that maps the keywords to their respective list of TweetObject(s) 
	 * that have been created during the TimeOfDay t as specified by the user
	 * @param t:
	 * 		the TimeOfDay that the tweet should be created during
	 * @param data:
	 * 		the map data structure of keyword(s) -> TweetObject(s) on which the filter is applied
	 * @return filterData : Map<String,List<TweetObject>>:
	 * 		the tweetObjects that have tweets created only during TimeOfDay t
	 * @throws IllegalArgumentException:
	 * 		if either the data is null or empty or if the TimeOfDay t is null
	 */
	public static Map<String, List<TweetObject>> getByTime(TimeOfDay t, Map<String, List<TweetObject>> data) 
			throws IllegalArgumentException{
		if (data == null || data.size() == 0){
			throw new IllegalArgumentException("Bad Data for input , there is nothing in \"data\"!");
		}
		if (t == null){
			throw new IllegalArgumentException("Bad TimeOfDay \"t\" for input, its null!");
		}
		Map<String, List<TweetObject>> filteredData = new HashMap<String, List<TweetObject>>();
		for (String keyword : data.keySet()){
			List<TweetObject> keysTweets = data.get(keyword);
			List<TweetObject> filteredTweets = new ArrayList<TweetObject>();
			for (TweetObject tweet: keysTweets){
				if (tweet != null){
					Date tweetCreatedAt = tweet.getDate();
					if (TimeOfDay.getTimeOfDay(tweetCreatedAt) == t){
						filteredTweets.add(tweet);
					}
				}
			}
			filteredData.put(keyword, filteredTweets);
		}
		return filteredData;
	}
	
	/**
	 * Get a Map data structure that maps the keywords to their respective list of TweetObject(s) 
	 * that have only the hash-tags specified by the user in "hashtags"
	 * @param hashtags:
	 * 		the list of hash-tags which are allowed to be in the filtered tweetObjects or a subset
	 * 		of which are allowed to be in the filtered tweetObjects
	 * @param data:
	 * 		the map data structure of keyword(s) -> TweetObject(s) on which the filter is applied
	 * @return filterData : Map<String,List<TweetObject>>:
	 * 		the tweetObjects that have tweets containing only the Hash tags in hashtags
	 * @throws IllegalArgumentException:
	 * 		if either the data is null or empty or if hashtags is null or empty
	 */
	public static Map<String, List<TweetObject>> getByHashtags(List<String> hashtags, Map<String, List<TweetObject>> data) 
			throws IllegalArgumentException{
		if (data == null || data.size() == 0){
			throw new IllegalArgumentException("Bad Data for input, there is nothing in \"data\"!");
		}
		if (hashtags == null || hashtags.size() == 0){
			throw new IllegalArgumentException("Bad Data for input, there is nothing in \"hashtags\"");
		}
		Map<String, List<TweetObject>> filteredData = new HashMap<String, List<TweetObject>>();
		for (String keyword : data.keySet()){
			List<TweetObject> keysTweets = data.get(keyword);
			List<TweetObject> filteredTweets = new ArrayList<TweetObject>();
			for (TweetObject tweet: keysTweets){
				if (tweet != null){
					List<String> tweetHashtags = tweet.getHashtags();
					boolean addToList = true;
					for (String hashtag : tweetHashtags){
						addToList &= hashtags.contains(hashtag);
					}
					if (addToList){
						filteredTweets.add(tweet);
					}
				}
			}
			filteredData.put(keyword, filteredTweets);
		}
		return filteredData;
	}
	
	/**
	 * Get a Map data structure that maps the keywords to their respective list of TweetObject(s) 
	 * that are only retweets
	 * @param data:
	 * 		the map data structure of keyword(s) -> TweetObject(s) on which the filter is applied
	 * @return filteredData:
	 * 		the tweetObjects that have only retweets
	 * @throws IllegalArgumentException:
	 * 		if either the data is null or empty or if the TimeOfDay t is null
	 */
	public static Map<String, List<TweetObject>> getRetweetsOnly(Map<String, List<TweetObject>> data) 
		throws IllegalArgumentException{
		if (data == null || data.size() == 0){
			throw new IllegalArgumentException("Bad Data for input , there is nothing in \"data\"!");
		}
		Map<String, List<TweetObject>> filteredData = new HashMap<String, List<TweetObject>>();
		for (String keyword : data.keySet()){
			List<TweetObject> keysTweets = data.get(keyword);
			List<TweetObject> filteredTweets = new ArrayList<TweetObject>();
			for (TweetObject tweet: keysTweets){
				if (tweet != null){
					if (tweet.isRetweet()){
						filteredTweets.add(tweet);
					}
				}
			}
			filteredData.put(keyword, filteredTweets);
		}
		return filteredData;
	}
	
	/**
	 * Get a Map data structure that maps the keywords to their respective list of TweetObject(s) 
	 * that are not retweets
	 * @param data:
	 * 		the map data structure of keyword(s) -> TweetObject(s) on which the filter is applied
	 * @return filteredData:
	 * 		the tweetObjects that have no retweets
	 * @throws IllegalArgumentException:
	 * 		if either the data is null or empty or if the TimeOfDay t is null
	 */
	public static Map<String, List<TweetObject>> getTweetsOnly(Map<String, List<TweetObject>> data) 
			throws IllegalArgumentException{
		if (data == null || data.size() == 0){
			throw new IllegalArgumentException("Bad Data for input , there is nothing in \"data\"!");
		}
		Map<String, List<TweetObject>> filteredData = new HashMap<String, List<TweetObject>>();
		for (String keyword : data.keySet()){
			List<TweetObject> keysTweets = data.get(keyword);
			List<TweetObject> filteredTweets = new ArrayList<TweetObject>();
			for (TweetObject tweet: keysTweets){
				if (tweet != null){
					if (!tweet.isRetweet()){
						filteredTweets.add(tweet);
					}
				}
			}
			filteredData.put(keyword, filteredTweets);
		}
		return filteredData;
	}
}
