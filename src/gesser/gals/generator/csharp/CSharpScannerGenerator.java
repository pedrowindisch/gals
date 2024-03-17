package gesser.gals.generator.csharp;

import gesser.gals.generator.Options;
import gesser.gals.generator.scanner.FiniteAutomata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 
 * @author Gustavo
 * @see gesser.gals.generator.java.JavaScannerGenerator
 */
public class CSharpScannerGenerator
{
    private Options options;
    private FiniteAutomata finiteAutomata;

	boolean sensitive = true;
	boolean lookup = true;
	
    public Map<String, String> generate(FiniteAutomata fa, Options o) {
        options = o;
        finiteAutomata = fa;

        Map<String, String> result = new HashMap<String, String>();

        String classname = options.scannerName;

        String scanner;
        if (fa != null) {
            sensitive = options.scannerCaseSensitive;
            lookup = finiteAutomata.getSpecialCases().length > 0;
            scanner = buildScanner();
        } else
            scanner = buildEmptyScanner();

        StringBuffer scannerBuffer = new StringBuffer();

        scannerBuffer.append(scanner);

        addNamespace(scannerBuffer);

        scannerBuffer.insert(0, "\n");

        scannerBuffer.insert(0, emitStaticImports());
        scannerBuffer.insert(0, emitStaticImports());
        scannerBuffer.insert(0, emitStaticImports());
        scannerBuffer.insert(0, "using System.IO;\n");
        scannerBuffer.insert(0, "using System;\n");

        result.put(classname + ".cs", scannerBuffer.toString());

        return result;
    }
    
	private String buildEmptyScanner()
    {
        StringBuffer result = new StringBuffer();
        String cls = "    public class " + options.scannerName + "\n" +
                "    {\n" +
                "        public Token NextToken()\n" +
                "        {\n" +
                "            return null;\n" +
                "        }\n" +
                "    }\n" +
                "";
        result.append(cls);
        addNamespace(result);

        result.insert(0, emitStaticImports());
        return result.toString();
    }

    private void addNamespace(StringBuffer buffer) {
        String _package = options.pkgName;
        if (_package != null && _package.length() > 0) {
            buffer.append("namespace ");
            buffer.append(_package);
            buffer.append(";\n\n");
        }
    }
    
    private String emitStaticImports()
    {
        String imports = "";
        
        if (options.pkgName != null && !options.pkgName.equals("")) {
            imports += "using static " + options.pkgName + ".Constants;\n";
            imports += "using static " + options.pkgName + ".ParserConstants;\n";
            imports += "using static " + options.pkgName + ".ScannerConstants;\n";
        }
        
        return imports;
    }

	private String emitImports()
    {
        String imports = "using System.Collections;\n";
        imports += emitStaticImports();

        return imports;
    }

