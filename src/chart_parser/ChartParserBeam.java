package chart_parser;

import io.Sentence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

import lexicon.Relations;
import model.Features;
import model.Lexicon;
import model.Weights;
import utils.Pair;
import cat_combination.FilledDependency;
import cat_combination.RuleInstancesParams;
import cat_combination.SuperCategory;

public class ChartParserBeam extends ChartParser {
	private boolean cubePruning;
	private int beamSize;
	private double beta;

	public ChartParserBeam(
					String grammarDir,
					boolean altMarkedup,
					boolean eisnerNormalForm,
					int MAX_WORDS,
					int MAX_SUPERCATS,
					boolean output,
					RuleInstancesParams ruleInstancesParams,
					Lexicon lexicon,
					String featuresFile,
					String weightsFile,
					boolean newFeatures,
					boolean compactWeights,
					boolean cubePruning,
					int beamSize,
					double beta) throws IOException {
		super(grammarDir, altMarkedup, eisnerNormalForm, MAX_WORDS,
					MAX_SUPERCATS, output, false, false, ruleInstancesParams,
					lexicon, null, null, newFeatures);

		if ( compactWeights ) {
			this.weights = new Weights();
			this.features = new Features(featuresFile, weightsFile, weights, categories, newFeatures);
		} else {
			this.features = (featuresFile != null ? new Features(featuresFile, categories, newFeatures) : null);
			this.weights = (weightsFile != null ? new Weights(weightsFile, features.numFeatures) : null);
		}

		this.chart = new Chart(MAX_WORDS, output, categories.dependencyRelations, false, false);
		this.chart.setWeights(this.weights);

		this.cubePruning = cubePruning;

		this.beamSize = beamSize;
		this.beta = beta;
	}

	/*
	 * need the decoder to provide an IgnoreDepsEval object; this is breaking
	 * the separation between the two
	 */
	/**
	 * Parses one supertagged sentence using chart parser with beam search.
	 * 
	 * The method calls functions preParse() and postParse(), which can perform
	 * pre-parsing and post-parsing operations depending on the subclass e.g.
	 * ChartTrainParserBeam.
	 * 
	 * @param in file containing supertagged sentences to parse
	 * @param stagsIn file containing additional supertags
	 * @param log log file
	 * @param betas array of values of beta
	 * @return true if sentence is parsed or skipped, false if there are no
	 * sentences left
	 */
	@Override
	public boolean parseSentence(BufferedReader in, BufferedReader stagsIn, PrintWriter log, double[] betas) {
		if (betas.length != 1) {
			throw new IllegalArgumentException("Only need 1 beta value.");
		}

		if (!readSentence(in, stagsIn)) {
			return false;
		}

		maxWordsExceeded = false;
		int numWords = sentence.words.size();
		if ( numWords > chart.MAX_WORDS ) {
			System.out.println(" Sentence has " + numWords + " words; MAX_WORDS exceeded.");
			log.println(" Sentence has " + numWords + " words; MAX_WORDS exceeded.");
			maxWordsExceeded = true;
			return true;
		} else {
			System.out.println(" Sentence has " + numWords + " words; chart capacity: " + (beamSize*numWords*(numWords+1)/2));
			log.println(" Sentence has " + numWords + " words; chart capacity: " + (beamSize*numWords*(numWords+1)/2));
		}

		if (lexicon != null) {
			sentence.addIDs(lexicon);
		}

		maxSuperCatsExceeded = false;
		chart.clear();
		chart.load(sentence, betas[0], false, true);

		setClearCounters(chart);

		if (!preParse()) {
			return true;
		}

		/*
		 * apply unary rules to lexical categories; typeChange needs to come
		 * before typeRaise since some results of typeChange can be type-raised
		 * (but not vice versa)
		 */
		for (int i = 0; i < numWords; i++) {
			for (SuperCategory superCat : chart.cell(i, 1).getSuperCategories()) {
				calcScore(superCat, false);
			}

			typeChange(chart.cell(i, 1), i, 1);
			typeRaise(chart.cell(i, 1), i, 1);

			/*
			 * 260215: it was discovered that sorting the leaves (just sorting,
			 * no beam) can cause slight variations in the results; this has
			 * probably got to do with supercategories that have the same score,
			 * but only part of them survive the (later) beam.
			 * 
			 * By changing the order of supercategories in the leaves, the order
			 * of supercategories in the higher cells vary too, in the sense
			 * that supercategories having the same score can be in different
			 * order. This is because there is no further tiebreaker after
			 * comparing scores.
			 */
			chart.cell(i, 1).applyBeam(0, beta);

			postParse(i, 1, numWords);
		}

		jloop:
		for (int j = 2; j <= numWords; j++) {
			for (int i = j - 2; i >= 0; i--) {
				if (Chart.numSuperCategories > MAX_SUPERCATS) {
					maxSuperCatsExceeded = true;
					System.out.println("MAX_SUPERCATS exceeded. (" + Chart.numSuperCategories + " > " + MAX_SUPERCATS + ")");
					log.println("MAX_SUPERCATS exceeded. (" + Chart.numSuperCategories + " > " + MAX_SUPERCATS + ")");
					break jloop;
				}

				int span = j - i;
				setCellSize(chart, i, span);

				for (int k = i + 1; k < j; k++) {
					int leftSpan = k - i;
					int rightSpan = j - k;

					if (printDetailedOutput) {
						System.out.println("Combining cells: (" + i + "," + leftSpan + ") (" + k + "," + rightSpan + ")");
					}

					if (cubePruning) {
						combineBetter(chart.cell(i, leftSpan), chart.cell(k, rightSpan), i, span, (span == numWords));
					} else {
						combine(chart.cell(i, leftSpan), chart.cell(k, rightSpan), i, span, (span == numWords));
					}
				}

				if (cubePruning) {
					chart.cell(i, span).combinePreSuperCategories(beamSize);
				}

				if (span < numWords) {
					typeChange(chart.cell(i, span), i, span);
					typeRaise(chart.cell(i, span), i, span);
				}

				chart.cell(i, span).applyBeam(beamSize, beta);

				postParse(i, span, numWords);
			}
		}

		return true;
	}

