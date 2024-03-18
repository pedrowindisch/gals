package gesser.gals.generator.csharp;

import static gesser.gals.generator.Options.PARSER_SLR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gesser.gals.generator.Options;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.Production;
import gesser.gals.generator.parser.ll.LLParser;
import gesser.gals.generator.parser.ll.NotLLException;
import gesser.gals.generator.parser.lr.Command;
import gesser.gals.generator.parser.lr.LRGeneratorFactory;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.util.IntList;

public class CSharpCommonGenerator {
    private Options options = null;
    private Grammar grammar = null;
    private FiniteAutomata finiteAutomata = null;

    int[][][] lrTable = null;

    public Map generate(FiniteAutomata fa, Grammar g, Options o) throws NotLLException {
        finiteAutomata = fa;
        grammar = g;
        options = o;

        Map result = new HashMap();

        result.put("Token.cs", generateToken());
        result.put("Constants.cs", generateConstants());

        if (fa != null)
            result.put("ScannerConstants.cs", generateScannerConstants());

        if (g != null)
            result.put("ParserConstants.cs", generateParserConstants());

        result.put("AnalysisError.cs", generateAnalysisError());
        result.put("LexicalError.cs", generateLexicalError());
        result.put("SyntaticError.cs", generateSyntaticError());
        result.put("SemanticError.cs", generateSemanticError());

        return result;
    }

    private void addNamespace(StringBuffer buffer) {
        String _package = options.pkgName;
        if (_package != null && _package.length() > 0) {
            buffer.append("namespace ");
            buffer.append(_package);
            buffer.append(";\n\n");
        }
    }
    
    private String generateToken() {
        StringBuffer result = new StringBuffer();
        addNamespace(result);

        // C#:
        String[] lines = {
                "public class Token",
                "{",
                "    private int id;",
                "    private string lexeme;",
                "    private int position;",
                "",
                "    public Token(int id, string lexeme, int position)",
                "    {",
                "        this.id = id;",
                "        this.lexeme = lexeme;",
                "        this.position = position;",
                "    }",
                "",
                "    public int GetId()",
                "    {",
                "        return id;",
                "    }",
                "",
                "    public String GetLexeme()",
                "    {",
                "        return lexeme;",
                "    }",
                "",
                "    public int GetPosition()",
                "    {",
                "        return position;",
                "    }",
                "",
                "    public override string ToString()",
                "    {",
                "        return id+\" ( \"+lexeme+\" ) @ \"+position;",
                "    }",
                "}"
        };

        for (String line : lines) {
            result.append(line);
            result.append("\n");
        }

        return result.toString();
    }
    
    private String generateAnalysisError() {
        StringBuffer result = new StringBuffer();
        addNamespace(result);

        // Now C#:
        String[] lines = {
                "public class AnalysisError : Exception",
                "{",
                "    private int position;",
                "",
                "    public AnalysisError(string msg, int position) : base(msg)",
                "    {",
                "        this.position = position;",
                "    }",
                "",
                "    public AnalysisError(string msg) : base(msg)",
                "    {",
                "        this.position = -1;",
                "    }",
                "",
                "    public int GetPosition()",
                "    {",
                "        return position;",
                "    }",
                "",
                "    public override string ToString()",
                "    {",
                "        return base.ToString() + \", @ \"+position;",
                "    }",
                "}"
        };

        for (String line : lines) {
            result.append(line);
            result.append("\n");
        }

        return result.toString();
    }

    private String generateLexicalError() {
        StringBuffer result = new StringBuffer();
        addNamespace(result);

        // Now C#:
        String[] lines = {
                "public class LexicalError : AnalysisError",
                "{",
                "    public LexicalError(string msg, int position) : base(msg, position)",
                "    {",
                "    }",
                "",
                "    public LexicalError(string msg) : base(msg)",
                "    {",
                "    }",
                "}"
        };

        for (String line : lines) {
            result.append(line);
            result.append("\n");
        }

        return result.toString();
    }

    private String generateSyntaticError() {
        StringBuffer result = new StringBuffer();
        addNamespace(result);

        // Now C#:
        String[] lines = {
                "public class SyntaticError : AnalysisError",
                "{",
                "    public SyntaticError(string msg, int position) : base(msg, position)",
                "    {",
                "    }",
                "",
                "    public SyntaticError(string msg) : base(msg)",
                "    {",
                "    }",
                "}"
        };

        for (String line : lines) {
            result.append(line);
            result.append("\n");
        }

        return result.toString();
    }

