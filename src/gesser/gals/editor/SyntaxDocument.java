package gesser.gals.editor;

import java.awt.Color;
import java.awt.Font;

import javax.swing.event.UndoableEditListener;
import javax.swing.text.*;

public abstract class SyntaxDocument extends DefaultStyledDocument
{
	public static final Font FONT = new Font("Lucida Console",Font.PLAIN, 13);
	
	protected static final SimpleAttributeSet NORMAL = new SimpleAttributeSet();
	static
	{
		StyleConstants.setForeground(NORMAL, Color.BLACK);
		StyleConstants.setBackground(NORMAL, Color.WHITE);
		StyleConstants.setFontFamily(NORMAL, FONT.getFamily());
		StyleConstants.setFontSize(NORMAL, FONT.getSize());
		StyleConstants.setBold(NORMAL, false);
		StyleConstants.setItalic(NORMAL, false);
	}
	
	protected static final SimpleAttributeSet STRING = new SimpleAttributeSet(NORMAL);
	protected static final SimpleAttributeSet OPERATOR = new SimpleAttributeSet(NORMAL);
	protected static final SimpleAttributeSet REG_EXP = new SimpleAttributeSet(NORMAL);
	protected static final SimpleAttributeSet ERROR = new SimpleAttributeSet(NORMAL);
	protected static final SimpleAttributeSet COMMENT = new SimpleAttributeSet(NORMAL);

	
	private static void initAttributes()
	{	
		StyleConstants.setBackground(REG_EXP, Color.WHITE);
		StyleConstants.setForeground(REG_EXP, new Color(0, 128, 0));
		StyleConstants.setBold(REG_EXP, false);
		StyleConstants.setItalic(REG_EXP, false);
		
		StyleConstants.setBackground(STRING, Color.WHITE);
		StyleConstants.setForeground(STRING, Color.RED);
		StyleConstants.setBold(STRING, false);
		StyleConstants.setItalic(STRING, false);
		
		StyleConstants.setBackground(OPERATOR, Color.WHITE);
		StyleConstants.setForeground(OPERATOR, new Color(0, 0, 128));
		StyleConstants.setBold(OPERATOR, false);
		StyleConstants.setItalic(OPERATOR, false);
		/*
		StyleConstants.setBackground(ERROR, new Color(255, 32, 32));
		StyleConstants.setForeground(ERROR, Color.WHITE);
		StyleConstants.setBold(ERROR, true);
		StyleConstants.setItalic(ERROR, false);
		*/
		StyleConstants.setBackground(COMMENT, Color.WHITE);
		StyleConstants.setForeground(COMMENT, Color.DARK_GRAY);
		StyleConstants.setBold(COMMENT, false);
		StyleConstants.setItalic(COMMENT, true);
	}
	static  {initAttributes(); }
	
	protected abstract void apply(int startOffset, int endOffset, String input) throws BadLocationException;
	
	public void insertString(int offset, String str, AttributeSet a) throws BadLocationException 
	{	
		super.insertString(offset, str, a);
		
		int start = offset;
		int end = start+str.length();
		
		int length = getLength();		
		
		String text = getText(0, length);
		
		start--;
		while (start >= 0 && text.charAt(start) != '\n')
			start--;
		start++;
		
		while (end < length && text.charAt(end) != '\n')
			end++;

		refresh(start, end, text);
	}
	
	public void remove(int offset, int length) throws BadLocationException
	{
		super.remove(offset, length);
		
		int start = offset;
		int end = start;
		
		length = getLength();

		String text = getText(0, length);

		start--;
		while (start >= 0 && text.charAt(start) != '\n')
			start--;
		start++;

		while (end < length && text.charAt(end) != '\n')
			end++;

		refresh(start, end, text);
	}

	private void refresh(int start, int end, String text)
		throws BadLocationException
	{
		UndoableEditListener[] listeners = getUndoableEditListeners();
		for (int i=0; i<listeners.length; i++)
		{
			removeUndoableEditListener(listeners[i]);
		}
		apply(start, end, text);
		for (int i=0; i<listeners.length; i++)
		{
			addUndoableEditListener(listeners[i]);
		}
	}
}
