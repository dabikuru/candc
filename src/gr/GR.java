package gr;

import java.util.List;

public class GR {
    public String label;
    public List<Argument> arguments; //in place of C++ Arguments typedef

}
//C++ GRs will become List<GR>


class Argument {
    public String raw;
    public int pos;
}
//C++ Aguments will become List<Agument>

