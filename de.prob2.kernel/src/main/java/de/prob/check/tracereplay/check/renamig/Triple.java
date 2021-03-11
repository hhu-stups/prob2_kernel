package de.prob.check.tracereplay.check.renamig;

public class Triple<U,V,W> {


	final U first;
	final V second;
	final W third;

	public Triple(U first, V second, W third){
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public U getFirst() {
		return first;
	}

	public V getSecond() {
		return second;
	}

	public W getThird() {
		return third;
	}
}
