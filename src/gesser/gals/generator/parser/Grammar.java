package gesser.gals.generator.parser;

import gesser.gals.HTMLDialog;
import gesser.gals.util.BitSetIterator;
import gesser.gals.util.IntList;
import gesser.gals.util.ProductionList;
import java.util.*;

/**
 * A classe Grammar representa as Gramáticas Livres de Contexto, utilizadas
 * pelos análisadores sintáticos
 *
 * @author Carlos Eduardo Gesser
 */

public class Grammar implements Cloneable
{
    public static final int EPSILON = 0;
    public static final int DOLLAR = 1;
    public static final int FIRST_TERMINAL = EPSILON+2;
    
    public static final String EPSILON_STR = "î";

    protected String[] symbols;
    public int FIRST_NON_TERMINAL = 0;
    public int FIRST_SEMANTIC_ACTION() { return symbols.length; }
    public int LAST_SEMANTIC_ACTION() { return FIRST_SEMANTIC_ACTION()+SEMANTIC_ACTION_COUNT; }
    public int SEMANTIC_ACTION_COUNT = 0;
    protected int startSymbol;
    
    public BitSet[] firstSet;
    public BitSet[] followSet;
    
    private boolean normalLR = false;

    protected ProductionList productions = new ProductionList();
    
    /**
     * Contrói um objeto do tipo Grammar
     *
     * @param t símbolos terminais
     * @param n símbolos não terminais
     * @param p produções
     * @param startSymbol súimbolo inicial da gramática
     */
    public Grammar(String[] t, String[] n, ProductionList p, int startSymbol)
    {        
        setSymbols(t, n, startSymbol);
        setProductions(p);
        fillFirstSet();
        fillFollowSet();        
    }

	/**
     * Contrói um objeto do tipo Grammar
     *
     * @param t símbolos terminais
     * @param n símbolos não terminais
     * @param p produções
     * @param startSymbol súimbolo inicial da gramática
     */
	public Grammar(List t, List n, List p, int start)
	{
		String[] T = new String[t.size()];
    	System.arraycopy(t.toArray(), 0, T, 0, T.length);
    	String[] N = new String[n.size()];
    	System.arraycopy(n.toArray(), 0, N, 0, N.length);
		ProductionList P = new ProductionList();
		P.addAll(p);
		
		setSymbols(T, N, start);
        setProductions(P);
        fillFirstSet();
        fillFollowSet();
	}

    
    /**
     * Preenche os símbolos e inicializa arrays;
     *
     * @param t símbolos terminais
     * @param n símbolos não terminais
     */
    private void setSymbols(String[] t, String[] n, int startSymbol)
    {
        symbols = new String[t.length + n.length + 2];
        FIRST_NON_TERMINAL = t.length + 2;
        symbols[EPSILON] = EPSILON_STR;
        symbols[DOLLAR] = "$";
        for (int i = 0, j = FIRST_TERMINAL; i < t.length; i++, j++)
            symbols[j] = t[i];

        for (int i = 0, j = FIRST_NON_TERMINAL; i < n.length; i++, j++)
            symbols[j] = n[i];

        this.startSymbol = startSymbol;
    }

    /**
     * @param p produções
     */
    private void setProductions(ProductionList p)
    {
        productions.add(p);
        int max = 0;
        for (int i=0; i<productions.size(); i++)
        {
        	productions.getProd(i).setGrammar(this);
        	for (int j=0; j<productions.getProd(i).get_rhs().size(); j++)
        		if (productions.getProd(i).get_rhs().get(j) > max)
        			max = productions.getProd(i).get_rhs().get(j);
        }
        SEMANTIC_ACTION_COUNT = max - FIRST_SEMANTIC_ACTION();
    }

    /**
     * @return TRUE se x eh um símbolo terminal
     */
    public final boolean isTerminal(int x)
    {
        return x < FIRST_NON_TERMINAL;
    }

    /**
     * @return TRUE se x eh um símbolo não terminal
     */
    public final boolean isNonTerminal(int x)
    {
        return x >= FIRST_NON_TERMINAL && x < FIRST_SEMANTIC_ACTION();
    }
    
    public final boolean isSemanticAction(int x)
    {
        return x >= FIRST_SEMANTIC_ACTION();
    }

	public ProductionList getProductions()
	{
		return productions;
	}

	public String[] getSymbols()
	{
		return symbols;
	}
	
	public String[] getTerminals()
	{
		String[] terminals = new String[FIRST_NON_TERMINAL-2];
		System.arraycopy(symbols,2,terminals,0,terminals.length);
		return terminals;
	}

	public String[] getNonTerminals()
	{
		String[] nonTerminals = new String[FIRST_SEMANTIC_ACTION() - FIRST_NON_TERMINAL];
		System.arraycopy(symbols,FIRST_NON_TERMINAL,nonTerminals,0,nonTerminals.length);
		return nonTerminals;
	}
	
	public int getStartSymbol()
	{
		return startSymbol;
	}

	public Grammar asNormalLR()
	{
		if (normalLR)
			return this;
			
		String[] t = getTerminals();

		int newSymbols = 1+SEMANTIC_ACTION_COUNT+1;

		String[] nt_old = getNonTerminals();
		String[] nt = new String[nt_old.length+newSymbols];
		System.arraycopy(nt_old, 0, nt, 0, nt_old.length);

		ProductionList p = new ProductionList();
		p.add(getProductions());

		for (int i=0; i<SEMANTIC_ACTION_COUNT+1; i++)
		{
			nt[nt_old.length+i] = "<#"+i+">";
			p.add(new Production(null,FIRST_SEMANTIC_ACTION()+i, new int[]{ }));			
		}

		nt[nt.length-1] = "<-START->";                   
		p.add(new Production(null,FIRST_SEMANTIC_ACTION()+newSymbols-1, new int[]{ getStartSymbol() }));

		Grammar g = new Grammar(t, nt, p, FIRST_SEMANTIC_ACTION()+newSymbols-1);
		
		g.normalLR = true;
		
		return g;
	}

