package gr;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores lexical constraints from markedup as "label word" strings in a Set
 */
public class GRConstraints {
    //C++ based on  pImpl trick

    private Set<String> set;

    public GRConstraints() {
        this.set = new HashSet<String>();
    }

    public int size() {
        return set.size();
    }

    public boolean get(String label, String word) {
        return set.contains(label + " " + word);
    }

    public void add(String label, String word) {
        set.add(label + " " + word);
    }

    @Override
    public String toString() {
        return "GRConstraints{" +
                "set=" + set +
                '}';
    }
}
