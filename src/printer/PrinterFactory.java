package printer;

import lexicon.Categories;

public class PrinterFactory {

    public static Printer getPrinter(String printer, Categories categories) {
        switch (printer) {
            case "deps":
                return new DepsPrinter(categories);
            case "grs":
                return new GRsPrinter(categories);
            case "usd":
                //TODO: add sanity checks
                return new GRsPrinter(categories);

        }
        throw new Error("Invalid printer name");
    }
}
