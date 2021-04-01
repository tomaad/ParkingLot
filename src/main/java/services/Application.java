package services;

import services.command.CommandProcessor;

import java.io.IOException;

public class Application {

    public static void main(String[] args) throws IOException {
        if (null == args || args.length < 1)
            new CommandProcessor().run();
        else
            new CommandProcessor(args[0]).run();
    }
}
