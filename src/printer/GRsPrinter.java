package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import chart_parser.Chart;
import gr.GR;
import io.Sentence;
import lexicon.Categories;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;

class GRsPrinter extends Printer {

    public List<FilledDependency> filledDependencies = new LinkedList<>();  // "seen" dependencies
    public List<GR> GRs = new LinkedList<>();                               // GRs to be printed

    GRsPrinter(Categories cats) {
        super(cats);
    }

    public void printDerivation(PrintWriter out, Chart chart, Set<FilledDependency> deps, Sentence sentence) {
        filledDependencies.clear();
        GRs.clear();

        // Find the highest scoring root Category
        chart.root().getSuperCategories().stream()
                .max(comparing(sc -> sc.score))
                .ifPresent(maxRoot -> {
                    if (deps == null)       // ParserBeam does not provide a set of dependencies
                        getGRs(maxRoot, sentence);
                    else                    // Parser does
                        getGRsFromMaxEquiv(maxRoot, sentence);
                });

        GRs.forEach(out::println);
    }

//    @Override
//    public void printDerivation(PrintWriter out, Set<FilledDependency> deps, Sentence sentence) {
//    }

//    @Override
//    public void printDerivation(PrintWriter out, Chart chart, Sentence sentence) {
//        filledDependencies.clear();
//        GRs.clear();
//
//        // - Find the root Category with the highest score
//        // - Get GRs from the dependencies stemming from the maximum-score root category
//        chart.root().getSuperCategories().stream()
//                .max(comparing(sc -> sc.score))
//                .ifPresent(maxRoot -> getGRs(maxRoot, sentence));
//
//        GRs.forEach(out::println);
//    }

    /**
     * Populate the list of GRs to print, given the sentence and a supercategory
     */
    protected void getGRs(SuperCategory sc, Sentence sent) {
        //FIXME: unify for Parser and ParserBeam
        if (sc.leftChild != null) {
            getGRs(sc.leftChild, sent);

            if (sc.rightChild != null) {
                getGRs(sc.rightChild, sent);
            }
        }

        //Store the lexical categories for printing
        if (sc.leftChild == null) {
            sent.addOutputSupertag(sc.cat);
        }

        sc.getGRs(GRs, cats.dependencyRelations, filledDependencies, sent);
    }

    protected void getGRsFromMaxEquiv(SuperCategory sc, Sentence sent) {
        if (sc.leftChild != null && sc.leftChild.maxEquivSuperCat != null) {
            System.out.println("sc.leftChild.cat = " + sc.leftChild.cat);
            getGRsFromMaxEquiv(sc.leftChild.maxEquivSuperCat, sent);

            if (sc.rightChild != null && sc.rightChild.maxEquivSuperCat != null) {
                System.out.println("sc.rightChild.cat = " + sc.rightChild.cat);
                getGRsFromMaxEquiv(sc.rightChild.maxEquivSuperCat, sent);
            }
        }

        //Store the lexical categories for printing
        if (sc.leftChild == null) {
            sent.addOutputSupertag(sc.cat);
        }

        sc.getGRs(GRs, cats.dependencyRelations, filledDependencies, sent);
    }
}
