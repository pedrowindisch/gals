package gesser.gals.util;

import gesser.gals.generator.parser.Production;
import java.util.ArrayList;

public class ProductionList extends ArrayList
{	
	public Production getProd(int index)
	{
		return (Production) super.get(index);
	}

	public void add(Production[] p)
	{
		for (int i=0; i<p.length; i++)
			add(p[i]);
	}
	public void add(ProductionList p)
	{
		for (int i=0; i<p.size(); i++)
			add(p.get(i));
	}
}