    private String generateSemanticError() {
        StringBuffer result = new StringBuffer();
        addNamespace(result);

        // Now C#:
        String[] lines = {
                "public class SemanticError : AnalysisError",
                "{",
                "    public SemanticError(string msg, int position) : base(msg, position)",
                "    {",
                "    }",
                "",
                "    public SemanticError(string msg) : base(msg)",
                "    {",
                "    }",
                "}"
        };

        for (String line : lines) {
            result.append(line);
            result.append("\n");
        }

        return result.toString();
    }

    private String generateConstants() {
        StringBuffer result = new StringBuffer();
        addNamespace(result);

        String[] lines = {
                "public static class Constants",
                "{",
                "    public const int EPSILON = 0;",
                "    public const int DOLLAR = 1;",
                constList(),
                "}"
        };

        for (String line : lines) {
            result.append(line);
            result.append("\n");
        }

        return result.toString();
    }
    
    private String constList() {
        StringBuffer result = new StringBuffer();

        List tokens = null;

        if (finiteAutomata != null)
            tokens = finiteAutomata.getTokens();
        else if (grammar != null)
            tokens = Arrays.asList(grammar.getTerminals());
        else
            throw new RuntimeException("Erro Interno");

        for (int i = 0; i < tokens.size(); i++) {
            String t = (String) tokens.get(i);

            if (t.charAt(0) == '\"')
                result.append("    public const int t_TOKEN_" + (i + 2) + " = " + (i + 2) + "; " + "//" + t + "\n");
            else
                result.append("    public const int t_" + t + " = " + (i + 2) + ";\n");
        }

        return result.toString();
    }
    
    private String generateScannerConstants()
	{
		StringBuffer result = new StringBuffer();
        addNamespace(result);
		
		result.append(
		"    public static class ScannerConstants\n"+
		"    {\n");
		
		result.append(genLexTables());
			
		result.append("    }\n");
		return result.toString();
	}
	
	private String generateParserConstants() throws NotLLException
	{
		StringBuffer result = new StringBuffer();
        addNamespace(result);

		result.append(
		"    public static class ParserConstants\n"+
		"    {\n");
		
		result.append(genSyntTables());
			
		result.append("    }\n");

		return result.toString();
	}

	private String genLexTables()
	{
		String lexTable;
		
		switch (options.scannerTable)
		{
			case Options.SCANNER_TABLE_FULL:
				lexTable = lex_table();
				break;
			case Options.SCANNER_TABLE_COMPACT:
				lexTable = lex_table_compress();
				break;
			case Options.SCANNER_TABLE_HARDCODE:
				lexTable = "";
				break;
			default:
				//nunca acontece
				lexTable = null;
				break;
		}
			
		return 
			lexTable+
			"\n"+
			token_state()+
			(finiteAutomata.hasContext() ? 
			"\n"+
			context() : "")+
			"\n"+
			(finiteAutomata.getSpecialCases().length > 0 ?
			special_cases()+			
			"\n" : "")+
			scanner_error()+
			"\n";
	}
	
	private String context()
	{
		StringBuffer result = new StringBuffer();
		
		result.append("        public static readonly int[][] SCANNER_CONTEXT =\n"+
		              "        {\n");
		
		for (int i=0; i<finiteAutomata.getTransitions().length; i++)
		{
			result.append("            new[] {");
			result.append(finiteAutomata.isContext(i)?"1":"0");
			result.append(", ");
			result.append(finiteAutomata.getOrigin(i));
			result.append("},\n");
		}
		
		result.setLength(result.length()-2);
		result.append(
		"\n        };\n");
		
		return result.toString();
	}

	private String scanner_error()
	{
		StringBuffer result = new StringBuffer();

		result.append(
		"        public static readonly string[] SCANNER_ERROR =\n"+
		"        {\n");

		int count = finiteAutomata.getTransitions().length;
		for (int i=0; i< count; i++)
		{
			result.append("            \"");
			
			String error = finiteAutomata.getError(i);
			for (int j=0; j<error.length(); j++)
			{
				if (error.charAt(j) == '"')
					result.append("\\\"");
				else
					result.append(error.charAt(j));
			}
			
			result.append("\",\n");
		}
		result.setLength(result.length()-2);
		result.append(
		"\n        };\n");

		return result.toString();
	}
	
