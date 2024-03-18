package gesser.gals;

import gesser.gals.analyser.AnalysisError;
import gesser.gals.gas.BNFImporter;
import gesser.gals.generator.Options;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.cpp.*;
import gesser.gals.generator.csharp.CSharpCommonGenerator;
import gesser.gals.generator.csharp.CSharpParserGenerator;
import gesser.gals.generator.csharp.CSharpScannerGenerator;
import gesser.gals.generator.java.*;
import gesser.gals.generator.parser.*;
import gesser.gals.generator.parser.ll.*;
import gesser.gals.generator.parser.lr.*;
import gesser.gals.generator.delphi.*;
import gesser.gals.generator.scanner.FiniteAutomata;
import gesser.gals.simulator.SimulateWindow;
import gesser.gals.util.*;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.URL;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.undo.UndoManager;

public class Actions
{
	private static abstract class ToolTipedAction extends AbstractAction
	{
		public ToolTipedAction(String name, Icon icon, String tooltip)
		{
			super(name, icon);
			putValue(SHORT_DESCRIPTION, tooltip);
		}
		
		public ToolTipedAction(String name, Icon icon)
		{
			this(name, icon, name);
		}
	}
	
	private static final Icon NEW = new ImageIcon(ClassLoader.getSystemResource("icons/new.gif"));
	private static final Icon OPEN = new ImageIcon(ClassLoader.getSystemResource("icons/open.gif"));
	private static final Icon SAVE = new ImageIcon(ClassLoader.getSystemResource("icons/save.gif"));	
	private static final Icon VERIFY = new ImageIcon(ClassLoader.getSystemResource("icons/verify.gif"));
	private static final Icon SIMULATOR = new ImageIcon(ClassLoader.getSystemResource("icons/simulator.gif"));
	private static final Icon GENERATOR = new ImageIcon(ClassLoader.getSystemResource("icons/generator.gif"));
	private static final Icon OPTIONS = new ImageIcon(ClassLoader.getSystemResource("icons/options.gif"));
	private static final Icon UNDO = new ImageIcon(ClassLoader.getSystemResource("icons/undo.gif"));
	private static final Icon REDO = new ImageIcon(ClassLoader.getSystemResource("icons/redo.gif"));
	
	public static final JFileChooser FILE_CHOOSER = new JFileChooser();
	
	private static Grammar OLD_GRAMMAR = null;
	
	private static boolean saved = false;
	private static boolean changed = false;
	private static File file = null;
	
	public static void setSaved(boolean s)
	{
		if (s)
			UNDO_MAN.discardAllEdits();
			
		saved = s;
		changed = !s;		
	}
	
	public static boolean checkSaved()
	{
		if (! saved && changed)
		{
			switch (JOptionPane.showConfirmDialog(MainWindow.getInstance(),"Salvar Alteraï¿½ï¿½es?"))
			{
				case JOptionPane.YES_OPTION:
					save.actionPerformed(null);
					break;
				case JOptionPane.NO_OPTION:
					return true;
				case JOptionPane.CANCEL_OPTION:
					return false;	
				default:
					return false;				
			}
		}
		return true;
	}
	