	/**
	 * Dummy function for extensions to parseSentence() by subclasses.
	 * 
	 * @return true if pre-parsing succeeds, false if pre-parsing fails
	 */
	protected boolean preParse() {
		return true;
	}

	/**
	 * Dummy function for extensions to parseSentence() by subclasses.
	 * 
	 * @param pos position of the current cell in parseSentence()
	 * @param span span of the current cell in parseSentence()
	 * @param numWords number of words in sentence
	 */
	protected void postParse(int pos, int span, int numWords) {
		return;
	}

	/**
	 * Initialises clearCounters for all cells. The value of a cell (pos, span)
	 * is numWords - span.
	 * 
	 * @param chart chart
	 */
	private void setClearCounters(Chart chart) {
		int numWords = chart.numWords;

		for (int span = 1; span <= numWords; span++) {
			int clearCounter = numWords - span;
			for (int pos = 0; pos <= numWords - span; pos++) {
				chart.cell(pos, span).setClearCounter(clearCounter);
			}
		}
	}

	/**
	 * Initialises initial cell capacity.
	 * 
	 * The initial (expected cell capacity) is the number of combine operations
	 * (span-1) multiplied by the number of possible combinations of supercats
	 * from two cells (beamSize * beamSize), and finally by 2 to allocate for
	 * extra supercats generated by type changing and raising.
	 * 
	 * @param chart chart
	 */
	private void setCellSize(Chart chart, int pos, int span) {
		int minCapacity = (span-1) * beamSize * beamSize * 2;
		chart.cell(pos, span).getSuperCategories().ensureCapacity(minCapacity);
	}

	public void combine(Cell leftCell, Cell rightCell, int position, int span, boolean atRoot) {
		results.clear();

		for (SuperCategory leftSuperCat : leftCell.getSuperCategories()) {
			for (SuperCategory rightSuperCat : rightCell.getSuperCategories()) {
				rules.combine(leftSuperCat, rightSuperCat, results, printDetailedOutput, sentence);
			}
		}

		chart.addNoDP(position, span, results);

		for (SuperCategory superCat : results) {
			calcScore(superCat, atRoot);
		}

		leftCell.decrementClearCounter();
		rightCell.decrementClearCounter();
	}