	private String genSyntTables() throws NotLLException
	{
		switch (options.parser)
		{
			case Options.PARSER_REC_DESC:
			case Options.PARSER_LL:
				return genLLSyntTables();
			case Options.PARSER_SLR:
			case Options.PARSER_LALR:
			case Options.PARSER_LR:
				return genLRSyntTables();
			default:
				return null;
		}
	}
	
	private String genLRSyntTables()
    {
        lrTable = LRGeneratorFactory.createGenerator(grammar).buildIntTable();

        StringBuffer result = new StringBuffer(
                "        public const int FIRST_SEMANTIC_ACTION = " + grammar.FIRST_SEMANTIC_ACTION() + ";\n" +
                        "\n" +
                        "        public const int SHIFT  = 0;\n" +
                        "        public const int REDUCE = 1;\n" +
                        "        public const int ACTION = 2;\n" +
                        "        public const int ACCEPT = 3;\n" +
                        "        public const int GO_TO  = 4;\n" +
                        "        public const int ERROR  = 5;\n");

        result.append("\n");

        result.append(emitLRTable());

        result.append("\n");

        result.append(emitProductionsForLR());

        result.append("\n");

        result.append(emitErrorTableLR());

        return result.toString();
    }
    
	private Object emitProductionsForLR()
	{
		StringBuffer result = new StringBuffer();
		
		List<Production> prods = grammar.getProductions();
		
		result.append("        public static readonly int[][] PRODUCTIONS =\n");
		result.append("        {\n");
		
		for (int i=0; i<prods.size(); i++)
		{
			result.append("            new[] { ");
			result.append(prods.get(i).get_lhs());
			result.append(", ");
			result.append(prods.get(i).get_rhs().size());
			result.append(" },\n");
		}		
		result.setLength(result.length()-2);
		result.append("\n        };\n");
		
		return result.toString();
	}
	
	private String emitLRTable()
	{
		StringBuffer result = new StringBuffer();
				
		int[][][] tbl = lrTable;
		
		result.append("        public static readonly int[][][] PARSER_TABLE =\n");
		result.append("        {\n");
		
		int max = tbl.length;
		if (grammar.getProductions().size() > max)
			max = grammar.getProductions().size();
			
		max = (""+max).length();
		for (int i=0; i< tbl.length; i++)
		{
			result.append("            new[]\n"+
			              "            {");
			for (int j=0; j<tbl[i].length; j++)
			{
				if(j%5 == 0)
					result.append("\n               ");
				
				result.append(" new[] { ");
				result.append(Command.CONSTANTS[tbl[i][j][0]]);
				result.append(", ");
				String str = ""+tbl[i][j][1];
				for (int k=str.length(); k<max; k++)
					result.append(" ");
				result.append(str).append("},");
			}
			result.setLength(result.length()-1);
			result.append("\n            },\n");
		}	
		result.setLength(result.length()-2);
		result.append("\n        };\n");
		
		return result.toString();
	}
	
    private String genLLSyntTables() throws NotLLException {
        StringBuffer result = new StringBuffer();

        if (options.parser == Options.PARSER_LL) {
            int start = grammar.getStartSymbol();
            int fnt = grammar.FIRST_NON_TERMINAL;
            int fsa = grammar.getSymbols().length;

            String syntConsts = "        public const int START_SYMBOL = " + start + ";\n" +
                    "\n" +
                    "        public const int FIRST_NON_TERMINAL    = " + fnt + ";\n" +
                    "        public const int FIRST_SEMANTIC_ACTION = " + fsa + ";\n";

            result.append(syntConsts);

            result.append("\n");

            result.append(emitLLTable(new LLParser(grammar)));

            result.append("\n");

            result.append(emitProductionsForLL());

            result.append("\n");

            result.append(emitErrorTableLL());

            return result.toString();
        } else if (options.parser == Options.PARSER_REC_DESC)
            return emitErrorTableLL();
        else
            return null;
    }
    
