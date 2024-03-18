package gesser.gals.editor;

import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.Token;
import gesser.gals.generator.parser.Grammar;

import java.awt.Color;

import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author Gesser
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BNFDocument extends NTDocument
{	
	private static final SimpleAttributeSet ACTION_SEM = new SimpleAttributeSet(NORMAL);
	private static final SimpleAttributeSet EPSILON = new SimpleAttributeSet(NORMAL);
	
	static
	{	
		StyleConstants.setBackground(ACTION_SEM, Color.WHITE);
		StyleConstants.setForeground(ACTION_SEM, new Color(0, 128, 0));
		StyleConstants.setBold(ACTION_SEM, false);
				
		StyleConstants.setBackground(EPSILON, Color.WHITE);
		StyleConstants.setForeground(EPSILON, Color.MAGENTA);
		StyleConstants.setBold(EPSILON, true);				
	}
	
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
						case PIPE:
						case SEMICOLON:
						case DERIVES:
							att = OPERATOR;						
							break;
						case TERM:
							if (t.getLexeme().charAt(0) == '"')
								att = STRING;
							else if (t.getLexeme().equals(Grammar.EPSILON_STR))
								att = EPSILON;
							else
								att = NORMAL;						
							break;
						case NON_TERM: 
							att = NON_TERMINAL;
							break;
						case -1: 
							att = COMMENT;
							break;
						case ACTION: 
							att = ACTION_SEM;
							length++;
							break;
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
