package com.example.gomokuexample;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("console")) {
            GomokuGameConsole.main(args);
        } else {
            GomokuGameFX.launch(GomokuGameFX.class, args);
        }
    }
}
