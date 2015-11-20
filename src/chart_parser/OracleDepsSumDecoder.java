package chart_parser;

import io.Sentence;
import lexicon.Categories;
import cat_combination.FilledDependency;
import cat_combination.SuperCategory;

/*
 * two-stage process to get the dependencies on a max-parse: decode
 * first, which finds the correct derivations; and then getParserDeps
 * which traces one out
 *
 * assumes a gold root category as input, and checks for that first in
 * getParserDeps; if it doesn't find a derivation with the same number
 * of deps as gold deps, it then relaxes the root constraint and tries
 * them all
 */
public class OracleDepsSumDecoder extends OracleDecoder {
	public OracleDepsSumDecoder(Categories categories, boolean extractRuleInstances) {
		super(categories, extractRuleInstances);
	}

	@Override
	public double bestScore(SuperCategory superCat, Sentence sentence) {
		double score = 0.0;

		if (superCat.leftChild != null) {
			score += superCat.leftChild.maxEquivScore;

			if (superCat.rightChild != null) {
				score += superCat.rightChild.maxEquivScore;
			}
		}
		/*
		 * determine how many dependencies are gold (note we're ignoring any
		 * that match the first 4 elements of the tuple but would get ignored by
		 * the evaluate script);
		 */
		for (FilledDependency filled = superCat.filledDeps; filled != null; filled = filled.next) {
			if (goldDeps.contains(filled)
					&& !ignoreDeps.ignoreDependency(filled, sentence)) {
				score++;
			} else if (!ignoreDeps.ignoreDependency(filled, sentence)) {
				score = score - 100;
			}
		}
		superCat.score = score;
		return score;
	}

	/*
	 * finds the dependencies on a best-scoring parse; assumes we've already run
	 * decode
	 * 
	 * first looks for a root category matching the oracle root - note the use
	 * of the Category equals method here, for which S[X] = S, but now NP[nb] !=
	 * NP and N =! N[num] (update as of 10/6/14)
	 */
	@Override
	public boolean getParserDeps(Chart chart, double maxDeps, Sentence sentence, boolean ignoreDepsFlag, boolean checkRoot) {
		parserDeps.clear();
		Cell root = chart.root();

		SuperCategory bestEquivSuperCat = null;
		for (SuperCategory superCat : root.getSuperCategories()) {
			if (superCat.maxEquivScore == maxDeps
					&& superCat.cat.equals(rootCat)) {
				bestEquivSuperCat = superCat.maxEquivSuperCat;
				break;
			}
		}
		if (bestEquivSuperCat == null) {
			for (SuperCategory superCat : root.getSuperCategories()) {
				if (superCat.maxEquivScore == maxDeps) {
					bestEquivSuperCat = superCat.maxEquivSuperCat;
					break;
				}
			}
			if (bestEquivSuperCat != null)
			{
				newRootCat = bestEquivSuperCat.cat;
				// gets printed out with deps
			}
		}
		if (bestEquivSuperCat == null) {
			System.err.println("No best!\n");
			return false;
		}
		getDeps(bestEquivSuperCat, sentence, ignoreDepsFlag);

		return true;
	}

}
