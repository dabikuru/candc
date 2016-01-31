package printer;

import cat_combination.FilledDependency;
import chart_parser.Chart;
import io.Sentence;
import lexicon.Categories;

import java.io.PrintWriter;
import java.util.Set;

public abstract class Printer {

    Categories cats;

    Printer(Categories cats) {
        this.cats = cats;
    }

    //TODO: implement in both subclasses
    abstract public void printDerivation(PrintWriter out, Chart chart, Set<FilledDependency> deps, Sentence sentence);

    /**
     * Print derivation when the correct dependency set has been identified already (e.g. by the Viterbi decoder)
     */
//    abstract public void printDerivation(PrintWriter out, Set<FilledDependency> deps, Sentence sentence);


    /**
     * Print derivation when the correct dependencies have to be found by traversing the chart (e.g. for the Beam parser)
     */
//    abstract public void printDerivation(PrintWriter out, Chart chart, Sentence sentence);

}