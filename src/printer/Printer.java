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

    /**
     * Print derivation.
     * If the correct set of dependencies has been found already, pass it as 'deps' (e.g. for Parser).
     * Otherwise, it will be obtained from the chart (e.g. for ParserBeam)
     */
    abstract public void printDerivation(PrintWriter out, Chart chart, Set<FilledDependency> deps, Sentence sentence);
}