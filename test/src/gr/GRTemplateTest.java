package gr;

import lexicon.Categories;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.*;

public class GRTemplateTest {

    static Categories categories;

    @BeforeClass
    public static void setUp() throws Exception {
        categories = new Categories("./test/resources", false);
    }


    @Test
    public void testConstructor() throws Exception {
        GRTemplate t;

        // Rule with no constraints
        t = new GRTemplate(categories, "", (short) 0,
                "  2 xcomp _ %l %f");

        assertEquals("xcomp _ %l %f", t.fmt);
        assertFalse(t.ignore);
        assertFalse(t.constrained);


        // Rule with Cat constraint
        t = new GRTemplate(categories, "", (short) 0,
                "  xcomp _ %l %f =PP/(S[adj]\\NP)");

        assertEquals("xcomp _ %l %f", t.fmt);
        assertFalse(t.ignore);
        assertTrue(t.constrained);
        assertEquals("PP/(S[adj]\\NP)", t.tmpCat);


        // Rule with Lex and Cat constraints, and %c
        t = new GRTemplate(categories, "", (short) 0,
                "  2 xcomp %f %l %c =be =PP/NP");

        assertEquals("xcomp %f %l %c", t.fmt);
        assertFalse(t.ignore);
        assertTrue(t.constrained);
        assertEquals("=be", t.conLex);
        assertEquals("PP/NP", t.tmpCat);
        assertEquals(1, t.conRel);


        // Rule with ignore
        t = new GRTemplate(categories, "", (short) 0,
                "  2 ignore");

        assertEquals("ignore", t.fmt);
        assertTrue(t.ignore);
        assertFalse(t.constrained);


        // Rule with numbered argument
        t = new GRTemplate(categories, "", (short) 0,
                "  2 ncmod _ %f %3");

        assertEquals("Should have %o", "ncmod _ %f %o", t.fmt);
        assertFalse(t.ignore);
        assertFalse(t.constrained);

    }


    @Ignore
    @Test
    public void testGet() throws Exception {
    }

    @Ignore
    @Test
    public void testSet_cat() throws Exception {

    }

    @Ignore
    @Test
    public void testSatisfy() throws Exception {

    }

    @Ignore
    @Test
    public void testGet1() throws Exception {

    }
}