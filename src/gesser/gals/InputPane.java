package gesser.gals;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.editor.BNFDocument;
import gesser.gals.editor.DefinitionsDocument;
import gesser.gals.editor.NTDocument;
import gesser.gals.editor.TokensDocument;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.parser.Grammar;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.parserparser.Parser;
import gesser.gals.scannerparser.LineParser;
import gesser.gals.util.MetaException;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.text.StyledEditorKit;

/**
 * @author Gesser
 */

public class InputPane extends JPanel implements MouseListener, UndoableEditListener
{
	public static final int LEXICAL  = 0;
	public static final int SYNTATIC = 1;
	public static final int BOTH     = 2;
	
	private JEditorPane grammar      = new JEditorPane();
	private JEditorPane nonTerminals = new JEditorPane();
	private JEditorPane tokens       = new JEditorPane();
	private JEditorPane definitions  = new JEditorPane();
	
	private JPanel base = new JPanel(new BorderLayout());
	private JList errorList = new JList();
	
	private JPanel pnlGrammar 		= createPanel(" Gramática", grammar, new BNFDocument());
	private JPanel pnlNonTerminals = createPanel(" Não Terminais", nonTerminals, new NTDocument());
	private JPanel pnlTokens 		= createPanel(" Tokens", tokens, new TokensDocument());
	private JPanel pnlDefinitions  = createPanel(" Definições Regulares", definitions, new DefinitionsDocument());
	
	private int mode;
	
	public InputPane()
	{
		super (new BorderLayout());
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, base, new JScrollPane(errorList));
		split.setResizeWeight(0.9);
		add(split);
		
		setMode(BOTH);
		
		split.setDividerLocation(355);
		