	public static final Action close = new AbstractAction("Fechar") 
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (checkSaved())
        		System.exit(0);
		}		
	};
	
	public static final Action about = new AbstractAction("Sobre")
	{
		public void actionPerformed(ActionEvent e) 
		{
			String msg = 
				"G.A.L.S.\n"+
				"Gerador de Analisadores\n"+
				"Lï¿½xicos e Sintï¿½ticos (Versï¿½o 2003.10.03)\n"+
				"\n"+
				"Carlos Eduardo Gesser\n"+
				"gals.sourceforge.net";
        	JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
		}		
	};
	
	public static final Action doc = new AbstractAction("Documentaï¿½ï¿½o")
	{
		public void actionPerformed(ActionEvent e) 
		{
			URL url = ClassLoader.getSystemResource("help.html");
			HTMLDialog.getInstance().show("Documentaï¿½ï¿½o", url);
		}		
	};
	
	public static final Action options = new ToolTipedAction("Opï¿½ï¿½es", OPTIONS)
	{
		
		public void actionPerformed(ActionEvent e) 
		{
			OptionsDialog.getInstance().show();
		}		
	};
	
	public static final Action save = new ToolTipedAction("Salvar", SAVE)
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (file == null/*! saved*/)
				saveAs.actionPerformed(e);
			else
			{
                try
                {
                	InputPane.Data inData = MainWindow.getInstance().getData();
                    GalsData data = new GalsData(MainWindow.getInstance().getOptions(), inData);
                    
                    OutputStream os = new FileOutputStream(file);
                    XMLProcessor.store(data, os);                    
                    setSaved(true);
                }
                catch (FileNotFoundException e1)              
                {
                	e1.printStackTrace();
                }
			}
		}		
	};
	
	public static final Action saveAs = new AbstractAction("Salvar Como...")
	{
		public void actionPerformed(ActionEvent e) 
		{
            FILE_CHOOSER.setFileFilter(FileFilters.GALS_FILTER);
            if (FILE_CHOOSER.showSaveDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
            {
            	file = FILE_CHOOSER.getSelectedFile();
            	String name = file.getPath();
            	if (name.length() < 5 || !name.substring(name.length()-5).equals(".gals"))
            	{
            		name = name+".gals";
            		file = new File(name);
            	}                    	
            	save.actionPerformed(e);
            }
    	}		
	};
	
	public static final Action new_ = new ToolTipedAction("Novo", NEW, "Criar Novo Arquivo")
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (checkSaved())
			{
				reset();
				options.actionPerformed(e);
			}
		}					
	};
	
	public static void reset()
	{
		MainWindow.getInstance().reset();
		saved = false;
		changed = false;
		file = null;
		UNDO_MAN.discardAllEdits();
	}	
	
	public static final Action load = new ToolTipedAction("Abrir...", OPEN, "Abrir Arquivo")
	{
		public void actionPerformed(ActionEvent e) 
		{
			if (checkSaved())
			{
            	String msg;
                try
                {
                	FILE_CHOOSER.setFileFilter(FileFilters.GALS_FILTER);
                    if (FILE_CHOOSER.showOpenDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
                    {
                    	file = FILE_CHOOSER.getSelectedFile();
                    	InputStream is = new FileInputStream(file);
                    	GalsData lsd = XMLProcessor.load(is);
                    	MainWindow.getInstance().updateData(lsd);
                    	setSaved(true);
                    }
                }
                catch (XMLParsingException e1)
                {    
                	e1.printStackTrace();            	
                    msg = "Arquivo invï¿½lido!!!";
                    JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
                }    
                catch (IOException e1)
                {
                	e1.printStackTrace();
                }
			}
		}		
	};
	
	public static final Action showTable = new AbstractAction("Tabela de Anï¿½lise Sintï¿½tica")
	{
		public void actionPerformed(ActionEvent e) 
		{
        	try
            {
            	Grammar g = MainWindow.getInstance().getGrammar();

				switch (OptionsDialog.getInstance().getOptions().parser)
				{
					case Options.PARSER_REC_DESC:
					case Options.PARSER_LL:
						LLParser llg = new LLParser(g);
						HTMLDialog.getInstance().show("Tabela de Anï¿½lise LL(1)", llg.tableAsHTML());
						break;
					case Options.PARSER_SLR:
					case Options.PARSER_LALR:
					case Options.PARSER_LR:
            			LRGenerator parser = LRGeneratorFactory.createGenerator(g);
         				HTMLDialog.getInstance().show("Tabela de Anï¿½lise SLR(1)", parser.tableAsHTML());
            			break;
				}
			} 
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }       
			catch (NotLLException e1)
			{
				String msg = "Esta gramï¿½tica nï¿½o ï¿½ LL(1): "+e1.getMessage();
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
				e1.printStackTrace();
			}       
		}		
	};
	
	public static final Action simulate = new ToolTipedAction("Simulador", SIMULATOR)
	{
		public void actionPerformed(ActionEvent e) 
		{	
            try
            {
            	FiniteAutomata fa = MainWindow.getInstance().getFiniteAutomata();
            	
            	Grammar g = MainWindow.getInstance().getGrammar();
            	
            	List terminals = MainWindow.getInstance().getTokens();
				
				switch (OptionsDialog.getInstance().getOptions().parser)
				{
					case Options.PARSER_REC_DESC:
					case Options.PARSER_LL:
						SimulateWindow.getInstance().simulateLL(fa, g,  terminals);
						break;
					case Options.PARSER_SLR:
					case Options.PARSER_LALR:
					case Options.PARSER_LR:
						SimulateWindow.getInstance().simulateSLR(fa, g,  terminals);
						break;
				}				
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }
            catch (NotLLException e1)
			{
				String msg = "Esta gramï¿½tica nï¿½o ï¿½ LL(1): "+e1.getMessage();
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
				e1.printStackTrace();
			}            
		}		
	};
	
	public static final Action factored = new AbstractAction("Fatoraï¿½ï¿½o")
	{
		public void actionPerformed(ActionEvent e) 
		{        	
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                BitSet bs = g.getNonFactoratedProductions();
                if (bs.cardinality() == 0)
                    msg = "Estï¿½ fatorada";
                else
                {
                    StringBuffer bfr = new StringBuffer();
                    bfr.append("As produï¿½ï¿½es\n");
                    for (int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1))
                    {
                        bfr.append(i).append(": ")
                            .append(g.getProductions().getProd(i))
                            .append('\n');
                    }
                    bfr.append("Nï¿½o estï¿½o fatoradas");
                    msg = bfr.toString();
                }
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }   
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                  
		}		
	};
	
	public static final Action recursion = new AbstractAction("Recursï¿½o ï¿½ Esquerda")
	{
		public void actionPerformed(ActionEvent e) 
		{        	
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar(); 
                int s = g.getLeftRecursiveSimbol();
                if (s == -1)
                    msg = "Nï¿½O possui recursï¿½o";
                else
                    msg = "Foi detectada recursï¿½o ï¿½ esquerda (direta ou indireta)\n" +
                        " em produï¿½ï¿½es iniciadas por \""+g.getSymbols()[s]+"\"";
                    /*                   
                if (g.hasLeftRecursion())
                    msg = "Possui recursï¿½o";
                else
                    msg = "Nï¿½O possui recursï¿½o";
                */
                JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                     
		}		
	};
	
	public static final Action removeUnitary = new AbstractAction("Remover Produï¿½ï¿½es Unitï¿½rias")
	{
		public void actionPerformed(ActionEvent e) 
		{        	
			try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                OLD_GRAMMAR = (Grammar) g.clone();
                g.removeUnitaryProductions();
                //MainWindow.getInstance().updateData(null, g);
                msg = "Produï¿½ï¿½es Unitï¿½rias Removidas";
                
                JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                       
		}		
	};
	
	public static final Action condition3 = new AbstractAction("Terceira Condiï¿½ï¿½o LL(1)")
	{
		public void actionPerformed(ActionEvent e) 
		{
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                if (g.passThirdCondition())
                    msg = "Passou na 3a condiï¿½ï¿½o";
                else
                    msg = "Nï¿½O passou na 3a condiï¿½ï¿½o";
                    
				JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }          
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }
        }		
	};
	
	public static final Action removeUseless = new AbstractAction("Remover Simbolos Inï¿½teis")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {                	
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                OLD_GRAMMAR = (Grammar) g.clone();
                try
                {
                    g.removeUselessSymbols();                    
                    //MainWindow.getInstance().updateData(null, g);
                	msg = "Sï¿½mbolos inï¿½teis removidos";
                }
                catch (EmptyGrammarException e1)
                {
                    msg = "Gramï¿½tica Vazia!!!";
                }	            
                JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                          
        }		
	};
	
	public static final Action removeEpsilon = new AbstractAction("Remover Epsilon")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                OLD_GRAMMAR = (Grammar) g.clone();
                g.removeEpsilon();
                //MainWindow.getInstance().updateData(null, g);
                msg = "Epsilon Removido";
                
                JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                        
        }		
	};
	
	public static final Action removeRecursion = new AbstractAction("Remover Recursï¿½o ï¿½ Esquerda")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {
            	String msg;
                Grammar g = MainWindow.getInstance().getGrammar();
                OLD_GRAMMAR = (Grammar) g.clone();
                g.removeRecursion();
                //MainWindow.getInstance().updateData(null, g);
                msg = "Recursï¿½es a esquerda removidas";
                
                JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                      
        }		
	};
	
	public static final Action factorate = new AbstractAction("Fatorar")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {
            	String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                
                try
				{
					OLD_GRAMMAR = (Grammar) g.clone();
					
					g.factorate();
					//MainWindow.getInstance().updateData(null, g);
	                msg = "fatorada";
				}
				catch (LeftRecursionException e1)
	            {
	                msg = "Impossï¿½vel fatorar, a gramï¿½tica possui recursï¿½o a esquerda";
	            }	            
                
                JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
            }   
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                       
        }		
	};
	
	public static final Action ff = new AbstractAction("First & Follow")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {
            	//String msg;
            	
                Grammar g = MainWindow.getInstance().getGrammar();
                //msg = g.stringFirstFollow();
                
                //JOptionPane.showMessageDialog(MainWindow.getInstance(),msg);
                
				HTMLDialog.getInstance().show("First & Follow", g.ffAsHTML());
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                      
        }		
	};
	
	public static final Action showItemSet = new AbstractAction("Conjunto de Itens ")
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {
                Grammar g = MainWindow.getInstance().getGrammar();
                LRGenerator parser = LRGeneratorFactory.createGenerator(g);
                List l = parser.getErrors(parser.buildTable());
                int i=0;
                for (Iterator iter = l.iterator(); iter.hasNext(); i++)
				{
					String element = (String) iter.next();
					System.out.println(i+"->"+element);
				}
         		HTMLDialog.getInstance().show("Itens SLR(1)", parser.itemsAsHTML());
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                      
        }		
	};
	
	public static final Action viewLexTable = new AbstractAction("Tabela de Anï¿½lise Lï¿½xica")
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					FiniteAutomata fa = MainWindow.getInstance().getFiniteAutomata();
					HTMLDialog.getInstance().show("Automato Finito", fa.asHTML());
				}
				catch(MetaException e1)
				{
					MainWindow.getInstance().handleError(e1);
				}
			}
		};
	
	public static final Action verify = new ToolTipedAction("Verificar Erros", VERIFY)
	{
		public void actionPerformed(ActionEvent e) 
		{			
            try
            {	
            	MainWindow.getInstance().getFiniteAutomata();            	
            	MainWindow.getInstance().getGrammar();
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Nenhum erro foi encontrado");
            }
            catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }                   
        }		
	};
	
	public static final UndoManager UNDO_MAN = new UndoManager();
	
	public static final Action undo = new ToolTipedAction("Desfazer", UNDO)
	{
		{
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
			
		}
	
		public void actionPerformed(ActionEvent e) 
		{
			if (UNDO_MAN.canUndo())
				UNDO_MAN.undo();
			else
				Toolkit.getDefaultToolkit().beep();
		}	
	};
	
	public static final Action redo = new ToolTipedAction("Resfazer", REDO)
	{
		{
			putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK)); 
		}

		public void actionPerformed(ActionEvent e) 
		{
			if (UNDO_MAN.canRedo())
				UNDO_MAN.redo();
			else
				Toolkit.getDefaultToolkit().beep();
		}	
	};
	
	public static final Action useless = new AbstractAction("Sï¿½mbolos inï¿½teis")
	{
		public void actionPerformed(ActionEvent e) 
		{			
			try
			{
				Grammar g = MainWindow.getInstance().getGrammar();
				HTMLDialog.getInstance().show("Sï¿½mbolos inï¿½ties", g.uselessSymbolsHTML());
			}
			catch(MetaException e1)
			{
				MainWindow.getInstance().handleError(e1);
			}                              
		}
	};
	
	public static final Action genCode = new ToolTipedAction("Gerar Cï¿½digo", GENERATOR)
	{
		public void actionPerformed(ActionEvent e) 
		{			
			final String lb = System.getProperty("line.separator");
            try
			{				
				FiniteAutomata fa = MainWindow.getInstance().getFiniteAutomata();
				Grammar g = MainWindow.getInstance().getGrammar();				
            	
            	Options options = MainWindow.getInstance().getOptions();
				
				if (options != null)
				{            	
	            	FILE_CHOOSER.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	            	FILE_CHOOSER.setFileFilter(FileFilters.DIRECTORY_FILTER);
	            	String title = FILE_CHOOSER.getDialogTitle();
	            	FILE_CHOOSER.setDialogTitle("Escolher Pasta");
	            							
					if (FILE_CHOOSER.showSaveDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION)
					{
						String path = FILE_CHOOSER.getSelectedFile().getPath();
	            		            	
						TreeMap allFiles = new TreeMap();
						
						switch (options.language)
						{
							case Options.LANG_JAVA:
								allFiles.putAll( new JavaCommonGenerator().generate(fa, g, options) );
								allFiles.putAll( new JavaScannerGenerator().generate(fa, options) );							
								allFiles.putAll( new JavaParserGenerator().generate(g, options));
								break;
							case Options.LANG_CPP:
								allFiles.putAll( new CppCommomGenerator().generate(fa, g, options) );
								allFiles.putAll( new CppScannerGeneretor().generate(fa, options) );
								allFiles.putAll( new CppParserGenerator().generate(g, options) );
								break;
							case Options.LANG_DELPHI:
								allFiles.putAll( new DelphiCommomGenerator().generate(fa, g, options) );
								allFiles.putAll( new DelphiScannerGenerator().generate(fa, options) );
								allFiles.putAll( new DelphiParserGenerator().generate(g, options));
								break;
							case Options.LANG_CSHARP:
								allFiles.putAll( new CSharpCommonGenerator().generate(fa, g, options) );
								allFiles.putAll( new CSharpScannerGenerator().generate(fa, options) );
								allFiles.putAll( new CSharpParserGenerator().generate(g, options));
								break;
						}
						
						try
						{
							if (FileGenerationSelector.getInstance().show(allFiles))
								for (Iterator i = allFiles.keySet().iterator(); i.hasNext(); )
								{
									String f = (String) i.next();

									String dir = path + File.separator + f;
									try {
										File file = new File(dir);
										file.getParentFile().mkdirs();

										file.createNewFile();
									} catch (IOException e1) {
										e1.printStackTrace();
									}

									BufferedWriter writer = new BufferedWriter(new FileWriter(dir));
									StringBuffer bfr = new StringBuffer((String)allFiles.get(f));
									
									for (int j=0; j<bfr.length(); j++)
									{
										if (bfr.charAt(j) == '\n')
										{
											bfr.replace(j, j+1, lb);
											j += lb.length() - 1;
										}
									}
									
									writer.write( bfr.toString() );
									writer.close();									
								}
						}
						catch (IOException ee)
						{
							ee.printStackTrace();
							System.exit(1);
						}
	            	}
	            	FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_ONLY);
	            	FILE_CHOOSER.setDialogTitle(title);
				}
			}
			catch(MetaException e1)
            {
            	MainWindow.getInstance().handleError(e1);
            }		
			catch (NotLLException e1)
			{
				JOptionPane.showMessageDialog(MainWindow.getInstance(), "Esta gramï¿½tica nï¿½o ï¿½ LL(1): "+e1.getMessage());
				e1.printStackTrace();
			}
        }
	};
	
	public static final Action importGAS = new AbstractAction("Importar BNF")
	{
		public void actionPerformed(ActionEvent e)
		{
			try
			{					
				GalsData lsd = new BNFImporter().importGAS();
				if (lsd != null)
					MainWindow.getInstance().updateData(lsd);				
			}
			catch (AnalysisError ae)
			{
				JOptionPane.showMessageDialog(MainWindow.getInstance(), "Nï¿½o foi possï¿½vel importar o arquivo");
				ae.printStackTrace();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}	
	};
}