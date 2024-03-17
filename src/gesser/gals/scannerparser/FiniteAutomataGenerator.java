package gesser.gals.scannerparser;

import gesser.gals.analyser.SemanticError;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.scanner.*;
import gesser.gals.util.BitSetIterator;

import java.util.*;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Gesser
 */

public class FiniteAutomataGenerator implements Constants
{
	private Map definitions = new HashMap();
	private Map expressions = new HashMap();
	private Map specialCases = new HashMap();
	private Node root = null;
	private BitSet alphabet = new BitSet();
	private int lastPosition = -1;
	private List tokenList = new ArrayList();
	private boolean sensitive = true; 
	
	private int contextCount = 0;
	
	public FiniteAutomataGenerator()
	{		
		sensitive = OptionsDialog.getInstance().getOptions().scannerCaseSensitive;
	}
	
	private BitSet[] next;
	private Node[] nodes;
	
	public void addDefinition(String id, Node root) throws SemanticError
	{
		if (definitions.containsKey(id))
			throw new SemanticError("Definição repetida: "+id);
		
		definitions.put(id, root);
		
		alphabet.or(root.getAlphabet());
	}
	
	public Node getDefinition(String id)
	{
		return (Node) definitions.get(id);
	}	
	
	public void addExpression(String id, Node root, boolean backtrack) throws SemanticError
	{
		/*
		if (tokenList.contains(id))
			throw new SemanticError("Token '"+id+"' já definido");
		*/	
		alphabet.or(root.getAlphabet());
		
		if (!tokenList.contains(id))
			tokenList.add(id);
		
		int pos = tokenList.indexOf(id);
		
		Node end = Node.createEndNode(pos+2, backtrack);
		root = Node.createConcatNode(root, end);
		
		Node ctx = root.getLeft().getRight();
		if (ctx != null)
		{
			ctx = ctx.deepestLeft();
			if (ctx != null && ctx.getContext() >= 0)
			{
				contextCount++;
				ctx.setContext(contextCount);
				end.setContext(contextCount);				
			}
		}
				
		expressions.put(id, root);
		
				
		if (this.root == null)
			this.root = root;
		else
		{
			this.root = Node.createUnionNode(this.root, root);
		}
	}
	
	public void addIgnore(Node root, boolean backtrack)
	{
		alphabet.or(root.getAlphabet());
	
		Node end = Node.createEndNode(0, backtrack);
		root = Node.createConcatNode(root, end);
					
		if (this.root == null)
			this.root = root;
		else
		{
			this.root = Node.createUnionNode(this.root, root);
		}
	}
	
	public void addSpecialCase(String id, String base, String value) throws SemanticError
	{			
		if (! sensitive)
			value = value.toUpperCase();
			
		if (!expressions.containsKey(base))
			throw new SemanticError("Token '"+base+"' não definido");
			
		int b = tokenList.indexOf(base)+2;
		
		if (tokenList.contains(id))
			throw new SemanticError("Token '"+id+"' já definido");
		
		Integer i = new Integer(tokenList.size()+2);
		
		Map s = (Map) specialCases.get(new Integer(b));
		
		if (s == null)
		{
			s = new TreeMap();
			specialCases.put(new Integer(b), s);
		}
		else if (s.get(value) != null)
			throw new SemanticError("Já houve a definição de um caso especial de '"+base+"' com o valor\""+value+"\"");
			
		s.put(value, i);
		
		tokenList.add(id);
	}
	
