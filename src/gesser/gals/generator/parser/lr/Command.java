package gesser.gals.generator.parser.lr;

public class Command
{
	private int parameter;
	private int type;
	
	protected Command(int type, int parameter)
	{
		this.type = type;
		this.parameter = parameter;
	}
	
	public static final int SHIFT  = 0;
	public static final int REDUCE = 1;
	public static final int ACTION = 2;
	public static final int ACCEPT = 3;
	public static final int GOTO   = 4;
	public static final int ERROR  = 5;
	
	public static final String[] CONSTANTS = 
	{
		"SHIFT ",
		"REDUCE",
		"ACTION",
		"ACCEPT",
		"GO_TO ",
		"ERROR "
	};
	
	public int getType()
	{
		return type;
	}
	
	public int getParameter()
	{
		return parameter;
	}
	
	public static Command createShift(int state)
	{
		return new Command(SHIFT, state);
	}
	
	public static Command createReduce(int production)
	{
		return new Command(REDUCE, production);
	}
	
	public static Command createAction(int production)
	{
		return new Command(ACTION, production);
	}
	
	public static Command createAccept()
	{
		return new Command(ACCEPT, 0);
	}
	
	public static Command createGoTo(int state)
	{
		return new Command(GOTO, state);
	}
	
	public static Command createError()
	{
		return new Command(ERROR, 0);
	}
	
	public String toString()
	{
		switch (type)
		{
			case SHIFT: return "SHIFT("+parameter+")";
			case REDUCE: return "REDUCE("+parameter+")";
			case ACTION: return "SEM.ACT("+parameter+")";
			case ACCEPT: return "ACCEPT";
			case GOTO: return ""+parameter;
			case ERROR: return "-";
			default: return "???";
		}
	}
	
	public boolean equals(Object obj)
	{
		try
		{
			Command other = (Command) obj;
			
			return 
				type == other.type &&
				parameter == other.parameter;
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}
	
	public int hashCode()
	{
		int result = 43;
		result = result*parameter + 17;
		result = result*type + 17;
		return result;
	}
}
