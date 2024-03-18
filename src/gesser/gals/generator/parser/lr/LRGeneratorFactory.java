package gesser.gals.generator.parser.lr;

import gesser.gals.generator.Options;
import gesser.gals.generator.OptionsDialog;
import gesser.gals.generator.parser.Grammar;

/**
 * @author Gesser
 */
public class LRGeneratorFactory
{
	private LRGeneratorFactory() {}
	
	
	public static LRGenerator createGenerator(Grammar g)
	{
		switch (OptionsDialog.getInstance().getOptions().parser)
		{
			case Options.PARSER_SLR: return new SLRGenerator(g);
			case Options.PARSER_LR: return new LRCanonicGenerator(g);
			case Options.PARSER_LALR : return new LALRGenerator(g);
			default: return null;
		}
	}
}
