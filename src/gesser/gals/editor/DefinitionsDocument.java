package gesser.gals.editor;

import gesser.gals.analyser.Token;
import gesser.gals.scannerparser.LineScanner;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;

/**
 * @author Gesser
 */

public class DefinitionsDocument extends SyntaxDocument
{
	LineScanner ls = new LineScanner();
	
	protected void apply(int startOffset, int endOffset, String input) throws BadLocationException
	{
		if (startOffset >= endOffset)
			return;
		
		ls.setText(input);
		ls.setRange(startOffset, endOffset);
		
		int oldPos = startOffset;
		int pos = startOffset;
		Token t=null;
		
		t = ls.nextToken();
				
		while (t != null)
		{
			pos = t.getPosition();
			int length = t.getLexeme().length();
			
			SimpleAttributeSet att = NORMAL;
			
			switch (t.getId())
			{						
				case LineScanner.COLON: 
					att = OPERATOR;
					break;
				case LineScanner.ID: 
					att = NORMAL;
					break;
				case LineScanner.STR: 
					att = STRING;
					break;
				case LineScanner.RE: 
					att = REG_EXP;
					break;
				case LineScanner.COMMENT: 
					att = COMMENT;
					break;
				case LineScanner.EQUALS: 
				case LineScanner.ERROR: 
					att = ERROR;
					break;
			}
			setCharacterAttributes(oldPos, pos-oldPos, NORMAL, true);
			setCharacterAttributes(pos, length, att, true);
			oldPos = pos+length;
			
			t = ls.nextToken();
		}			
			
		
		setCharacterAttributes(oldPos, endOffset-oldPos, NORMAL, true);
	}
}
