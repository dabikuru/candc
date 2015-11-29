package printer;

public class PrinterFactory {

    public static Printer getPrinter(String printer) {
        switch (printer) {
            case "deps":
                return new DepsPrinter();
            case "grs":
                return new GRsPrinter();
            default:
                throw new Error("Invalid printer name");
        }
    }
}
