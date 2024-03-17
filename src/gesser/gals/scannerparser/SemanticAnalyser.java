package gesser.gals.scannerparser;

import java.util.Stack;

import gesser.gals.analyser.SemanticError;
import gesser.gals.analyser.Token;
import gesser.gals.util.BitSetIterator;

public class SemanticAnalyser implements Constants
{
	private Stack exp_simp1 = new Stack();
	private Stack exp_simp2 = new Stack();
	private Stack termo1 = new Stack();
	private Stack termo2 = new Stack();
	private Stack fator = new Stack();
	
	private FiniteAutomataGenerator gen;
	
	private Token token;
	
	public SemanticAnalyser(FiniteAutomataGenerator gen)
	{
		this.gen = gen;
	}
	
    public void executeAction(int action, Token currentToken)	throws SemanticError
    {
        token = currentToken;
        try
        {
        	actions[action].run();
        }
        catch(RuntimeException e)
        {
        	if (e.getCause() instanceof SemanticError)
        		throw (SemanticError) e.getCause();
        }
    }	
    
    public Node getRoot()
    {
    	return (Node) exp_simp1.pop();
    }
    
    private final Runnable[] actions = 
	{
		null,
		new Runnable(){public void run(){ action1(); }},
		new Runnable(){public void run(){ action2(); }},
		new Runnable(){public void run(){ action3(); }},
		new Runnable(){public void run(){ action4(); }},
		new Runnable(){public void run(){ action5(); }},
		new Runnable(){public void run(){ action6(); }},
		new Runnable(){public void run(){ action7(); }},
		new Runnable(){public void run(){ action8(); }},
		new Runnable(){public void run(){ action9(); }},
		new Runnable(){public void run(){ action10(); }},
		new Runnable(){public void run(){ action11(); }},
		new Runnable(){public void run(){ action12(); }},
		new Runnable(){public void run(){ action13(); }},
		new Runnable(){public void run(){ action14(); }},
		new Runnable(){public void run(){ action15(); }}
	};
    
    private void action1()
    {
    	exp_simp1.push( termo1.pop() );
    }
    
    private void action2()
    {
		Node n1 = (Node) exp_simp1.pop();
		Node n2 = (Node) termo1.pop();

		exp_simp1.push( Node.createUnionNode(n1, n2) );
    }
    
    private void action3()
    {
		Node n2 = (Node) exp_simp1.pop();
		Node n1 = (Node) exp_simp1.pop();
		
	
		exp_simp1.push( Node.createContextNode(n1, n2) );  
    }
    
    private void action4()
    {
		termo1.push( fator.pop() );
    }
    
    private void action5()
    {
		Node n1 = (Node) termo1.pop();
		Node n2 = (Node) fator.pop();
	
		termo1.push( Node.createConcatNode(n1, n2) );    	
    }
    
    private void action6()
    {
		Node n = (Node) fator.pop();
    	    	
		fator.push( Node.createClosureNode(n) );    	
    }
    
    private void action7()
    {
		Node n = (Node) fator.pop();
    	    	
		fator.push( Node.createClosureObNode(n) );    	
    }
    
    private void action8()
    {
		Node n = (Node) fator.pop();
    	    	
		fator.push( Node.createOptionalNode(n) );
    }
    
    private void action9()
    {    	
    	fator.push( exp_simp1.pop() );
    }
    
    private void action10()
    {
		fator.push( Node.createAllNode() );
    }
    
    private void action11()
    {
		Node def = gen.getDefinition(token.getLexeme());
		if (def == null)
			throw new RuntimeException(new SemanticError("Definição não declarada: "+token.getLexeme(), token.getPosition()));
	
		fator.push( def.clone() );
    }
    
	private void action12()
    {
		fator.push( Node.createCharNode( token.getLexeme().charAt(0) ) );
    }
    
    private void action13()
    {
		Node n = (Node) fator.pop();		
		fator.push( Node.createComplementNode(n) );
    }
    
    private void action14()
    {
		Node n2 = (Node) fator.pop();
		Node n1 = (Node) fator.pop();
    	
		fator.push( Node.createUnionNode(n1, n2) );
    }
    
    private void action15()
    {
    	Node n1 = (Node) fator.pop();
    	Node n2 = Node.createCharNode( token.getLexeme().charAt(0));


		char c1 = (char) new BitSetIterator(n1.alphabet).nextInt();
		char c2 = (char) new BitSetIterator(n2.alphabet).nextInt();
		
		if (c1 >= c2)
			throw new RuntimeException(new SemanticError("Intervalo inválido", token.getPosition()));
			
    	fator.push( Node.createIntervalNode( c1, c2) );
    }
}

