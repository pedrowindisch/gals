package gesser.gals.scannerparser;

import gesser.gals.util.BitSetIterator;
import java.util.BitSet;

/**
 * @author Gesser
 *
 */

public class Node implements Constants, Cloneable
{
	private Node left;
	private Node right;
	private int  id;
	private String value;
	private boolean backtrack = true;
	private int context = -1;
	private int end = -1;
	
	protected BitSet alphabet = new BitSet();
	
	public MetaData metaData = new MetaData();
		
	public Node getLeft()
	{
		return left;
	}
	
	public Node getRight()
	{
		return right;
	}
	
	public int getId()
	{
		return id;
	}
	
	public int getEnd()
	{
		return end;
	}
	
	public void setEnd(int i)
	{
		end = i;
	}
	
	public boolean doBackTrack()
	{
		return backtrack;
	}	
	
	public int getContext()
	{
		return context;
	}	
	
	public void setContext(int ctx)
	{
		context = ctx;
	}
	
	public String toString()
	{
		return toString(0);
	}
			
	private String toString(int level)
	{
		StringBuffer bfr = new StringBuffer();
		for (int i=0; i<level-2; i++)
			bfr.append(' ');

		if (level >= 2)		
			bfr.append("\\-");

		bfr.append(value).append('\n');
		
		if (left != null)
			bfr.append(left.toString(level+2));
			
		if (right != null)
			bfr.append(right.toString(level+2));
			
		return bfr.toString();
	}

	public BitSet getAlphabet()
	{
		return alphabet;
	}
	
	public Node deepestLeft()
	{
		Node n = this;
		
		while (true)
		{
			Node n2 = n.left;
			if (n2 == null)
				n2 = n.right;
			
			if (n2 == null)
				break;
			else
				n = n2;
		}
		
		return n;
	}
	
	private Node(int id, Node left, Node right)
	{
		this.id = id;
		this.left = left;
		this.right = right;
		
		if (left != null)
			alphabet.or(left.alphabet);
			
		if (right != null)
			alphabet.or(right.alphabet);
	}
	
	private Node(int id, Node child)
	{
		this(id, child, null);
	}
	
	private Node(int id)
	{
		this(id, null, null);
	}
	
	public static Node createUnionNode(Node n1, Node n2)
	{
		Node n = new Node(UNION, n1, n2);
		
		n.value = "|";
						
		return n;
	}
	
	public static Node createConcatNode(Node n1, Node n2)
	{
		Node n = new Node(-1, n1, n2);
		
		n.value = "&";
				
		return n;
	}
	
	public static Node createContextNode(Node n1, Node n2)
	{/*
		Node x = new Node(CHAR);
		x.value = ""+(char)0;
		x.context = true;
		x.alphabet.set(0);
		
		Node nc2 = new Node(-1, x, n2);
		nc2.value = "&";
		
		Node nc1 = new Node(-1, n1, nc2);
		nc1.value = "&";
			
		return nc1;
		*/
		Node x = n2.deepestLeft();
		x.context = 0;
		
		Node n = new Node(-1, n1, n2);
		n.value = "&";
		
		return n;
	}

	public static Node createClosureNode(Node n)
	{
		Node nn = new Node(CLOSURE, n);

		nn.value = "*";
		
		return nn;
	}
	
	public static Node createClosureObNode(Node n)
	{
		Node nn = new Node(CLOSURE_OB, n);
		
		nn.value = "+";
		
		return nn;
	}
	
	public static Node createOptionalNode(Node n)
	{
		Node nn = new Node(OPTIONAL, n);
		
		nn.value = "?";
		
		return nn;
	}
	
	public static Node createIntervalNode(char c1, char c2)
	{
		Node n = new Node(CHAR);
		
		for (char i=c1; i<=c2; i++)
			n.alphabet.set(i);		
		
		StringBuffer bfr = new StringBuffer("[");
		for (BitSetIterator i=new BitSetIterator(n.alphabet); i.hasNext(); )
			bfr.append( (char)i.nextInt() );
		bfr.append("]");
			
		n.value = bfr.toString();
		
		return n;
	}
	
	public static Node createComplementNode(Node n)
	{
		Node nn = new Node(CHAR);
			
		nn.alphabet.set('\t', !n.alphabet.get('\t'));	
		nn.alphabet.set('\n', !n.alphabet.get('\n'));
		nn.alphabet.set('\r', !n.alphabet.get('\r'));
		nn.alphabet.set(' ',  !n.alphabet.get(' '));
    	
		for (char c= 32; c <= 126; c++)
			if (!n.alphabet.get(c))
				nn.alphabet.set(c);
				
		for (char c= 161; c <= 255; c++)
			if (!n.alphabet.get(c))
				nn.alphabet.set(c);	
		
		StringBuffer bfr = new StringBuffer("[");
		for (BitSetIterator i=new BitSetIterator(nn.alphabet); i.hasNext(); )
			bfr.append( (char)i.nextInt() );
		bfr.append("]");
			
		nn.value = bfr.toString();
		
		return nn;
	}
	
	public static Node createCharNode(char c)
	{
		Node nn = new Node(CHAR);
		nn.value = ""+c;
		nn.alphabet.set(c);
		
		return nn;
	}
	
	public static Node createAllNode()
	{
		Node n = new Node(CHAR);
		
		n.alphabet.set('\t');
		//n.alphabet.set('\n');
		//n.alphabet.set('\r');
    	
		for (char c= 32; c <= 126; c++)
			n.alphabet.set(c);
			
		for (char c= 161; c <= 255; c++)
			n.alphabet.set(c);
		
		StringBuffer bfr = new StringBuffer("[");
		for (BitSetIterator i=new BitSetIterator(n.alphabet); i.hasNext(); )
			bfr.append( (char)i.nextInt() );
		bfr.append("]");
			
		n.value = bfr.toString();
		
		return n;
	}
	
	public static Node createEndNode(int tokenId, boolean back)
	{
		Node nn = new Node(CHAR);
		
		nn.end = tokenId;
		
		nn.backtrack = back;
		
		nn.value = "#"+nn.end;
				
		return nn;
	}
			
	/*
	public Object clone()
	{
		Node newMe = new Node(id);
		
		newMe.value = value;
		
		newMe.alphabet = (BitSet) alphabet.clone();
		
		if (left != null)
			newMe.left = (Node) left.clone();
			
		if (right != null)
			newMe.right = (Node) right.clone();
		
		return newMe;
	} 
	 
				
			/*/
	public Object clone()
	{	
		try
		{	
			Node newMe = (Node) super.clone();		
			
			newMe.alphabet = (BitSet) alphabet.clone();
			
			newMe.metaData = new MetaData();
			newMe.backtrack = true;
			newMe.context = -1;
			newMe.end = -1;
			
			if (left != null)
				newMe.left = (Node) left.clone();
				
			if (right != null)
				newMe.right = (Node) right.clone();
			
			return newMe;
		}
		catch (CloneNotSupportedException e) 
		{ 
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}
	
	public static class MetaData
	{
		public int position = -1;
		public boolean nullable = false;
		public BitSet first = new BitSet();
		public BitSet last = new BitSet();
	}
}
