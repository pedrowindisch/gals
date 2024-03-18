package gesser.gals.generator;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;

/**
 * @author Gesser
 */
public class OptionsPane extends JPanel implements ChangeListener
{
	private JTextField scanner = new JTextField("Lexico");
	private JTextField parser = new JTextField("Sintatico");
	private JTextField semantic = new JTextField("Semantico");
	
	private JTextField pkgName = new JTextField("");	
	private JCheckBox pkg = new JCheckBox("Package / Namespace");
	
	private JRadioButton analLex = new JRadioButton("Analisador Lï¿½xico");
	private JRadioButton analSynt = new JRadioButton("Analisador Sintï¿½tico");
	private JRadioButton analBoth = new JRadioButton("Analisadores Lï¿½xico e Sintï¿½tico");
	
	private JRadioButton langJava = new JRadioButton("Java");
	private JRadioButton langCpp = new JRadioButton("C++");
	private JRadioButton langDelphi = new JRadioButton("Delphi");
	private JRadioButton langCSharp = new JRadioButton("C#");
	
	private JRadioButton parserRD = new JRadioButton("Descendente Recursivo");
	private JRadioButton parserLL = new JRadioButton("LL(1)");
	private JRadioButton parserSLR = new JRadioButton("SLR(1)");
	private JRadioButton parserLALR = new JRadioButton("LALR(1)");
	private JRadioButton parserLR = new JRadioButton("LR(1)");
	
	private JRadioButton inStream = new JRadioButton("Stream");
	private JRadioButton inString = new JRadioButton("String");
	
	private JCheckBox sensitive = new JCheckBox("<html>Diferenciar maiï¿½scula/minï¿½scula<br>em casos especiais</html>", true);
	
	private JRadioButton lexFull   = new JRadioButton("Tabela Completa");
	private JRadioButton lexCompac = new JRadioButton("Tabela Compactada (Sï¿½ p/ JAVA por enquanto)");
	private JRadioButton lexHard   = new JRadioButton("Especï¿½fica (Cï¿½digo)");

	private JTextField[] pascalT = {new JTextField("T"), new JTextField("T"), new JTextField("T")};
	
	private JPanel general = createGeneralPanel();
	private JPanel lex = createLexPanel();
	private JPanel synt = createSyntPanel();
	
	public OptionsPane() 
	{
		super(new BorderLayout());
		
		JTabbedPane tabPane = new JTabbedPane();
		
		add(tabPane);
		
		tabPane.insertTab("Geral", null, general, null, 0);
		tabPane.insertTab("Lï¿½xico", null, lex, null, 1);
		tabPane.insertTab("Sintï¿½tico", null, synt, null, 2);
	}
	
	private JPanel createGeneralPanel()
	{
		JPanel result = new JPanel(new GridLayout(3, 1, 5, 5));

		result.add(buildAnalyserKind());
		
		result.add(buildLanguage());
		
		result.add(buildClassNames());

		//result.add(buildError());
		
		return result;
	}
	
	private JPanel createLexPanel()
	{
		JPanel result = new JPanel(new BorderLayout());
		
		result.add(buildIn(), BorderLayout.NORTH);
		
		JPanel next = new JPanel(new BorderLayout());
		next.add(buildLexTable(), BorderLayout.NORTH);
		
		JPanel next2 = new JPanel(new BorderLayout());
		next2.add(sensitive, BorderLayout.NORTH);
		
		next.add(next2);
		result.add(next);
		
		return result;
	}
		
	private JPanel createSyntPanel()
	{
		JPanel result = new JPanel(new BorderLayout());
		
		result.add(buildParser(), BorderLayout.NORTH);
		
		return result;
	}

	private JPanel buildLabels()
	{
		JPanel result = new JPanel(new GridLayout(0, 1));
		
		result.add(new JLabel("Analisador Lï¿½xico"));
		result.add(new JLabel("Analisador Sintï¿½tico"));
		result.add(new JLabel("Analisador Semï¿½ntico"));
		result.add(pkg);
		
		pkg.addChangeListener(this);
		
		return result;
	}
	
