package gesser.gals.generator.scanner;

import gesser.gals.HTMLDialog;
import gesser.gals.analyser.SemanticError;
import gesser.gals.simulator.FiniteAutomataSimulator;
import gesser.gals.util.BitSetIterator;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class FiniteAutomata
{
	public static class KeyValuePar
	{
		public String key;
		public int value;
		
		public KeyValuePar(String key, int value)
		{
			this.key = key;
			this.value = value;
		}
		
		public String toString()
		{
			return "['"+key+"'->"+value+"]";
		}
	}

	private Map[] transitions;
	private int[] finals;
	private int[][] context;
	private BitSet alphabet;
	private List tokenNames;
	private String[] errors;
	private boolean hasContext = false;
	
	private int[][] specialCasesIndexes;
	private KeyValuePar[] specialCases;
	
	public Map[] getTransitions()
	{
		return transitions;
	}
	
	public List getTokens()
	{
		return tokenNames;
	}
	
	public KeyValuePar[] getSpecialCases()
	{
		return specialCases;
	}
	
	public int[][] getSpecialCasesIndexes()
	{
		return specialCasesIndexes;
	}
	
	public String getError(int state)
	{
		return errors[state];
	}
	
	public boolean isContext(int state)
	{
		return context[state][0] == 1;
	}
	
	public int getOrigin(int state)
	{
		return context[state][1];
	}
	
	public boolean hasContext()
	{
		return hasContext;
	}
	
	public FiniteAutomata(
		BitSet alphabet, Map[] transitions, 
		int[] finals, int[][] specialCasesIndexes, 
		KeyValuePar[] specialCases, int[][] context, List tokenNames) throws SemanticError
	{
		this.alphabet = alphabet;
		this.transitions = transitions;
		this.finals = finals;
		this.context = context;	
		this.specialCasesIndexes = specialCasesIndexes;
		this.specialCases = specialCases;
		this.tokenNames = tokenNames;
		
		for (int i=0; i<context.length; i++)
		{
			if (context[i][0] == 1)
			{
				hasContext = true;
				break;
			}
		}
		
		buildErrors();
		
		checkSpecialCases();
	}
	
	
	private int[] buildContext(boolean[] ctxt)
	{
		int [] result = new int[ctxt.length];
		
		for (int i=0; i<ctxt.length; i++)
		{
			result[i] = -1;
		}
		
		for (int i=0; i<result.length; i++)
		{
			if (ctxt[i])
			{
				BitSet states = finalStatesFromState(i);
				for (BitSetIterator it=new BitSetIterator(states); it.hasNext(); )
				{
					result[it.nextInt()] = i;
				}
			}
		}
		
		return result;
	}

	private void checkSpecialCases() throws SemanticError
	{
		FiniteAutomataSimulator sim = new FiniteAutomataSimulator(this);
		for (int i = 0; i < specialCasesIndexes.length; i++)
		{
			int[] index = specialCasesIndexes[i];
			for (int j=index[0]; j<index[1]; j++)
			{
				if (sim.analyse(specialCases[j].key) != i)
					throw new SemanticError("O valor \""+specialCases[j].key+
						"\" não é válido como caso especial de '"+tokenNames.get(i-2)+
                       "', na definição de '"+tokenNames.get(specialCases[j].value-2)+"'" );
			}
		}
	}
	
	public int nextState(char c, int state)
	{
		Integer in = (Integer) transitions[state].get(new Character(c));
		if (in == null)
			return -1;
		else
			return in.intValue();
	}
	
	public int tokenForState(int state)
	{
		if (state < 0 || state >= finals.length)
			return -1;
			
		return finals[state];
	}
	
	public String toString()
	{
		int max = String.valueOf(transitions.length).length();
		
		StringBuffer bfr = new StringBuffer();

		for (int i=0; i< max*2 + 1; i++)		
			bfr.append(' ');
		bfr.append('|');
		
		for (BitSetIterator i = new BitSetIterator(alphabet); i.hasNext(); )
		{
			char c = (char) i.nextInt();
			for (int j = 0; j<max/2; j++)
				bfr.append(' ');
			bfr.append(c);
			for (int j = max/2+1; j < max; j++)
				bfr.append(' ');
			bfr.append('|');
		}
		bfr.append('\n');
		
		for (int it = 0; it < transitions.length; it++ )
		{
			String f = "";
			if (finals[it] >= 0)
				f = String.valueOf(finals[it]) + '*';
			
			for (int i=0; i<max+1-f.length(); i++)
				bfr.append(' ');
			bfr.append(f);
				
			String s = String.valueOf(it);
			for (int i=0; i<max-s.length(); i++)
				bfr.append(' ');
			bfr.append(s);
			bfr.append('|');
			
			Map x = transitions[it];
			for (BitSetIterator i = new BitSetIterator(alphabet); i.hasNext(); )
			{
				Integer integ = (Integer)x.get(new Character((char)i.nextInt()));
				String str = "";
				if (integ.intValue() >= 0)
					str = integ.toString();
					
				for (int j = 0; j<max-str.length(); j++)
					bfr.append(' ');
				bfr.append(str);
				bfr.append('|');
			}
			bfr.append('\n');
		}

		return bfr.toString();
	}	
	
	public String asHTML()
	{
		StringBuffer result = new StringBuffer();

		result.append(
		"<HTML>"+
		"<HEAD>"+
		"<TITLE> Tabela de Transições </TITLE>"+
		"</HEAD>"+
		"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
		"<TABLE border=1 cellspacing=0>");
		
		result.append(
                "<TR align=center>"+
                "<TD rowspan=\"2\" bgcolor=black><FONT color=white><B>ESTADO</B></FONT></TD>"+
                "<TD rowspan=\"2\" bgcolor=black><FONT color=white><B>TOKEN<BR>RETORNADO</B></FONT></TD>"+
                "<TD colspan=\""+alphabet.cardinality()+"\" bgcolor=black><FONT color=white><B>ENTRADA</B></FONT></TD>"+
				"</TR>"+
				"<TR align=center>");
			
		for (BitSetIterator i = new BitSetIterator(alphabet); i.hasNext(); )
		{
			char c = (char)i.nextInt();
			result.append("<TD bgcolor=#99FF66 nowrap><B>"+getChar(c)+"</B></TD>");
		}
		result.append("</TR>");
                      
		for (int it = 0; it < transitions.length; it++ )
		{
			result.append("<TR align=center>"+
						  "<TD bgcolor=#99FF66><B>"+it+"</B></TD>");
			int t = finals[it];
			String clr = /*context[it] ? "#9999FF" : */null;
			
			if (t > 0)
			{
				if (clr == null)
					clr = "#FFFFCC";
					
				String caption = HTMLDialog.translateString((String)tokenNames.get(t-2));
				if (getOrigin(it) >= 0)
					caption += " / "+getOrigin(it);
				result.append("<TD bgcolor="+clr+" nowrap>"+caption+"</TD>");
			}
			else if (t == 0)
			{
				if (clr == null)
					clr = "#99CCFF";
				result.append("<TD bgcolor="+clr+"><B>:</B></TD>");
			}
			else if (t == -2)
				result.append("<TD bgcolor=#FF0000>?</TD>");
			else
			{
				if (clr == null)
					clr = "#FFCC99";
				result.append("<TD bgcolor="+clr+">?</TD>");
			}
				
			Map x = transitions[it];
			for (BitSetIterator i = new BitSetIterator(alphabet); i.hasNext(); )
			{
				result.append("<TD width=40 bgcolor=#F5F5F5>");
				Integer integ = (Integer)x.get(new Character((char)i.nextInt()));
				
				if (integ != null && integ.intValue() >= 0)
					result.append(integ);
				else
					result.append("-");
					
				result.append("</TD>");
			}
			result.append("</TR>");
		}
		
		result.append(
		"</TABLE>"+
		"</FONT></BODY>"+
		"</HTML>"+		"");
				
		return result.toString();
	}
	
	private String getChar(char c)
	{
		switch (c)
		{
			case '\n' : return "\\n";
			case '\r' : return "\\r";
			case '\t' : return "\\t";
			case ' ' : return "' '";
			
			case '"' : return "&quot;";
			case '&' : return "&amp;";
			case '<' : return "&lt;";
			case '>' : return "&gt;";
			
			default: 
				if ( (c>=32 && c <= 126) || (c>=161 && c<=255))
					return ""+c;
				else
					return ""+(int)c;
		}
	}
	
	private BitSet finalStatesFromState(int state)
	{
		BitSet visited = new BitSet();
		visited.set(state);

		boolean changed = true;

		loop: while (changed)
		{			
			changed = false;
			for (BitSetIterator it=new BitSetIterator(visited); it.hasNext(); )
			{
				int st = it.nextInt();
				for (BitSetIterator it2=new BitSetIterator(alphabet); it2.hasNext(); )
				{
					char c = (char)it2.nextInt();
					int next = nextState(c, st);
					if (next != -1 && !visited.get(next))
					{
						visited.set(next);
						changed = true;
						continue loop;
					}
				}
			}
		}

		BitSet result = new BitSet();
		
		for (BitSetIterator it=new BitSetIterator(visited); it.hasNext(); )
		{
			int i = it.nextInt();
			int token = tokenForState(i);
			if (token >= 0)
				result.set(i);
		}
		
		return result;
	}
	
	private BitSet tokensFromState(int state)
	{
		BitSet visited = finalStatesFromState(state);
		
		BitSet result = new BitSet();
		
		for (BitSetIterator it=new BitSetIterator(visited); it.hasNext(); )
		{
			int token = tokenForState(it.nextInt());
			if (token >= 0)
				result.set(token);
		}
		
		return result;
	}
	
	private void buildErrors()
	{
		errors = new String[transitions.length];
		/*
		if (tokenForState(0) >= 0)
			errors[0] = "";
		else*/
			errors[0] = "Caractere não esperado";
		
		for (int i = 1; i < transitions.length; i++ )
		{
			if (tokenForState(i) >= 0)
				errors[i] = "";
			else
			{				
				BitSet tokens = tokensFromState(i);
				StringBuffer bfr = new StringBuffer("Erro identificando ");
				for (BitSetIterator it=new BitSetIterator(tokens); it.hasNext(); )
				{
					int t = it.nextInt();
					if (t  > 0)
						bfr.append(tokenNames.get(t-2));
					else
						bfr.append("<ignorar>");
					bfr.append(" ou ");
				}
				bfr.setLength(bfr.length()-4);
				errors[i] = bfr.toString();
			}
		}
	}	
}