	/**
	 * Cria uma nova produção. Se a produção criada já existe na gramática,
	 * null é retornado.
	 * 
	 * @param lhs lado esquerdo da produção
	 * @param rhs lado direito da produção
	 * 
	 * @return produção gerada, ou null se esta já existir
	 * */
	public Production createProduction(int lhs, int[] rhs)
	{
		Production p = new Production(this, lhs, rhs);
		for (int i = 0; i < productions.size(); i++)
			if (productions.getProd(i).equals( p ))
				return null;
				
		return p;
	}
	
	/**
	 * Cria uma nova produção. Se a produção criada já existe na gramática,
	 * null é retornado.
	 * 
	 * @param lhs lado esquerdo da produção
	 * @param rhs lado direito da produção
	 * 
	 * @return produção gerada, ou null se esta já existir
	 * */
	public Production createProduction(int lhs, IntList rhs)
	{
		Production p = new Production(this, lhs, rhs);
		for (int i = 0; i < productions.size(); i++)
			if (productions.getProd(i).equals( p ))
				return null;
				
		return p;
	}
	
	public Production createProduction(int lhs)
	{
		return new Production(this, lhs, new IntList());
	}
	
	protected boolean isEpsilon(IntList x, int start)
	{
		for (int i=start; i<x.size(); i++)
			if (! isSemanticAction(x.get(i)))
				return false;
		return true;
	}
	
	protected boolean isEpsilon(IntList x)
	{		
		return isEpsilon(x, 0);
	}
	
	/**
     * @return BitSet indicando os symbolos que derivam Epsilon
     */
    private BitSet markEpsilon()
    {
        BitSet result = new BitSet();

        for (int i = 0; i < productions.size(); i++)
        {
            Production P = productions.getProd(i);
            if (isEpsilon(P.get_rhs()))
                result.set(P.get_lhs());
        }
        for (int i=FIRST_SEMANTIC_ACTION(); i <= LAST_SEMANTIC_ACTION(); i++)
        	result.set(i);
        	
        boolean change = true;
        while (change)
        {
            change = false;
            boolean derivesEpsilon;
            for (int i = 0; i < productions.size(); i++)
            {
                Production P = productions.getProd(i);
                derivesEpsilon = true;
                for (int j = 0; j < P.get_rhs().size(); j++)
                {
                    derivesEpsilon = derivesEpsilon && result.get(P.get_rhs().get(j));
                }
                if (derivesEpsilon && !result.get(P.get_lhs()))
                {
                    change = true;
                    result.set(P.get_lhs());
                }
            }
        }
        return result;
    }
	
	private static final BitSet EMPTY_SET = new BitSet();
	static { EMPTY_SET.set(EPSILON); }
	public BitSet first(int symbol)
	{
		if (isSemanticAction(symbol))
			return EMPTY_SET;
		else
			return firstSet[symbol];
	}
	
	public BitSet first(IntList x)
	{
		return first(x, 0);
	}
	
	public BitSet first(IntList x, int start)
	{
		BitSet result = new BitSet();
		
		if (x.size()-start == 1 && x.get(start) == DOLLAR)
			result.set(DOLLAR);
		if (isEpsilon(x, start))
			result.set(EPSILON);
		else
		{
			int k = x.size();
			while (isSemanticAction(x.get(start)))
				start++;
				
			BitSet f = (BitSet)first(x.get(start)).clone();
            f.clear(EPSILON);
            result.or(f);
            int i=start;
            while (i < k-1 && first(x.get(i)).get(EPSILON))
            {
                i++;
                f = (BitSet)first(x.get(i)).clone();
                f.clear(EPSILON);
                result.or(f);
            }
            if (i == k-1 && first(x.get(i)).get(EPSILON))
                result.set(EPSILON);
		}
		return result;
	}
	
	/**
     * Calcula os conjuntos FIRST de todos os símbolos de Gramática
     */
    private void fillFirstSet()
    {
        BitSet derivesEpsilon = markEpsilon();
        firstSet = new BitSet[symbols.length];
        for (int i = 0; i < firstSet.length; i++)
        {
            firstSet[i] = new BitSet();
        }

        for (int A = FIRST_NON_TERMINAL; A < FIRST_SEMANTIC_ACTION(); A++)
        {
            if (derivesEpsilon.get(A))
                firstSet[A].set(EPSILON);
        }
        for (int a = FIRST_TERMINAL; a < FIRST_NON_TERMINAL; a++)
        {
            firstSet[a].set(a);
            for (int A = FIRST_NON_TERMINAL; A < FIRST_SEMANTIC_ACTION(); A++)
            {
                boolean exists = false;
                for (int i = 0; i < productions.size(); i++)
                {
                    Production P = productions.getProd(i);
                    if (P.get_lhs() == A && !isEpsilon(P.get_rhs()) && P.firstSymbol() == a)
                    {
                        exists = true;
                        break;
                    }
                }
                if (exists)
                    firstSet[A].set(a);
            }
        }
        boolean changed;
        do
        {
            changed = false;
            for (int i = 0; i < productions.size(); i++)
            {
                Production P = productions.getProd(i);
                BitSet old = (BitSet)firstSet[P.get_lhs()].clone();
                firstSet[P.get_lhs()].or(first(P.get_rhs()));
                if (!changed && !old.equals(first(P.get_lhs())) )
                    changed = true;
            }
        }
        while (changed);
    }
	
