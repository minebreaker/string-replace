package rip.deadcode.javac.stringreplace;


public final class ReplacerException extends RuntimeException {

    private final String messageKey;
    private final String[] args;

    public ReplacerException( String messageKey, String... args ) {
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String[] getArgs() {
        return args;
    }

    public static void check( boolean state, String messageKey, String... args ) {
        if ( !state ) {
            throw new ReplacerException( messageKey, args );
        }
    }
}