	public FiniteAutomata generateAutomata() throws SemanticError
	{
		List states = new ArrayList();
		Map context = new TreeMap();
		Map ctxMap = new TreeMap();
		Map trans = new TreeMap();
		Map finals = new TreeMap();
		Map back = new TreeMap();
		
		if (root == null)
			throw new SemanticError("A Especificação Léxica deve conter a definição de pelo menos um Token");
		
		computeNext();
		
		states.add(root.metaData.first);
		for (int i=0; i< states.size(); i++)
		{
			BitSet T = (BitSet) states.get(i);
			for (BitSetIterator it = new BitSetIterator(alphabet); it.hasNext(); )
			{
				char c = (char)it.nextInt();
				
				BitSet U = new BitSet();

				for (BitSetIterator it2 = new BitSetIterator(T); it2.hasNext(); )
				{
					int p = it2.nextInt();
					Node n = nodes[p];					
					if (n.getEnd() >= 0)					
					{
						Integer in = new Integer(i);
						if (!finals.containsKey(in))
						{
							finals.put(in, new Integer(n.getEnd()));
							back.put(in, new Boolean(n.doBackTrack()));
							
							if (n.getContext() > 0)
							{
								if (! context.containsKey(in))
									context.put(in, ctxMap.get(new Integer(n.getContext())));
							}
						}						
					}
					if (n.getContext() >= 0)
					{
						if (! ctxMap.containsKey(new Integer(n.getContext())))
							ctxMap.put(new Integer(n.getContext()), new Integer(i));
					}
						
					if (n.getAlphabet().get(c))
						U.or(next[p]);
					
				}
				
				int pos = -1;
				if (! U.isEmpty() )
				{ 
					pos = states.indexOf(U);
					if (pos == -1)
					{
						states.add(U);
						pos = states.size()-1;
					}						
				}		
				Integer I = new Integer(i);
				if (! trans.containsKey(I))
					trans.put(I, new TreeMap());
				if (pos != -1)
					((Map)trans.get ( I )).put(new Character(c), new Integer(pos));			
			}			
		}
		
		return makeAtomata(states, trans, finals, back, context);
				
	}

	public FiniteAutomata makeAtomata(List states, Map trans, Map finals, Map back, Map context)
		throws SemanticError
	{
		Map[] transitions = new Map[states.size()];
		int count = 0;
		for (Iterator it = trans.values().iterator(); it.hasNext(); )
		{
			transitions[count] = (Map) it.next();
		
			count++;
		}
		
		int[] fin = new int[states.size()];
		for (int i=0; i<fin.length; i++)
		{
			Integer expr = (Integer) finals.get(new Integer(i));
			if (expr != null)
				fin[i] = expr.intValue();
			else
				fin[i] = -1;							
		}
		
		for (int i=0; i<fin.length; i++)
		{			
			Boolean b = (Boolean) back.get(new Integer(i));
			if (b != null && b.booleanValue() == false)
			{
				BitSet pre = computPrecedersOf(i, transitions);
				for (BitSetIterator iter = new BitSetIterator(pre); iter.hasNext(); )
				{
					int state = iter.nextInt();
					if (fin[state] <0)
						fin[state] = -2;
				}
			}							
		}
				
		List scList = new ArrayList();
		int[][] scIndexes = new int[tokenList.size()+2][];
		for (int i=0; i<scIndexes.length; i++)
		{
			Map m = (Map) specialCases.get(new Integer(i));
			int start = scList.size();
			if (m != null)
			{
				for (Iterator it = m.keySet().iterator(); it.hasNext(); )
				{
					String k = (String) it.next();
					Integer v = (Integer) m.get(k);
					
					scList.add(new FiniteAutomata.KeyValuePar(k, v.intValue()));
				}
			}
			int end = scList.size();
			scIndexes[i] = new int[]{start, end};
		}
		FiniteAutomata.KeyValuePar[] sc = new FiniteAutomata.KeyValuePar[scList.size()];
		System.arraycopy(scList.toArray(), 0, sc, 0, sc.length);
		int[][] cont = new int[states.size()][2];
		for (int i=0; i<cont.length; i++)
		{
			cont[i][0] = 0;
			cont[i][1] = -1;
		}
		for (Iterator i=context.entrySet().iterator(); i.hasNext(); )
		{
			Map.Entry entry = (Entry) i.next();
			Integer key = (Integer) entry.getKey();
			Integer value = (Integer) entry.getValue();
			
			cont[value.intValue()][0] = 1;
			cont[key.intValue()][1] = value.intValue();
			
		}
		
		return  new FiniteAutomata(alphabet, transitions, fin, scIndexes, sc, cont, tokenList);		
	}
	