    private String lex_table_compress()
	{
		StringBuffer result = new StringBuffer();
		
		Map[] trans = finiteAutomata.getTransitions();
		
		
		int[] sti = new int[trans.length+1];
		int count = 0;
		for (int i=0; i<trans.length; i++)
		{
			sti[i] = count;
			count += trans[i].size();
		}
		sti[sti.length-1] = count;
		
		int[][] st = new int[count][2];
		
		count = 0;
		for (int i=0; i<trans.length; i++)
		{
			for (Iterator iter = trans[i].keySet().iterator(); iter.hasNext();)
			{
				Character ch = (Character) iter.next();
				Integer itg = (Integer) trans[i].get(ch); 
				
				st[count][0] = ch.charValue();
				st[count][1] = itg.intValue();
				
				count++;
			}
		}
		
		result.append("        public static readonly int[] SCANNER_TABLE_INDEXES = \n");
		result.append("        {");
		for (int i=0; i<sti.length; i++)
		{
			if(i%32 == 0)
				result.append("\n            ");
			result.append(sti[i]).append(", ");
		}		
		
		result.setLength(result.length()-2);
		result.append("\n        };\n\n");	
		
		result.append("        public static readonly int[][] SCANNER_TABLE = \n");
		result.append("        {");
		for (int i=0; i<st.length; i++)
		{
			if(i%6 == 0)
				result.append("\n            ");
			result.append("new[] {")
			      .append(st[i][0])
			      .append(", ")
			      .append(st[i][1])
			      .append("}, ");
		}		

		result.setLength(result.length()-2);
		result.append("\n        };\n");	
		
		return result.toString();
	}
	
	private String lex_table()
	{
		StringBuffer result = new StringBuffer();
		result.append("        public static readonly int[][] SCANNER_TABLE =\n");
        result.append("        {\n");
		
        int count = finiteAutomata.getTransitions().length;
		int max = String.valueOf(count).length();
		if (max == 1)
            max = 2;
            
		int indent = 0;	
		
		for (int i=0; i<count; i++)
		{
			result.append("            new[]\n");
			result.append("            {\n");
			result.append("                ");
			for (char c = 0; c<256; c++)
			{
				String n = String.valueOf(finiteAutomata.nextState(c, i));
				for (int j = n.length(); j<max; j++)
					result.append(" ");
				result.append(n).append(", ");
				if(++indent%16 == 0 && c<255)
					result.append("\n                ");
			}
			result.setLength(result.length()-2);
			result.append("\n			},\n");
		}
		result.setLength(result.length()-2);
		
		result.append("\n        };\n");
		
		return result.toString();
	}
	
	private String token_state()
	{
		StringBuffer result = new StringBuffer();
		
		result.append("        public static readonly int[] TOKEN_STATE =\n");
		result.append("        {\n");
		result.append("            ");
		int count = finiteAutomata.getTransitions().length;
		int max = String.valueOf(count).length();
		if (max == 1)
			max = 2; 
		
		for (int i=0; i<count; i++)
		{
			int fin = finiteAutomata.tokenForState(i);
			String n = String.valueOf(fin);
			for (int j = n.length(); j<max; j++)
				result.append(" ");
			result.append(n).append(", ");
		}
		result.setLength(result.length()-2);		
		result.append("\n        };\n");
		
		return result.toString();
	}
	
	private String special_cases()
	{
		int[][] indexes = finiteAutomata.getSpecialCasesIndexes();
		FiniteAutomata.KeyValuePar[] sc = finiteAutomata.getSpecialCases();
		
		StringBuffer result = new StringBuffer();
		
		int count = sc.length;
							
		result.append(
			"        public static readonly int[] SPECIAL_CASES_INDEXES =\n"+
			"        {\n"+
			"            ");
		
		count = indexes.length;
		for (int i=0; i<count; i++)
		{
			result.append(indexes[i][0]).append(", ");
		}
		result.append(indexes[count-1][1]);
		result.append("\n        };\n\n");
				
		result.append(
					"        public static readonly string[] SPECIAL_CASES_KEYS =\n"+
					"        {\n"+
					"            ");
		count = sc.length;
		for (int i=0; i<count; i++)
		{
			result.append("\"").append(sc[i].key).append("\", ");
		}
		result.setLength(result.length()-2);
				
		result.append("\n        };\n\n");
		
		result.append(
					"        public static readonly int[] SPECIAL_CASES_VALUES =\n"+
					"        {\n"+
					"            ");
		count = sc.length;
		for (int i=0; i<count; i++)
		{
			result.append(sc[i].value).append(", ");
		}
		result.setLength(result.length()-2);
				
		result.append("\n        };\n");
		
		return result.toString();
	}
	
