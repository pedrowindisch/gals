package gesser.gals.generator;

import gesser.gals.util.XMLParsingException;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.StringTokenizer;

public class Options
{
	public String scannerName  = "Lexico";
	public String parserName   = "Sintatico";
	public String semanticName = "Semantico";
	public String pkgName      = "";
	
	public boolean generateScanner = true;
	public boolean generateParser  = true;
	
	public static final int LANG_JAVA   = 0;
	public static final int LANG_CPP    = 1;
	public static final int LANG_DELPHI = 2;
	public static final int LANG_CSHARP = 3;
	
	public int language = LANG_JAVA;
	
	public static final int PARSER_LR   = 0;
	public static final int PARSER_LALR = 1;
	public static final int PARSER_SLR  = 2;
	public static final int PARSER_LL   = 3;
	public static final int PARSER_REC_DESC = 4;
	
	public int parser = PARSER_SLR;
	
	public boolean scannerCaseSensitive = true;
	
	public static final int SCANNER_TABLE_FULL = 0;
	public static final int SCANNER_TABLE_COMPACT = 1;
	public static final int SCANNER_TABLE_HARDCODE = 2;

	public int scannerTable = SCANNER_TABLE_FULL;

	
	public static final int INPUT_STRING = 0;
	public static final int INPUT_STREAM = 1;

	public int input = INPUT_STRING;
	
	public String toString()
	{
		StringWriter bfr = new StringWriter();
		PrintWriter out = new PrintWriter(bfr);
		
		out.println("GenerateScanner = "+generateScanner);
		out.println("GenerateParser = "+generateParser);
		
		out.print("Language = ");
		switch (language)
		{
			case LANG_CPP:		out.println("C++"); break;
			case LANG_JAVA:	out.println("Java"); break;
			case LANG_DELPHI: out.println("Delphi"); break;
			case LANG_CSHARP: out.println("C#"); break;
		}
		
		out.println("ScannerName = "+scannerName);
		if (generateParser)
		{
			out.println("ParserName = "+parserName);
			out.println("SemanticName = "+semanticName);
		}
		if (pkgName.length() > 0)
			out.println("Package = "+pkgName);
		if (generateScanner)
		{
			out.println("ScannerCaseSensitive = "+scannerCaseSensitive);
			
			out.print("ScannerTable = ");
			switch (scannerTable)
			{
				case SCANNER_TABLE_FULL:		out.println("Full"); break;
				case SCANNER_TABLE_COMPACT:	out.println("Compact"); break;
				case SCANNER_TABLE_HARDCODE:	out.println("Hardcode"); break;
			}
			
			out.print("Input = ");
			switch (input)
			{
				case INPUT_STREAM:	out.println("Stream"); break;
				case INPUT_STRING:	out.println("String"); break;
			}
		}
		if (generateParser)
		{
			out.print("Parser = ");
			switch (parser)
			{
				case PARSER_LR:	out.println("LR"); break;
				case PARSER_LALR:	out.println("LALR"); break;
				case PARSER_SLR:	out.println("SLR"); break;
				case PARSER_LL:	out.println("LL"); break;
				case PARSER_REC_DESC:	out.println("RD"); break;
			}
		}		
		out.flush();
		return bfr.toString();
	}

	public static Options fromString(String str) throws XMLParsingException
	{
		Options o = new Options();
		
		LineNumberReader in = new LineNumberReader(new StringReader(str));
		String line = null;
		try
		{
			while ( (line = in.readLine()) != null)
			{   
				StringTokenizer tknzr = new StringTokenizer(line);
				
                if (! tknzr.hasMoreTokens())
                    continue;
                
				String name = tknzr.nextToken();
				if (!tknzr.hasMoreTokens())
					throw new XMLParsingException("Erro processando arquivo");
				tknzr.nextToken();//=
				String value = "";
				if (tknzr.hasMoreTokens())
					value = tknzr.nextToken();
				
				o.setOption(name, value);
			}
		}
		catch (IOException e)
		{
			throw new XMLParsingException("Erro processando arquivo");
		}
		
		return o;
	}

	/**
	 * @param name
	 * @param value
	 */
	private void setOption(String name, String value) throws XMLParsingException
	{
		if (name.equalsIgnoreCase("GenerateScanner"))
			generateScanner = Boolean.valueOf(value).booleanValue();
		else if (name.equalsIgnoreCase("GenerateParser"))
			generateParser = Boolean.valueOf(value).booleanValue();
		else if (name.equalsIgnoreCase("Language"))
		{
			if (value.equalsIgnoreCase("C++"))
				language = LANG_CPP;
			else if (value.equalsIgnoreCase("Java"))
				language = LANG_JAVA;
			else if (value.equalsIgnoreCase("Delphi"))
				language = LANG_DELPHI;
			else if (value.equalsIgnoreCase("C#"))
				language = LANG_CSHARP;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else if (name.equalsIgnoreCase("ScannerName"))
			scannerName = value;
		else if (name.equalsIgnoreCase("ParserName"))
			parserName = value;
		else if (name.equalsIgnoreCase("SemanticName"))
			semanticName = value;
		else if (name.equalsIgnoreCase("Package"))
			pkgName = value;
		else if (name.equalsIgnoreCase("ScannerCaseSensitive"))
			scannerCaseSensitive = Boolean.valueOf(value).booleanValue();
		else if (name.equalsIgnoreCase("ScannerTable"))
		{
			if (value.equalsIgnoreCase("Full"))
				scannerTable = SCANNER_TABLE_FULL;
			else if (value.equalsIgnoreCase("Compact"))
				scannerTable = SCANNER_TABLE_COMPACT;
			else if (value.equalsIgnoreCase("Hardcode"))
				scannerTable = SCANNER_TABLE_HARDCODE;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else if (name.equalsIgnoreCase("Input"))
		{
			if (value.equalsIgnoreCase("Stream"))
				input = INPUT_STREAM;
			else if (value.equalsIgnoreCase("String"))
				input = INPUT_STREAM;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else if (name.equalsIgnoreCase("Parser"))
		{
			if (value.equalsIgnoreCase("LR"))
				parser = PARSER_LR;
			else if (value.equalsIgnoreCase("LALR"))
				parser = PARSER_LALR;
			else if (value.equalsIgnoreCase("SLR"))
				parser = PARSER_SLR;
			else if (value.equalsIgnoreCase("LL"))
				parser = PARSER_LL;
			else if (value.equalsIgnoreCase("RD"))
				parser = PARSER_REC_DESC;
			else
				throw new XMLParsingException("Erro processando arquivo");
		}
		else
			throw new XMLParsingException("Erro processando arquivo");						
	}	
	
	public static void main(String[] args) throws XMLParsingException
	{
		String in = 
		"GenerateScanner = true\n"+
		"GenerateParser = true\n"+
		"Language = C++\n"+
		"ScannerName = Lexico\n"+
		"ParserName = Sintatico\n"+
		"SemanticName = Semantico\n"+
		"Package = \n"+
		"ScannerCaseSensitive = true\n"+
		"ScannerTable = Compact\n"+
		"Input = Stream\n"+
		"Parser = LALR\n";
		
		System.out.println(Options.fromString(in));
	}
}
