package de.prob.animator.domainobjects;

/**
 * Provides constants for the names of the most commonly used dot output formats.
 * These are all of the common layout engines listed in the dot(1) man page of Graphviz 2.40.1.
 */
public final class DotOutputFormat {
	public static final String DOT = "dot";
	public static final String XDOT = "xdot";
	public static final String POSTSCRIPT = "ps";
	public static final String PDF = "pdf";
	public static final String SVG = "svg";
	public static final String SVGZ = "svgz";
	public static final String XFIG = "fig";
	public static final String PNG = "png";
	public static final String GIF = "gif";
	public static final String JPEG = "jpeg";
	public static final String XDOT_JSON = "json";
	public static final String HTTPD_IMAGEMAP = "imap";
	public static final String HTML_IMAGEMAP = "cmapx";
	
	private DotOutputFormat() {
		throw new AssertionError("Utility class");
	}
}