	private Box buildTextFields()
	{
		for (int i=0; i< pascalT.length; i++)
		{
			pascalT[i].setEditable(false);
			pascalT[i].setVisible(false);
		}
		
		Box result = new Box(BoxLayout.Y_AXIS);
		
		result.add(Box.createHorizontalStrut(120));
		result.add(Box.createVerticalGlue());

		JPanel line = new JPanel(new BorderLayout());
		line.add(pascalT[0], BorderLayout.WEST);
		line.add(scanner);
		result.add(line);
		
		result.add(Box.createVerticalGlue());
		
		line = new JPanel(new BorderLayout());
		line.add(pascalT[1], BorderLayout.WEST);
		line.add(parser);		
		result.add(line);
				
		result.add(Box.createVerticalGlue());
		
		line = new JPanel(new BorderLayout());
		line.add(pascalT[2], BorderLayout.WEST);
		line.add(semantic);
		result.add(line);
		
		result.add(Box.createVerticalGlue());
		
		result.add(pkgName);
		
		result.add(Box.createVerticalGlue());
		
		return result;
	}

	private JPanel buildClassNames()
	{
		JPanel cn = new JPanel(new BorderLayout());
		cn.setBorder(BorderFactory.createTitledBorder("Classes"));
		
		cn.add(buildLabels(), BorderLayout.WEST);
		cn.add(buildTextFields());
		
		return cn;
	}

	private JPanel buildAnalyserKind()
	{
		JPanel anal = new JPanel(new GridLayout(0, 1));
		anal.setBorder(BorderFactory.createTitledBorder("Gerar"));
		
		anal.add(analLex);
		anal.add(analSynt);
		anal.add(analBoth);
		
		ButtonGroup analType = new ButtonGroup();
	
		analType.add(analLex);
		analType.add(analSynt);
		analType.add(analBoth);
		
		analBoth.setSelected(true);
		
		analLex.addChangeListener(this);
		analSynt.addChangeListener(this);
		analBoth.addChangeListener(this);
		
		return anal;
	}

	private JPanel buildLanguage()
	{
		JPanel lang = new JPanel(new GridLayout(0, 1));
		lang.setBorder(BorderFactory.createTitledBorder("Linguagem"));
		
		lang.add(langJava);
		lang.add(langCpp);
		lang.add(langDelphi);
		lang.add(langCSharp);
		
		ButtonGroup language = new ButtonGroup();
	
		language.add(langJava);
		language.add(langCpp);
		language.add(langDelphi);
		language.add(langCSharp);
		
		language.setSelected(langJava.getModel(), true);
		
		langJava.addChangeListener(this);
		langCpp.addChangeListener(this);
		langDelphi.addChangeListener(this);
		langCSharp.addChangeListener(this);
		
		return lang;
	}
	
	private JPanel buildParser()
	{
		JPanel result = new JPanel(new GridLayout(0, 1));
		result.setBorder(BorderFactory.createTitledBorder("Classe do Analisador Sintï¿½tico"));
		
		result.add(new JLabel("Descendentes"));
		result.add(parserRD);
		result.add(parserLL);
		result.add(new JLabel("Ascendentes"));
		result.add(parserSLR);
		result.add(parserLALR);
		result.add(parserLR);
		
		ButtonGroup parserType = new ButtonGroup();
		
		parserType.add(parserRD);
		parserType.add(parserLL);
		parserType.add(parserSLR);
		parserType.add(parserLALR);
		parserType.add(parserLR);
		
		parserType.setSelected(parserSLR.getModel(), true);
		
		return result;
	}
		
	private JPanel buildIn()
	{
		JPanel result = new JPanel(new GridLayout(0, 1));
		result.setBorder(BorderFactory.createTitledBorder("Forma de Entrada"));
		
		result.add(inStream);
		result.add(inString);
		
		ButtonGroup in = new ButtonGroup();
		
		in.add(inStream);
		in.add(inString);
		
		in.setSelected(inString.getModel(), true);
		
		return result;
	}
	
	private JPanel buildLexTable()
	{
		JPanel result = new JPanel(new GridLayout(0, 1));
		result.setBorder(BorderFactory.createTitledBorder("Implementaï¿½ï¿½o do Autï¿½mato"));
	
		result.add(lexFull);
		result.add(lexCompac);
		result.add(lexHard);
	
		ButtonGroup lex = new ButtonGroup();
	
		lex.add(lexFull);
		lex.add(lexCompac);
		lex.add(lexHard);
	
		lex.setSelected(lexFull.getModel(), true);
	
		return result;
	}
	
	public boolean genLex()
	{
		return analLex.isSelected() || analBoth.isSelected();
	}
	
	public boolean genSynt()
	{
		return analSynt.isSelected() || analBoth.isSelected();
	}
	