	private String emitProductionsForLL()
	{
		
		List<Production> pl = grammar.getProductions();
		String[][] productions = new String[pl.size()][];
		int max = 0;
		for (int i=0; i< pl.size(); i++)
		{
			IntList rhs = pl.get(i).get_rhs();
			if (rhs.size() > 0)
			{
				productions[i] = new String[rhs.size()];
				for (int j=0; j<rhs.size(); j++)
				{
					productions[i][j] = String.valueOf(rhs.get(j));
					if (productions[i][j].length() > max)
						max = productions[i][j].length();
				}
			}
			else
			{
				productions[i] = new String[1];
				productions[i][0] = "0";
			}
		}
		
		StringBuffer bfr = new StringBuffer();
		
		bfr.append("        public static readonly int[][] PRODUCTIONS = \n");
		bfr.append("        {\n");
		
		for (int i=0; i< productions.length; i++)
		{
			bfr.append("            new[] {");
			for (int j=0; j<productions[i].length; j++)
			{
				bfr.append(" ");
				for (int k = productions[i][j].length(); k<max; k++)
					bfr.append(" ");
				bfr.append(productions[i][j]).append(",");
			}
			bfr.setLength(bfr.length()-1);
	 		bfr.append(" },\n");
		}	
		bfr.setLength(bfr.length()-2);
		bfr.append("\n        };\n");
		
		return bfr.toString();
	}
	
	private String emitLLTable(LLParser g)
	{
		int[][] tbl = g.generateTable();
		String[][] table = new String[tbl.length][tbl[0].length];
		
		int max = 0;
		for (int i = 0; i < table.length; i++)
		{
			for (int j = 0; j < table[i].length; j++)
			{
				String tmp = String.valueOf(tbl[i][j]);
				table[i][j] = tmp;
				if (tmp.length() > max)
					max = tmp.length();
			}
		}
		
		StringBuffer bfr = new StringBuffer();
		
		bfr.append("        public static readonly int[][] PARSER_TABLE =\n");
		bfr.append("        {\n");
		
		for (int i=0; i< table.length; i++)
		{
			bfr.append("            new[] {");
			for (int j=0; j<table[i].length; j++)
			{
				bfr.append(" ");
				for (int k = table[i][j].length(); k<max; k++)
					bfr.append(" ");
				bfr.append(table[i][j]).append(",");
			}
			bfr.setLength(bfr.length()-1);
	 		bfr.append("\n            },\n");
		}	
		bfr.setLength(bfr.length()-2);
		bfr.append("\n        };\n");
		
		return bfr.toString();
	}
	
	private String emitErrorTableLR()
	{
		int count = lrTable.length;
		
		StringBuffer result = new StringBuffer();
	
		result.append(
		"        public static readonly string[] PARSER_ERROR =\n"+
		"        {\n");
		
		for (int i=0; i< count; i++)
		{
			result.append("            \"Erro estado "+i+"\",\n");
		}
		
		result.setLength(result.length()-2);
		result.append(
		"\n        };\n");
	
		return result.toString();
	}
	
	private String emitErrorTableLL()
	{
		String[] symbs = grammar.getSymbols();
		StringBuffer result = new StringBuffer();
		
		result.append(
		"        public static readonly string[] PARSER_ERROR =\n"+
		"        {\n"+
		"            \"\",\n"+
		"            \"Era esperado fim de programa\",\n");
		
		for (int i=2; i< grammar.FIRST_NON_TERMINAL; i++)
		{
			result.append("            \"Era esperado ");
			for (int j=0; j<symbs[i].length(); j++)
			{
				switch (symbs[i].charAt(j))
				{
					case '\"': result.append("\\\""); break;
					case '\\': result.append("\\\\"); break;
					default: result.append(symbs[i].charAt(j));				
				}
			}
			
			result.append("\",\n");
		}
					
		for (int i=grammar.FIRST_NON_TERMINAL; i< symbs.length; i++)
			result.append("            \""+symbs[i]+" invÃ¡lido\",\n");
			
		result.setLength(result.length()-2);
		result.append(
		"\n        };\n");
		
		return result.toString();
	}
}
