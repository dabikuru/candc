package lexicon;

import gr.GRConstraints;
import io.Preface;
import utils.ByteWrapper;
import utils.ShortWrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Categories {
    // map from the plain category string to the markedup string:
    private HashMap<String, String> markedupStrings;

    // map from the markedup string to the plain category string:
    private HashMap<String, String> plainCategoryStrings;

    // map from the plain category string to the category object:
    private HashMap<String, Category> markedupCategories;

    // map from markedup category, slot pairs to a relation ID;
    // ID also indexes into an array of relation objects:
    public Relations dependencyRelations;

    // set with lexical constraints on GRs
    public GRConstraints grConstraints;

    // used in ruleInstances (and elsewhere) - use just one canonical
    // version of each category to prevent building the same category
    // over and over
    private HashMap<String, Category> seenCategories;
    private HashMap<Category, Category> canonicalCats;

    // lists of type-raised category objects of various types:
    public ArrayList<TypeRaisedCategory> trNPs;
    public ArrayList<TypeRaisedCategory> trPPs;
    public ArrayList<TypeRaisedCategory> trAPs;
    public ArrayList<TypeRaisedCategory> trVP_TOs;

    // ALT_MARKEDUP signals to use the alternative lines in the
    // markedup file (marked ! in the file)

    /**
     * @param grammarDir
     * @param ALT_MARKEDUP signals to use the alternative lines in the markedup file (marked ! in the file)
     */
    public Categories(String grammarDir, boolean ALT_MARKEDUP) {
        dependencyRelations = new Relations();
        grConstraints = new GRConstraints();

        readMarkedupFile(grammarDir, ALT_MARKEDUP);
        seenCategories = new HashMap<>();
        canonicalCats = new HashMap<>();

        trNPs = new ArrayList<>();
        readTRcategories(grammarDir + "/trNP", trNPs);

        trPPs = new ArrayList<>();
        readTRcategories(grammarDir + "/trPP", trPPs);

        trAPs = new ArrayList<>();
        readTRcategories(grammarDir + "/trAP", trAPs);

        trVP_TOs = new ArrayList<>();
        readTRcategories(grammarDir + "/trVP_to", trVP_TOs);
    }

    public String getString(String plainCategoryString) {
        return markedupStrings.get(plainCategoryString);
    }

    public String getPlainString(String markedupString) {
        return plainCategoryStrings.get(markedupString);
    }

    public Category getCategory(String plainCategoryString) {
        return markedupCategories.get(plainCategoryString);
    }


    private void readMarkedupFile(String grammarDir, boolean ALT_MARKEDUP) {
        Pattern constraintPattern, rulePattern;
        constraintPattern = Pattern.compile("=\\w+ (\\S+ ?)+\\n", Pattern.MULTILINE);
        rulePattern = Pattern.compile("\\S+ ?\\n(  \\d \\S+\\n)(  ! \\S+\\n)*(  \\d .+\\n)*", Pattern.MULTILINE);

        markedupStrings = new HashMap<>();
        plainCategoryStrings = new HashMap<>();
        markedupCategories = new HashMap<>();

        // not sure when the sentinel is used for categories, but
        // retaining from C&C:
        markedupStrings.put("+", "+");
        markedupCategories.put("+", null);

        String markedupFile = grammarDir + "/markedup";


        try (Scanner in = new Scanner(new File(markedupFile))) {

            Preface.readPreface(in);

            String plainCatString;
            String markedupCatString;
            String chunk;

            // Parse all constraints
            while ((chunk = in.findWithinHorizon(constraintPattern, 0)) != null) {
                //System.out.println("Contraints: " + chunk);

                String[] tokens = chunk.split("\\s");
                if (tokens.length < 2) {
                    throw new Error(
                            "error parsing constraints line in markedup");
                }

                String label = tokens[0];
                for (int i = 1; i < tokens.length; i++)
                    grConstraints.add(label, tokens[i]);
            }


            // Parse all markedup rule instances
            while ((chunk = in.findWithinHorizon(rulePattern, 0)) != null) {
                //System.out.println("GR rule:\n" + chunk + "\n");

                String[] lines, tokens;
                Category cat;

                lines = chunk.split("\\n");

                // 1st line: CAT
                tokens = (lines[0].trim()).split("\\s+");
                if (tokens.length != 1)
                    throw new Error(
                            "error parsing plain cat line in markedup");
                plainCatString = tokens[0];


                // 2nd line: MARKEDUP
                tokens = (lines[1].trim()).split("\\s+");
                if (tokens.length != 2) {
                    throw new Error(
                            "error parsing markedup cat line in markedup");
                }

                markedupCatString = tokens[1];
                cat = parse(markedupCatString);

                byte[] seenVariables = new byte[VarID.NUM_VARS];
                Arrays.fill(seenVariables, VarID.NONE);

                cat.reorderVariables(seenVariables, new ByteWrapper(
                        (byte) (0)));

                markedupStrings.put(plainCatString, markedupCatString);
                plainCategoryStrings.put(markedupCatString, plainCatString);
                markedupCategories.put(plainCatString, cat);

                // 3rd+line: GR
                for (int i = 2; i < lines.length; i++) {
                    tokens = lines[i].trim().split("\\s+");
                    if (tokens.length < 2) {
                        throw new Error("error parsing gr cat line in markedup");
                    }

                    if ("!".equals(tokens[0])) {
                        if (!ALT_MARKEDUP)
                            // Ignore alternative markedup
                            continue;

                        // Parse alternative markedup
                        markedupCatString = tokens[1];
                        cat = parse(markedupCatString);

                        seenVariables = new byte[VarID.NUM_VARS];
                        Arrays.fill(seenVariables, VarID.NONE);

                        cat.reorderVariables(seenVariables, new ByteWrapper(
                                (byte) (0)));

                        markedupStrings.put(plainCatString, markedupCatString);
                        plainCategoryStrings.put(markedupCatString,
                                plainCatString);
                        markedupCategories.put(plainCatString, cat);

                    } else {
                        // Read GR rule
                        short slot = Short.parseShort(tokens[0]);
                        dependencyRelations.addGR(this, markedupCatString, slot, lines[i]); //TODO: test?
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }


    /**
     * When parsing a markedup string, this will add new relations to
     * dependencyRelations (so be careful when using this outside of parsing the
     * markedup file)
     */
    public Category parse(String catString) {
        ShortWrapper numJuliaSlots = new ShortWrapper((short) (0));
        ShortWrapper prevSlot = new ShortWrapper((short) (0));
        ShortWrapper index = new ShortWrapper((short) (0));

        Category cat = consumeCategory(catString, index, true, numJuliaSlots,
                prevSlot);

        if (index.value != catString.length()) {
            throw new Error("failed to parse category string: " + catString);
        }

        return cat;
    }

    private Category consumeCategory(String markedupString, ShortWrapper index,
                                     boolean inResult, ShortWrapper numJuliaSlots, ShortWrapper prevSlot) {
        switch (markedupString.charAt(index.value)) {
            case '(':
                index.value++;
                return consumeComplex(markedupString, index, inResult,
                        numJuliaSlots, prevSlot);
            case ')': // fall through
            case '\\': // |
            case '/': // |
                throw new Error("failed to parse category string" + markedupString);
            default:
                return consumeBasic(markedupString, index, numJuliaSlots, prevSlot);
        }
    }

    private Category consumeBasic(String markedupString, ShortWrapper index,
                                  ShortWrapper numJuliaSlots, ShortWrapper prevSlot) {
        char currentChar = markedupString.charAt(index.value);

        if (!Character.isLetter(currentChar) && currentChar != ','
                && currentChar != '.' && currentChar != ';'
                && currentChar != ':') {
            throw new Error("unexpected token parsing category in consumeBasic");
        }

        String atomString = "";
        while (Character.isLetter(currentChar) || currentChar == ','
                || currentChar == '.' || currentChar == ';'
                || currentChar == ':') {
            atomString += currentChar;
            index.value++;
            if (index.value < markedupString.length()) {
                currentChar = markedupString.charAt(index.value);
            } else {
                break;
            }
        }
        Atom atom = new Atom(atomString);
        GrammaticalFeature feature = new GrammaticalFeature(
                GrammaticalFeature.NONE);

        if (index.value < markedupString.length()
                && markedupString.charAt(index.value) == '[') {
            index.value++;
            feature = consumeFeature(markedupString, index);
        }

        byte var = VarID.NONE;
        ShortWrapper lrange = new ShortWrapper((short) (0));

        if (index.value < markedupString.length()
                && markedupString.charAt(index.value) == '{') {
            index.value++;
            var = consumeVar(markedupString, index, lrange);
        }
        short slot = 0;

        if (index.value < markedupString.length()
                && markedupString.charAt(index.value) == '<') {
            index.value++;
            slot = consumeSlot(markedupString, index, numJuliaSlots, prevSlot);
        }
        checkBracketChar(markedupString, index);

        return new Category(atom, feature, var, slot, lrange.value);
    }

    private Category consumeComplex(String markedupString, ShortWrapper index,
                                    boolean inResult, ShortWrapper numJuliaSlots, ShortWrapper prevSlot) {
        Category left = consumeCategory(markedupString, index, inResult,
                numJuliaSlots, prevSlot);

        char currentChar = markedupString.charAt(index.value);
        byte slash = 0;

        if (currentChar == '/') {
            slash = Category.FWD_SLASH;
        } else if (currentChar == '\\') {
            slash = Category.BWD_SLASH;
        } else {
            throw new Error("expected a forward or backward slash"
                    + markedupString);
        }

        index.value++;

        if (inResult) {
            numJuliaSlots.value++;
        }

        Category right = consumeCategory(markedupString, index, false,
                numJuliaSlots, prevSlot);

        currentChar = markedupString.charAt(index.value);
        if (currentChar != ')') {
            throw new Error("unbalanced parentheses in category string"
                    + markedupString);
        }

        index.value++;

        byte var = VarID.NONE;
        ShortWrapper lrange = new ShortWrapper((short) (0));

        if (index.value < markedupString.length()
                && markedupString.charAt(index.value) == '{') {
            index.value++;
            var = consumeVar(markedupString, index, lrange);
        }
        short slot = 0;

        if (index.value < markedupString.length()
                && markedupString.charAt(index.value) == '<') {
            index.value++;
            slot = consumeSlot(markedupString, index, numJuliaSlots, prevSlot);
        }
        checkBracketChar(markedupString, index);

        return new Category(left, slash, right, var, slot, lrange.value);
    }

    private GrammaticalFeature consumeFeature(String markedupString,
                                              ShortWrapper index) {
        String featureString = "";
        char currentChar = markedupString.charAt(index.value);

        while (Character.isLetter(currentChar)) {
            featureString += currentChar;
            currentChar = markedupString.charAt(++index.value);
        }
        if (currentChar != ']') {
            throw new Error("unexpected character parsing feature");
        }

        index.value++;

        return new GrammaticalFeature(featureString);
    }

    private byte consumeVar(String markedupString, ShortWrapper index,
                            ShortWrapper lrange) {
        String varString = "";
        char currentChar = markedupString.charAt(index.value);

        while (Character.isLetter(currentChar) || currentChar == '_') {
            varString += currentChar;
            currentChar = markedupString.charAt(++index.value);
        }
        if (currentChar == '*') {
            /*
             * from the C&C code: lrange.value = markedup.size(); this assumes
			 * that the markedup data structure is ordered and categories can be
			 * accessed also by an index (current size gives the index)
			 *
			 * leave for later:
			 */
            lrange.value = 1;
            currentChar = markedupString.charAt(++index.value);
        }
        if (currentChar != '}') {
            throw new Error("unexpected character parsing var: "
                    + markedupString + " " + currentChar);
        }

        index.value++;

        return VarID.convert(varString);
    }

    private short consumeSlot(String markedupString, ShortWrapper index,
                              ShortWrapper numJuliaSlots, ShortWrapper prevSlot) {
        String slotString = "";
        char currentChar = markedupString.charAt(index.value);

        while (Character.isDigit(currentChar)) {
            slotString += currentChar;
            currentChar = markedupString.charAt(++index.value);
        }
        if (currentChar != '>') {
            throw new Error("unexpected character parsing dependency");
        }

        index.value++;

		/*
         * the addRelation method from the Relations class assumes that the
		 * relations for a particular string are added consecutively and that
		 * the slots are ordered from 1, increasing by 1 each time:
		 */
        short slotShort = Short.valueOf(slotString);
        if (slotShort != prevSlot.value + 1) {
            throw new Error("slots are not increasing by 1! " + markedupString
                    + " " + slotShort + " " + prevSlot.value);
        }
        prevSlot.value++;

        return dependencyRelations.addRelation(markedupString, slotShort,
                numJuliaSlots.value);
    }

    private void checkBracketChar(String markedupString, ShortWrapper index) {
        if (index.value < markedupString.length()) {
            char currentChar = markedupString.charAt(index.value);

            if (currentChar == '<' || currentChar == '(' || currentChar == '!') {
                throw new Error(
                        "unexpected nesting or dependency parsing basic category");
            }
        }
    }

    /*
     * there are two fields to be read on each line: the markedup category and a
     * variable. The variable is there because for some cases (AP, VP_TO) there
     * is already a dependency in the category being type-raised; varString
     * indicates what the variable is associated with the dependency, which
     * needs carrying over
     */
    private void readTRcategories(String trFile,
                                  ArrayList<TypeRaisedCategory> trList) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(trFile));

            String markedupCatString = "";
            String varString = "";
            byte[] seenVariables = new byte[VarID.NUM_VARS];

            Preface.readPreface(in);

            String line = "";
            while ((line = in.readLine()) != null) {
                String[] tokens = line.split("\\s");
                if (tokens.length != 2) {
                    throw new Error(
                            "expecting 2 items per line in the TR file!");
                }

                markedupCatString = tokens[0];
                varString = tokens[1];
                Category cat = parse(markedupCatString);
                /*
                 * typically the varString is the sentinel "+" - this gets
				 * converted to NONE by VarID.convert
				 */
                byte depVar = VarID.convert(varString);

                Arrays.fill(seenVariables, VarID.NONE);
                cat.reorderVariables(seenVariables, new ByteWrapper((byte) (0)));

				/*
                 * seenVariables[VarID.X] is the new variable corresponding to
				 * the lexical item - this is needed by the SuperCategory
				 * constructor when called later to copy over the corresponding
				 * Variable; seenVariables[depVar] is the new variable for the
				 * dependency variable (see TypeRaisedCategory.java)
				 */
                TypeRaisedCategory trCat = new TypeRaisedCategory(cat,
                        seenVariables[VarID.X], seenVariables[depVar]);
                trList.add(trCat);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public Category canonize(String catString) {
        Category cat = seenCategories.get(catString);
        if (cat == null) {
            cat = parse(catString);
            if (cat == null) {
                throw new Error("failed to parse catString!" + catString);
            }
            canonicalCats.put(cat, cat);
        }
        return cat;
    }

    /**
     * Returns the input if it's not found in the canonical map; otherwise
     * returns the canonical version
     */
    public Category canonize(Category checkCat) {
        Category cat = canonicalCats.get(checkCat);
        if (cat == null) {
            canonicalCats.put(checkCat, checkCat);
            return checkCat;
        } else {
            return cat;
        }
    }

}
