package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
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

    protected void unary(Sentence sent) {}

    public void printDerivation(PrintWriter out, Chart chart, Relations relations, Sentence sentence) {}

    protected void getGRs(SuperCategory sc, Sentence sent) {
        if (sc.leftChild != null) {
            try {
                getGRs(sc.leftChild.maxEquivSuperCat, sent);

                if (sc.rightChild != null) {
                    getGRs(sc.rightChild.maxEquivSuperCat, sent);
                }
            } catch (NullPointerException e) {
                System.err.print(e.getStackTrace());
            }
        } else {
            //store the lexical categories for printing
            sent.outputSupertags.add(sc.cat);
        }

        //TODO: implement SuperCategory.getGRs
//        sc.getGRs(GRs, cats.markedup, cats.relations, filled, sent);

    }
}
