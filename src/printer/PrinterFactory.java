package printer;

import lexicon.Categories;
import lexicon.DependencyType;

public class PrinterFactory {

    public static Printer getPrinter(String printer, Categories categories) {
        switch (printer) {
            case "deps":
                return new DepsPrinter(categories);
            case "grs":
                return new GRsPrinter(categories, DependencyType.GR);
            case "usd":
                return new GRsPrinter(categories, DependencyType.USD);

        }
        throw new Error("Invalid printer name");
    }
}
