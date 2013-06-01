package edu.cmu.cs.cs214.hw5.framework;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import twitter4j.Status;
import edu.cmu.cs.cs214.hw5.plugin.Plugin;

/**
 * TwitterFramework : is a Framework object that handles the
 * back-end work and management of our application, and the
 * display of the plugins.
 * 
 * @author Esha Uboweja (euboweja) & Samantha Traiman(straiman)
 *
 */
@SuppressWarnings("serial")
public class TwitterFramework extends JFrame implements Framework{

	
	private Set<String> keywords;
	private Set<String> defaultKeywords;
	private int maxKeywords;
	private List<Plugin> plugins;
	private int pluginWidth;
	private int pluginHeight;
	private int maxTweetsToSearch;
	private int totalMaxTweets;
	private int totalRunningTweets;
	private Map<Integer,JPanel> pluginPanelsByID;
	private JTabbedPane pluginViewerPane; // TABBED PANE
	private JPanel userInteractionPanel;
	private JPanel frameContentPanel;
	private JPanel progressPanel;
	private JProgressBar fetchBar;
	private ExecutorService processDataAnalysisService;
	private ExecutorService searchDataService;
	
	// data containing keyword-> [TweetObject] is package-private for
	// modification by "UpdateDataForKeyword" Runnable(s)
	ConcurrentHashMap<String,List<TweetObject>> data;
	// the label displaying the last refresh time is package-private for
	// modification by the refreshListener of the button that 
	// launches a call to 'fetchData'
	JLabel sinceLastRefresh;
	// the boolean value 'done' is set to true after every SearchTask
	// completes its search, so package-private
	boolean done;
	// the uiListener is disabled by the refreshListener so that the 
	// end-user cannot modify the list of keywords, so package-private
	UserInput uiListener;
	// the refreshTimeInSeconds is package-private so that the refreshListener
	// can force avoiding rate-limiting 
	int refreshTimeInSeconds;
	// the lastRefreshedAt date is package-private so that the refreshListener
	// can update it when new data is fetched
	Date lastRefreshedAt;
	// the dataForPlugins is package-private so that the update task for the
	// last keyword can update it with the newly fetched and processed data
	Map<String,List<TweetObject>> dataForPlugins;
	// the updating boolean is package-private so that the update task for the
	// last keyword can change it when its done, makes sure that if the data
	// is updating (true) then new 'fetchData' calls simply return
	boolean updating;
	// the lastKeyword is package-private to keep track of when the entire
	// fetch_data-process_data-update_data cycle completes
	String lastKeyword;
	// use a TabbedPane to display the plugins, can have atmost 9 plugins as 
	// there are only 9 distinct KeyEvent(s) for plugin access by end-user
	private static final int[] mnemonicEvents = {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,KeyEvent.VK_5,
	                                             KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9};
	
	/**
	 * Initialize the TwitterFramework with default values and capacities 
	 * for refreshtTimeInSeconds, maxTweetsToSearch per keyword, maximum
	 * number of keywords allowed, pluginPanel width and height, the 
	 * default set of keywords
	 * 
	 */
	public TwitterFramework() {
		this.data = new ConcurrentHashMap<String, List<TweetObject>>();
		this.keywords = new HashSet<String>();
		this.lastKeyword = "";
		this.plugins = new ArrayList<Plugin>();
		this.maxTweetsToSearch = 1500;
		this.refreshTimeInSeconds = 16 * 60; // 16 minutes
		this.lastRefreshedAt = null;
		this.updating = false;
		this.maxKeywords = 5;
		
		// can only have as many threads as the number of processors
		this.processDataAnalysisService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.searchDataService = Executors.newSingleThreadExecutor();
		this.dataForPlugins = new HashMap<String,List<TweetObject>>();
		// default set
		this.defaultKeywords = new HashSet<String>();
		this.defaultKeywords.add("\"pasta\"");
		this.defaultKeywords.add("\"burger\"");
		this.defaultKeywords.add("\"pizza\"");
		this.defaultKeywords.add("\"taco\"");
		this.defaultKeywords.add("\"sub\"");
		assert(this.defaultKeywords.size() <= this.maxKeywords);
		this.pluginPanelsByID = new HashMap<Integer,JPanel>();
		this.pluginWidth  = 640;
		this.pluginHeight = 480;
		
		initGui();
	}
	
	// package-private so that its fields may be modified by the refreshListener
	private void initGui(){
		this.frameContentPanel = new JPanel();
		this.frameContentPanel.setLayout(new BorderLayout());
		
		this.userInteractionPanel = new JPanel();
		this.uiListener = new UserInput(this);
		this.userInteractionPanel.add(this.uiListener);
		
		this.pluginViewerPane = new JTabbedPane();
		this.pluginViewerPane.setPreferredSize(new Dimension(this.pluginWidth+50,this.pluginHeight+50));
		
		frameContentPanel.add(userInteractionPanel, BorderLayout.WEST);
		frameContentPanel.add(pluginViewerPane, BorderLayout.CENTER);
		this.setContentPane(frameContentPanel);
		this.setTitle("ESAnalytics@214");
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.validate();
		this.pack();
	}
	
