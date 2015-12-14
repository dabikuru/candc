package lexicon;

/*
 * a triple consisting of: markedup category string, relation slot number,
 * and relation slot number according to Julia's ordering in CCGbank, which
 * is different and required for evaluating against CCGbank
 */

import gr.GRTemplate;

import java.util.LinkedList;
import java.util.List;

public class Relation {
    public final String category;
    public final short slot;
    public final short jslot;
    public List<GRTemplate> grs;

    public Relation(String category, short slot, short jslot) {
        this.category = category;
        this.slot = slot;
        this.jslot = jslot;
        this.grs = new LinkedList<>();
    }

    public Relation(Relation other) {
        this(other.category, other.slot, other.jslot);
    }

    public void printSlot(boolean juliaSlots) {
        if (juliaSlots) {
            System.out.print(category + " " + Short.toString(jslot));
        } else {
            System.out.print(category + " " + Short.toString(slot));
        }
    }

    public void setConstraints(Categories categories) {
        //TODO: set all (not just first); check difference
        grs.forEach(gr -> gr.setCat(categories));
    }
}
