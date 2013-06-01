package edu.cmu.cs.cs214.hw5;

import edu.cmu.cs.cs214.hw5.framework.TwitterFramework;
import edu.cmu.cs.cs214.hw5.plugin.*;

public class Main {

	public static void main(String args[]) {
		TwitterFramework framework = new TwitterFramework();
		
		framework.setVisible(true);
		Plugin hashCount = new HashTagCountPlugin();
		framework.registerPlugin(hashCount);
		Plugin test1 = new TweetvsRetweetPlugin();
		framework.registerPlugin(test1);
		Plugin test2 = new KeywordRelationshipGraphPlugin();
		framework.registerPlugin(test2);
		
	}

}