	private BitSet computPrecedersOf(int state, Map[] transitions)
	{		
		BitSet result = new BitSet();	
		result.set(state);
		
		boolean contin;
		do
		{
			contin = false;
			for (BitSetIterator bsi = new BitSetIterator(result); bsi.hasNext();  )
gathering:	{
				Integer st = new Integer(bsi.nextInt());
				for (int i=0; i<transitions.length; i++)
				{
					for (Iterator iter = transitions[i].entrySet().iterator(); iter.hasNext(); )
					{
						Integer next = (Integer) ((Map.Entry) iter.next()).getValue();
						if (result.get(next.intValue()) && next.equals(st))
						{
							if (! result.get(i))
							{
								result.set(i); 
								contin = true;
								break gathering;
							}						
						}
					}
				}
			}
		}
		while (contin);
		
		return result;
	}

	public void computeNext()
	{
		computeMetaData(root);
		
		next = new BitSet[lastPosition+1];
		nodes = new Node[lastPosition+1];
				
		for (int i=0; i<next.length; i++)
		{
			next[i] = new BitSet();
		}
		
		computeNext(root);
	}
	
	private void computeMetaData(Node root)
	{					
		if (root.getLeft() != null)
			computeMetaData(root.getLeft());
			
		if (root.getRight() != null)
			computeMetaData(root.getRight());
			
		Node.MetaData n = root.metaData;
		Node l = root.getLeft();
		Node r = root.getRight();
		
		switch (root.getId())
		{
			case CHAR:
				lastPosition++;	
				
				n.position = lastPosition;
				n.nullable = false;
				n.first.set(lastPosition);
				n.last.set(lastPosition);				
				break;
			
			case OPTIONAL:
			case CLOSURE:				
				n.nullable = true;
				n.first.or(l.metaData.first);
				n.last.or(l.metaData.last);
				break;
				
			case CLOSURE_OB:				
				n.nullable = false;
				n.first.or(l.metaData.first);
				n.last.or(l.metaData.last);
				break;
			
			case UNION:				
				n.nullable = l.metaData.nullable || r.metaData.nullable;
				
				n.first.or(l.metaData.first);
				n.first.or(r.metaData.first);
				
				n.last.or(l.metaData.last);
				n.last.or(r.metaData.last);
				break;
				
			case -1://concat
				n.nullable = l.metaData.nullable && r.metaData.nullable;
		
				n.first.or(l.metaData.first);
				if (l.metaData.nullable)
					n.first.or(r.metaData.first);
				
				n.last.or(r.metaData.last);
				if (r.metaData.nullable)
					n.last.or(l.metaData.last);
				break;
		}
	}
	
	private void computeNext(Node root)
	{	
		switch (root.getId())
		{
			case -1: //concat
				for (BitSetIterator it = new BitSetIterator(root.getLeft().metaData.last); it.hasNext(); )
				{
					int i = it.nextInt();
					next[i].or(root.getRight().metaData.first);
				}
				break;
			case CLOSURE:
			case CLOSURE_OB:
				for (BitSetIterator it = new BitSetIterator(root.getLeft().metaData.last); it.hasNext(); )
				{
					int i = it.nextInt();
					next[i].or(root.getLeft().metaData.first);
				}
				break;
			case CHAR:
				nodes[root.metaData.position] = root;
				break;
		}
		
		if (root.getLeft() != null)
			computeNext(root.getLeft());
			
		if (root.getRight() != null)
			computeNext(root.getRight());
	}

	
	public String toString()
	{
		StringBuffer result = new StringBuffer();
		result.append(root);
		
		return result.toString();
	}
}
