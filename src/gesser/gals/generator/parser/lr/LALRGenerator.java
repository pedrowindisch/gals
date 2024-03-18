package gesser.gals.generator.parser.lr;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import gesser.gals.generator.parser.Grammar;

public class LALRGenerator extends LRCanonicGenerator
{
	private boolean compress;
	
	public LALRGenerator(Grammar g)
	{
		super(g);
	}
	
	private Set core(List state)
	{
		Set result = new TreeSet();
		
		for (int i=0; i<state.size(); i++)
		{
			LRItem item = (LRItem) state.get(i);
			LRItem x = new LRItem(item.getProduction(), item.getPosition());
			
			if (! result.contains(x))
				result.add(x);
		}
		return result;
	}
	
	protected List computeItems()
	{
		List items = super.computeItems();
		
		for (int i=0; i<items.size(); i++)
		{
			List state = (List) items.get(i);
			Set core = core(state);
			
			for (int j=i+1; j<items.size(); j++)
			{
				List state2 = (List) items.get(j);
				Set core2 = core(state2);
				
				if (core.equals(core2))
				{
					for (int k=0; k<state2.size(); k++)
					{
						LRItem item = (LRItem) state2.get(k);
						if (!state.contains(item))
							state.add(item);
					}
					items.remove(j);
					j--;
				}
			}
		}
		
		this.compress = true;
		return items;
	}
	
	protected List goTo(List items, int s)
	{
		List x = super.goTo(items, s);
		
		if (compress)
		{
			Set core = core(x);
			
			for (int i=0; i<itemList.size(); i++)
			{
				List state = (List) itemList.get(i);
				if (core.equals(core(state)))
					return state;
			}
			//se n achar... n deve acontecer
		}
		return x;
	}
}
