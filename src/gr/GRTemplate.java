package gr;

import cat_combination.FilledDependency;
import cat_combination.SuperCategory;
import io.Sentence;
import lexicon.Categories;
import lexicon.Category;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: implement methods
public class GRTemplate {
    protected String tmpCat; // temporary storage for the category constraint
    public String markedup;
    public boolean ignore;
    public String fmt;
    public short otherRel;

    public boolean constrained;     // are there any constraints on
    public GRConstraints groups;    // lexical constraint groups
    public String conLex;          // lexical constraint label
    public Category conCat;        // category constraint
    public short conRel;           // relation that the category constraint applies to
//    public GRTemplate next;      TODO: these should be in a linked list anyway

    private static final Pattern grPattern = Pattern.compile("(?<fmt>[a-z]+(\\s(%[\\dflck]|_))*)(?<cons>(\\s=\\S+)*)");
    private static final Pattern consPattern = Pattern.compile("=(\\S+)");
    private static final Pattern fmtPattern = Pattern.compile("%[\\dflck]");


    /**
     * Create a template and adds format information by scanning a markedup line
     */
    public GRTemplate(Categories cats, String cat, short slot, String markedup) {
        this.markedup = markedup;
        this.groups = cats.grConstraints;

        Matcher m = grPattern.matcher(markedup);

        if (!m.find())
            throw new Error("Invalid format for GR rule");
        fmt = m.group("fmt");

        // Check for ignore
        if ("ignore".equals(fmt)) {
            ignore = true;
            return;
        }


        //Process the rule: check slots numbers and replace with 'o', set relation number
        Matcher fmtMatcher = fmtPattern.matcher(fmt);
        while (fmtMatcher.find()) {
            char c = fmtMatcher.group().charAt(1);
            switch (c) {
                case '1':
                case '2':
                case '3':
                    short oslot = (short) Character.getNumericValue(c);
                    if (oslot == slot) throw new Error("GR should not use own slot as field specifier %" + c);
                    otherRel = cats.dependencyRelations.getRelID(cat, slot);
                    break;
                case 'c':
                    conRel = 1;
                    break;
                case 'k':
                    conRel = 2;
                    break;
            }
        }
        fmt = fmt.replaceAll("\\d", "o").replace('k', 'c');


        // Process constraints, if any
        if (!m.group("cons").isEmpty()) {
            constrained = true;
            Matcher constraintMacher = consPattern.matcher(m.group("cons"));
            while (constraintMacher.find()) {
                String cons = constraintMacher.group();
                if (Character.isLowerCase(cons.charAt(1))) {
                    //Lexical constraint
                    if (!(conLex == null)) throw new Error("lexical constraint has already been set for " + markedup);
                    conLex = cons; // preserve '='
                } else {
                    //Categorial constraint
                    if (!(tmpCat == null)) throw new Error("category constraint has already been set for " + markedup);
                    tmpCat = cons.substring(1); // get rid of '='
                }
            }
        }
    }

    protected void get(List<GR> grs, String format, Sentence sent, SuperCategory sc,
                       FilledDependency dep, FilledDependency other, FilledDependency constraint) {
        throw new UnsupportedOperationException("Not implemented yet");




    }

    public void setCat(Categories cats) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * Check if all constraints are satisfied
     */
    public boolean satisfy(Sentence sent, SuperCategory sc, FilledDependency filled) {
        if (!constrained)
            return true;

        // False is the categorial constraint is not satisfied
        if (conCat != null && !sent.outputSupertags.get(filled.fillerIndex - 1).equals(conCat))
            return false;

        // Check if lexical constraints are satisfied
        if (!conLex.isEmpty()) {
            String word = sent.words.get(filled.headIndex - 1).toLowerCase();
            return groups.get(conLex, word);
        }

        return true;
    }

    public void get(List<GR> grs, Sentence sent, SuperCategory sc,
                    List<FilledDependency> seen, List<FilledDependency> filled) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
