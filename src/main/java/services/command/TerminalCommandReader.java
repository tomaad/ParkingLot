package services.command;

import java.io.Closeable;
import java.io.IOException;
import java.util.Scanner;

public class TerminalCommandReader implements Closeable {
    private Scanner scanner;

    public TerminalCommandReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String read() {
        return scanner.nextLine();
    }

    @Override
    public void close() throws IOException {
        scanner.close();
    }
}