	/**
     * Calcula os conjuntos FOLLOW de todos os símbolos não terminais de Gramática
     */
    private void fillFollowSet()
    {
        followSet = new BitSet[symbols.length];
        for (int i = 0; i < followSet.length; i++)
        {
            followSet[i] = new BitSet();
        }
        followSet[startSymbol].set(DOLLAR);
        boolean changes;
        do
        {
            changes = false;
            for (int i = 0; i < productions.size(); i++)
            {
                Production P = productions.getProd(i);
                for (int j=0;j<P.get_rhs().size(); j++)
                {
                    if (isNonTerminal(P.get_rhs().get(j)))
                    {
                        BitSet s = first(P.get_rhs(), j+1);
                        boolean deriveEpsilon = s.get(EPSILON);

                        if( P.get_rhs().size() > j+1 )
                        {
                            s.clear(EPSILON);
                            BitSet old = (BitSet)followSet[P.get_rhs().get(j)].clone();
                            followSet[P.get_rhs().get(j)].or(s);
                            if (!changes && !followSet[P.get_rhs().get(j)].equals(old))
                                changes = true;
                        }

                        if (deriveEpsilon)
                        {
                            BitSet old = (BitSet)followSet[P.get_rhs().get(j)].clone();
                            followSet[P.get_rhs().get(j)].or(followSet[P.get_lhs()]);
                            if (!changes && !followSet[P.get_rhs().get(j)].equals(old))
                                changes = true;
                        }
                    }
                }
            }
        }
        while (changes);
    }
    
    /**
     * Gera uma representação String dos conjuntos First e Follow
     * @return First e Follow como uma String
     */
    public String stringFirstFollow()
    {
        StringBuffer result = new StringBuffer();
        for (int i = FIRST_NON_TERMINAL; i < firstSet.length; i++)
        {
            StringBuffer bfr = new StringBuffer();
            bfr.append("FIRST(").append(symbols[i]).append(") = { ");
            for (int j = 0; j < firstSet[i].size(); j++)
            {
                if (firstSet[i].get(j))
                    bfr.append("").append(symbols[j]).append(" ");
            }
            bfr.append("}");
            result.append(bfr).append('\n');
        }
        for (int i = FIRST_NON_TERMINAL; i < followSet.length; i++)
        {
            StringBuffer bfr = new StringBuffer();
            bfr.append("FOLLOW(").append(symbols[i]).append(") = { ");
            for (int j = 0; j < followSet[i].size(); j++)
            {
                if (followSet[i].get(j))
                    bfr.append(symbols[j]).append(" ");
            }
            bfr.append("}");
            result.append(bfr).append('\n');
        }
        return result.toString();
    }
    
    public String ffAsHTML()
    {
    	StringBuffer result = new StringBuffer();

		result.append(
			"<HTML>"+
			"<HEAD>"+
			"<TITLE>First &amp; Follow</TITLE>"+
			"</HEAD>"+
			"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
			"<TABLE border=1 cellspacing=0>");
			
		result.append(
			"<TR align=center>"+
			"<TD bgcolor=black><FONT color=white><B>SÍMBOLO</B></FONT></TD>"+
			"<TD bgcolor=black><FONT color=white><B>FIRST</B></FONT></TD>"+
			"<TD bgcolor=black><FONT color=white><B>FOLLOW</B></FONT></TD>"+
			"</TR>");
			
		for (int i = FIRST_NON_TERMINAL; i < FIRST_SEMANTIC_ACTION(); i++)
        {
        	result.append("<TR align=center>");
			
			result.append("<TD nowrap bgcolor=#F5F5F5><B>"+HTMLDialog.translateString(symbols[i])+"</B></TD>");
			
			StringBuffer bfr = new StringBuffer("  ");
            for (int j = 0; j < firstSet[i].size(); j++)
            {
                if (firstSet[i].get(j))
                    bfr.append(symbols[j]).append(", ");
            }
            bfr.setLength(bfr.length()-2);
            
            result.append("<TD nowrap bgcolor=#F5F5F5>"+HTMLDialog.translateString(bfr.toString())+"</TD>");
			
            bfr = new StringBuffer("  ");
            for (int j = 0; j < followSet[i].size(); j++)
            {
                if (followSet[i].get(j))
                    bfr.append(symbols[j]).append(", ");
            }
            bfr.setLength(bfr.length()-2);
            
            result.append("<TD nowrap bgcolor=#F5F5F5>"+HTMLDialog.translateString(bfr.toString())+"</TD>");
            
            result.append("</TR>");
        }
			
		result.append(
			"</TABLE>"+
			"</FONT></BODY>"+
			"</HTML>");
			
		return result.toString();
    }

    /**
     * Remove os estados improdutivos da gramática
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    protected void removeImproductiveSymbols() throws EmptyGrammarException
    {
        BitSet SP = getProductiveSymbols();

        updateSymbols(SP);
    }

    /**
     * Remove os estados inúteis, os inprodutívos e os inalcansáveis
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    public void removeUselessSymbols() throws EmptyGrammarException
    {
        removeImproductiveSymbols();
        removeUnreachableSymbols();
        //removeRepeatedProductions();
    }

	/**
	 * Elimina as produções repetidas da gramática.
	 */	
	private void removeRepeatedProductions() throws EmptyGrammarException
	{/*
		BitSet repeated = new BitSet();
		sortProductions();
		
		Production p = productions[0];		
		for (int i = 1; i < productions.length; i++)
		{
			Production local = productions[i];
			if (local.equals(p))
				repeated.set(i);
			p = local;
		}
		
		//retira as produçoes que não possuem símbolos úteis
        Production[] P = new Production[productions.length];
        int k = 0;
        for (int i=0;i<productions.length;i++)
        {
            if (! repeated.get(i))
                P[k++] = productions[i];
        }
        productions = new Production[k];
        for (int i=0; i< productions.length; i++)
            productions[i] = P[i];*/
	}

    /**
     * Calcula as produções cujo lado esquerdo é <code>symbol</code>
     * @return BitSet indicando essas produções
     */
    public BitSet productionsFor(int symbol)
    {       
    	BitSet result = new BitSet();
        for (int i = 0; i < productions.size(); i++)
        {
            if (productions.getProd(i).get_lhs() == symbol)
                result.set(i);
        }
        return result;
    }

