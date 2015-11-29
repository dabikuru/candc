package printer;

import chart_parser.Chart;
import io.Sentence;
import lexicon.Relations;

import java.io.PrintWriter;

public abstract class Printer {

    abstract
    public void printDerivation(PrintWriter out, Chart chart, Relations relations, Sentence sentence);
}