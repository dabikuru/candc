package printer;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import chart_parser.Chart;
import io.Sentence;
import lexicon.Categories;

import java.io.PrintWriter;

import static java.util.Comparator.comparing;

class DepsPrinter extends Printer {


    DepsPrinter(Categories cats) {
        super(cats);
    }

    public void printDerivation(PrintWriter out, Chart chart, Sentence sentence) {
        // Find the root Category with the highest score
        // Print the dependencies stemming from the maximum-score root category
        chart.root().getSuperCategories().stream()
                .max(comparing(sc -> sc.score))
                .ifPresent(maxRoot -> printDeps(out, sentence, maxRoot));
    }

    public void printDeps(PrintWriter out, Sentence sentence, SuperCategory superCat) {
        for (FilledDependency filled = superCat.filledDeps; filled != null; filled = filled.next) {
            filled.printFullJslot(out, cats.dependencyRelations, sentence);
        }

        if (superCat.leftChild != null) {
            printDeps(out, sentence, superCat.leftChild);

            if (superCat.rightChild != null) {
                printDeps(out, sentence, superCat.rightChild);
            }
        } else {
            sentence.addOutputSupertag(superCat.cat);
        }
    }

}
