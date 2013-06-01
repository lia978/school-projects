package edu.cmu.cs.cs214.hw5.framework;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TweetObject : a useful wrapper object that contains
 * information about the tweets obtained for a keyword
 * with simple fields that can be accessed by the Plugin
 * and used for analysis, via the getter methods provided
 * below. The purpose is to make data-parsing
 * application-writer/plugin-programmer friendly.
 * <p>
 * We use package-privacy for setter methods so that only
 * a TwitterFramework object can create and modify a 
 * TweetObject. This ensures that the data obtained from the
 * Twitter website is not accidentally mutated by the Plugins 
 * as it shouldn't be (either they need it or they don't). 
 * Also, Plugins can create their own wrappers for the TweetObjects 
 * so that they can add more information about the specific tweet
 * to that wrapper object. 
 * 
 * @author Esha Uboweja (euboweja) & Samantha Traiman(straiman)
 *
 */
public class TweetObject {

	private String tweet; // the tweet's text
	private Date date;  // date of creation of the tweet
	private List<String> hashtags; // the hashtags in that tweet
	private String username; // the screen-name of the poster of the tweet
	private boolean isRetweet; // if its a re-tweet
	private String retweeterName; // the screen-name of the person who retweeted it
	private Date retweetPostDate; // the date the retweet
	
	public TweetObject() {
		// Don't want to return nulls
		this.tweet = "";
		this.date = new Date();
		this.retweetPostDate = new Date();
		this.hashtags = new ArrayList<String>();
		this.username = "";
		this.isRetweet = false;
		this.retweeterName = "";
	}
	
	/* Setters
	    Note : I use package level privacy so that the TwitterFramework
		has access to modifying the TweetObject properties but the
		Plugins do not. They can only use the public getter methods.
	*/
	void setTweetText(String text){
		this.tweet = text;
	}
	
	void setPostDate(Date d){
		this.date = d;
	}
	
	void setTweetHashes(List<String> hashtags){
		this.hashtags = hashtags;
	}
	
	void setUserName(String uname){
		this.username = uname;
	}
	
	void setIsRetweet(boolean isRT){
		this.isRetweet = isRT;
	}
	
	void setRetweeterName(String retweeterName){
		this.retweeterName = retweeterName;
	}
	
	void setRetweetPostDate(Date retweetDate){
		this.retweetPostDate = retweetDate;
	}
	
	/* Getters */
	public String getTweet() {return this.tweet;}
	
	public Date getDate() {return this.date;}
	
	public List<String> getHashtags() {return this.hashtags;}
	
	public String getUserName() {return this.username;}
	
	public boolean isRetweet() {return this.isRetweet;}
	
	public Date getRetweetPostDate() {return this.retweetPostDate;}
	
	public String getRetweeterName() {return this.retweeterName;}
	
}
