package services.command;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CommandProcessorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void commandProcessorReadsFromFileCommandReader() throws IOException {
        FileCommandReader fileCommandReader =
                new FileCommandReader(ClassLoader.getSystemResourceAsStream("sampleCommand.txt"));

        TerminalCommandReader mockTerminalCommandReader = Mockito.mock(TerminalCommandReader.class);

        CommandProcessor commandProcessor =
                new CommandProcessor(new CommandParser(), fileCommandReader, mockTerminalCommandReader);
        commandProcessor.run();
    }

    @Test
    public void commandProcessorReadsFromTerminalCommandReader() throws IOException {
        FileCommandReader fileCommandReader = Mockito.mock(FileCommandReader.class);
        Scanner scanner = new Scanner(new BufferedInputStream(ClassLoader.getSystemResourceAsStream("sampleCommand.txt")));
        TerminalCommandReader terminalCommandReader = new TerminalCommandReader(scanner);

        new CommandProcessor(new CommandParser(), fileCommandReader, terminalCommandReader).run();
    }

    @Test
    public void executeThrowsExceptionForUninitializedParking() throws IOException {
        final String command = "Park KA-01-HH-1234 driver_age 21";

        FileCommandReader fileCommandReader =
                new FileCommandReader(new ByteArrayInputStream(command.getBytes(StandardCharsets.UTF_8)));
        TerminalCommandReader mockTerminalCommandReader = Mockito.mock(TerminalCommandReader.class);

        CommandProcessor commandProcessor =
                new CommandProcessor(new CommandParser(), fileCommandReader, mockTerminalCommandReader);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Parking Lot service is not initialized yet!");
        commandProcessor.run();
    }

    @Test
    public void executeThrowsExceptionForAlreadyInitializedParkingLot() throws IOException {
        final String command = "Create_parking_lot 6\n" +
                "Park KA-01-HH-1234 driver_age 21\n" +
                "Create_parking_lot 6";

        FileCommandReader fileCommandReader =
                new FileCommandReader(new ByteArrayInputStream(command.getBytes(StandardCharsets.UTF_8)));
        TerminalCommandReader mockTerminalCommandReader = Mockito.mock(TerminalCommandReader.class);

        CommandProcessor commandProcessor =
                new CommandProcessor(new CommandParser(), fileCommandReader, mockTerminalCommandReader);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Parking Lot has been created already!");
        commandProcessor.run();
    }
}
