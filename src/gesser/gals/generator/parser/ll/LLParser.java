package gesser.gals.generator.parser.ll;

import gesser.gals.HTMLDialog;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.ConflictSolver;
import gesser.gals.generator.parser.Production;

import java.util.BitSet;

/**
 * LL1Grammar representa a classe das gramáticas LL(1)
 * Esta classe possui um algorítimo de parsing preditivo.
 *
 * @author Carlos Eduardo Gesser
 */

public class LLParser
{	
	private Grammar g;
    public LLParser(Grammar g) throws NotLLException
    {
    	if (! g.isFactored())
    		throw new NotLLException("Gramática não Fatorada");
    	if (g.hasLeftRecursion())
			throw new NotLLException("Gramática possui Recursão à Esquerda");
    	
    	this.g = g;    	
    }
    
    public Grammar getGrammar()
    {
    	return g;
    }

    /**
     * @param p produção para se calcular o conjunto predict
     *
     * @return BitSet contendo os tokens do lookahead de p
     */
    private BitSet lookahead(Production p)
    {
        BitSet result = g.first(p.get_rhs());
        if (result.get(Grammar.EPSILON))
        {
            result.clear(Grammar.EPSILON);
            result.or(g.followSet[p.get_lhs()]);
        }
        return result;
    }

    public int[][] generateTable()
    {
    	String[] symbols = g.getSymbols();
        BitSet[][] table = new BitSet[symbols.length-g.FIRST_NON_TERMINAL][g.FIRST_NON_TERMINAL-1];

        for (int i = 0; i < table.length; i++)
            for (int j = 0; j < table[i].length; j++)
                table[i][j] = new BitSet();

        for (int i=0; i<g.getProductions().size(); i++)
        {
            Production p = g.getProductions().getProd(i);
            BitSet pred = lookahead(p);
            for (int j = 1; j<g.FIRST_NON_TERMINAL; j++)
            {
            	if (pred.get(j))
            	{
                	table[p.get_lhs()-g.FIRST_NON_TERMINAL][j-1].set(i);
            	}
            }
        }

		return resolveConflicts(table,ConflictSolver.getInstance());
    }

	private int[][] resolveConflicts(BitSet[][] table, LLConflictSolver cs)
	{
		int[][] result = new int[table.length][table[0].length];
		
		for (int i=0; i<table.length; i++)
			for (int j=0; j<table[i].length; j++)
            {
                switch (table[i][j].cardinality())
                {
                    case 0:
                        result[i][j] = -1;
                        break;        
                    case 1:
                        result[i][j] = table[i][j].nextSetBit(0);
                        break;
                    default:
                        result[i][j] = cs.resolve(g, table[i][j], j, i);
                        break;
                }
            }
			
		return result;
	}    
	
	public String tableAsHTML()
	{
		int [][] tbl = generateTable();
		StringBuffer result = new StringBuffer();

		result.append(
			"<HTML>"+
			"<HEAD>"+
			"<TITLE>Tabela de Análise LL(1)</TITLE>"+
			"</HEAD>"+
			"<BODY><FONT face=\"Verdana, Arial, Helvetica, sans-serif\">"+
			"<TABLE border=1 cellspacing=0>");
			
		result.append(
			"<TR align=center>"+
			"<TD bgcolor=black><FONT color=white><B>&nbsp;</B></FONT></TD>"+
			"<TD bgcolor=black><FONT color=white><B>$</B></FONT></TD>");
		
		for (int i=Grammar.FIRST_TERMINAL; i<g.FIRST_NON_TERMINAL; i++)
		{
			result.append("<TD nowrap bgcolor=black><FONT color=white><B>"+HTMLDialog.translateString(g.getSymbols()[i])+"</B></FONT></TD>");
		}
		
		result.append(
			"</TR>");
		
		for (int i=0; i<tbl.length; i++)
		{
			result.append(
				"<TR align=center>"+
				"<TD nowrap bgcolor=black><FONT color=white><B>"+HTMLDialog.translateString(g.getSymbols()[i+g.FIRST_NON_TERMINAL])+"</B></FONT></TD>");
			
			for (int j=0; j<tbl[i].length; j++)
			{
				int val = tbl[i][j];
				
				if (val >= 0)					
					result.append("<TD width=40 bgcolor=#F5F5F5>"+val+"</TD>");
				else
					result.append("<TD width=40 bgcolor=#F5F5F5>-</TD>");
			}
				
			result.append(
				"</TR>");
		}
		
		result.append("</TABLE>");
			
		result.append(			
			"<BR></FONT><CODE><TABLE border=0>");
			
		for (int i=0;i<g.getProductions().size(); i++)
		{
			result.append("<TR>");
			
			result.append("<TD align=right nowrap>"+i+"&nbsp;-&nbsp;</TD>");
			result.append("<TD>"+HTMLDialog.translateString(g.getProductions().get(i).toString())+"</TD>");
			
			result.append("</TR>");
		}
			
		result.append(
			"</TABLE></CODE>"+
			"</BODY>"+
			"</HTML>");
				
		return result.toString();
	}	
}