	public void combineBetter(Cell leftCell, Cell rightCell, int position, int span, boolean atRoot) {
		if ( !leftCell.isEmpty() && !rightCell.isEmpty() ) {
			int leftSize = leftCell.getSuperCategories().size();
			int rightSize = rightCell.getSuperCategories().size();

			LinkedList<SuperCategory> kbest = new LinkedList<SuperCategory>();

			if ( leftSize*rightSize <= beamSize ) {
				results.clear();

				for (SuperCategory leftSuperCat : leftCell.getSuperCategories()) {
					for (SuperCategory rightSuperCat : rightCell.getSuperCategories()) {
						rules.combine(leftSuperCat, rightSuperCat, results, printDetailedOutput, sentence);
					}
				}

				for (SuperCategory superCat : results) {
					calcScore(superCat, atRoot);
					kbest.add(superCat);
				}
			} else {
				LinkedList<Pair<Integer, Integer>> pairs = new LinkedList<Pair<Integer, Integer>>();
				PriorityQueue<Pair<SuperCategory, Pair<Integer, Integer>>> queue = 
						new PriorityQueue<Pair<SuperCategory, Pair<Integer, Integer>>>(beamSize,
								new Comparator<Pair<SuperCategory, Pair<Integer, Integer>>>(){
							@Override
							public int compare(Pair<SuperCategory, Pair<Integer, Integer>> p1, Pair<SuperCategory, Pair<Integer, Integer>> p2){
								if ( p1.x == null ) {
									return 0;
								} else if ( p2.x == null ) {
									return -1;
								} else {
									return p1.x.compareTo(p2.x);
								}
							}});

				boolean[][] track = new boolean[leftSize][rightSize];

				int leftIndex = 0;
				int rightIndex = 0;

				pairs.add(new Pair<Integer, Integer>(leftIndex, rightIndex));
				track[leftIndex][rightIndex] = true;

				while (kbest.size() < beamSize) {
					while (!pairs.isEmpty()) {
						results.clear();

						Pair<Integer, Integer> pair = pairs.poll();
						leftIndex = pair.x;
						rightIndex = pair.y;

						SuperCategory leftSuperCat = leftCell.getSuperCategories().get(leftIndex);
						SuperCategory rightSuperCat = rightCell.getSuperCategories().get(rightIndex);

						rules.combine(leftSuperCat, rightSuperCat, results, printDetailedOutput, sentence);

						if ( !results.isEmpty() ) {
							for (SuperCategory resultSuperCat : results) {
								calcScore(resultSuperCat, atRoot);
								Pair<SuperCategory, Pair<Integer, Integer>> queueElement = 
										new Pair<SuperCategory, Pair<Integer, Integer>>(resultSuperCat, new Pair<Integer, Integer>(leftIndex, rightIndex));
								queue.add(queueElement);
							}
						} else {
								queue.add(new Pair<SuperCategory, Pair<Integer, Integer>>(null, new Pair<Integer, Integer>(leftIndex, rightIndex)));
						}
					}

					Pair<SuperCategory, Pair<Integer, Integer>> topElement = queue.poll();

					if ( topElement == null ) {
						break;
					} else if ( topElement.x != null ) {
						kbest.add(topElement.x);
					}

					if ( topElement.y.x+1 < leftSize && !track[topElement.y.x+1][topElement.y.y] ) {
						pairs.add(new Pair<Integer, Integer>(topElement.y.x+1, topElement.y.y));
						track[topElement.y.x+1][topElement.y.y] = true;
					}

					if ( topElement.y.y+1 < rightSize && !track[topElement.y.x][topElement.y.y+1] ) {
						pairs.add(new Pair<Integer, Integer>(topElement.y.x, topElement.y.y+1));
						track[topElement.y.x][topElement.y.y+1] = true;
					}
				}
			}

			Collections.sort(kbest);
			chart.cell(position, span).getPreSuperCategories().add(kbest);
		}

		leftCell.decrementClearCounter();
		rightCell.decrementClearCounter();
	}

	@Override
	public void typeChange(Cell cell, int position, int span) {
		results.clear();
		rules.typeChange(cell.getSuperCategories(), results);
		chart.addNoDP(position, span, results);

		for (SuperCategory superCat : results) {
			calcScore(superCat, false);
		}
	}

	@Override
	public void typeRaise(Cell cell, int position, int span) {
		results.clear();
		rules.typeRaise(cell.getSuperCategories(), results);
		chart.addNoDP(position, span, results);

		for (SuperCategory superCat : results) {
			calcScore(superCat, false);
		}
	}

	public boolean root() {
		Cell root = chart.root();

		return !root.isEmpty();
	}

	/**
	 * Calculates the score of a supercategory.
	 * 
	 * The method assumes that the scores of its children (if any) have been
	 * calculated.
	 * 
	 * The method also assumes if the supercategory is a leaf, only its initial
	 * score has been calculated.
	 * 
	 * @param superCat supercategory
	 * @param atRoot if the supercategory is a root supercategory
	 */
	public void calcScore(SuperCategory superCat, boolean atRoot) {
		SuperCategory leftChild = superCat.leftChild;
		SuperCategory rightChild = superCat.rightChild;

		if (leftChild != null) {
			if (rightChild != null) {
				calcScoreBinary(superCat, leftChild.score + rightChild.score);
				if (atRoot) {
					calcScoreRoot(superCat);
				}
			} else {
				// assumes no unary rules applied at the root
				calcScoreUnary(superCat, leftChild.score);
			}
		} else {
			calcScoreLeaf(superCat);
		}
	}

	/**
	 * Calculates the sum of initial scores of leaves of a tree.
	 * 
	 * @param superCat supercategory
	 * @return sum of initial scores of leaves
	 */
	public double calcSumLeafInitialScore(SuperCategory superCat) {
		SuperCategory leftChild = superCat.leftChild;
		SuperCategory rightChild = superCat.rightChild;

		double sum = 0.0;

		if (leftChild != null) {
			if (rightChild != null) {
				sum += calcSumLeafInitialScore(superCat.rightChild);
			}
			sum += calcSumLeafInitialScore(superCat.leftChild);
		} else {
			if ( weights.getWeight(0) != 0.0 ) {
				sum += superCat.inside/weights.getWeight(0);
			}
		}

		return sum;
	}

