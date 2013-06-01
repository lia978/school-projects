package edu.cmu.cs.cs214.hw5;

public enum PhraseLocation {
	BEGINSWITH,
	ENDSWITH,
	ISPRESENT,
	NOTPRESENT;
	
	public static PhraseLocation getPhraseLocation(String queryString, String inputString){
		/*
		 * Example : InputString = "I like this weather!"
		 * 			 
		 * 			 queryString = "I like"
		 * 			 output : BEGINSWITH
		 * 			
		 * 			 queryString = "this"
		 * 			 output : ISPRESENT
		 * 			
		 * 			 queryString = "weather!"
		 * 			 output : ENDSWITH 
		 * 
		 * 			 queryString = "Pittsburgh"
		 * 			 output : NOTPRESENT
		 */
		
		if (inputString.contains(queryString)){
			int startIndex = inputString.indexOf(queryString);
			if (startIndex == 0){
				return BEGINSWITH;
			}else if ((startIndex + queryString.length()) == inputString.length()){
				return ENDSWITH;
			}else{
				return ISPRESENT;
			}
		}else{
			return NOTPRESENT;
		}
		
	}
}
