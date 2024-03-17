package gesser.gals.generator.csharp;

import gesser.gals.generator.Options;
import gesser.gals.generator.RecursiveDescendent;
import gesser.gals.generator.RecursiveDescendent.Function;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.parser.ll.NotLLException;
import gesser.gals.util.IntList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CSharpParserGenerator
{
    private Options options;
    private Grammar grammar;

	public Map generate(Grammar g, Options o) throws NotLLException
    {
        options = o;
        grammar = g;

		Map result = new HashMap();
		
		if (g != null)
		{		
			String classname = options.parserName;
			
			String parser;
			
			switch (options.parser)
			{
				case Options.PARSER_REC_DESC:
					parser = buildRecursiveDecendantParser();
					break;
				case Options.PARSER_LL:
					parser = buildLLParser();
					break;
				case Options.PARSER_SLR:
				case Options.PARSER_LALR:
				case Options.PARSER_LR:	
					parser = buildLRParser();
					break;
				default:
					parser = null;
			}
							
			result.put(classname+".cs", parser);
			
			result.put(options.semanticName + ".cs", generateSemanticAnalyser(options));
		}
		
		return result;
	}

	private String buildRecursiveDecendantParser() throws NotLLException
	{
		StringBuffer result = new StringBuffer();
	
		String package_ = options.pkgName;
	
		result.append(emitPackage(package_));
	
		result.append(emitRecursiveDecendantClass());
	
		return result.toString();
	}

	private String buildLLParser()
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		
		result.append(emitPackage(package_));
		
		result.append(emitImports());
		
		result.append(emitLLClass());
		
		return result.toString();
	}

	private String buildLRParser()
	{
		StringBuffer result = new StringBuffer();
	
		String package_ = options.pkgName;
	
		result.append(emitPackage(package_));
	
		result.append(emitImports());
	
		result.append(emitLRClass());
	
		return result.toString();
	}

	private String emitPackage(String package_)
    {
        if (package_ != null && !package_.equals(""))
            return "namespace " + package_ + ";\n\n";
        else
            return "";
    }
    
    private String emitStaticImports()
    {
        String imports = "";
        
        if (options.pkgName != null && !options.pkgName.equals("")) {
            imports += "using static " + options.pkgName + ".Constants;\n";
            imports += "using static " + options.pkgName + ".ParserConstants;\n";
        }
        
        return imports;
    }

	private String emitImports()
    {
        String imports = "using System.Collections;\n";
        imports += emitStaticImports();

        return imports;
    }

	private String emitLRClass()
	{
		StringBuffer result = new StringBuffer();
	
		String classname = options.parserName;
		result.append("public class ").append(classname).append("\n{\n");
	
		String scannerName = options.scannerName;
		String semanName = options.semanticName;
	
		String variables = 
		"    private Stack stack = new Stack();\n"+
		"    private Token currentToken;\n"+
		"    private Token previousToken;\n"+
		"    private "+scannerName+" scanner;\n"+
		"    private "+semanName+" semanticAnalyser;\n"+
		"\n";
		
		result.append(variables);
			
		result.append(
		
		"    public void parse("+scannerName+" scanner, "+semanName+" semanticAnalyser)\n"+
		"    {\n"+
		"        this.scanner = scanner;\n"+
		"        this.semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"        stack.Clear();\n"+
		"        stack.Push(0);\n"+
		"\n"+
		"        currentToken = scanner.NextToken();\n"+
		"\n"+
		"        while ( ! step() )\n"+
		"            ;\n"+
		"    }\n"+		
		"\n"+
		"    private boolstep()\n"+
		"    {\n"+
		"        if (currentToken == null)\n"+
        "        {\n"+
		"            int pos = 0;\n"+
		"            if (previousToken != null)\n"+
		"                pos = previousToken.GetPosition()+previousToken.GetLexeme().Length;\n"+
		"\n"+
        "            currentToken = new Token(Constants.DOLLAR, \"$\", pos);\n"+
        "        }\n"+
        "\n"+
        "        int token = currentToken.GetId();\n"+
		"        int state = (int) stack.Peek();\n"+
		"\n"+
        "        int[] cmd = ParserConstants.PARSER_TABLE[state][token-1];\n"+
		"\n"+
		"        switch (cmd[0])\n"+
		"        {\n"+
		"            case SHIFT:\n"+
		"                stack.Push(cmd[1]);\n"+
		"                previousToken = currentToken;\n"+
		"                currentToken = scanner.NextToken();\n"+
		"                return false;\n"+
		"\n"+
		"            case REDUCE:\n"+
		"                int[] prod = ParserConstants.PRODUCTIONS[cmd[1]];\n"+
		"\n"+
		"                for (int i=0; i<prod[1]; i++)\n"+
		"                    stack.pop();\n"+
		"\n"+
		"                int oldState = ((Integer)stack.Peek()).intValue();\n"+
		"                stack.Push(new Integer(ParserConstants.PARSER_TABLE[oldState][prod[0]-1][1]));\n"+
		"                return false;\n"+
		"\n"+
		"            case ACTION:\n"+
		"                int action = ParserConstants.FIRST_SEMANTIC_ACTION + cmd[1] - 1;\n"+
		"                stack.Push(new Integer(ParserConstants.PARSER_TABLE[state][action][1]));\n"+
		"                semanticAnalyser.executeAction(cmd[1], previousToken);\n"+
		"                return false;\n"+
		"\n"+
		"            case ACCEPT:\n"+
		"                return true;\n"+
		"\n"+
		"            case ERROR:\n"+
		"                throw new SyntaticError(ParserConstants.PARSER_ERROR[state], currentToken.GetPosition());\n"+
		"        }\n"+
		"        return false;\n"+
		"    }\n"+
		"\n"
	
		);
		result.append("}\n");

		return result.toString();
	}

	private String emitLLClass()
	{
		StringBuffer result = new StringBuffer();
		
		String classname = options.parserName;
		result.append("public class ").append(classname).append("\n{\n");
		
		String scannerName = options.scannerName;
		String semanName = options.semanticName;
		
		String variables = 
		"    private Stack stack = new Stack();\n"+
		"    private Token currentToken;\n"+
		"    private Token previousToken;\n"+
		"    private "+scannerName+" scanner;\n"+
		"    private "+semanName+" semanticAnalyser;\n"+
		"\n";
		
		result.append(variables);
				
		result.append(emitLLFunctions());
		
		result.append("}\n");

		return result.toString();
	}

	private String emitLLFunctions()
	{
		StringBuffer result = new StringBuffer();
		
		result.append(emitTesters());
		
		result.append("\n");
		
		result.append(emitStep());
		
		result.append("\n");
		
		result.append(emitDriver());
		
		
		return	result.toString();
	}

	private String emitTesters()
	{
		return 
		"    private static bool isTerminal(int x)\n"+
		"    {\n"+
		"        return x < ParserConstants.FIRST_NON_TERMINAL;\n"+
		"    }\n"+
		"\n"+
		"    private static bool isNonTerminal(int x)\n"+
		"    {\n"+
		"        return x >= ParserConstants.FIRST_NON_TERMINAL && x < ParserConstants.FIRST_SEMANTIC_ACTION;\n"+
		"    }\n"+
		"\n"+
		"    private static bool isSemanticAction(int x)\n"+
		"    {\n"+
		"        return x >= ParserConstants.FIRST_SEMANTIC_ACTION;\n"+
		"    }\n"+
		"";
	}
	
	private String emitDriver()
	{
		String scannerName = options.scannerName;
		String semanName   = options.semanticName;
				
		return 
		"    public void parse("+scannerName+" scanner, "+semanName+" semanticAnalyser)\n"+
	    "    {\n"+
		"        this.scanner = scanner;\n"+
		"        this.semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"        stack.Clear();\n"+
		"        stack.Push(Constants.DOLLAR);\n"+
		"        stack.Push(ParserConstants.START_SYMBOL);\n"+
		"\n"+
		"        currentToken = scanner.NextToken();\n"+
		"\n"+
		"        while ( ! step() )\n"+
		"            ;\n"+
	    "    }\n"+		
		"";
	}

	private String emitStep()
	{
		return 
		"    private bool step()\n"+
		"    {\n"+			
		"        if (currentToken == null)\n"+
        "        {\n"+
		"            int pos = 0;\n"+
		"            if (previousToken != null)\n"+
		"                pos = previousToken.GetPosition()+previousToken.GetLexeme().Length;\n"+
		"\n"+
        "            currentToken = new Token(Constants.DOLLAR, \"$\", pos);\n"+
        "        }\n"+
        "\n"+
		"        int x = (int) stack.Pop();\n"+
		"        int a = currentToken.GetId();\n"+
		"\n"+
		"        if (x == Constants.EPSILON)\n"+
		"        {\n"+
		"            return false;\n"+
		"        }\n"+
		"        else if (isTerminal(x))\n"+
		"        {\n"+
		"            if (x == a)\n"+
		"            {\n"+
		"                if (stack.Count == 0)\n"+
		"                    return true;\n"+
		"                else\n"+
		"                {\n"+
		"                    previousToken = currentToken;\n"+
		"                    currentToken = scanner.NextToken();\n"+
		"                    return false;\n"+
		"                }\n"+
		"            }\n"+
		"            else\n"+
		"            {\n"+
		"                throw new SyntaticError(ParserConstants.PARSER_ERROR[x], currentToken.GetPosition());\n"+
		"            }\n"+
		"        }\n"+
		"        else if (isNonTerminal(x))\n"+
		"        {\n"+
		"            if (PushProduction(x, a))\n"+
		"                return false;\n"+
		"            else\n"+
		"                throw new SyntaticError(ParserConstants.PARSER_ERROR[x], currentToken.GetPosition());\n"+
		"        }\n"+
		"        else // isSemanticAction(x)\n"+
		"        {\n"+
		"            semanticAnalyser.executeAction(x-ParserConstants.FIRST_SEMANTIC_ACTION, previousToken);\n"+
		"            return false;\n"+
		"        }\n"+
		"    }\n"+
		"\n"+
		"    private bool PushProduction(int topStack, int tokenInput)\n"+
		"    {\n"+
		"        int p = ParserConstants.PARSER_TABLE[topStack-ParserConstants.FIRST_NON_TERMINAL][tokenInput-1];\n"+
		"        if (p >= 0)\n"+
		"        {\n"+
		"            int[] production = ParserConstants.PRODUCTIONS[p];\n"+
		"            //empilha a produ��o em ordem reversa\n"+
		"            for (int i=production.Length-1; i>=0; i--)\n"+
		"            {\n"+
		"                stack.Push(production[i]);\n"+
		"            }\n"+
		"            return true;\n"+
		"        }\n"+
		"        else\n"+
		"            return false;\n"+
		"    }\n"+
		"";
	}
	
	private String emitRecursiveDecendantClass() throws NotLLException
	{
		RecursiveDescendent rd = new RecursiveDescendent(grammar);
		
		StringBuffer result = new StringBuffer();

		String classname = options.parserName;
		result.append("public class ").append(classname).append("\n{\n");

		String scannerName = options.scannerName;
		String semanName = options.semanticName;

		String variables = 
		"    private Token currentToken;\n"+
		"    private Token previousToken;\n"+
		"    private "+scannerName+" scanner;\n"+
		"    private "+semanName+" semanticAnalyser;\n"+
		"\n";
	
		result.append(variables);
		
		result.append(	
		"    public void parse("+scannerName+" scanner, "+semanName+" semanticAnalyser)\n"+
		"    {\n"+
		"        this.scanner = scanner;\n"+
		"        this.semanticAnalyser = semanticAnalyser;\n"+
		"\n"+
		"        currentToken = scanner.NextToken();\n"+
		"        if (currentToken == null)\n"+
		"            currentToken = new Token(Constants.DOLLAR, \"$\", 0);\n"+
		"\n"+
		"        "+rd.getStart()+"();\n"+
		"\n"+
		"        if (currentToken.GetId() != Constants.DOLLAR)\n"+
		"            throw new SyntaticError(ParserConstants.PARSER_ERROR[Constants.DOLLAR], currentToken.GetPosition());\n"+
		"    }\n"+		
		"\n"+
		"    private void match(int token)\n"+
		"    {\n"+
		"        if (currentToken.GetId() == token)\n"+
		"        {\n"+
		"            previousToken = currentToken;\n"+
		"            currentToken = scanner.NextToken();\n"+
		"            if (currentToken == null)\n"+
		"            {\n"+
		"                int pos = 0;\n"+
		"                if (previousToken != null)\n"+
		"                    pos = previousToken.GetPosition()+previousToken.GetLexeme().Length;\n"+
		"\n"+
		"                currentToken = new Token(Constants.DOLLAR, \"$\", pos);\n"+
		"            }\n"+
		"        }\n"+
		"        else\n"+
		"            throw new SyntaticError(ParserConstants.PARSER_ERROR[token], currentToken.GetPosition());\n"+
		"    }\n"+
		"\n");

		Map funcs = rd.build();

		for (int symb=grammar.FIRST_NON_TERMINAL; symb<grammar.FIRST_SEMANTIC_ACTION(); symb++)
		{
			String name = rd.getSymbols(symb);
			RecursiveDescendent.Function f = (Function) funcs.get(name);
			
			result.append(
						"    private void "+name+"()\n"+
						"    {\n"+
						"        switch (currentToken.GetId())\n"+
						"        {\n" );
					
			List keys = new LinkedList(f.input.keySet());
					
			for (int i = 0; i<keys.size(); i++)
			{
				IntList rhs = (IntList) f.input.get(keys.get(i));
				int token = ((Integer)keys.get(i)).intValue();
	
				result.append(
						"            case "+token+": // "+rd.getSymbols(token)+"\n");
				for (int j=i+1; j<keys.size(); j++)
				{
					IntList rhs2 = (IntList) f.input.get(keys.get(j));
					if (rhs2.equals(rhs))
					{
						token = ((Integer)keys.get(j)).intValue();
						result.append(
						"            case "+token+": // "+rd.getSymbols(token)+"\n");
						keys.remove(j);
						j--;
					}
				}
				
				if (rhs.size() == 0)
					result.append(
						"                // Constants.EPSILON\n");	
			
				for (int k=0; k<rhs.size(); k++)
				{
					int s = rhs.get(k);
					if (grammar.isTerminal(s))
					{
						result.append(
						"                match("+s+"); // "+rd.getSymbols(s)+"\n");	
					}
					else if (grammar.isNonTerminal(s))
					{
						result.append(
						"                "+rd.getSymbols(s)+"();\n");	
					}
					else //isSemanticAction(s)
					{
						result.append(
						"                semanticAnalyser.executeAction("+(s-grammar.FIRST_SEMANTIC_ACTION())+", previousToken);\n");
					}
				}
			
				result.append(
						"                break;\n");
			}

			result.append(
						"            default:\n"+
						"                throw new SyntaticError(ParserConstants.PARSER_ERROR["+f.lhs+"], currentToken.GetPosition());\n"+
						"        }\n"+
						"    }\n"+
						"\n");
		}
		
		result.append("}\n");

		return result.toString();
	}
	
	private String generateSemanticAnalyser(Options options)
	{
		StringBuffer result = new StringBuffer();
		
		String package_ = options.pkgName;
		if (package_ != null && !package_.equals(""))
			result.append("namespace " + package_ + ";\n\n");
			
		String cls = 
		"public class "+options.semanticName+"\n"+
		"{\n"+
		"    public void executeAction(int action, Token token)\n"+
		"    {\n"+
		"        Console.WriteLine(\"A��o #\"+action+\", Token: \"+token);\n"+
		"    }	\n"+
		"}\n"+
		"";
		
		result.append(cls);
		
		return result.toString();
	}
}
