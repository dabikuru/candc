package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import chart_parser.Chart;
import gr.GR;
import io.Sentence;
import lexicon.Categories;
import lexicon.DependencyType;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Comparator.comparing;

public class GRsPrinter extends Printer {

    public List<FilledDependency> filledDependencies = new LinkedList<>();  // "seen" dependencies
    public Set<GR> GRs = new HashSet<>();                               // GRs to be printed

    public DependencyType dependencyType;

    public GRsPrinter(Categories cats, DependencyType dependencyType) {
        super(cats);

        // Ensure the output flag matches the given markedup file
        DependencyType markedupParameter = cats.getDependencyType();
        if (dependencyType == markedupParameter)
            this.dependencyType = dependencyType;
        else
            throw new Error("Output type (" + dependencyType + ") and markedup (" + markedupParameter + ") mismatch");
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


    /**
     * Populate the list of GRs to print, given the sentence and a supercategory
     */
    protected void getGRs(SuperCategory sc, Sentence sent) {
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

        sc.getGRs(GRs, cats.dependencyRelations, filledDependencies, sent, dependencyType);
    }

    /**
     * Populate the list of GRs to print, given the sentence and a supercategory;
     * unlike geGRs, this looks at the maxEquivSuperCat for each child
     */
    protected void getGRsFromMaxEquiv(SuperCategory sc, Sentence sent) {
        if (sc.leftChild != null && sc.leftChild.maxEquivSuperCat != null) {
            getGRsFromMaxEquiv(sc.leftChild.maxEquivSuperCat, sent);

            if (sc.rightChild != null && sc.rightChild.maxEquivSuperCat != null) {
                getGRsFromMaxEquiv(sc.rightChild.maxEquivSuperCat, sent);
            }
        }

        //Store the lexical categories for printing
        if (sc.leftChild == null) {
            sent.addOutputSupertag(sc.cat);
        }

        sc.getGRs(GRs, cats.dependencyRelations, filledDependencies, sent, dependencyType);
    }
}