		errorList.addMouseListener(this);
	}
	
	public Grammar getGrammar() throws MetaException
	{
		errorList.setListData(new Object[]{});
		
		if (mode != SYNTATIC && mode != BOTH)
			return null;
		
		List tokens = getTokens();
		List nt = new ArrayList();
		
		StringTokenizer ntTok = new StringTokenizer(nonTerminals.getText(), "\n", true);		
		while (ntTok.hasMoreTokens())
			nt.add(ntTok.nextToken());
		
		String gram = grammar.getText();
		
		
		Grammar g = new Parser().parse(tokens, nt, gram);
		return g;		
	}
	
	public FiniteAutomata getFiniteAutomata() throws MetaException
	{
		errorList.setListData(new Object[]{});
		
		if (mode != LEXICAL && mode != BOTH)
			return null;
		
		LineParser lp = new LineParser();
						
		return lp.parseFA(definitions.getText(), tokens.getText());
	}
	
	public List getTokens() throws MetaException
	{
		List result = new ArrayList();
		if (mode == BOTH || mode == LEXICAL)
		{
			List tokens = getFiniteAutomata().getTokens();
			for (int i=0; i<tokens.size(); i++)
			{
				result.add(tokens.get(i));
				result.add("\n");
			}
		}
		else //mode == SYNTATIC
		{
			StringTokenizer tknzr = new StringTokenizer(tokens.getText(), "\n", true);
			
			while (tknzr.hasMoreTokens())
				result.add(tknzr.nextToken());
		}
		return result;
	}
	
	public Data getData()
	{
		boolean lex = mode == LEXICAL || mode == BOTH;
		boolean synt = mode == SYNTATIC || mode == BOTH;
		
		return 
			new Data(
				lex ? definitions.getText() : "", 
				tokens.getText(),
				synt ? nonTerminals.getText() : "", 
				synt ? grammar.getText() : "");
	}
	
	public void setData(Data d)
	{
		definitions.setText(d.getDefinitions());
		tokens.setText(d.getTokens());
		nonTerminals.setText(d.getNonTerminals());				
		grammar.setText(d.getGrammar());
		
		definitions.setCaretPosition(0);
		tokens.setCaretPosition(0);
		nonTerminals.setCaretPosition(0);
		grammar.setCaretPosition(0);
		
		MainWindow.getInstance().setChanged();
	}
	
	public void reset()
	{
		grammar.setText("");
		nonTerminals.setText("");
		tokens.setText("");
		definitions.setText("");
		OptionsDialog.getInstance().reset();
		MainWindow.getInstance().setChanged();
	}
	
	public void setMode(int mode)
	{
		this.mode = mode;
		
		switch (mode)
		{
			case LEXICAL:  setLex(); break;
			case SYNTATIC: setSynt(); break;
			case BOTH:     setBoth(); break;
		}
	}
	
	private void setBoth()
	{
		base.removeAll();
		
		JSplitPane top = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlDefinitions, pnlTokens);
		
		top.setResizeWeight(0.25);
		
		JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlNonTerminals, pnlGrammar);
		
		bottom.setResizeWeight(0.15);
		
		JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
		
		main.setResizeWeight(0.5);
		
		base.add(main);
		
		validate();
		repaint();
		
		top.setDividerLocation(0.25);
		bottom.setDividerLocation(0.15);
		main.setDividerLocation(0.5);
	}

	private void setSynt()
	{
		base.removeAll();
		
		JSplitPane left = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlTokens, pnlNonTerminals);
		
		left.setResizeWeight(0.5);
		
		JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, pnlGrammar);
		
		main.setResizeWeight(0.15);
		
		base.add(main);
		
		validate();
		repaint();
		
		left.setDividerLocation(0.5);
		main.setDividerLocation(0.15);
	}

	
	private void setLex()
	{
		base.removeAll();
		
		JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlDefinitions, pnlTokens);
		
		main.setResizeWeight(0.25);
		
		base.add(main);
		
		validate();
		repaint();
		
		main.setDividerLocation(0.25);
	}
	
	public void undoableEditHappened(UndoableEditEvent e)
	{
		Actions.UNDO_MAN.addEdit(e.getEdit());
		Actions.setSaved(false);
		MainWindow.getInstance().setChanged();
	}
				
	private JPanel createPanel(String caption, final JEditorPane comp, Document doc)
	{
		JPanel pnl = new JPanel(new BorderLayout());
		
		comp.setEditorKit(new StyledEditorKit());
		comp.setDocument(doc);
		
		comp.getDocument().addUndoableEditListener(this);
		comp.getKeymap().addActionForKeyStroke((KeyStroke)Actions.undo.getValue(Action.ACCELERATOR_KEY), Actions.undo);
		comp.getKeymap().addActionForKeyStroke((KeyStroke)Actions.redo.getValue(Action.ACCELERATOR_KEY), Actions.redo);
					
		pnl.add(new JLabel(caption), BorderLayout.NORTH);
		
		JPanel tmp = new JPanel(new BorderLayout());
		tmp.add(comp);
		
		JScrollPane scroll = new JScrollPane(tmp);
		scroll.getVerticalScrollBar().setUnitIncrement(10);
		scroll.getHorizontalScrollBar().setUnitIncrement(10);
		pnl.add(scroll);
		
		return pnl;
	}
	
	
	private static class ErrorData
	{
		int index, position, mode;
		String message;
		
		ErrorData(String message, int index, int position, int mode)
		{
			this.message = message;
			this.index = index;
			this.position = position;
			this.mode = mode;
		}
		
		public String toString()
		{
			return message /*+ ", linha: "+index+", coluna "+position*/;
		}
	}
	public void handleError(MetaException e)
	{			
		AnalysisError ae = (AnalysisError) e.getCause();
		int line = e.getIndex();
		String msg = "";
		switch (e.getMode())
		{
			case MetaException.DEFINITION :
				msg = "Erro em Definição Regular: ";
				break;
			case MetaException.TOKEN :
				msg = "Erro na Especificação de Tokens: ";
				break;
			case MetaException.NON_TERMINAL :
				msg = "Erro na Declaração dos Não-Terminais: ";
				break;
			case MetaException.GRAMMAR :
				msg = "Erro na Especificação da Gramática: ";
				break;
		}
		msg += ae.getMessage();
		
		ErrorData ed = new ErrorData(msg, line, ae.getPosition(), e.getMode());
		
		ErrorData[] errors = {ed};
		
		errorList.setListData(errors);
		e.printStackTrace();
		Toolkit.getDefaultToolkit().beep();
		mouseClicked(null);
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void mouseClicked(MouseEvent e)
	{
		ErrorData error = (ErrorData) errorList.getSelectedValue();

		if (error != null)
		{
			switch (error.mode)
			{
				case MetaException.DEFINITION:
					setPosition(definitions, error.index, error.position);
					definitions.requestFocus();
					break;
				case MetaException.TOKEN :
					setPosition(tokens, error.index, error.position);
					tokens.requestFocus();
					break;
				case MetaException.NON_TERMINAL :
					setPosition(nonTerminals, error.index, error.position);
					nonTerminals.requestFocus();
					break;
				case MetaException.GRAMMAR :
					grammar.getCaret().setDot(error.position);
					grammar.requestFocus();
					break;
			}
		}
	}
	
	private void setPosition(JEditorPane pane, int line, int col )
	{
		String text = pane.getText();
		int pos = 0;
		int strpos = 0;
		while (line>0)
		{
			while (strpos < text.length() && text.charAt(strpos) != '\n')
			{				
				if (text.charAt(strpos) != '\r')
					pos++;
				strpos++;				
			}
			strpos++;
			pos++;
			line--;
		}
		pos += col;
		pane.setCaretPosition(pos);
	}

	public static class Data
	{
		private String definitions = "";
		private String tokens = "";
		private String nonTerminals = "";
		private String grammar = "";
		
		public Data(String definitions, String tokens, String nonTerminals, String grammar)
		{
			this.definitions = definitions;
			this.tokens = tokens;
			this.nonTerminals = nonTerminals;
			this.grammar = grammar;
		}
		
		public String getDefinitions()
		{
			return definitions;
		}

		public String getGrammar()
		{
			return grammar;
		}

		public String getNonTerminals()
		{
			return nonTerminals;
		}

		public String getTokens()
		{
			return tokens;
		}
		
		public void setGrammar(String string)
		{
			this.grammar = string;
		}

		public void setNonTerminals(String string)
		{
			this.nonTerminals = string;
		}
	}
}
