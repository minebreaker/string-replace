package rip.deadcode.stringreplace.sample;

import rip.deadcode.stringreplace.Replace;


public final class Main {

    @Replace("FOO")
    private static final String VERSION = "";

    public static void main( String[] args ) {
        System.out.println("Version: " + VERSION);
    }
}
