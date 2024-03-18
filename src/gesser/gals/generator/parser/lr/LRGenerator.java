package gesser.gals.generator.parser.lr;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gesser.gals.HTMLDialog;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.ConflictSolver;
import gesser.gals.generator.parser.Production;

/**
 * @author Gesser
 */
public abstract class LRGenerator
{
	protected Grammar g;
	protected List itemList;
	protected int semanticStart;
	protected int firstSementicAction;

	public LRGenerator(Grammar g)
	{	
		semanticStart = g.FIRST_SEMANTIC_ACTION();
		firstSementicAction = g.FIRST_SEMANTIC_ACTION();// g.SEMANTIC_ACTION_COUNT;
	
		this.g = g.asNormalLR();
	
		itemList = computeItems();
	}

	

	public List getErrors(Command[][] table)
	{
		List result = new ArrayList();
		
		for (int state=0; state<table.length; state++)
		{
			BitSet bs = new BitSet();
			for (int j = 1; j<g.FIRST_NON_TERMINAL; j++)
			{
				if (table[state][j-1].getType() != Command.ERROR)
					bs.set(j);
			}
			StringBuffer bfr = new StringBuffer();
			int total = bs.cardinality();
			for (int i = bs.nextSetBit(0), count = 0; i>=0; i = bs.nextSetBit(i+1), ++count)
			{
				if (i == 1)//DOLAR
					bfr.append("fim de sentença");
				else
					bfr.append(g.getSymbols()[i]);
		
				if (total - count == 2)
					bfr.append(" ou ");
				else if (total - count > 2)
				bfr.append(", ");
			}
			result.add(bfr.toString());
		}
	
	/*
		for (Iterator iter = itemList.iterator(); iter.hasNext();)
		{
			List items = (List) iter.next();
		
			BitSet first = new BitSet();			
			for (Iterator i = items.iterator(); i.hasNext(); )
			{
				LRItem item = (LRItem)i.next();
				Production p = item.getProduction();
			
				first.or(g.first(p.get_rhs(), item.getPosition()));
				if (first.get(0))
				{
					first.clear(0);
					if (item.getLookahead() != 0)
						first.set(item.getLookahead());
					else
						first.or(g.followSet[p.get_lhs()]);
				}
				
			}
			
			StringBuffer bfr = new StringBuffer();
			int total = first.cardinality();
			for (int i = first.nextSetBit(0), count = 0; i>=0; i = first.nextSetBit(i+1), ++count)
			{
				if (i == 1)//DOLAR
					bfr.append("fim de sentença");
				else
					bfr.append(g.getSymbols()[i]);
					
				if (total - count == 2)
					bfr.append(" ou ");
				else if (total - count > 2)
				bfr.append(", ");
			}
			result.add(bfr.toString());
		}*/
	
		return result;
	}
		
	public Grammar getGrammar()
	{
		return g;
	}

	public int getFirstSemanticAction()
	{
		return firstSementicAction;
	}
	
	protected abstract List closure(List items);
	protected abstract List goTo(List items, int s);
	protected abstract List computeItems();
	public abstract Command[][] buildTable();
	
	public int[][][] buildIntTable()
	{
		Command[][] commands = buildTable();
	
		int[][][] result = new int[commands.length][commands[0].length][2];
	
		for (int i=0; i<result.length; i++)
			for (int j=0; j<result[i].length; j++)
			{
				result[i][j][0] = commands[i][j].getType();
				result[i][j][1] = commands[i][j].getParameter();
			}
	 
		return result;
	}

	protected Command[][] resolveConflicts(Set[][] table)
	{
		Command[][] result = new Command[table.length][table[0].length];
	
		Command error = Command.createError();
		for (int i=0; i<result.length; i++)
		{
			for (int j=0; j<table[0].length; j++)
			{
				switch (table[i][j].size())
				{
					case 0:
						result[i][j] = error;
						break;
					case 1:
						result[i][j] = (Command) table[i][j].iterator().next();
						break;
					default:
						result[i][j] = solve(table[i][j], i, j);
						break;
				}
			}
		}
	
		return result;
	}

	private Command solve(Set set, int state, int input)
	{
		Command[] cmds = new Command[set.size()];
		int i=0;
		for (Iterator iter = set.iterator(); iter.hasNext();)
		{
			cmds[i] = (Command) iter.next();
			i++;
		}
	
		boolean equals = true;
		for (int j = 1; j < cmds.length; j++)
		{
			equals = equals && cmds[j-1].equals(cmds[j]);
			if (!equals)
				break;
		}
	
		if (equals)
			return cmds[0];
		else
		{
			return cmds[ConflictSolver.getInstance().resolve(g, cmds, state, input)];
		}
	}



