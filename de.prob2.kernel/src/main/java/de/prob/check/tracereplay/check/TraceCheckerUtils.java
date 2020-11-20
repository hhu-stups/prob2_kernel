package de.prob.check.tracereplay.check;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TraceCheckerUtils {

	public static <T, U> Map<T, U> zip(List<T> list1, List<U> list2){
		if(list1.size() == list2.size()){
			Map<T, U> sideResult = new HashMap<>();
			for(int i = 0;  i < list1.size(); i++){
				sideResult.put(list1.get(i), list2.get(i));
			}
			return sideResult;
		}
		return Collections.emptyMap();
	}
}
