package gr;

import java.util.LinkedList;
import java.util.List;

public class GR {
    public String label;
    public List<Argument> arguments = new LinkedList<>();


    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();

        out.append("( ").append(label);
        for (Argument arg: arguments) {
            out.append(' ').append(arg.raw);
            if (arg.pos >= 0)
                out.append("_").append(arg.pos);
        }
        out.append(")");

        return out.toString();
    }
}


class Argument {
    public String raw;
    public int pos;
}
