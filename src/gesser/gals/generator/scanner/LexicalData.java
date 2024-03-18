package gesser.gals.generator.scanner;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.SemanticError;
import gesser.gals.scannerparser.FiniteAutomataGenerator;
import gesser.gals.scannerparser.Node;
import gesser.gals.scannerparser.REParser;
import gesser.gals.util.MetaException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LexicalData
{
	public static class SpecialCaseValue
	{
		private String lexeme;
		private String base;
		
		public SpecialCaseValue(String lexeme, String base)
		{
			this.lexeme = lexeme;
			this.base = base;
		}
		
		public String getLexeme() { return lexeme; }
		public String getBase() { return base; }
	}	
	
	private Map expressionFor = new HashMap();
	private Map specialCasesValues = new HashMap();
	
	private List definitions = new ArrayList();	
	private List tokens = new ArrayList();
	private List specialCases = new ArrayList();
	private String ignore = "";
		
	public void addDefinition(String token, String expression)
	{
		definitions.add(token);
		expressionFor.put(token, expression);
	}
	
	public void addToken(String token, String expression)
	{
		tokens.add(token);
		expressionFor.put(token, expression);
	}
	
	public void clear()
	{
		definitions.clear();
		tokens.clear();
		specialCases.clear();
		expressionFor.clear();
		specialCasesValues.clear();		
	}
	
	public String expressionFor(String token)
	{
		return (String) expressionFor.get(token);
	}
	
	public List getTokens()
	{
		return tokens;
	}
	
	public List getDefinitions()
	{
		return definitions;
	}

	public List getSpecialCases()
	{
		return specialCases;
	}

	public String getIgnore()
	{
		return ignore;
	}

	public void addIgnore(String ignore)
	{
		if (this.ignore.length() > 0)
			this.ignore = this.ignore+"|"+ignore;
		else
			this.ignore = ignore;
	}

	public void addSpecialCase(String name, String value, String base)
	{		
		specialCases.add(name);
		specialCasesValues.put(name, new SpecialCaseValue(value, base));
	}
	
	public SpecialCaseValue getSpecialCase(String name) throws SemanticError
	{		
		return (SpecialCaseValue) specialCasesValues.get(name);
	}

	public FiniteAutomata getFA() throws MetaException
    {
    	REParser parser = new REParser();
		FiniteAutomataGenerator gen = new FiniteAutomataGenerator();
		
		int i = -1;
		try
		{	
			for (i=0; i<definitions.size(); i++)
			{						
				Node n = parser.parse(expressionFor((String)definitions.get(i)), gen);
				
				gen.addDefinition((String) definitions.get(i), n);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(MetaException.DEFINITION, i, ee);
		}
			
		try
		{
			for (i=0; i<tokens.size(); i++)
			{						
				Node n = parser.parse(expressionFor((String)tokens.get(i)), gen);
				
				gen.addExpression((String)tokens.get(i), n, true);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(MetaException.TOKEN, i, ee);
		}
		
		try
		{
			for (i=0; i<specialCases.size(); i++)
			{					
				String t = 	(String) specialCases.get(i);
				SpecialCaseValue v = (SpecialCaseValue) specialCasesValues.get(t);
				
				gen.addSpecialCase(t, v.base, v.lexeme);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(MetaException.TOKEN, i, ee);
		}
		
		try
		{						
			if (ignore.length() > 0)
			{
				Node n = parser.parse(ignore, gen);
				
				gen.addIgnore(n, true);
			}
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(MetaException.TOKEN, tokens.size(), ee);
		}
		
		try
		{
			return gen.generateAutomata();
		}
		catch (AnalysisError ee)
		{
			throw new MetaException(MetaException.TOKEN, tokens.size(), ee);
		}
    }
}
