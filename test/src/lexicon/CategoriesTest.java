package lexicon;

import org.junit.Test;

import java.io.File;
import java.util.Scanner;
import java.util.regex.Pattern;

public class CategoriesTest {

    @org.junit.Before
    public void setUp() throws Exception {
    }

    @Test
    public void testReadMarkedupFile() throws Exception {
    }

    @Test
    public void testRegex() throws Exception {
        Scanner sc = new Scanner(new File("./test/resources/markedup.small"));

        // "\S+ ?\n(  \d \S+\n)(  ! \S+\n)*(  \d .+\n)*"
        Pattern p1 = Pattern.compile("\\S+ ?\\n(  \\d \\S+\\n)(  ! \\S+\\n)*(  \\d .+\\n)*", Pattern.MULTILINE);

        // "=\w+ (\S+ ?)+\n"
        Pattern p2 = Pattern.compile("=\\w+ (\\S+ ?)+\\n", Pattern.MULTILINE);

        String next;
        while ((next = sc.findWithinHorizon(p2, 0)) != null) {
            String[] lines = next.split(" ");



            System.out.println(next);
        }



        while ((next = sc.findWithinHorizon(p1, 0)) != null) {
            String[] lines = next.split("\n");
            System.out.println(next);
        }
    }
}