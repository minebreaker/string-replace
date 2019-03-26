package rip.deadcode.javac.stringreplace.sample;

import rip.deadcode.javac.stringreplace.Replace;


public final class Main {

    @Replace( "VERSION" )
    private static final String VERSION = "";

    public static void main( String[] args ) {
        System.out.println( "Version: " + VERSION );
    }
}
