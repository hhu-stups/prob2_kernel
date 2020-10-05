package de.prob.check.tracereplay.check;

import de.prob.statespace.OperationInfo;

import java.util.List;

public class CheckerUtils {


	/**
	 * Return true if two list contain equal elements
	 * @param a the first list
	 * @param b the second list
	 * @return the result of the comparison
	 */
	public static boolean listComparator(List<String> a, List<String> b){
		if(a.size() == b.size()){
			for(int i = 0; i < a.size(); i++){
				if(!a.get(i).equals(b.get(i))){
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * compares if two OperationInfos are the same expect for their name
	 * @param old the operationInfo form the file
	 * @param currentObject the current operationInfo
	 * @return true if equal
	 */
	public static boolean equals(OperationInfo old, OperationInfo currentObject){
		boolean equalNonDetWrittenVariables = listComparator(old.getNonDetWrittenVariables(), currentObject.getNonDetWrittenVariables());
		boolean outputParameterNames = listComparator(old.getOutputParameterNames(), currentObject.getOutputParameterNames());
		boolean parameterNames = listComparator(old.getParameterNames(), currentObject.getParameterNames());
		boolean writtenVariables = listComparator(old.getWrittenVariables(), currentObject.getWrittenVariables());
		boolean readVariables = listComparator(old.getReadVariables(), currentObject.getReadVariables());

		return  equalNonDetWrittenVariables && outputParameterNames && parameterNames && writtenVariables && readVariables;
	}


}
