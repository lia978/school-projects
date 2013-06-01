package edu.cmu.cs.cs214.hw5;

import java.util.Date;

public enum TimeOfDay {
	MORNING, 
	AFTERNOON, 
	EVENING, 
	NIGHT;
	
	@SuppressWarnings("deprecation")
	public static TimeOfDay getTimeOfDay(Date d){
		/*
		 * morning : [5 am, 12 pm)
		 * afternoon  : [12 pm, 5 pm)
		 * evening : [5 pm, 8 pm)
		 * night : [8 pm, 5 am)
		 * 
		 * for example : 11:59:59 am is morning but 12:00:00 pm is afternoon
		 */
		int hours = d.getHours();
		
		if (hours >= 5 && hours < 12){
			return MORNING;
		}
		else if (hours >= 12 && hours < 17){
			return AFTERNOON;
		}
		else if (hours >= 17 && hours < 20){
			return EVENING;
		}
		else{
			assert(hours >= 20 || hours < 5);
			return NIGHT;
		}
	}
}