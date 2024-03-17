package gesser.gals.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.generator.parser.ll.LLParser;
import gesser.gals.generator.parser.ll.NotLLException;
import gesser.gals.util.IntList;
import gesser.gals.util.ProductionList;

public class RecursiveDescendent
{
	private Grammar grammar;
	private int[][] llTable;
	private String[] symbols;
	
	private Map functions = new HashMap();
	
	public RecursiveDescendent(Grammar grammar) throws NotLLException
	{
		this.grammar = grammar;
		llTable = new LLParser(grammar).generateTable();
		symbols = grammar.getSymbols();
		for (int i=0; i<symbols.length; i++)
			if (symbols[i].charAt(0) == '<')
				symbols[i] = symbols[i].substring(1, symbols[i].length()-1);
				
		build();
	}
	
	public String getSymbols(int s)
	{
		return symbols[s];
	}
	
	public String getStart()
	{
		return symbols[grammar.getStartSymbol()];
	}
	
	public Map build()
	{
		ProductionList prods = grammar.getProductions();
		
		for (int i=0; i<llTable.length; i++)
		{
			int t = i+grammar.FIRST_NON_TERMINAL;
			
			Function f = new Function(t);			
			functions.put(symbols[t], f);		
			
			for (int j=0; j<llTable[0].length; j++)
			{
				int prod = llTable[i][j];
				if (prod >= 0)
				{
					int n = j+1;
					Production p = prods.getProd(prod);
					IntList rhs = p.get_rhs();
					f.input.put(new Integer(n), rhs);					
				}
			}
		}
		
		return functions;
	}
	
	public static class Function
	{
		public Map input = new TreeMap();
		public int lhs;
		
		public Function(int lhs)
		{
			this.lhs = lhs; 		
		}
	}
}
