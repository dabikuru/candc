package lexicon;

import org.junit.Test;

public class CategoriesTest {

    Categories categories;

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @Test
    public void testReadMarkedupFile() throws Exception {
        categories = new Categories("./test/resources", false);
    }

}