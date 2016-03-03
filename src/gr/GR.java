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

    @Override
    public int compareTo(GR o) {
        if (!this.arguments.isEmpty() && !o.arguments.isEmpty()) {
            return this.arguments.get(0).pos - o.arguments.get(0).pos;
        }
        return 0;
    }
}


class Argument {
    public String raw;
    public int pos;
}