	// package-private so that its fields may be modified by the refreshListener
	class UserInput extends JPanel implements ActionListener{
		
		JTextField keywordAdder;
		JCheckBox enableDefault;
		JTextArea keywordsLister;
		private TwitterFramework twApp;
		
		public UserInput(TwitterFramework tw){
			this.twApp = tw;
			initGui();
		}
	
		private void initGui(){
			setLayout(new GridBagLayout());
			
			keywordAdder = new JTextField(10);
			keywordAdder.addActionListener(this);
			
			// the list of keywords added :
			keywordsLister = new JTextArea(6, 10);
			JScrollPane scrollPane = new JScrollPane(keywordsLister);
			
			enableDefault = new JCheckBox("Use Default Keywords");
			//enableDefault.addActionListener(this);
			enableDefault.addItemListener(new ItemListener(){

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					if (arg0.getStateChange() == ItemEvent.DESELECTED){
						// was selected before, so now make keywords set empty
						// and clear the text area
						keywordsLister.setText("");
						twApp.clearKeywordsSet();
						keywordAdder.setEditable(true);
						keywordAdder.setEnabled(true);
					}
					else{
						//just selected, enable the default set of keywords
						//and display them in the text are
						keywordAdder.setText("");
						twApp.enableDefaultSet();
						keywordsLister.setText("");
						Set<String> keywords = twApp.getKeywords();
						for (String keyword: keywords){
							keywordsLister.append(keyword + "\n");
						}
						keywordAdder.setEditable(false);
						keywordAdder.setEnabled(false);
					}
				}
				
			});
			
			JLabel timeSince = new JLabel("Last Refresh @:");
			sinceLastRefresh = new JLabel("<html>" + "<b>(No data fetched yet)</b>");
			
			progressPanel = new JPanel();
			fetchBar = new JProgressBar();
			fetchBar.setVisible(false);
			progressPanel.add(fetchBar);			
			
			JButton getMoreData = new JButton("Get New Data");
			getMoreData.addActionListener(new RefreshListener(twApp));
			
			// add the keyword field and text are to the panel
			GridBagConstraints gbc = new GridBagConstraints();
			// set the number of cells in a row for the component's dispaly are
			// REMAINDER - occupy all of the cells the right in the same row
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			
			// if the panel is resized, then the text field will fit the panel
			// horizontally only 
			gbc.fill = GridBagConstraints.HORIZONTAL;
			add(keywordAdder, gbc);
			
			// if the panel is resized, then the text area will fit the panel
			// both horizontal and vertically
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			add(scrollPane, gbc);
			
			add(enableDefault,gbc);
			add(timeSince, gbc);
			add(sinceLastRefresh,gbc);
			add(getMoreData,gbc);

			add(progressPanel,gbc);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// when the user enters a keyword, it is added to the 
			// framework and to the keyword list text are defined above
			String keyword = this.keywordAdder.getText();
			