	private String buildScanner() {
		String cls = 
		"    public class "+options.scannerName+"\n"+
		"    {\n";
		if (options.input == Options.INPUT_STREAM)
		{
			cls +=    "        private int _position = 0;\n"
					+ "        \n"
					+ "        private string _input;\n"
					+ "        public string Input \n"
					+ "        {\n"
					+ "            get => _input;\n"
					+ "            set \n"
					+ "            { \n"
					+ "                _input = value; \n"
					+ "                _position = 0;\n"
					+ "            }\n"
					+ "        }\n"
					+ "\n"
					+ "        public " + options.scannerName + "() : this(\"\") { }\n"
					+ "\n"
					+ "        public " + options.scannerName + "(string input) => Input = input;\n"
					+ "\n"
					+ "        private char NextChar()\n"
					+ "        {\n"
					+ "            if (HasInput())\n"
					+ "                return Input[_position++];\n"
					+ "            else\n"
					+ "                return char.MaxValue;\n"
					+ "        }\n"
					+ "\n"
					+ "        private char PeekNextChar()\n"
					+ "\n"
					+ "        {\n"
					+ "            return Input[_position];\n"
					+ "        }\n"
					+ "\n"
					+ "        private bool HasInput()\n"
					+ "        {\n"
					+ "            return _position < Input.Length;\n"
					+ "        }\n"
					+ "\n";			
		}
		else
		{
			cls +=    "        private int _position = 0;\n"
					+ "\n"
					+ "        private StreamReader _streamReader;\n"
					+ "\n"
					+ "        private Stream _input;\n"
					+ "        public Stream Input\n"
					+ "        {\n"
					+ "            get => _input;\n"
					+ "            set\n"
					+ "            {\n"
					+ "                _streamReader?.Dispose();\n"
					+ "\n"
					+ "                _streamReader = null;\n"
					+ "                _input = value;\n"
					+ "                if (_input != null)\n"
					+ "                {\n"
					+ "                    _input.Position = 0;\n"
					+ "                    _streamReader = new StreamReader(_input);\n"
					+ "                }\n"
					+ "            }\n"
					+ "        }\n"
					+ "\n"
					+ "        public " + options.scannerName + "() : this(null) { }\n"
					+ "\n"
					+ "        public " + options.scannerName + "(Stream input) => Input = input;\n"
					+ "\n"
					+ "        private char NextChar()\n"
					+ "        {\n"
					+ "            _position++;\n"
					+ "            return (char)_streamReader.Read();\n"
					+ "        }\n"
					+ "\n"
					+ "        private char PeekNextChar()\n"
					+ "        {\n"
					+ "            return (char)_streamReader.Peek();\n"
					+ "        }\n"
					+ "\n"
					+ "        private bool HasInput()\n"
					+ "        {\n"
					+ "            return !_streamReader.EndOfStream;\n"
					+ "        }"
					+ "\n";
		}
		cls +=
	
		"\n"+
		mainDriver()+
		"\n"+
		auxFuncions()+
		
		"    }\n"+
		"";
		
		return cls;
	}
	private String mainDriver()
	{
		return 
		"        public Token NextToken()\n"+
		"        {\n"+
		"            if ( ! HasInput() )\n"+
		"                return null;\n"+
		"\n"+		
		"            int start = _position;\n"+
		"\n"+		
		"            int state = 0;\n"+
		"            int lastState = 0;\n"+
		"            int endState = -1;\n"+
		
		"            string lexeme = \"\";\n"+
		(finiteAutomata.hasContext() ?
		"            int ctxtState = -1;\n"+
		"            int ctxtEnd = -1;\n" : "")+
		"\n"+
		"            while (HasInput())\n"+
		"            {\n"+
		"                lastState = state;\n"+
		"                state = NextState(PeekNextChar(), state);\n"+
		"\n"+
		"                if (state < 0)\n"+
		"                    break;\n"+
		"\n"+
		"                else\n"+
		"                {\n"+
		
		"                    lexeme += NextChar();\n"+
		"                    if (TokenForState(state) >= 0)\n"+
		"                    {\n"+
		"                        endState = state;\n"+
		"                    }\n"+
		(finiteAutomata.hasContext() ? 
		"                    if (SCANNER_CONTEXT[state][0] == 1)\n" +
		"                    {\n" +
		"                        ctxtState = state;\n" +
		"                        ctxtEnd = _position;\n" +
		"                    }\n" : "")+
		"                }\n"+
		"            }\n"+
		"            if (endState < 0 || (endState != state && TokenForState(lastState) == -2))\n"+
		"                throw new LexicalError(ScannerConstants.SCANNER_ERROR[lastState], start);\n"+
		"\n"+
		(finiteAutomata.hasContext() ? 
		"            if (ctxtState != -1 && SCANNER_CONTEXT[endState][1] == ctxtState)\n"+
		"                end = ctxtEnd;\n"+
		"\n" : "" )+
		"\n"+
		"            int token = TokenForState(endState);\n"+
		"\n"+
		"            if (token == 0)\n"+
		"                return NextToken();\n"+
		"            else\n"+
		"            {\n"+
		(lookup ?
		"                token = LookupToken(token, lexeme);\n" : "")+
		"                return new Token(token, lexeme, start);\n"+
		"            }\n"+
		"        }\n"+
		"";
	}
	private String auxFuncions()
	{		 
		String nextState;
		
		switch (options.scannerTable)
		{
			case Options.SCANNER_TABLE_FULL:
				nextState =
					"        private int NextState(char c, int state)\n"+
					"        {\n"+
					"            int next = ScannerConstants.SCANNER_TABLE[state][c];\n"+
					"            return next;\n"+
					"        }\n";
                break;
			case Options.SCANNER_TABLE_COMPACT:
				nextState =
					
					"        private int NextState(char c, int state)\n"+
					"        {\n"+
					"            int start = ScannerConstants.SCANNER_TABLE_INDEXES[state];\n"+
					"            int end   = ScannerConstants.SCANNER_TABLE_INDEXES[state+1]-1;\n"+
					"\n"+
					"            while (start <= end)\n"+
					"            {\n"+
					"                int half = (start+end)/2;\n"+
					"\n"+
					"                if (ScannerConstants.SCANNER_TABLE[half][0] == c)\n"+
					"                    return ScannerConstants.SCANNER_TABLE[half][1];\n"+
					"                else if (ScannerConstants.SCANNER_TABLE[half][0] < c)\n"+
					"                    start = half+1;\n"+
					"                else  //(ScannerConstants.SCANNER_TABLE[half][0] > c)\n"+
					"                    end = half-1;\n"+
					"            }\n"+
					"\n"+
					"            return -1;\n"+
					"        }\n";
                break;
			case Options.SCANNER_TABLE_HARDCODE:
			{
				Map[] trans = finiteAutomata.getTransitions();
				StringBuffer casesState = new StringBuffer();
				for (int i=0; i<trans.length; i++)
				{
					Map m = trans[i];
					if (m.size() == 0)
						continue;
						
					casesState.append(
				"            case "+i+":\n"+
				"                switch ((byte)c)\n"+
				"                {\n");
				
                    for (Iterator iter = m.entrySet().iterator(); iter.hasNext(); )
					{
						Map.Entry entry = (Entry) iter.next();
						Character ch = (Character) entry.getKey();
						Integer it = (Integer) entry.getValue();
						casesState.append(
				"                    case "+((int)ch.charValue())+": return "+it+";\n");
					}
				
					casesState.append(
				"                    default: return -1;\n"+
				"                }\n");
				}
				
				nextState = 
				"    private int NextState(char c, int state)\n"+
				"    {\n"+
				"        switch (state)\n"+
				"        {\n"+
				casesState.toString()+
				"            default: return -1;\n"+
				"        }\n"+
				"    }\n";
			}
				break;
			default:
				//nunca acontece
				nextState = null;
		}
		
		return 
		nextState+
		"\n"+
		"        private int TokenForState(int state)\n"+
		"        {\n"+
		"            if (state < 0 || state >= ScannerConstants.TOKEN_STATE.Length)\n"+
		"                return -1;\n"+
		"\n"+
		"            return ScannerConstants.TOKEN_STATE[state];\n"+
		"        }\n"+
		"\n"+
		(lookup ?
		"        public int LookupToken(int @base, string key)\n"+
		"        {\n"+
		"            int start = ScannerConstants.SPECIAL_CASES_INDEXES[@base];\n"+
		"            int end   = ScannerConstants.SPECIAL_CASES_INDEXES[@base+1]-1;\n"+
		"\n"+
		(sensitive?"":
		"            key = key.ToUpper();\n"+
		"\n")+
		"            while (start <= end)\n"+
		"            {\n"+
		"                int half = (start+end)/2;\n"+
		"                int comp = ScannerConstants.SPECIAL_CASES_KEYS[half].CompareTo(key);\n"+
		"\n"+
		"                if (comp == 0)\n"+
		"                    return ScannerConstants.SPECIAL_CASES_VALUES[half];\n"+
		"                else if (comp < 0)\n"+
		"                    start = half+1;\n"+
		"                else  //(comp > 0)\n"+
		"                    end = half-1;\n"+
		"            }\n"+		
		"\n"+
		"            return @base;\n"+
		"        }\n"+
		"\n":"")+
		"\n";
	}
}