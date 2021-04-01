package services.command;

import constants.CommandType;
import constants.Constants;
import factory.AppLogger;
import factory.Factory;
import services.parkingService.exceptions.DuplicateRegistrationIDException;
import services.parkingService.exceptions.ParkingLotFullException;
import services.parkingService.exceptions.SpotAlreadyVacantException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class CommandProcessor {
    private static final AppLogger log = Factory.getAppLogger(Factory.getSlf4jLogger(CommandProcessor.class));
    private CommandParser commandParser;
    private FileCommandReader fileCommandReader;
    private TerminalCommandReader commandReader;
    boolean isParkingLotInitialized = false;

    public CommandProcessor() {
        this.commandParser = new CommandParser();
        this.commandReader = new TerminalCommandReader(new Scanner(System.in));
    }

    public CommandProcessor(String fileLocation) throws FileNotFoundException {
        this.commandParser = new CommandParser();
        this.commandReader = new TerminalCommandReader(new Scanner(System.in));
        this.fileCommandReader = new FileCommandReader(fileLocation);
    }

    public CommandProcessor(CommandParser commandParser,
                            FileCommandReader fileCommandReader,
                            TerminalCommandReader commandReader) {
        this.commandParser = commandParser;
        this.fileCommandReader = fileCommandReader;
        this.commandReader = commandReader;
    }

    public void run() throws IOException {
        String cmd;
        if (null != fileCommandReader) {
            try {
                while (null != (cmd = fileCommandReader.read()) && !Constants.QUIT.equals(cmd)) {
                    execute(cmd);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            fileCommandReader.close();
        }
        while(!Constants.QUIT.equals(cmd = commandReader.read())) {
            execute(cmd);
        }
        commandReader.close();
    }

    private void execute(String cmd) {
        String[] args = cmd.split(Constants.SPACE);
        String id = args[0];
        Command command = commandParser.getCommandForCommandID(id);
        if (null == command) {
            return;
        }
        if (isParkingLotInitialized && CommandType.Create_parking_lot.getID().equals(id)) {
            throw new IllegalArgumentException("Parking Lot has been created already!");
        }
        else if (!isParkingLotInitialized && !CommandType.Create_parking_lot.getID().equals(id)) {
            throw new IllegalArgumentException("Parking Lot service is not initialized yet!");
        }
        else if (!isParkingLotInitialized && CommandType.Create_parking_lot.getID().equals(id)) {
            isParkingLotInitialized = true;
        }

        try {
            command.execute(CommandParser.parkingService, args);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Command \"%s\" is invalid! " +
                    "Exception raised during execution: %s", cmd, e.getMessage()));
        }
        catch (DuplicateRegistrationIDException e) {
            log.error(e.getMessage());
        }
        catch (SpotAlreadyVacantException e) {
            log.debug(e.getMessage());
        }
        catch (ParkingLotFullException e) {
            log.debug(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            log.error(String.format("Command \"%s\" has %s", cmd, e.getMessage()));
        }
    }
}
