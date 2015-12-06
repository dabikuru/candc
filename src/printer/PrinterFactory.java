package printer;

import lexicon.Categories;

public class PrinterFactory {

    public static Printer getPrinter(String printer, Categories categories) {
        if ("deps".equals(printer))
            return new DepsPrinter(categories);

        else if ("grs".equals(printer))
            return new GRsPrinter(categories);

        throw new Error("Invalid printer name");
    }
}
