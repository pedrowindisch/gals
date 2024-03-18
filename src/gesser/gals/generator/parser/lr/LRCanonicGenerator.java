
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

/**
 * @author Gesser
 */
public class LRCanonicGenerator extends LRGenerator
{
	public LRCanonicGenerator(Grammar g)
	{
		super(g);
	}
		
	protected List closure(List items)
	{
		boolean added = false;
		do
start:	{
			added = false;
			for (Iterator it = items.iterator(); it.hasNext(); )
			{
				LRItem item = (LRItem) it.next();
				Production p = item.getProduction();
				if (item.getPosition() < p.get_rhs().size())
				{				
					int B = p.get_rhs().get(item.getPosition());
					if (g.isNonTerminal(B))
					{
						BitSet prods = g.productionsFor(B);
						for (BitSetIterator bsi = new BitSetIterator(prods); bsi.hasNext(); )
						{
							Production p2 = g.getProductions().getProd(bsi.nextInt());
							IntList tmp = new IntList();
							for (int i=item.getPosition()+1; i<p.get_rhs().size(); i++)
								tmp.add(p.get_rhs().get(i));
							tmp.add(item.getLookahead());
							BitSet first = g.first(tmp);
							for (BitSetIterator bsi2 = new BitSetIterator(first); bsi2.hasNext(); )
							{
								int b = bsi2.nextInt();
								LRItem ni = new LRItem(p2, 0, b);
								if (! items.contains(ni))
								{
									items.add(ni);
									added = true;
									break start;
								}
							}					
						}
					}
				}
			}
		}
		while (added);
		
		return items;
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
					result.add(new LRItem(item.getProduction(), item.getPosition()+1, item.getLookahead()));
				}
			}
		}
		
		return closure(result);
	}

	protected List computeItems()
	{
		List s = new ArrayList();
		BitSet sp = g.productionsFor(g.getStartSymbol());
		int f = new BitSetIterator(sp).nextInt();
		s.add(new LRItem(g.getProductions().getProd(f), 0, Grammar.DOLLAR));
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
	
	/* (non-Javadoc)
	 * @see gesser.gals.generator.parser.lr.LRGenerator#buildTable()
	 */
	public Command[][] buildTable()
	{
//		Command[][] result = new Command[itemList.size()][g.getSymbols().length-1];
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
						result[i][s-1].add(Command.createShift(itemList.indexOf(next)));
					else //nonTerminal
						result[i][s-1].add(Command.createGoTo(itemList.indexOf(next)));
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
						int a = item.getLookahead();
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
		
		return resolveConflicts(result);
	}
}
