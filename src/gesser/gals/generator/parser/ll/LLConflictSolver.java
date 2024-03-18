package gesser.gals.generator.parser.ll;

import gesser.gals.generator.parser.Grammar;

import java.util.BitSet;


public interface LLConflictSolver
{
	int resolve(Grammar g, BitSet conflict, int input, int stackTop);
}
