package printer;

import chart_parser.Chart;
import io.Sentence;
import lexicon.Categories;
import lexicon.Relations;

import java.io.PrintWriter;

public abstract class Printer {

    Categories cats;

    Printer(Categories cats) {
        this.cats = cats;
    }

    abstract public void printDerivation(PrintWriter out, Chart chart, Relations relations, Sentence sentence);
}