	/**
	 * Calculates the score of a supercategory.
	 * 
	 * The method assumes that the scores of its children (if any) have not been
	 * calculated.
	 * 
	 * The method also assumes if the supercategory is a leaf, only its initial
	 * score has been calculated.
	 * 
	 * @param superCat supercategory
	 * @param atRoot if the supercategory is a root supercategory
	 */
	public void calcScoreRecursive(SuperCategory superCat, boolean atRoot) {
		SuperCategory leftChild = superCat.leftChild;
		SuperCategory rightChild = superCat.rightChild;

		if (leftChild != null) {
			calcScoreRecursive(leftChild, false);
			if (rightChild != null) {
				calcScoreRecursive(rightChild, false);
				calcScoreBinary(superCat, leftChild.score + rightChild.score);
				if (atRoot) {
					calcScoreRoot(superCat);
				}
			} else {
				// assumes no unary rules applied at the root
				calcScoreUnary(superCat, leftChild.score);
			}
		} else {
			calcScoreLeaf(superCat);
		}
	}

	/**
	 * Calculates the score of a root supercategory i.e. initial score + root
	 * feature scores.
	 * 
	 * The method assumes that the root supercategory already has an initial
	 * score from recursion.
	 * 
	 * @param superCat root supercategory
	 */
	private void calcScoreRoot(SuperCategory superCat) {
		featureIDs.clear();

		features.collectRootFeatures(superCat, sentence, featureIDs);

		Iterator<Integer> it = featureIDs.iterator();
		while ( it.hasNext() ) {
			superCat.score += weights.getWeight(it.next());
		}
	}

	/**
	 * Calculates the score of an unary supercategory i.e. child score + unary
	 * feature scores.
	 * 
	 * @param superCat unary supercategory
	 * @param childScore child score
	 */
	private void calcScoreUnary(SuperCategory superCat, double childScore) {
		superCat.score = childScore;
		featureIDs.clear();

		features.collectUnaryFeatures(superCat, sentence, featureIDs);

		Iterator<Integer> it = featureIDs.iterator();
		while ( it.hasNext() ) {
			superCat.score += weights.getWeight(it.next());
		}
	}

	/**
	 * Calculates the score of a binary supercategory i.e. children score +
	 * binary feature scores.
	 * 
	 * @param superCat binary supercategory
	 * @param childrenScore children score
	 */
	private void calcScoreBinary(SuperCategory superCat, double childrenScore) {
		superCat.score = childrenScore;
		featureIDs.clear();

		features.collectBinaryFeatures(superCat, sentence, featureIDs);

		Iterator<Integer> it = featureIDs.iterator();
		while ( it.hasNext() ) {
			superCat.score += weights.getWeight(it.next());
		}
	}

	/**
	 * Calculates the score of a leaf supercategory i.e. initial score + leaf
	 * feature scores.
	 * 
	 * The method assumes that the leaf supercategory already has an initial
	 * score from loading the sentence.
	 * 
	 * The method should only be called once for every leaf supercategory.
	 * 
	 * @param superCat leaf supercategory
	 */
	private void calcScoreLeaf(SuperCategory superCat) {
		featureIDs.clear();

		features.collectLeafFeatures(superCat, sentence, featureIDs);

		Iterator<Integer> it = featureIDs.iterator();
		while ( it.hasNext() ) {
			superCat.score += weights.getWeight(it.next());
		}
	}

	public void printDeps(PrintWriter out, Relations relations, Sentence sentence) {
		double maxScore = Double.NEGATIVE_INFINITY;
		SuperCategory maxRoot = null;

		Cell root = chart.root();

		for (SuperCategory superCat : root.getSuperCategories()) {
			double currentScore = superCat.score;
			if (currentScore > maxScore) {
				maxScore = currentScore;
				maxRoot = superCat;
			}
		}

		if (maxRoot != null) {
			printDeps(out, relations, sentence, maxRoot);
		}
	}

	public void printDeps(PrintWriter out, Relations relations, Sentence sentence, SuperCategory superCat) {
		for (FilledDependency filled = superCat.filledDeps; filled != null; filled = filled.next) {
			filled.printFullJslot(out, relations, sentence);
		}

		if (superCat.leftChild != null) {
			printDeps(out, relations, sentence, superCat.leftChild);

			if (superCat.rightChild != null) {
				printDeps(out, relations, sentence, superCat.rightChild);
			}
		} else {
			sentence.addOutputSupertag(superCat.cat);
		}
	}
}
