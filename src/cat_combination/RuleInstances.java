package cat_combination;

import io.Preface;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;

import lexicon.Categories;
import lexicon.Category;

public class RuleInstances {
	HashSet<RuleCategoryPair> ruleInstances;

	public RuleInstances(String ruleInstancesFile, Categories categories) {
		ruleInstances = new HashSet<RuleCategoryPair>();
		if (ruleInstancesFile != null) {
			readRuleInstances(ruleInstancesFile, categories);
		}
	}

	public boolean contains(Category cat1, Category cat2) {
		RuleCategoryPair catPair = new RuleCategoryPair(cat1, cat2);
		return ruleInstances.contains(catPair);
	}

	public void add(RuleCategoryPair catPair) {
		ruleInstances.add(catPair);
	}

	public void print(PrintWriter out) {
		Iterator<RuleCategoryPair> iterator = ruleInstances.iterator();
		while (iterator.hasNext()) {
			((RuleCategoryPair) (iterator.next())).print(out);
		}
	}

	private void readRuleInstances(String ruleInstancesFile,
			Categories categories) {
		try {
			BufferedReader in = new BufferedReader(new FileReader(
					ruleInstancesFile));
			Preface.readPreface(in);

			String line;
			while ((line = in.readLine()) != null) {
				String[] tokens = line.split("\\s");
				String catString1 = tokens[0];
				String catString2 = tokens[1];
				Category cat1 = categories.canonize(catString1);
				Category cat2 = categories.canonize(catString2);
				RuleCategoryPair catPair = new RuleCategoryPair(cat1, cat2);

				ruleInstances.add(catPair);
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}
