package printer;

import chart_parser.Chart;
import io.Sentence;
import lexicon.Categories;

import java.io.PrintWriter;

public abstract class Printer {

    Categories cats;

    Printer(Categories cats) {
        this.cats = cats;
    }

    //TODO: remove Relations (can access from cats)
    abstract public void printDerivation(PrintWriter out, Chart chart, Sentence sentence);
}