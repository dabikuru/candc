package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import chart_parser.Chart;
import gr.GR;
import io.Sentence;
import lexicon.Categories;
import lexicon.Relations;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import static java.util.Comparator.comparing;

class GRsPrinter extends Printer {

    public List<FilledDependency> filledDependencies = new LinkedList<>();  // "seen" dependencies
    public List<GR> GRs = new LinkedList<>();                               // GRs to be printed

    GRsPrinter(Categories cats) {
        super(cats);
    }

    protected void unary(Sentence sent) { }

    public void printDerivation(PrintWriter out, Chart chart, Relations relations, Sentence sentence) {
        filledDependencies.clear();
        GRs.clear();

        // - Find the root Category with the highest score
        // - Get GRs from the dependencies stemming from the maximum-score root category
        chart.root().getSuperCategories().stream()
                .max(comparing(sc -> sc.score))
                .ifPresent(maxRoot -> getGRs(maxRoot, sentence));

        GRs.forEach(out::println);
    }

    /**
     * Populate the list of GRs to print, given the sentence and a supercategory
     */
    protected void getGRs(SuperCategory sc, Sentence sent) {
        if (sc.leftChild != null && sc.leftChild.maxEquivSuperCat != null) {
            System.out.println("sc.leftChild.cat = " + sc.leftChild.cat);
            getGRs(sc.leftChild.maxEquivSuperCat, sent);

            if (sc.rightChild != null && sc.rightChild.maxEquivSuperCat != null) {
                System.out.println("sc.rightChild.cat = " + sc.rightChild.cat);
                getGRs(sc.rightChild.maxEquivSuperCat, sent);
            }
        }

        //FIXME: why does this work...?
//        if (sc.leftChild != null) {
//            getGRs(sc.leftChild, sent);
//
//            if (sc.rightChild != null) {
//                getGRs(sc.rightChild, sent);
//            }
//        }
//
//        //Store the lexical categories for printing
//        if (sc.leftChild == null) {
//            sent.addOutputSupertag(sc.cat);
//        }


        sc.getGRs(GRs, cats.dependencyRelations, filledDependencies, sent);
    }
}