	public void stateChanged(ChangeEvent e)
	{	
		pkg.setEnabled( !langDelphi.isSelected() );
		pkgName.setEnabled( pkg.isSelected() && ! langDelphi.isSelected() );
		
		boolean genLex  = genLex();
		boolean genSynt = genSynt();
		
		for (int i=0; i< pascalT.length; i++)
			pascalT[i].setVisible(!pkg.isEnabled());
		general.revalidate();
		
		parser.setEnabled(genSynt);
		semantic.setEnabled(genSynt);
		
		parserRD.setEnabled(genSynt);
		parserLL.setEnabled(genSynt);		
		parserSLR.setEnabled(genSynt);		
		parserLALR.setEnabled(genSynt);
		parserLR.setEnabled(genSynt);
		
		
		inStream.setEnabled(genLex);
		inString.setEnabled(genLex);
		sensitive.setEnabled(genLex);
		lexFull.setEnabled(genLex);
		lexCompac.setEnabled(genLex);
		lexHard.setEnabled(genLex);
	}
	
	public void reset()
	{
		setOptions(new Options());
	}
	
	public void setOptions(Options options)
	{		
		boolean lex = options.generateScanner;
		boolean synt = options.generateParser;
			
		if (lex && synt)
			analBoth.setSelected(true);
		else
		{
			if (lex)
				analLex.setSelected(true);
			if (synt)
				analSynt.setSelected(true);
		}
		
		scanner.setText(options.scannerName);
		parser.setText(options.parserName);
		semantic.setText(options.semanticName);
		
		String pkg = options.pkgName;
		pkgName.setText(pkg);
		this.pkg.setSelected(pkg.length() > 0);
		
		switch (options.language)
		{
			case Options.LANG_JAVA:
				langJava.setSelected(true);
				break;
			case Options.LANG_CPP:
				langCpp.setSelected(true);
				break;
			case Options.LANG_DELPHI:
				langDelphi.setSelected(true);
				break;
			case Options.LANG_CSHARP:
				langCSharp.setSelected(true);
				break;
		}	
		
		switch (options.parser)
		{
			case Options.PARSER_REC_DESC:
				parserRD.setSelected(true);
				break;
			case Options.PARSER_LL:
				parserLL.setSelected(true);
				break;
			case Options.PARSER_SLR:
				parserSLR.setSelected(true);
				break;
			case Options.PARSER_LALR:
				parserLALR.setSelected(true);
				break;
			case Options.PARSER_LR:
				parserLR.setSelected(true);
		}	

		switch (options.input)
		{
			case Options.INPUT_STREAM:
				inStream.setSelected(true);
				break;
			case Options.INPUT_STRING:
				inString.setSelected(true);
				break;
		}
		
		sensitive.setSelected(options.scannerCaseSensitive);
		
		switch (options.scannerTable)	
		{
			case Options.SCANNER_TABLE_FULL:
				lexFull.setSelected(true);
				break;
			case Options.SCANNER_TABLE_COMPACT:
				lexCompac.setSelected(true);
				break;
			case Options.SCANNER_TABLE_HARDCODE:
				lexHard.setSelected(true);
				break;
		}
	}
	
	public Options getOptions()
	{			
		Options result = new Options();
		
		result.generateScanner = analLex.isSelected() || analBoth.isSelected();
		result.generateParser  = analSynt.isSelected() || analBoth.isSelected();
		
		result.scannerName =  scanner.getText();
		result.parserName = parser.getText();
		result.semanticName = semantic.getText();
		
		if (pkg.isSelected())
			result.pkgName = pkgName.getText();
			
		if (langJava.isSelected())
			result.language = Options.LANG_JAVA;
		else if (langCpp.isSelected())
			result.language = Options.LANG_CPP;
		else if (langDelphi.isSelected())
			result.language = Options.LANG_DELPHI;
		else if (langCSharp.isSelected())
			result.language = Options.LANG_CSHARP;
			
		if (parserRD.isSelected())
			result.parser = Options.PARSER_REC_DESC;
		else if (parserLL.isSelected())
			result.parser = Options.PARSER_LL;
		else if (parserSLR.isSelected())
			result.parser = Options.PARSER_SLR;
		else if (parserLALR.isSelected())
			result.parser = Options.PARSER_LALR;
		else if (parserLR.isSelected())
			result.parser = Options.PARSER_LR;
		
		result.scannerCaseSensitive = sensitive.isSelected();
		
		if (inStream.isSelected())
			result.input = Options.INPUT_STREAM;
		else if (inString.isSelected())
			result.input = Options.INPUT_STRING;
			
		if (lexFull.isSelected())
			result.scannerTable = Options.SCANNER_TABLE_FULL;
		else if (lexCompac.isSelected())
			result.scannerTable = Options.SCANNER_TABLE_COMPACT;
		else if (lexHard.isSelected())
			result.scannerTable = Options.SCANNER_TABLE_HARDCODE;
			
		return result;
	}
}