			// try adding to keyword set:
			try{
				twApp.addKeyword(keyword);
				this.keywordsLister.append(keyword + "\n");
				// so that a new keyword can be typed without needing to delete the old keyword
				this.keywordAdder.selectAll();
				
				// ensure that the last added keyword is visible
				this.keywordsLister.setCaretPosition(this.keywordsLister.getDocument().getLength());
			}catch(IllegalStateException ise){
				JOptionPane.showMessageDialog(twApp, ise.getMessage(), "Keyword Entry Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void clearKeywordsSet(){
		this.keywords = new HashSet<String>();
	}
	
	private void enableDefaultSet(){
		this.keywords = this.defaultKeywords;
	}

	// package-private so that the refreshListener can trigger fetching data
	// This method launches searchTasks to sequentially run on a background
	// thread separate from the Event Dispatch thread.
	void fetchData(){
		if (this.updating){
			return;
		}
		this.updating = true;
		this.dataForPlugins = new HashMap<String, List<TweetObject>>(data);
		this.totalMaxTweets = this.maxTweetsToSearch * this.keywords.size();
		this.totalRunningTweets = 0;
		this.done = false;
		List<String> keysToSearch = new ArrayList<String>(this.keywords);
		for (int i = 0; i < keysToSearch.size() ; i++){
			String keyword = keysToSearch.get(i);
			if (i == keysToSearch.size() - 1){
				this.lastKeyword = keyword;
			}
			SearchTaskForKeyword stfkCur = new SearchTaskForKeyword(keyword, this, maxTweetsToSearch);
			searchDataService.execute(stfkCur);
		}
		
	}
	
	// package-private so that the search task can request the processing of newly
	// fetched data
	// this method launches updateTasks on a thread separate from the Event Dispatch
	// thread. The Executor service may be running more than one update Task 
	// concurrently with other Update tasks because update tasks work on different
	// keywords
	void updateDataKeyword(String keyword, List<Status> tweets){
		UpdateDataForKeyword upfkTask = new UpdateDataForKeyword(keyword, tweets, this);
		processDataAnalysisService.execute(upfkTask);
	}
	
	// package-private so that the update task for the last keyword
	// can trigger notification of new data and analysis to all plugins
	void notifyAllPlugins(){
		for (Plugin p : this.plugins){
			final Plugin pl = p;
			this.processDataAnalysisService.execute(new Runnable(){
				@Override
				public void run() {
					pl.startAnalysis(dataForPlugins);
				}
			});
		}
	}
	
	private void addKeyword(String keyword) throws IllegalStateException,IllegalArgumentException{
		if (keywords.contains(keyword)){
			throw new IllegalArgumentException(keyword + " already added to set!");
		}
		if (keywords.size() == maxKeywords){
			throw new IllegalStateException("Cannot add any more keywords, maximum keywords allowed = " + maxKeywords);
		}
		this.keywords.add(keyword);
	}
	
	/**
	 * @see edu.cmu.cs.cs214.hw5.framework.Framework#registerPlugin(edu.cmu.cs.cs214.hw5.plugin.Plugin)
	 */
	@Override
	public void registerPlugin(Plugin p) {
		int pID = this.plugins.size();
		p.registerFramework(this, pID);
		this.plugins.add(p);
		
		// fixed size plugin panel
		JPanel panelP = new JPanel();
		panelP.setSize((new Dimension(pluginWidth,pluginHeight)));
		panelP.setPreferredSize((new Dimension(pluginWidth,pluginHeight)));
		this.pluginPanelsByID.put(pID, panelP);
		String pTitle = p.getPluginTitle();
		if (pTitle == null || pTitle.length() == 0){
			pTitle = "Plugin" + pID;
		}
		this.pluginViewerPane.addTab(pTitle, panelP);
		this.pluginViewerPane.setMnemonicAt(pID, mnemonicEvents[pID]);
		
		p.initializePluginPanel(panelP);
	}

	/**
	 * @see edu.cmu.cs.cs214.hw5.framework.Framework#getKeywords()
	 */
	@Override
	public Set<String> getKeywords() {
		return this.keywords;
	}

	/**
	 * @see edu.cmu.cs.cs214.hw5.framework.Framework#getAll()
	 */
	@Override
	public Map<String, List<TweetObject>> getAll() {
		return this.dataForPlugins;
	}

	/**
	 * @see edu.cmu.cs.cs214.hw5.framework.Framework#revalidatePluginPanel(int)
	 */
	@Override
	public void revalidatePluginPanel(int pluginID)
		throws IllegalArgumentException{
		final int pID = pluginID;
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				if (pID < 0 || pID >= plugins.size()){
					throw new IllegalArgumentException("Bad plugin ID");
				}
				// need to refresh that plugin's content
				JPanel thisPanel = pluginPanelsByID.get(pID);
				thisPanel.revalidate();
				thisPanel.repaint();
				frameContentPanel.revalidate();
				validate();
				repaint();
			}
		});
	}
	
	// package-private so that a search task can notify the framework
	// of the number of tweets just fetched, and the total
	/**
	 * A Search task notifies the framework of the number of tweets
	 * it just fetched, the keyword that it fetched those tweets for
	 * and the total number of tweets it fetched. 
	 * This function displays a ProgressBar when the data is being
	 * fetched to show the end-user that 'something' is definitely
	 * happening in the background. 
	 * We keep track of the running total number of tweets fetched
	 * and the expected total of tweets to be fetched as a result
	 * of all the Search Tasks and present the percentage
	 * as a division of these two values.
	 * @param tweets : int
	 * 		the number of tweets the task just fetched (per query to API)
	 * @param keyword : String
	 * 		the keyword that the fetch happened for
	 * @param total : int
	 * 		the total number of tweets that it fetched
	 */
	void notifyTweetsUpdated(int tweets, String keyword, int total){
		totalRunningTweets += tweets;
		final int gotTweets = totalRunningTweets;
		final String key = keyword;
		final JFrame frame = this;
		final int totalTweets = total;
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				int value = (int)((float)gotTweets*100.0f/totalMaxTweets);
				if (done){
					totalMaxTweets -= maxTweetsToSearch;
					totalMaxTweets += totalTweets;
					value = (int)((float)gotTweets*100.0f/totalMaxTweets);
				}
				if (key.equals(lastKeyword)){
					if (totalRunningTweets == 0 && dataForPlugins.size() == 0){
						// if no data was found in the first search at all
						// its expected that the keywords are 'garbage' and no one
						// will ever tweet them
						JOptionPane.showMessageDialog(frame, "No tweets found :(, exiting application", "No tweets found", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
					}
					if (done){
						value = 100;
					}
				}
				fetchBar.setVisible(true);
				fetchBar.setValue(value);
				fetchBar.setStringPainted(true);
				userInteractionPanel.repaint();
				frameContentPanel.revalidate();
				validate();
				repaint();
				
			}
		});
	}
	
	// package-private so that once the update task for the
	// last keyword is done updating, then it removes the
	// progress bar to show the end-user that the 
	// fetch_data-process_data-update_data cycle is complete.
	void removeProgressBar(){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				fetchBar.setVisible(false);
				userInteractionPanel.repaint();
				frameContentPanel.revalidate();
				validate();
				repaint();
			}
		});
	}

}
