package services.command;

import constants.CommandType;
import constants.Constants;
import services.parkingService.IParkingLotService;
import services.parkingService.ParkingLotService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static constants.CommandType.*;

public class CommandParser {
    private static Map<CommandType, Command> commandMap;
    public static IParkingLotService parkingService;
    public static final Pattern REGISTRATION_ID = Pattern.compile(Constants.REGISTRATION_ID_REGEX);
    public static final String MISSING_ARGS = "missing arguments!";
    public static final String TOO_MANY_ARGS = "too many arguments!";
    public static final String WRONG_FORMAT = "wrong format!";

    private static void init() {
        commandMap.put(Create_parking_lot, ((parkingLotService, args) -> {
            if (null == args || args.length < 2) {
                throw new IllegalArgumentException(CommandParser.MISSING_ARGS);
            }
            else if (args.length > 2) {
                throw new IllegalArgumentException(CommandParser.TOO_MANY_ARGS);
            }
            CommandParser.parkingService = ParkingLotService.getInstance(Integer.parseInt(args[1]));
        }));
        commandMap.put(Park, ((parkingLotService, args) -> {
            if (null == args || args.length < 4) {
                throw new IllegalArgumentException(CommandParser.MISSING_ARGS);
            }
            else if (args.length > 4) {
                throw new IllegalArgumentException(CommandParser.TOO_MANY_ARGS);
            }
            if (!CommandParser.REGISTRATION_ID.matcher(args[1]).matches() ||
                !Constants.DRIVER_AGE_FIELD.equals(args[2])) {
                throw new IllegalArgumentException(CommandParser.WRONG_FORMAT +
                        " Correct Format is \"Park KA-01-HH-1234 driver_age 21\"");
            }
            parkingLotService.park(args[1], Integer.parseInt(args[3]));
        }));
        commandMap.put(Slot_numbers_for_driver_of_age, ((parkingLotService, args) -> {
            if (null == args || args.length < 2) {
                throw new IllegalArgumentException(CommandParser.MISSING_ARGS);
            }
            else if (args.length > 2) {
                throw new IllegalArgumentException(CommandParser.TOO_MANY_ARGS);
            }
            parkingLotService.getSlotsForDriverWithAge(Integer.parseInt(args[1]));
        }));
        commandMap.put(Slot_number_for_car_with_number, ((parkingLotService, args) -> {
            if (null == args || args.length < 2) {
                throw new IllegalArgumentException(CommandParser.MISSING_ARGS);
            }
            else if (args.length > 2) {
                throw new IllegalArgumentException(CommandParser.TOO_MANY_ARGS);
            }
            if (!CommandParser.REGISTRATION_ID.matcher(args[1]).matches()) {
                throw new IllegalArgumentException(CommandParser.WRONG_FORMAT +
                        " Correct Format is \"Slot_number_for_car_with_number PB-01-HH-1234\"");
            }
            parkingLotService.getSlotForVehicleWithRegID(args[1]);
        }));
        commandMap.put(Leave, ((parkingLotService, args) -> {
            if (null == args || args.length < 2) {
                throw new IllegalArgumentException(CommandParser.MISSING_ARGS);
            }
            else if (args.length > 2) {
                throw new IllegalArgumentException(CommandParser.TOO_MANY_ARGS);
            }
            parkingLotService.unpark(Integer.parseInt(args[1]));
        }));
        commandMap.put(Vehicle_registration_number_for_driver_of_age, ((parkingLotService, args) -> {
            if (null == args || args.length < 2) {
                throw new IllegalArgumentException(CommandParser.MISSING_ARGS);
            }
            else if (args.length > 2) {
                throw new IllegalArgumentException(CommandParser.TOO_MANY_ARGS);
            }
            parkingLotService.getVehicleRegIDsForDriverWithAge(Integer.parseInt(args[1]));
        }));
    }

    public CommandParser() {
        commandMap = new HashMap<>();
        init();
    }

    public Command getCommandForCommandID(String id) {
        CommandType commandType;
        if (null != (commandType = CommandType.commandTypeForCommandID(id))) {
            return commandMap.get(commandType);
        }
        return null;
    }
}
