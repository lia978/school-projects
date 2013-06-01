package edu.cmu.cs.cs214.hw5.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * SearchForTaskKeyword : is a Runnable that is launched
 * on a background thread different from the Event Dispatch Thread
 * so that the GUI doesn't freeze when the framework uses
 * Twitter4J to request new data via the SearchAPI over
 * the Internet. Only one SearchTaskForKeyword is executed
 * at one time in the background thread by the framework.
 * 
 * @author Esha Uboweja (euboweja) & Samantha Traiman(straiman)
 *
 */
public class SearchTaskForKeyword implements java.lang.Runnable{

	private final String searchingPhrase;
	private List<Status> results;
	private TwitterFramework ownerTF;
	private int maxTweetsToSearch;
	
	/**
	 * Create a Search Task
	 * @param searchingPhrase: String
	 * 		the keyword that has to be searched for via the Search API
	 * @param tw: TwitterFramework
	 * 		the framework that wants data obtained from Twitter
	 * @param maxTweets:
	 * 		the maximum number of tweets to be searched for and returned
	 * 		for the given keyword
	 */
	public SearchTaskForKeyword(String searchingPhrase, TwitterFramework tw, int maxTweets) {
		this.searchingPhrase = searchingPhrase;
		this.results = new ArrayList<Status>();
		this.ownerTF = tw;
		this.maxTweetsToSearch = maxTweets;
	}

	/**
	 * do the search via the Twitter Search API functions in the Twitter4J library
	 */
	@Override
	public void run() {
		if (this.searchingPhrase == null || this.searchingPhrase.length() == 0){
			return;
		}
		assert(this.results.size() == 0);
		Twitter twitter = new TwitterFactory().getInstance();
		Query query = new Query(this.searchingPhrase);
		query.setCount(100); // Get as many tweets as possible per query
		query.setLang("en"); // want English tweets ONLY
		QueryResult result;
		int numTweets = 0;
		try{
			ownerTF.done = false;
			do{
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();
				int num = 0;
				
				if (tweets.size() + numTweets > maxTweetsToSearch){
					num = maxTweetsToSearch-numTweets;
					results.addAll(tweets.subList(0, num));
					numTweets += num;
				}
				else{
					num = tweets.size();
					results.addAll(tweets);
					numTweets += tweets.size();
				}
				
				ownerTF.notifyTweetsUpdated(num, this.searchingPhrase, numTweets);
			}while((query = result.nextQuery()) != null && numTweets < maxTweetsToSearch);
			
			ownerTF.done = true;
		
		}catch(TwitterException e){
			// will only happen if the user tries to close and re-launch the
			// framework application every time
	    	e.printStackTrace();
	    	Map<String, RateLimitStatus> rateLimitStatus;
			try {
				rateLimitStatus = twitter.getRateLimitStatus();
				// for search , endpoint : /search/tweets
				String endpoint = "/search/tweets";
                RateLimitStatus status = rateLimitStatus.get(endpoint);
                System.out.println("Endpoint: " + endpoint);
                System.out.println(" Limit: " + status.getLimit());
                System.out.println(" Remaining: " + status.getRemaining());
                System.out.println(" ResetTimeInSeconds: " + status.getResetTimeInSeconds());
                System.out.println(" SecondsUntilReset: " + status.getSecondsUntilReset());
                SwingUtilities.invokeLater(new Runnable(){
        			@Override
        			public void run() {
        				JOptionPane.showMessageDialog(ownerTF, "You hit the rate limit by being cheeky. Now close the application and wait for 15 minutes before restarting it.", "Query Fetch Error", JOptionPane.ERROR_MESSAGE);
        				System.exit(1);
        			}
        		});
			} catch (TwitterException e1) {
				e1.printStackTrace();
			}
		}
		// done searching, return all found results
		// notify the framework with the data
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				ownerTF.updateDataKeyword(searchingPhrase,results);
			}
		});

	}

}
