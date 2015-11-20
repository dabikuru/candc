import io.Preface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import model.Lexicon;
import cat_combination.RuleInstancesParams;
import chart_parser.ChartParserBeam;
import chart_parser.CountFeaturesDecoder;

public class CountFeatures {
	public static void main(String[] args) {
		int MAX_WORDS = 150;
		int MAX_SUPERCATS = 500000;

		boolean altMarkedup = false;
		boolean eisnerNormalForm = true;
		boolean detailedOutput = false;
		boolean newFeatures = true;
		boolean cubePruning = false;
		// boolean oracleFscore = false;

		String grammarDir = "data/baseline_expts/grammar";
		String lexiconFile = "data/baseline_expts/working/lexicon/wsj02-21.wordsPos";
		String featuresFile = "data/baseline_expts/working/lexicon/wsj02-21.feats.1-22";

		RuleInstancesParams ruleInstancesParams = new RuleInstancesParams(true, false, false, false, false, false, grammarDir);

		double[] betas = { 0.0001 };

		// boolean adaptiveSupertagging = false;
		// double[] betas = { 0.0001, 0.001, 0.01, 0.03, 0.075 };

		int beamSize = 32;
		double beta = Double.NEGATIVE_INFINITY;

		if ( args.length < 7 ) {
			System.err.println("CountFeatures requires 7 arguments: <inputFile> <outputFile> <outputWeightsFile> <logFile> <weightsFile> <fromSentence> <toSentence>");
			return;
		}

		String inputFile = args[0];
		String outputFile = args[1];
		String outputWeightsFile = args[2];
		String logFile = args[3];
		String weightsFile = args[4];
		String fromSent = args[5];
		String toSent = args[6];

		int fromSentence = Integer.valueOf(fromSent);
		int toSentence = Integer.valueOf(toSent);

		Lexicon lexicon = null;

		try {
			lexicon = new Lexicon(lexiconFile);
		} catch (IOException e) {
			System.err.println(e);
			return;
		}

		ChartParserBeam parser = null;

		try {
			/*
			parser = new ChartParser(grammarDir, altMarkedup,
					eisnerNormalForm, MAX_WORDS, MAX_SUPERCATS, detailedOutput,
					oracleFscore, adaptiveSupertagging, ruleInstancesParams,
					lexicon, null, null);
			*/
			parser = new ChartParserBeam(grammarDir, altMarkedup,
					eisnerNormalForm, MAX_WORDS, MAX_SUPERCATS, detailedOutput,
					ruleInstancesParams, lexicon, featuresFile, weightsFile,
					newFeatures, false, cubePruning, beamSize, beta);
		} catch (IOException e) {
			System.err.println(e);
			return;
		}

		CountFeaturesDecoder countFeaturesDecoder = new CountFeaturesDecoder(parser.categories);

		BufferedReader in = null;
		PrintWriter out = null;
		PrintWriter log = null;
		PrintWriter weights = null;

		try {
			in = new BufferedReader(new FileReader(inputFile));

			Preface.readPreface(in);

			out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
			log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
			weights = new PrintWriter(new BufferedWriter(new FileWriter(outputWeightsFile)));

			out.println("# mandatory preface");
			out.println("# mandatory preface");
			out.println();

			weights.println("# mandatory preface");
			weights.println("# mandatory preface");
			weights.println();

			for (int numSentence = fromSentence; numSentence <= toSentence; numSentence++) {
				System.out.println("Parsing sentence "+ numSentence);
				log.println("Parsing sentence "+ numSentence);

				if ( !parser.parseSentence(in, null, log, betas) ) {
					System.out.println("No such sentence; no more sentences.");
					log.println("No such sentence; no more sentences.");
					break;
				}

				if (!parser.maxWordsExceeded && !parser.maxSuperCatsExceeded) {
					boolean success = countFeaturesDecoder.countFeatures(parser.chart, parser.sentence);

					if (success) {
						System.out.println("Success.");
						log.println("Success.");
					} else {
						System.out.println("No root category.");
						log.println("No root category.");
					}
				}
			}

			countFeaturesDecoder.mergeAllFeatureCounts(parser.features);
			parser.features.print(out);
			parser.features.printWeights(parser.weights, weights);
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				if ( weights != null ) { weights.close(); }
				if ( log != null ) { log.close(); }
				if ( out != null ) { out.close(); }
				if ( in != null ) { in.close(); }
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
}
