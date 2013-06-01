package edu.cmu.cs.cs214.hw5.plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * JPanel for display of a graph of keywords and their shared tweets
 */
public class GraphPanel extends JPanel{
	
	private final int width;
	private final int height;
	private final int numComp;
	private final int textWidth = 7;
	private final int textHeight = 20;
	private final int textBorder = 2;
	private final int TEXTFRAME_SIZE = 40;
	private final Font font = new Font(Font.MONOSPACED,Font.BOLD,12); 	
	private final Color LIGHT_BLUE = new Color(176,196,222);
	protected final Color DARK_BLUE = new Color(119,136,153);
	private List<String> keyList;
	private Map<Point, List<String>> overlapTweets;
	
	/**
	 * Constructor
	 * @param x width of panel
	 * @param y height of panel
	 * @param numComp number of keywords(nodes) to display
	 * @param keyList List of keywords
	 * @param listOverlap Map from Point(i,j) to List of Tweets containing the ith and jth keyword in keyList
	 */
	public GraphPanel (int x, int y, int numComp, List<String> keyList,  Map<Point, List<String>> listOverlap){
		
		width = x;
		height = y;	
		this.numComp = numComp;
		this.keyList = keyList;
		this.overlapTweets = listOverlap;
		this.setLayout(null);
	}
	
	@Override
	public void paintComponent(Graphics g){ 
		
		g.setColor(LIGHT_BLUE);
		String instruction = "Click on the Buttons to See Tweets!";
		g.setFont(font);
		g.drawString(instruction, width/2-instruction.length()*textWidth, 10);
		
		//get list of x,y coordinates for each node
        int [][] centers = getCenters(numComp);  
        
        for (int i = 0; i<numComp; i++){
        	for (int j = 0; j<i; j++){
        		int x1 =centers[i][0];
        		int y1 = centers[i][1];
        		int x2 = centers[j][0];
        		int y2 = centers[j][1];
        		
        		//draw connecting edge between every pair of nodes
        		g.setColor(LIGHT_BLUE);
        		g.drawLine(x1, y1, x2, y2);
        		
        		//add label to middle of every edge to display number of shared tweets
        		if (i!=j){
        			int [] center = getCenterofLine(x1, y1, x2, y2);
        			String display = "invalid";      			      			 				
        			int value = overlapTweets.get(new Point(i, j)).size();
        			display = " " + value + " ";    			
        			
        			int offSet = 0;
        			//for keyList of size 4, it gives a weird case, the center is actually two labels, 
        			//so must offset the labels to see them both     			      			
        			if (numComp==4){
        				if(i==3 && j==1){
        					offSet = 10;
        				}
        				else if (i==2 &&j==0){
        					offSet = -10;
        				}
        			}       		
        			drawButton(center[0], center[1]+offSet, display , g, i,j);
        		}
        			
        	}
        }
            
        //add label to each node: displays keyword and the number of tweets containing that keyword
        for (int i = 0; i<numComp; i++){	
        	String display = keyList.get(i) + ": ";
        	List<String> overlapList = overlapTweets.get(new Point(i,i));
        	display = keyList.get(i) + ": " + overlapList.size();       	 
        	drawButton(centers[i][0], centers[i][1], display, g, i, i); 
        }
            
        
	}
	
	/**
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return center = {x',y'} the coordinates of the middle of two points
	 */
	private int [] getCenterofLine(int x1, int y1, int x2, int y2){
		int [] center = new int[2];
		center[0] = (int)((x1+x2)/2);
		center[1] = (int)((y1+y2)/2);
		return center;
	}
	
	/**
	 * 
	 * @param x the x coord in panel
	 * @param y the y coord in panel
	 * @param string label
	 * @param g 
	 * @param firstInd first keyword index in keyList
	 * @param secondInd second keyword index in keyList
	 */
	private void drawButton(int x, int y, String string, Graphics g, int firstInd, int secondInd){
		
		JButton button = new JButton (string);				
		button.setFont(font);
		button.addActionListener(new GetTweetListener(firstInd,secondInd, keyList));		
		button.setLayout(null);
		int size = string.length();
		button.setBounds(x-size*textWidth/2-textBorder, y-textHeight/2-textBorder,size*textWidth+20, textHeight);			
		button.setBackground(Color.WHITE);
		button.setForeground(DARK_BLUE);
		this.add(button);

	}

	/**
	 * 
	 * @param n number of nodes
	 * @return the coordinates for the center of the nodes in the panel display
	 */
	private int[][] getCenters(int n){
		int [][] coord = new int[n][2];		
		int xcenter = width/2;
		int ycenter = height/2;			
		int radius =(int)( Math.sqrt(Math.pow(xcenter, 2) + Math.pow(ycenter, 2)))/2;
		double degree = 2*Math.PI/n;			
		for (int i = 0; i<n; i++){	
			coord[i][0] = (int) (xcenter - radius * Math.sin(i *degree));			
			coord[i][1] = (int) (ycenter - radius * Math.cos(i *degree));					
		}		
		return coord;
	}
	
	/**
	 * an ActionListener that is evoked when button is clicked to display a new JFrame holding a list of
	 * tweets pertaining to that button
	 *
	 */
	private class GetTweetListener implements ActionListener{
		
		private int x;
		private int y;
		private List<String> keywords;
		
		GetTweetListener (int x, int y, List<String> keywords){
			this.x = x;
			this.y = y;
			this.keywords = keywords;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			JFrame frame = new JFrame();
			JPanel panel = new JPanel();
			List<String> listTweets = overlapTweets.get(new Point(x,y));

			int numTweets = listTweets.size();			
			JTextArea textArea = new JTextArea(TEXTFRAME_SIZE, TEXTFRAME_SIZE);   
			textArea.setEditable(false);                               
		
			if(numTweets == 0){
				textArea.append("There are no tweets matching the criteria\n");
			}
			
			else {				
				for (int i =0; i<numTweets; i++){
					String label = listTweets.get(i);
					textArea.append(label + "\n");
					textArea.append("-------------------------\n" );
				}
			}
					
			JScrollPane scrollPane = new JScrollPane(textArea);
			panel.add(scrollPane);
			
			String title = "";
			if (x==y){
				title = "Recent Tweets Containing Keyword: " + keywords.get(x);
			}
			
			else {
				title = "Recent Tweets Containing Keywords: " + keywords.get(x) + " and " + keywords.get(y);
			}		
			title = title + " (" + numTweets + " matches)";
			frame.setTitle(title);			
			frame.add(panel);
			frame.pack();
			frame.setVisible(true);
						
		}
	}

	
	
	
}
