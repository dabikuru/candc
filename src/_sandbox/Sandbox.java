package _sandbox;

public class Sandbox {

    public static void main(String[] args) {
        String depTypeFlag = "<DEPENDENCY_TYPE=grs>";

        depTypeFlag = depTypeFlag.replaceFirst("<DEPENDENCY_TYPE=(\\w+)>", "$1");
        System.out.println("depTypeFlag = " + depTypeFlag);
    }
}
