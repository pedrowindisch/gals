package gesser.gals.parserparser;

import java.util.*;

import gesser.gals.analyser.*;
import gesser.gals.generator.parser.Production;
import gesser.gals.util.IntList;

public class SemanticAnalyser implements Constants
{	
	private Map symbols;
	private int actionCount = 0;
	private int lhs;
	private IntList rhs = new IntList();
	private List productions = new Vector();
	
	public SemanticAnalyser(Map symbols)
	{
		this.symbols = symbols;
	}
	
	public List getPoductions()
	{
		return productions;
	}
	
	public void executeAction(int action, Token currentToken)
		throws SemanticError
	{
		token = currentToken;
		switch (action)
		{
			case 0:
				action0();
				break;
			case 1:
				action1();
				break;
			case 2:
				action2();
				break;
			case 3:
				action3();
				break;
			case 4:
				action4();
				break;
			case 5:
				action5();
				break;
		}
	}

	private Token token;

	private void action0()
	{
		lhs = ((Integer)symbols.get(token.getLexeme())).intValue();
	}
	
	private void action1()
	{
		Production p = new Production(lhs);
		for (int i=0; i< rhs.size(); i++)
			p.get_rhs().add(rhs.get(i));
			
		productions.add(p);
		
		rhs.clear();
	}
	
	private void action2()
	{
		int s = ((Integer)symbols.get(token.getLexeme())).intValue();
		
		if (s != EPSILON)		
			rhs.add(s);
	}
	
	private void action3()
	{		
		int action = Integer.parseInt(token.getLexeme());	
		
		rhs.add(symbols.size() + action + 1); //falta $ em symbols
	}
	
	private void action4() throws SemanticError
	{
		if (! symbols.containsKey(token.getLexeme()))
			throw new SemanticError("S�mbolo "+token.getLexeme()+" n�o declarado", token.getPosition());
	}
	
	private void action5()
	{
		int action = Integer.parseInt(token.getLexeme());
		
		if (actionCount < action)
			actionCount = action;
	}
}
