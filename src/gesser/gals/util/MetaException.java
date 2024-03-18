package gesser.gals.util;

import gesser.gals.analyser.AnalysisError;

/**
 * @author Gesser
 */

public class MetaException extends Exception
{
	public static final int DEFINITION = 0;
	public static final int TOKEN = 1;
	public static final int NON_TERMINAL = 2;
	public static final int GRAMMAR = 3;
	
	private int mode;
	private int index;
	
	public MetaException(int mode, int index, AnalysisError cause)
	{
		super(cause);
		this.mode = mode;
		this.index = index;
	}
	
	public int getIndex()
	{
		return index;
	}

	public int getMode()
	{
		return mode;
	}

}
