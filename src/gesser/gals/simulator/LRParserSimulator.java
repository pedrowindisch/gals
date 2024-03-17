package gesser.gals.simulator;

import java.util.List;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.analyser.LexicalError;
import gesser.gals.analyser.SemanticError;
import gesser.gals.analyser.SyntaticError;
import gesser.gals.analyser.Token;
import gesser.gals.generator.parser.lr.Command;
import gesser.gals.generator.parser.lr.LRGenerator;
import gesser.gals.util.ProductionList;


public class LRParserSimulator
{
	private Stack stack = new Stack();
	
	private BasicScanner scanner;
	private Token currentToken = null;
	private Token previousToken = null;
	
	private Command[][] table;
	private int[][] productions;
	private int semanticStart;
	
	private String[] symbols;
	private Stack nodeStack = new Stack();
	
	private List errors;
	
	public static final int DOLLAR = 1;
	
	public LRParserSimulator(LRGenerator parser)
	{
		table = parser.buildTable();
		semanticStart = parser.getFirstSemanticAction();
		ProductionList pl = parser.getGrammar().getProductions();
		productions = new int[pl.size()][2];
		
		symbols = parser.getGrammar().getSymbols();
		
		for (int i=0; i<pl.size(); i++)
		{
			productions[i][0] = pl.getProd(i).get_lhs();
			productions[i][1] = pl.getProd(i).get_rhs().size();
		}
		
		errors = parser.getErrors(table);
	}
	
	public void parse(BasicScanner scanner, DefaultMutableTreeNode root) throws SemanticError, SyntaticError, SyntaticError, LexicalError
	{
		this.scanner = scanner;

		nodeStack.clear();

		stack.clear();
		stack.push(new Integer(0));
		
		currentToken = scanner.nextToken();
		
		try
		{
			while ( ! step() ) 
				; //faz nada
			root.add((MutableTreeNode)nodeStack.pop());
		}
		catch(AnalysisError e)
		{
			for (int i=0; i<nodeStack.size(); i++)
				root.add((MutableTreeNode)nodeStack.get(i));
			root.add(new DefaultMutableTreeNode(e.getMessage()));
			
			e.printStackTrace();
		}			
	}
	
	private boolean step() throws SyntaticError, SemanticError, LexicalError
	{
		int state = ((Integer)stack.peek()).intValue();
		
		if (currentToken == null)
		{
			int pos = 0;
			if (previousToken != null)
				pos = previousToken.getPosition()+previousToken.getLexeme().length();

			currentToken = new Token(DOLLAR, "$", pos);
		}
		
    	int token = currentToken.getId();
		
		Command cmd = table[state][token-1];
		
		switch (cmd.getType())
		{
			case Command.SHIFT:
				stack.push(new Integer(cmd.getParameter()));
				
				nodeStack.push(new DefaultMutableTreeNode(symbols[currentToken.getId()]));
				
				previousToken = currentToken;
				currentToken = scanner.nextToken();
				return false;
				
			case Command.REDUCE:				
				int[] prod = productions[cmd.getParameter()];
				
				Stack tmp = new Stack();
				for (int i=0; i<prod[1]; i++)
				{
					stack.pop();
					tmp.push(nodeStack.pop());					
				}
				int oldState = ((Integer)stack.peek()).intValue();
				stack.push(new Integer(table[oldState][prod[0]-1].getParameter()));
				
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(symbols[prod[0]]);				
				while (!tmp.isEmpty())
				{				
					node.add((MutableTreeNode)tmp.pop());
				}
				nodeStack.push(node);
				return false;
				
			case Command.ACTION:
				int action = semanticStart + cmd.getParameter() - 1;
				stack.push(new Integer(table[state][action].getParameter()));
				nodeStack.push(new DefaultMutableTreeNode("#"+cmd.getParameter()));
				//semanticAnalyser.executeAction(cmd.getParameter(), previousToken);
				return false;
			/*	
			case Command.GOTO:
				break;
			*/	
			case Command.ACCEPT:
				return true;
				
			case Command.ERROR:
				throw new SyntaticError("Era esperado: "+errors.get(state), currentToken.getPosition());
		}
		return false;
	}
}