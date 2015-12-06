package _sandbox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sandbox {
    private static final Pattern grPattern = Pattern.compile("(?<fmt>[a-z]+(\\s(%[\\dflck]|_))*)(?<cons>(\\s=\\S+)*)");
    private static final Pattern consPatter = Pattern.compile("=(\\S+)");
    private static final Pattern fmtPattern = Pattern.compile("%([\\dflck])");


    public static void main(String[] args) {
        boolean constrained;

        String s = "  2 xcomp %f %2 %k =be =(S[to]\\NP)/(S[b]\\NP)";
//        String s = "  2 ignore =be =(S[to]\\NP)/(S[b]\\NP)";
        Matcher m = grPattern.matcher(s);

        if (!m.find())
            throw new Error("WOAH");

        System.out.println("found:\n" + m.group("fmt"));

        Matcher fmtMatcher = fmtPattern.matcher(m.group("fmt"));
        short oslot;
        short slot = 0;
        while (fmtMatcher.find()) {
            char c = fmtMatcher.group().charAt(1);
            System.out.println("c = " + c);
            switch (c) {
                case '1':
                case '2':
                case '3':
                    System.out.println("number");
                    break;
                case 'k':
                case 'c':
                    System.out.println("C/K");
                    break;
                default:
                    System.out.println("letter");
            }
        }

        if (! m.group("cons").isEmpty()) {
            constrained = true;
            Matcher cm = consPatter.matcher(m.group("cons"));
            while (cm.find()) {
                String cons = cm.group();
                System.out.println("Cons: " + cons);
                if (Character.isLowerCase(cons.charAt(1))) {
                    System.out.println("Lex");
                } else
                    System.out.println("Cat");
            }



        }


        if ("ignore".equals(m.group("fmt"))) {
            System.out.println("IGNORE");
        }

        System.out.println(m.group("fmt").replaceAll("[123]", "o"));

    }

}
