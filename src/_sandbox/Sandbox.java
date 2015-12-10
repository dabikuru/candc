package _sandbox;

import java.util.Arrays;

public class Sandbox {

    public static void main(String[] args) {
        String s = "  2 ((S[to]{_}\\NP{Z}<1>){_}/(S[b]{Y}<2>\\NP{Z*}){Y}){_}";
        String[] tokens = (s.trim()).split("\\s+");

        System.out.println("tokens = " + Arrays.toString(tokens));
    }
}
