package printer;

public class PrinterFactory {

    public static Printer getPrinter(String printer) {
        if ("deps".equals(printer))
            return new DepsPrinter();

        else if ("grs".equals(printer))
            return new GRsPrinter();

        throw new Error("Invalid printer name");
    }
}
