package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import chart_parser.Cell;
import chart_parser.Chart;
import io.Sentence;
import lexicon.Relations;

import java.io.PrintWriter;

class DepsPrinter extends Printer{

    //TODO: check this works
    public void printDerivation(PrintWriter out, Chart chart, Relations relations, Sentence sentence) {
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