	public String tableAsHTML()
	{
		StringBuffer result = new StringBuffer();
	
		result.append(
			"<HTML>"+
			"<HEAD>"+
			"<TITLE>Tabela SLR(1)</TITLE>"+
			"</HEAD>"+
			"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
			"<TABLE border=1 cellspacing=0>");

		Command[][] table = buildTable();
	
		result.append("<TR>");
		result.append("<TD  align=center rowspan=2 bgcolor=black nowrap><FONT color=white><B>ESTADO</B></FONT></TD>");
		result.append("<TD  align=center colspan="+(g.FIRST_NON_TERMINAL-1)+" bgcolor=black nowrap><FONT color=white><B>AÇÃO</B></FONT></TD>");
		result.append("<TD  align=center colspan="+(g.FIRST_SEMANTIC_ACTION()-g.FIRST_NON_TERMINAL)+" bgcolor=black nowrap><FONT color=white><B>DESVIO</B></FONT></TD>");
		result.append("</TR>");
	
		result.append("<TR>");
		//result.append("<TD  align=center bgcolor=black>&nbsp;</TD>");
		for (int i=0; i<table[0].length-1; i++)
		{					
			result.append("<TD  align=center bgcolor=black nowrap><FONT color=white><B>"+HTMLDialog.translateString(g.getSymbols()[i+1])+"</B></FONT></TD>");
		}
		result.append("</TR>");
	
		for (int i=0; i<table.length; i++)
		{
			Command[] line = table[i];
		
			result.append("<TR>");
		
			result.append("<TD bgcolor=black align=right nowrap><FONT color=white><B>"+i+"</B></FONT></TD>");
		
			for (int j=0; j<line.length-1; j++)
			{	
				Command cmd = line[j];
				String value = "";
			
				if (cmd!= null)
					value = cmd.toString();
			
				String color = j+1<g.FIRST_NON_TERMINAL?"#F5F5F5":"#E6E6E6";
				
				result.append("<TD bgcolor="+color+" align=center nowrap>"+value+"</TD>");
			}	
			result.append("</TR>");		
		}
		
		result.append(
			"</TABLE>"+
			"</FONT></BODY>"+
			"</HTML>");
		
		return result.toString();
	}

	public String itemsAsHTML()
	{
		StringBuffer result = new StringBuffer();
	
		result.append(
			"<HTML>"+
			"<HEAD>"+
			"<TITLE>Itens SLR(1)</TITLE>"+
			"</HEAD>"+
			"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
			"<TABLE border=1 cellspacing=0>");

		List l = itemList;
	
		result.append("<TR>");
		result.append("<TD  align=center bgcolor=black><FONT color=white><B>Estado</B></FONT></TD>");
		result.append("<TD  align=center bgcolor=black><FONT color=white><B>Itens</B></FONT></TD>");
		result.append("<TD  align=center bgcolor=black><FONT color=white><B>Desvio</B></FONT></TD>");
		result.append("</TR>");
	
		for (int i=0; i<l.size(); i++)
		{
			String color = i%2==0?"#F5F5F5":"#E6E6E6";
		
			List item = (List) l.get(i);
		
			result.append("<TR>");
			result.append("<TD bgcolor="+color+" align=right rowspan="+item.size()+">"+i+"</TD>");
			result.append("<TD bgcolor="+color+" nowrap>"+HTMLDialog.translateString(item.get(0).toString())+"</TD>");
		
			LRItem it = (LRItem)item.get(0);
			Production p = it.getProduction();
			if (p.get_rhs().size() > it.getPosition())
			{			
				int x = p.get_rhs().get(it.getPosition());
				List next = goTo(item, x);
				int pos = l.indexOf(next);
				result.append("<TD bgcolor="+color+" align=right>"+pos+"</TD>");
			}
			else
				result.append("<TD bgcolor="+color+" align=right>"+"&nbsp"+"</TD>");
			result.append("</TR>");
		
			for (int j=1; j<item.size(); j++)
			{
				result.append("<TR>");
				result.append("<TD bgcolor="+color+" nowrap>"+HTMLDialog.translateString(item.get(j).toString())+"</TD>");
			
				it = (LRItem)item.get(j);
				p = it.getProduction();
				if (p.get_rhs().size() > it.getPosition())
				{			
					int x = p.get_rhs().get(it.getPosition());
					List next = goTo(item, x);
					int pos = l.indexOf(next);
					result.append("<TD bgcolor="+color+" align=right>"+pos+"</TD>");
				}
				else
					result.append("<TD bgcolor="+color+" align=right>"+"&nbsp"+"</TD>");
				result.append("</TR>");
			}
		
			result.append("</TR>");
		}
		
	
		
		result.append(
			"</TABLE>"+
			"</FONT></BODY>"+
			"</HTML>");
		
		return result.toString();
	}
}
