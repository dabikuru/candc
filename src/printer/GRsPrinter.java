package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import chart_parser.Cell;
import chart_parser.Chart;
import gr.GR;
import io.Sentence;
import lexicon.Categories;
import lexicon.Relations;

import java.io.PrintWriter;
import java.util.ArrayList;

class GRsPrinter extends Printer {

    public ArrayList<FilledDependency> filledDependencies;
    public ArrayList<GR> GRs;

    GRsPrinter(Categories cats) {
        super(cats);
    }

    protected void unary(Sentence sent) {
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

        // Get GRs from the dependencies stemming from the maximum-score root category
        if (maxRoot != null) {
            getGRs(maxRoot, sentence);
        }

        for (GR gr : GRs)
            out.println(gr.toString());
    }

    /**
     * Populate the list of GRs to print, given the sentence and a supercategory
     */
    protected void getGRs(SuperCategory sc, Sentence sent) {
        if (sc.leftChild != null && sc.leftChild.maxEquivSuperCat != null) {
            getGRs(sc.leftChild.maxEquivSuperCat, sent);

            if (sc.rightChild != null && sc.rightChild.maxEquivSuperCat != null)
                getGRs(sc.rightChild.maxEquivSuperCat, sent);

        }

        //Store the lexical categories for printing
        if (sc.leftChild != null)
            sent.outputSupertags.add(sc.cat);

        sc.getGRs(GRs, cats.dependencyRelations, filledDependencies, sent);
    }
}
