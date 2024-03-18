package gesser.gals.editor;

import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.Token;
import gesser.gals.parserparser.Constants;
import gesser.gals.parserparser.Scanner;
import java.awt.Color;
import javax.swing.text.*;

/**
 * @author Gesser
 */

public class NTDocument extends SyntaxDocument implements Constants
{	
	protected static final SimpleAttributeSet NON_TERMINAL = new SimpleAttributeSet(NORMAL);
	
	static
	{
		StyleConstants.setBackground(NON_TERMINAL, Color.WHITE);
		StyleConstants.setForeground(NON_TERMINAL, Color.BLACK);
		StyleConstants.setBold(NON_TERMINAL, true);
		StyleConstants.setItalic(NON_TERMINAL, false);		
	}
	
	protected Scanner scanner = new Scanner();
	
	protected void apply(int startOffset, int endOffset, String input) throws BadLocationException
	{
		if (startOffset >= endOffset)
			return;
		
		scanner.setInput( input );
		scanner.setRange(startOffset, endOffset);
		scanner.setReturnComents(true);
		
		int oldPos = startOffset;
		Token t=null;
		boolean done = false;
		
		while (!done)
		{
			int pos;
			
			try
			{				
				done = true;
				t = scanner.nextToken();
				
				while (t != null)
				{
					pos = t.getPosition();
					int length = t.getLexeme().length();
					
					SimpleAttributeSet att = NORMAL;
					
					switch (t.getId())
					{						
						case NON_TERM: 
							att = NON_TERMINAL;
							break;
						case -1: 
							att = COMMENT;
							break;
						default:
							throw new LexicalError("Não terminal inválido", pos);
					}
					setCharacterAttributes(oldPos, pos-oldPos, NORMAL, true);
					setCharacterAttributes(pos, length, att, true);
					oldPos = pos+length;
					
					t = scanner.nextToken();
				}			
			}
			catch (LexicalError e)
			{
				//modo panico
				pos = e.getPosition();	
				setCharacterAttributes(oldPos, pos-oldPos, NORMAL, true);					
				oldPos = pos;
				
				int length = 0;
				for (int i=e.getPosition(); i < input.length() && " \t\n\r".indexOf(input.charAt(i))==-1; i++)
					length++;
				
				setCharacterAttributes(pos, length, ERROR, true);
				oldPos = pos + length;
				
				scanner.setPosition(e.getPosition() + length);
				done = false;		
			}
		}
		setCharacterAttributes(oldPos, endOffset-oldPos, NORMAL, true);
	}
}
