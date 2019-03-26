package rip.deadcode.javac.stringreplace.sample;

import rip.deadcode.javac.stringreplace.Replace;


public final class Main {

    @Replace( "VERSION" )
    private static final String VERSION = "";

    @Replace( "COMMIT_HASH" )
    private static final String COMMIT_HASH = "";

    public static void main( String[] args ) {
        System.out.println( "Version: " + VERSION );
        System.out.println( "Commit:  " + COMMIT_HASH );
    }
}