    /**
     * Transforma as recursões à esquerda indiretas em recusões diretas
     * @param prods produções para serem processadas
     * @return lista de produçoes sem recursão indireta
     */
    private ProductionList transformToFindRecursion(ProductionList prods)
    {
        ProductionList prodList = new ProductionList();
        prodList.addAll(prods);
        for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++ )
        {
            for (int j=FIRST_NON_TERMINAL; j<i; j++)
            {
                for (int it = 0; it < prodList.size(); it++)
                {
                    Production P = (Production)prodList.get(it);
                    if (P.get_lhs() == i && P.firstSymbol() == j)
                    {
                        prodList.remove(it);
                        it--;
                        IntList actions = new IntList();
                        for (int k = 0; k < P.get_rhs().size() && isSemanticAction(P.get_rhs().get(k)); k++)
							actions.add(P.get_rhs().get(k));
							
                        for (int it2 = 0; it2 < prodList.size(); it2++)
                        {
                            Production P2 = (Production)prodList.get(it2);
                            if (P2.get_lhs() == j)
                            {
                                int[] rhs = new int[P2.get_rhs().size() + P.get_rhs().size()-1];
                                int k = 0;
                                for ( ; k<actions.size(); k++)
                                	rhs[k] = actions.get(k);
                                int m = k;
                                for ( k = 0 ; k<P2.get_rhs().size(); k++)
                                    rhs[k + m] = P2.get_rhs().get(k);
                                m = m + k - (actions.size() + 1);
                                for ( k = actions.size() + 1; k<P.get_rhs().size(); k++)
                                    rhs[k + m] = P.get_rhs().get(k);
                                
                                Production newProduction = createProduction(P.get_lhs(), rhs);
                                if (newProduction != null)
                                    prodList.add(newProduction);
                            }
                        }
                    }
                }
            }
        }
        return prodList;
    }

    /**
     * Remove as recursões á esquerda da gramática.
     * Primeiramente transforma a gramática para que as recursões
     * indiretas se tornem diretas. Em seguida remove as recursões
     * diretas
     */
    public void removeRecursion()
    {
        productions = transformToFindRecursion(productions);
        removeDirectRecursion();
    }

    /**
     * Remove as recursões á esquerda da gramática.
     *  É preciso que não existam recursões indiretas
     */
    private void removeDirectRecursion()
    {
        for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++)
        {
            BitSet recursive = productionsFor(i);
            BitSet prods = productionsFor(i);
            int newSymbol = -1;
            for (BitSetIterator iter = new BitSetIterator(recursive); iter.hasNext();)
            {
                int x = iter.nextInt();
                if (productions.getProd(x).get_lhs() != productions.getProd(x).firstSymbol())
                    iter.remove();
            }
            if (recursive.length() > 0)
            {
                newSymbol = createSymbol(addTail(symbols[i]));
                for (BitSetIterator iter = new BitSetIterator(prods); iter.hasNext();)
                {
                    int x = iter.nextInt();
                    Production P = productions.getProd(x);
                    if (recursive.get(x))
                    {
                        P.get_rhs().remove(0);
                        P.get_rhs().add(newSymbol);
                        P.set_lhs(newSymbol);
                    }
                    else
                    {
                    	P.get_rhs().add(newSymbol);
                    }
                }
            }
            if (newSymbol != -1)
                productions.add( createProduction(newSymbol) );
        }
        fillFirstSet();
        fillFollowSet();
        sort();
    }

	private int createSymbol(String s)
	{
		for (Iterator i = productions.iterator(); i.hasNext();)
		{
			Production p = (Production) i.next();
			IntList rhs = p.get_rhs();
			for (int j=0; j<rhs.size(); j++)
				if (isSemanticAction(rhs.get(j)))
					rhs.set(j, rhs.get(j) + 1);
		}
		String[] newSymbols = new String[symbols.length+1];
		System.arraycopy(symbols,0,newSymbols,0,symbols.length);
		symbols = newSymbols;
		symbols[symbols.length-1] = s;
		
		return symbols.length-1;
	}

	/**
	 * Verifica se o símbolo a deriva o simbolo b em 0 ou mais passos.
	 * 
	 * @param a índice do primeiro símbolo
	 * @param b índice do segundo símbolo
	 */
	private boolean derives(int a, int b)
	{
		if (a == b)
			return true;
		
		BitSet src = new BitSet();
		
		src.set(b);
		
		for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++)
		{
			for (BitSetIterator it = new BitSetIterator(src); it.hasNext(); )	
			{
				int cur = it.nextInt();
				if (derivesDirectly(i, cur) && !src.get(i))
				{
					src.set(i);
					i = -1;
					//break;
					continue;
				}
			}			
		}
		
		return src.get(a);
	}

	/**
	 * Verifica se o símbolo a deriva o simbolo b diretamente.
	 * 
	 * @param a índice do primeiro símbolo
	 * @param b índice do segundo símbolo
	 */	
	private boolean derivesDirectly(int a, int b)
	{		
		BitSet derivesEpsilon = markEpsilon();
					
		for (int i=0; i<productions.size(); i++)
		{
			Production p = productions.getProd(i);
			
			if (p.get_lhs() == a)
			{
				if (p.get_rhs().size() == 1)
				{
					if (p.get_rhs().get(0) == b)
						return true;
				}
				else
				{
					IntList rhs = p.get_rhs();
					
					for (int j=0; j<rhs.size(); j++)
					{
						if (rhs.get(j) == b)
						{
							boolean allEpsilon = true;
							for (int k=0; k<j; k++)
							{
								if (! derivesEpsilon.get(rhs.get(k)))
									allEpsilon = false;
							}
							for (int k=j+1; k<rhs.size(); k++)
							{
								if (! derivesEpsilon.get(rhs.get(k)))
									allEpsilon = false;
							}
							if (allEpsilon)
								return true;
						}
					}
				}		
			}
		}
		return false;
	}

    /**
     * Remove as produçoes Unitárias.
     * Estas produções são aquelas da forma A ::= X, onde X é um não-terminal.
     */
    public void removeUnitaryProductions()
    {
		ProductionList prods = new ProductionList();
        // as produções que NÃO são ciclos são adicionadas a prods
        for (int i = 0; i < productions.size(); i++)
        {
            Production p = productions.getProd(i);
            if (p.get_rhs().size() != 1 || p.get_rhs().get(0) != p.get_lhs())
                prods.add(p);
        }
        
        BitSet[] N = new BitSet[symbols.length];
        
        for (int i=FIRST_NON_TERMINAL; i < N.length; i++)
        {
        	N[i] = new BitSet();
        	for (int j=FIRST_NON_TERMINAL; j<FIRST_SEMANTIC_ACTION(); j++)
        		if (derives(i, j))
        			N[i].set(j);
        }
        
        productions.clear();
        
        for (int i=0; i<prods.size(); i++)
        {
        	Production p = prods.getProd(i);
        	if (p.get_rhs().size() != 1 || !isNonTerminal(p.get_rhs().get(0)))
        	{
        		for (int j=FIRST_NON_TERMINAL; j<N.length; j++)
        		{
        			if (N[j].get(p.get_lhs()))  
        			{
        				Production np = createProduction(j,p.get_rhs());
        				if (np != null)
	        				productions.add(np);
        			}
        		}        		
        	}
        }

        //TODO: terminar este algoritimo
        sort();
    }

    /**
     * Remove as Epsilon-Produções da Gramática
     */
    public void removeEpsilon()
    {   
		BitSet E = markEpsilon();		
        ProductionList prods = new ProductionList();
        	   
        for (int i=0; i<productions.size(); i++)
        {
        	Production p = productions.getProd(i);
        	if ( ! isEpsilon( p.get_rhs() ) )
        	{
        		boolean derivesEpsilon = true;
                for (int j = 0; j < p.get_rhs().size(); j++)
                {
                    derivesEpsilon = derivesEpsilon && E.get(p.get_rhs().get(j));
                }
                if (! derivesEpsilon)
	        		prods.add(p);
        	}
        }
        
        for (int it = 0; it < prods.size(); it++)
        {
            Production p = prods.getProd(it);
            
            if (! isEpsilon( p.get_rhs() ))//?INUTIL?
            {
                int i=0;
                while (i < p.get_rhs().size())
                {
                    //procura pelo epsilon-NT
                    for (; i<p.get_rhs().size(); i++)
                    {
                        if (!isSemanticAction(p.get_rhs().get(i)) && E.get(p.get_rhs().get(i)))
                            break;
                    }
                    if (i < p.get_rhs().size())
                    {
                    	Production pNew = derivationAt(p, i);
                    	if (pNew != null && !prods.contains(pNew))
                    		prods.add(pNew);
                        i++;
                    }
                }
            }
        }
        if (E.get(startSymbol))
        {
         //   String newSymbol = ;
//
  //          String[] s = new String[symbols.length+1];
    //        System.arraycopy(symbols, 0, s, 0, symbols.length);
      //      symbols = s;
        //    int newPos = symbols.length-1;
          //  symbols[newPos] = newSymbol;
            
            int newPos = createSymbol(addTail(symbols[startSymbol]));

            prods.add(createProduction(newPos, new int[]{startSymbol}));
            prods.add(createProduction(newPos));
            startSymbol = newPos;

            fillFirstSet();
            fillFollowSet();
        }
        productions = prods;

        sort();
    }
    
    private Production derivationAt(Production p, int index)
    {
    	IntList rhsP = new IntList();
    	for (int k=0; k<productions.size(); k++)
    	{
    		if ( (productions.getProd(k).get_lhs() == p.get_rhs().get(index)) && 
    				(isEpsilon(productions.getProd(k).get_rhs())) )
    		{
    			rhsP = productions.getProd(k).get_rhs();
    			break;
    		}
    	} 
    	IntList rhs = new IntList();
    	//int[] rhs = new int[p.get_rhs().size()-1];
        for (int k=0; k < index; k++)
            rhs.add(p.get_rhs().get(k));
        for (int k=0; k < rhsP.size(); k++)
        	rhs.add(rhsP.get(k));
        	
        for (int k=index+1; k < p.get_rhs().size(); k++)
            rhs.add(p.get_rhs().get(k));
            
        return createProduction(p.get_lhs(), rhs.toArray());    	
    }

    private String addTail(String s)
    {
    	s = s.substring(0,s.length()-1) + "_T>";
    	
        for (int i = 0; i < symbols.length; i++)
        {
            if (symbols[i] != null && symbols[i].equals(s))
            {
                s = s.substring(0,s.length()-1) + "_T>";
                i = 0;
            }
        }
        return s;
    }

    /**
     * Reordena os símbolos e as produções
     */
    public void sort()
    {    	
    	for (int i=FIRST_NON_TERMINAL; i < FIRST_SEMANTIC_ACTION(); i++)
    	{
    		String s = symbols[i].substring(0, symbols[i].length()-1) + "_T>";
    		int j=i+1;
    		for ( ; j < FIRST_SEMANTIC_ACTION(); j++)
    			if (symbols[j].equals( s ))
    				break;
    		if (j < FIRST_SEMANTIC_ACTION()) //achou
    		{
    			int to = i+1, 
    			    from = j;
    			    
    			if (to != from)
    			{
    				moveSymbol(from, to);
    			}
    		}
    	}
    	moveSymbol(startSymbol, FIRST_NON_TERMINAL);
    	
    	Collections.sort(productions);
    }
    
	private void moveSymbol(int from, int to)
	{
		String s = symbols[from];
		for (int k=from; k > to; k--)
			symbols[k] = symbols[k-1];
		symbols[to] = s;
		
		if (startSymbol == from)
			startSymbol = to;
		else if (startSymbol >= to && startSymbol < from)
			startSymbol++;
		
		for (Iterator iter = productions.iterator(); iter.hasNext(); )
		{
			Production p = (Production) iter.next();
			
			if (p.get_lhs() == from)
				p.set_lhs(to);
			else if (p.get_lhs() >= to && p.get_lhs() < from)
				p.set_lhs(p.get_lhs() + 1);
			IntList rhs = p.get_rhs();
			for (int k=0; k < rhs.size(); k++)
			{
				if (rhs.get(k) == from)
					rhs.set(k, to);
				else if (rhs.get(k) >= to && rhs.get(k) < from)
					rhs.set(k, rhs.get(k) + 1);
			}
		}
	}


    /**
     * Verifica as condições para esta gramática ser LL
     */
    public boolean isLL()
    {
        return 
        	isFactored() && 
        	!hasLeftRecursion() &&         	
        	passThirdCondition();
    }

    /**
     * Verifica se esta gramática possui recursão à esquerda
     */
    public boolean hasLeftRecursion()
    {
        ProductionList prods = transformToFindRecursion(productions);
        
		for (int i = 0; i < prods.size(); i++)
        {
            if (prods.getProd(i).get_lhs() == prods.getProd(i).firstSymbol())
            {
            	return true;
            }
            
        }
        return false;
    }
    
    public int getLeftRecursiveSimbol()
    {
        ProductionList prods = transformToFindRecursion(productions);
    
        for (int i = 0; i < prods.size(); i++)
        {
            if (prods.getProd(i).get_lhs() == prods.getProd(i).firstSymbol())
            {
                return prods.getProd(i).get_lhs();
            }
        
        }
        return -1;
    }

    /**
     * 
     * @return um BitSet contendo produçoes não fatoradas
     */
    public BitSet getNonFactoratedProductions()
    {
        BitSet result = new BitSet();
        
        for (int i=0; i< productions.size(); i++)
        {
            Production p1 = productions.getProd(i);
            for (int j=i+1; j< productions.size(); j++)
            {
                Production p2 = productions.getProd(j);

                if (p1.get_lhs() == p2.get_lhs())
                {
                    BitSet first = first(p1.get_rhs());
                    first.and(first(p2.get_rhs()));
                    if (! first.isEmpty())
                    {
                        result.set(i);
                        result.set(j);
                    }
                }
            }
            if (result.cardinality() > 0)
                break;
        }
        
        return result;
    }

    /**
     * Verifica se esta gramática está fatorada
     */
    public boolean isFactored()
    {
        for (int i=0; i< productions.size(); i++)
        {
            Production P1 = productions.getProd(i);
            for (int j=i+1; j< productions.size(); j++)
            {
                Production P2 = productions.getProd(j);

                if (P1.get_lhs() == P2.get_lhs())
                {
                    BitSet first = first(P1.get_rhs());
                    first.and(first(P2.get_rhs()));
                    if (! first.isEmpty())
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifica a terceira condição LL
     */
    public boolean passThirdCondition()
    {
        BitSet derivesEpsilon = markEpsilon();
        for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++)
        {
            if (derivesEpsilon.get(i))
            {
                BitSet first = (BitSet)firstSet[i].clone();
                first.and(followSet[i]);
                if (! first.isEmpty())
                    return false;
            }
        }
        return true;
    }

    /**
     * Calcula os estados produtivos
     * @return conjunto dos estados produtivos
     */
    private BitSet getProductiveSymbols()
    {
    	BitSet SP = new BitSet();
        for (int i=FIRST_TERMINAL; i< FIRST_NON_TERMINAL; i++)
            SP.set(i);

        for (int i=FIRST_SEMANTIC_ACTION(); i<= LAST_SEMANTIC_ACTION(); i++)
            SP.set(i);
            
        SP.set(EPSILON);
        boolean change;

        do
        {
            change = false;
            BitSet Q = new BitSet();
            for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++)
            {
                if (! SP.get(i))
                {
                    for (int j=0; j< productions.size(); j++)
                    {
                        Production P = productions.getProd(j);
                        if (P.get_lhs() == i)
                        {
                            boolean pass = true;
                            for (int k=0; k<P.get_rhs().size(); k++)
                                pass = pass && SP.get(P.get_rhs().get(k));
                            if (pass)
                            {
                                Q.set(i);
                                change = true;
                            }
                        }
                    }
                }
            }
            SP.or(Q);
        }
        while (change);
        return SP;
    }

    /**
     * Remove os símbolos inalcançáveis da gramática
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    protected void removeUnreachableSymbols() throws EmptyGrammarException
    {
        BitSet SA = getReachableSymbols();

        updateSymbols(SA);
    }

    /**
     * Calcula os símbolos que são alcansáveis
     *
     * @return BitSet indicando os symbolos alcansáveis
     */
    private BitSet getReachableSymbols()
    {
    	BitSet SA = new BitSet();
        SA.set(startSymbol);
        boolean change;
        do
        {
            change = false;
            BitSet M = new BitSet();
            for (int i=0; i<symbols.length; i++)
            {
                if (! SA.get(i))
                {
                    for (int j=0; j< productions.size(); j++)
                    {
                        Production P = productions.getProd(j);
                        if (SA.get(P.get_lhs()))
                        {
                            for (int k=0; k<P.get_rhs().size(); k++)
                            {
                                if (P.get_rhs().get(k) == i)
                                {
                                    M.set(i);
                                    change = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            SA.or(M);
        }
        while (change);
        return SA;
    }

	public String uselessSymbolsHTML()
	{
		Grammar clone = (Grammar) clone();
		
		try
		{
			clone.removeUselessSymbols();
		}
		catch (EmptyGrammarException e)
		{
		}
		
		String[] cs = clone.symbols;
		
		BitSet s = new BitSet();
		
		
		for (int i=2; i<symbols.length; i++)
		{
			for (int j=0; j<cs.length; j++)
			{
				if (cs[j].equals(symbols[i]))
				{
					s.set(i);
					break;
				}
			}
		}
		
		StringBuffer result = new StringBuffer();
		
		result.append(
					"<HTML>"+
					"<HEAD>"+
					"<TITLE>Símbolos inúteis</TITLE>"+
					"</HEAD>"+
					"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">");
		
		int count = 0;
		for (int i=2; i<symbols.length; i++)
		{
			if (!s.get(i))
			{
				result.append(HTMLDialog.translateString(symbols[i])+"<br>");
				count++;
			}
		}
		if (count == 0)
			result.append("Não há símbolos inúteis");
		
		result.append(
					"</TABLE>"+
					"</FONT></BODY>"+
					"</HTML>");
		
		return result.toString();
	}

    /**
     * Gera uma representação de um BitSet utilizando os símbolos da Gramática
     *
     * @param b BitSet a ser convertido
     *
     * @return representação do BitSet
     */
    public String setToStr(BitSet b)
    {
    	StringBuffer bfr = new StringBuffer("{ ");
        for (int j = 0; j < b.size(); j++)
        {
            if (b.get(j))
                bfr.append("\"").append(symbols[j]).append("\" ");
        }
        bfr.append("}");
        return bfr.toString();
    }

    /**
     * Fatora a gramática
     */

    public void factorate() throws LeftRecursionException
    {
        if (hasLeftRecursion())
            throw new LeftRecursionException();

		boolean change = true;
		while (change)
		{
			change = false;
        	for (int i=FIRST_NON_TERMINAL; i<FIRST_SEMANTIC_ACTION(); i++)
        	{
            	change = change || factorate(i);
        	}
		}
    }

    /**
     * Efetua a fatoração das produções que possuam <code>symb</code> como lado esquerdo
     *
     * @param symb lado esquerdo das produções a serem fatoradas
     * @return <code>true</code> se hove alguma mudança, <code>fals</code>e em caso contrário
     */
    private boolean factorate(int symb)
    {
    	boolean result = false;
        BitSet prods = productionsFor(symb);

        BitSet conflict = new BitSet();

        int confictSymbol = conflict(prods, conflict);

        if (! conflict.isEmpty())
        {
            result = true;
            
			//transforma as producoes para revelar os conflito indiretos
            for (int i=0; i< productions.size(); i++)
            {
                Production p = productions.getProd(i);
                if (p.get_lhs() == symb && first(p.get_rhs()).get(confictSymbol) && p.firstSymbol() != confictSymbol)
                {
                    ProductionList np = leftMostDerive(p);
                    productions.remove(i);
                    productions.addAll(np);
                    i--;           
                    fillFirstSet();
            		fillFollowSet();         
                }
            }

            conflict = new BitSet();
            for (int i=0; i< productions.size(); i++)
            {
                Production p = productions.getProd(i);
                if (p.get_lhs() == symb && p.firstSymbol() == confictSymbol)
                {
                    conflict.set(i);
                }
            }

            int newIndex = createSymbol(addTail(symbols[symb]));

            IntList prefix = extractPrefix(conflict);

            for (BitSetIterator it = new BitSetIterator(conflict); it.hasNext(); )
            {
                Production p = productions.getProd(it.nextInt());
                p.set_lhs(newIndex);
                if (p.get_rhs().size() > prefix.size())
                    p.get_rhs().removeRange(0, prefix.size());
                else // p.rhs.length == prefix.length
                    p.get_rhs().clear();
            }
            IntList rhs = new IntList();
            rhs.addAll(prefix);
			
            rhs.add(newIndex);
            productions.add(createProduction(symb, rhs));
            
            fillFirstSet();
            fillFollowSet();
            sort();
        }
        return result;
    }

    /**
     * Executa uma derivação mais a esquerda na produção passada como parametro
     *
     * @param p produção a sofrer a derivação
     */
    public ProductionList leftMostDerive(Production p)
    {
    	if (isTerminal(p.firstSymbol()))
            return new ProductionList();
        else
        {
            ProductionList newProds = new ProductionList();
            int symb = p.firstSymbol();
            IntList actions = new IntList();
            for (int i=0; i<p.get_rhs().size() && isSemanticAction(p.get_rhs().get(i)); i++)
            	actions.add(p.get_rhs().get(i));

            for (BitSetIterator it = new BitSetIterator(productionsFor(symb)); it.hasNext(); )
            {
                Production p1 = productions.getProd(it.nextInt());
                IntList rhs = new IntList();
                for (int i=0; i<actions.size(); i++)
                	rhs.add(actions.get(i));
                for (int i=0; i<p1.get_rhs().size(); i++)
                	rhs.add(p1.get_rhs().get(i));
                for (int i=actions.size()+1; i<p.get_rhs().size(); i++)
                	rhs.add(p.get_rhs().get(i));
                
                Production n = createProduction(p.get_lhs(), rhs);
                if (n != null && !newProds.contains(n))
 	               newProds.add(n);
            }
            return newProds;
        }
    }

	/**
	 * Calcula o prefixo comum de um conjunto de produções.
	 *
	 * @param prods conjunto de produções com prefixo comum.
	 * 
	 * @return prefixo comum entre as produções. 
	 * 
	 */
	
    private IntList extractPrefix(BitSet prods)
    {
    	IntList prefix = new IntList();
        boolean repeat;
        int index = 0;
        do
        {
            repeat = true;
            BitSetIterator it = new BitSetIterator(prods);
            Production pro = productions.getProd(it.nextInt());
            if (pro.get_rhs().size() > index)
            {
                int s = pro.get_rhs().get(index);
                for ( ; it.hasNext(); )
                {
                    Production p = productions.getProd(it.nextInt());
                    if (p.get_rhs().size() <= index || p.get_rhs().get(index) != s)
                        repeat = false;
                }
                if (repeat)
                {
                    prefix.add(pro.get_rhs().get(index));
                    index++;
                }
            }
            else
                repeat = false;
        }
        while (repeat);
        return prefix;
    }

    /**
     * Seleciona em um conjunto de produções, aquelas que possuem
     * o mesmo simbolo iniciando o lado direito.
     * Caso existam dois grupos de produções conflitantes, o grupo maior
     * é selecionado
     *
     * @param prods produçoes a seram pesquizadas
     *
     * @return produções conflitantes
     */
    private int conflict(BitSet prods, BitSet result)
    {
    	int[] symbs = new int[symbols.length];
        //BitSet epsilon = markEpsilon();

        for (int i = 0; i < symbs.length; i++)
        {
            symbs[i] = 0;
        }

        for (BitSetIterator it = new BitSetIterator(prods); it.hasNext(); )
        {
            Production p = productions.getProd(it.nextInt());
            for (BitSetIterator i=new BitSetIterator(first(p.get_rhs())); i.hasNext(); )
                symbs[i.nextInt()]++;
        }

        symbs[EPSILON] = 0;
        symbs[DOLLAR] = 0;

        int max = 0;
        int indexMax = 0;
        for (int i = 0; i < symbs.length; i++)
        {
            if (symbs[i] > max)
            {
                max = symbs[i];
                indexMax = i;
            }
        }

       // BitSet result = new BitSet();
        if (max > 1)
        {
            for (BitSetIterator it = new BitSetIterator(prods); it.hasNext(); )
            {
                int pos = it.nextInt();
                if (first(productions.getProd(pos).get_rhs()).get(indexMax))
                    result.set(pos);
            }
        }

        return indexMax;
    }

    /**
     * @return a representação de gramática em String
     */
    public String toString()
    {
        StringBuffer bfr = new StringBuffer();
        String lhs = "";
        boolean first = true;
        for (int i = 0; i < productions.size(); i++)
        {
            Production P = productions.getProd(i);
            if (! symbols[P.get_lhs()].equals(lhs))
            {
            	if (! first)
            	{            	
            		bfr.append(";\n\n");
            	}
            	first = false;
            	lhs = symbols[P.get_lhs()];
            	bfr.append(lhs).append(" ::=");
            }
            else
            {
            	bfr.append("\n");
            	for (int j=0; j<lhs.length(); j++)
            		bfr.append(" ");
				bfr.append("   |");            	
            }	
            if (P.get_rhs().size() == 0)
            {
				bfr.append(" "+EPSILON_STR);
            }
            else
            {
	            for (int j = 0; j < P.get_rhs().size(); j++)
	            {
	                bfr.append(" ");
	                if (isSemanticAction(P.get_rhs().get(j)))
	                {
	                	int action = P.get_rhs().get(j) - FIRST_SEMANTIC_ACTION();
	                	bfr.append("#"+action);
	                }	
	                else
	                {
	                	String s = symbols[P.get_rhs().get(j)];
	                	bfr.append(s);
	                }
	            }
            }
        }
        bfr.append(";\n");
        return bfr.toString();
    }

    /**
     * Cria uma cópia da Gramática
     */
    public Object clone()
    {
    	try
		{
			Grammar g = (Grammar) super.clone();
			
			String[] T = new String[FIRST_NON_TERMINAL-2];
			String[] N = new String[FIRST_SEMANTIC_ACTION() - FIRST_NON_TERMINAL];
			for (int i = 0; i < T.length; i++)
			     T[i] = new String(symbols[i+2]);
			for (int i = 0; i < N.length; i++)
			     N[i] = new String(symbols[i+FIRST_NON_TERMINAL]);
			ProductionList P = new ProductionList();
			for (int i = 0; i < productions.size(); i++)
			{
			    int[] rhs = new int[productions.getProd(i).get_rhs().size()];
			    for (int j=0; j<rhs.length; j++)
			        rhs[j] = productions.getProd(i).get_rhs().get(j);
			    P.add( new Production(null, productions.getProd(i).get_lhs(),rhs));
			}
			
			g.setSymbols(T, N, startSymbol);
			g.setProductions(P);
			g.fillFirstSet();
			g.fillFollowSet();  
					
			return g;
		}
		catch (CloneNotSupportedException e)
		{
			throw new InternalError();
		}
    }
    
    private void removeSymbol(int s)
    {
    	String[] newSymbols = new String[symbols.length-1];
    	System.arraycopy(symbols, 0, newSymbols, 0, s);
    	System.arraycopy(symbols, s+1, newSymbols, s, symbols.length - s - 1);
    	symbols = newSymbols;
    	
    	if (startSymbol > s)
    		startSymbol--;
    	if (FIRST_NON_TERMINAL > s)
    		FIRST_NON_TERMINAL--;
    	for (Iterator i = productions.iterator(); i.hasNext();)
		{
			Production p = (Production) i.next();
			
			if (p.get_lhs() == s)
			{
				i.remove();
				continue;
			}
			else if (p.get_lhs() > s)
				p.set_lhs(p.get_lhs()-1);
				
			for (int j=0; j<p.get_rhs().size(); j++)
			{
				if (p.get_rhs().get(j) == s)
				{
					i.remove();
					break;
				}
				if (p.get_rhs().get(j) > s)
					p.get_rhs().set(j, p.get_rhs().get(j) - 1);
			}			
		}
    }
    
    /**
     * Remove todos os symbolos, exceto os que devem ser mantidos;
     * @paramam keep conjunto dos símbolos a serem mantidos
     * @throws EmptyGrammarException se o símbolo inicial for removido
     */
    private void updateSymbols(BitSet keep) throws EmptyGrammarException
    {
        keep.set(EPSILON);
        keep.set(DOLLAR);

		/*
        if (checkEmpty && ! keep.get(startSymbol))
            throw new EmptyGrammarException();
        */
        int removed = 0;
        for (int i=0; i<symbols.length; i++)
        	if (! keep.get(i) )
        	{
        		removeSymbol(i - removed);
        		removed++;
        	}
        
        fillFirstSet();
        fillFollowSet();
    }
}