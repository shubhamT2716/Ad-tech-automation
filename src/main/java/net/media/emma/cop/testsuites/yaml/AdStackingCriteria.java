package net.media.emma.cop.testsuites.yaml;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdStackingCriteria {

	private  Map<String, String> networkCallPositive;

	private Map<String, Map<String, Map<String , String >>> positiveCheck; 
	
	private Map<String, Map<String, Map<String , String >>> negativeCheck; 
	
	
//	public class PositiveCheck{
//		
//		private 
//		
//	}
//	
	
	
}
