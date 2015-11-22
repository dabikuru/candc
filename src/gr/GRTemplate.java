package gr;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import io.Sentence;
import lexicon.Categories;
import lexicon.Category;

import java.util.List;

//TODO: implement methods
public class GRTemplate {
    protected String _tmpCat; // temporary storage for the category constraint
    public String markedup;
    public boolean ignore;
    public String fmt;
    public short other_rel;

    public boolean constrained;    // are there any constraints on
    public GRConstraints groups; // lexical constraint groups
    public String con_lex; // lexical constraint label
    public Category con_cat;  // category constraint
    public short con_rel;       // relation that the category constraint applies to
    public GRTemplate next;

    public GRTemplate(Categories cats, String cat, long slot, String fmt) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    protected void get(List<GR> grs, String format, Sentence sent, SuperCategory sc,
                       FilledDependency dep, FilledDependency other, FilledDependency constraint) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void set_cat(Categories cats) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean satisfy(Sentence sent, SuperCategory sc, FilledDependency filled) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void get(List<GR> grs, Sentence sent, SuperCategory sc,
                    List<FilledDependency> seen, List<FilledDependency> filled) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
