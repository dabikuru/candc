package gr;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores lexical constraints from markedup as "label word" strings in a Set
 */
public class GRConstraints {
    //C++ based on  pImpl trick

    //FIXME: 'name' is probably useless?
    private String name;
    private Set<String> set;

    public GRConstraints() {
        this.set = new HashSet<String>();
    }

    public GRConstraints(String name) {
        this.name = name;
        this.set = new HashSet<String>();
    }

    public String getName() { return name; }

    public int size() { return set.size(); }

    public boolean get(String label, String word) {
        return set.contains(label + " " + word);
    }

    public void add(String label, String word) {
        set.add(label + " " + word);
    }
}
