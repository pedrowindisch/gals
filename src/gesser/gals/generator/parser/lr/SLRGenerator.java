package gesser.gals.generator.parser.lr;

import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.util.BitSetIterator;
import gesser.gals.util.IntList;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SLRGenerator extends LRGenerator
{   
	public SLRGenerator(Grammar g)
	{	
		super(g);
	}
	
	protected List closure(List items)
    {
    	List result = new ArrayList();
    	result.addAll(items);
    	
    	for (int i=0; i<result.size(); i++)
    	{
    		LRItem it = (LRItem) result.get(i);
    		
    		Production p = it.getProduction();
    		if (it.getPosition() < p.get_rhs().size())
    		{
	    		int s = p.get_rhs().get(it.getPosition());
	    		if (g.isNonTerminal(s))
	    		{
	    			BitSet bs = g.productionsFor(s);
	    			for (BitSetIterator iter = new BitSetIterator(bs); iter.hasNext(); )
	    			{
		    			LRItem n = new LRItem(g.getProductions().getProd(iter.nextInt()), 0);
		    			if ( ! result.contains(n) )
		    				result.add(n);
	    			}
	    		}
    		}
    	}
    	
    	return result;
    }
    
	protected List goTo(List items, int s)
    {
    	List result = new ArrayList();
    	
    	for (Iterator i = items.iterator(); i.hasNext(); )
		{
			LRItem item = (LRItem)i.next();
			Production p = item.getProduction();
			
			if (item.getPosition() < p.get_rhs().size())
			{
				int symb = p.get_rhs().get(item.getPosition());
				
				if (symb == s)
				{
					result.add(new LRItem(item.getProduction(), item.getPosition()+1));
				}
			}
		}
		
		return closure(result);
    }
    
    /**
     * Calcula os itens LR
     * @return List
     */
    
	protected List computeItems()
    {
    	
    	List s = new ArrayList();
    	BitSet sp = g.productionsFor(g.getStartSymbol());
    	int f = new BitSetIterator(sp).nextInt();
    	s.add(new LRItem(g.getProductions().getProd(f), 0));
    	List c = new ArrayList();
    	c.add(closure(s));
    	
    	boolean repeat = true;
    	    	    	
    	while (repeat)
    	{
    		start:
			{
	    		repeat = false;
	    			    		
	    		for (Iterator it=c.iterator(); it.hasNext(); )
	    		{
	    			List items = (List) it.next();
	    			
	    			for (int i=0; i<items.size(); i++)
	    			{
	    				LRItem m = (LRItem) items.get(i);
	    				
	    				Production p = m.getProduction();
	    				if (p.get_rhs().size() > m.getPosition())
	    				{
	    					List gt = goTo(items, p.get_rhs().get(m.getPosition()));
		    				if (gt.size() != 0 && ! c.contains(gt))
		    				{
		    					c.add(gt);
		    					repeat = true;
		    					break start;
		    				}
	    				}
	    			}
	    		}
			}
    	}
    	return c;
    }
    
    /**
     * Cria a tabale de parse SLR
     * 
     * */
    public Command[][] buildTable()
    {
    	//Command[][] result = new Command[itemList.size()][g.getSymbols().length-1];
		Set[][] result = new Set[itemList.size()][g.getSymbols().length-1];
    	
    	for (int i=0; i<result.length; i++)
    	{
    		for (int j=0; j<result[i].length; j++)
    		{
    			result[i][j] = new HashSet();
    		}
    	}
    	
    	for (int i=0; i<result.length; i++)
    	{
    		List items = (List) itemList.get(i);
    		
    		for (int j=0; j<items.size(); j++)
    		{
    			LRItem item = (LRItem) items.get(j);
    			
    			Production p = item.getProduction();
    			IntList rhs = p.get_rhs();
    			
    			if (rhs.size() > item.getPosition())
    			{
    				int s = rhs.get(item.getPosition());
    				List next = goTo(items, s);
    				
    				if (g.isTerminal(s))        
    				{
    					result[i][s-1].add(Command.createShift(itemList.indexOf(next)));
    				}
    				else //nonTerminal
    				{
    					result[i][s-1].add(Command.createGoTo(itemList.indexOf(next)));
    				}
    			}
    			else
    			{
    				int lhs = p.get_lhs();
    				
    				if (lhs == g.getStartSymbol())
    				{
    					result[i][0].add(Command.createAccept());
    				}
    				else
    				{
	    				BitSet follow = g.followSet[lhs];
	    				for (BitSetIterator iter=new BitSetIterator(follow); iter.hasNext(); )
	    				{
	    					int a = iter.nextInt();
	    					Command cmd;
	    					if (lhs < semanticStart)
	    						cmd = Command.createReduce(g.getProductions().indexOf(p));
	    					else
	    						cmd = Command.createAction(lhs-semanticStart);
	    						
	    					result[i][a-1].add(cmd);
	    				}
    				}
    			}
    		}
    	}
    	
    	return resolveConflicts(result);
    }
}
