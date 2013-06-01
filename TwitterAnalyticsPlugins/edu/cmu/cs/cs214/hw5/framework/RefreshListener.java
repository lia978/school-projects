package edu.cmu.cs.cs214.hw5.framework;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

/**
 * RefreshListener : is an ActionListener for the button
 * that the end user uses to request data updates from 
 * the Twitter website internet. 
 * 
 * @author Esha Uboweja (euboweja) & Samantha Traiman(straiman)
 *
 */
public class RefreshListener implements ActionListener{

	TwitterFramework twitterApp;
	
	/**
	 * Create a new refreshListener
	 * @param tw : TwitterFramework
	 * 		the framework that uses the button requiring
	 * 		a refreshListener
	 */
	public RefreshListener(TwitterFramework tw) {
		twitterApp = tw;
	}


	@Override
	public void actionPerformed(ActionEvent arg0) {
		Date now = new Date();
		long timePassed;
		boolean refresh = true;
		// if there are no keywords to do a search for
		if (twitterApp.getKeywords().size() <= 0){
			JOptionPane.showMessageDialog(twitterApp, "Please enter atleast one keyword to query", "Update Data Request Error", JOptionPane.ERROR_MESSAGE);
			refresh = false;
			return;
		}
		// now the user cannot modify the list of keywords anymore
		twitterApp.uiListener.keywordAdder.setEditable(false);
		twitterApp.uiListener.keywordAdder.setEnabled(false);
		twitterApp.uiListener.keywordsLister.setEditable(false);
		twitterApp.uiListener.enableDefault.setEnabled(false);
		if (twitterApp.lastRefreshedAt == null){
			// if its the first refresh
			twitterApp.lastRefreshedAt = now;
		}
		else{
			// get the time since last refresh
			timePassed = (now.getTime() - twitterApp.lastRefreshedAt.getTime())/1000;
			if (timePassed < twitterApp.refreshTimeInSeconds){
				int seconds = twitterApp.refreshTimeInSeconds - (int)timePassed;
				// for forcing rate-limit respect
				JOptionPane.showMessageDialog(twitterApp, "Sorry, please wait atleast " + seconds + " seconds more before refreshing again!", "Update Data Request Error", JOptionPane.ERROR_MESSAGE);
				refresh = false;
			}
		}
		
		if (refresh){
			// update time since last refreshed
			twitterApp.lastRefreshedAt = now;
			DateFormat df = new SimpleDateFormat("hh:mm:ss aaa z");
			twitterApp.sinceLastRefresh.setText("<html><b>" + df.format(now) + "</b>");
			twitterApp.fetchData();	
		}
	}

}
