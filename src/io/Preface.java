package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class Preface {
    public static void readPreface(BufferedReader in) throws IOException {
        String line = in.readLine();

        if (line.charAt(0) != '#') {
            throw new IllegalArgumentException("File does not start with the mandatory preface.");
        }

        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                break;
            }
            if (line.charAt(0) != '#') {
                throw new IllegalArgumentException("Uncommented line within preface.");
            }
        }
    }

    public static void readPreface(Scanner in) throws IOException {
        if (in.nextLine().charAt(0) != '#')
            throw new IllegalArgumentException("File does not start with the mandatory preface.");

        while (in.hasNextLine()) {
            if (in.nextLine().isEmpty())
                break;
            if (in.nextLine().charAt(0) != '#')
                throw new IllegalArgumentException("Uncommented line within preface.");
        }
    }
}
