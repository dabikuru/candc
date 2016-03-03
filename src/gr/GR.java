package gr;

import java.util.LinkedList;
import java.util.List;

public class GR implements Comparable<GR> {
    public String label;
    public List<Argument> arguments = new LinkedList<>();


    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("(").append(label);
        for (Argument arg: arguments) {
            out.append(' ').append(arg.raw);
            if (arg.pos >= 0)
                out.append("_").append(arg.pos);
        }
        out.append(")");

        return out.toString();
    }

    /**
     * @param o GR to compare against
     * @return 1 or -1 if this comes before or after; 0 if they are equal
     */
    @Override
    public int compareTo(GR o) {
        // Comparison for 2-argument dependencies
        if (this.arguments.size() == 2 && this.arguments.size() == 2) {
            // First compare the governing words
            int d1 = this.arguments.get(0).pos - o.arguments.get(0).pos;
            // If the words are equal (same position), compare the dependent words.
            // If they are also equal, the whole GR is equal (TreeSet will exclude the second)
            return (d1 != 0) ? d1 : this.arguments.get(1).pos - o.arguments.get(1).pos;
        }

        // For deps with more or less than 2 arguments, return default value
        return 1;
    }
}


class Argument {
    public String raw;
    public int pos;
}
