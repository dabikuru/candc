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
    public String fmt = "";
    public short other_rel;

    public boolean constrained;     // are there any constraints on
    public GRConstraints groups;    // lexical constraint groups
    public String con_lex;          // lexical constraint label
    public Category con_cat;        // category constraint
    public short con_rel;           // relation that the category constraint applies to
    public GRTemplate next;

    /**
     * Creates a template and adds format information by scanning a markedup line
     */
    public GRTemplate(Categories cats, String cat, short slot, String markedup) {
        this.markedup = markedup;
        this.groups = cats.grConstraints;

        for (int i = 0; i < markedup.length(); i++) {
            if (markedup.charAt(i) == '#')
                break;

            if (markedup.charAt(i) == '=') {
                constrained = true;

                String val = markedup.substring(i, markedup.indexOf(' ', i));
                // If the first char after '=' is lowercase, this is a lexical constraint
                if (Character.isLowerCase(val.charAt(1))) {
                    if (!con_lex.isEmpty())
                        throw new Error("lexical constraint has already been set for " + markedup);
                    con_lex = '=' + val;
                } else {
                    if (!_tmpCat.isEmpty())
                        throw new Error("category constraint has already been set for " + markedup);
                    _tmpCat = val;
                }
                continue;
            }

            fmt += markedup.charAt(i);
            if (markedup.charAt(i) == '%') {
                if (++i == markedup.length())
                    throw new Error("GR format expression ends with a single %");

                short oslot = 0;
                char c = markedup.charAt(i);
                switch (c) {
                    case '%':
                    case 'f':
                    case 'l':
                        fmt += c;
                        continue;
                    case '1':
                    case '2':
                    case '3':
                        oslot = (short) Character.getNumericValue(c);
                        if (oslot == slot)
                            throw new Error("GR should not use own slot as field specifier %" + c);
                        other_rel = cats.dependencyRelations.getRelID(cat, slot); //FIXME: type & RelId 1 or 2?
                        fmt += 'o';
                        continue;
                    case 'c':
                        fmt += 'c';
                        con_rel = 1;
                        continue;
                    case 'k':
                        fmt += 'c';
                        con_rel = 2;
                        continue;
                    default:
                        throw new Error("unrecognised GR field specifier %" + c);

                }
            }
        }

        //FIXME: figure out why we erase the tail of string
        for (int i = fmt.length(); i != 0; i--)
            if (fmt.charAt(i - 1) != ' ') {
                fmt = fmt.substring(i + 1);
                break;
            }

        if ("ignore".equals(this.fmt))
            ignore = true;
    }

    protected void get(List<GR> grs, String format, Sentence sent, SuperCategory sc,
                       FilledDependency dep, FilledDependency other, FilledDependency constraint) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void set_cat(Categories cats) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Checks if all constraints are satisfied
     */
    public boolean satisfy(Sentence sent, SuperCategory sc, FilledDependency filled) {
        if (!constrained)
            return true;

        // False is the categorial constraint is not satisfied
        if (con_cat != null && !sent.outputSupertags.get(filled.fillerIndex - 1).equals(con_cat))
            return false;

        // Check if lexical constraints are satisfied
        if (!con_lex.isEmpty()) {
            String word = sent.words.get(filled.headIndex - 1).toLowerCase();
            return groups.get(con_lex, word);
        }

        return true;
    }

    public void get(List<GR> grs, Sentence sent, SuperCategory sc,
                    List<FilledDependency> seen, List<FilledDependency> filled) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
