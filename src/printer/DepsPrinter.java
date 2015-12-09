package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import chart_parser.Cell;
import chart_parser.Chart;
import io.Sentence;
import lexicon.Categories;
import lexicon.Relations;

import java.io.PrintWriter;

class DepsPrinter extends Printer {


    DepsPrinter(Categories cats) {
        super(cats);
    }

    public void printDerivation(PrintWriter out, Chart chart, Relations relations, Sentence sentence) {
        double maxScore = Double.NEGATIVE_INFINITY;
        SuperCategory maxRoot = null;

        Cell root = chart.root();

        // Find the root Category with the highest score
        for (SuperCategory superCat : root.getSuperCategories()) {
            double currentScore = superCat.score;
            if (currentScore > maxScore) {
                maxScore = currentScore;
                maxRoot = superCat;
            }
        }

        // Print the dependencies stemming from the maximum-score root category
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
