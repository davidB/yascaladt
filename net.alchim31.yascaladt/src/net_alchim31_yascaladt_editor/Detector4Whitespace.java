package net_alchim31_yascaladt_editor;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class Detector4Whitespace implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
