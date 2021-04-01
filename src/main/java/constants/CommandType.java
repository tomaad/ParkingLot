package constants;

import java.util.HashMap;
import java.util.Map;

public enum CommandType {
    Create_parking_lot("Create_parking_lot"),
    Park("Park"),
    Slot_numbers_for_driver_of_age("Slot_numbers_for_driver_of_age"),
    Slot_number_for_car_with_number("Slot_number_for_car_with_number"),
    Leave("Leave"),
    Vehicle_registration_number_for_driver_of_age("Vehicle_registration_number_for_driver_of_age");

    private String ID;
    private static Map<String, CommandType> commandTypeMap;

    private static void put(String ID, CommandType commandType) {
        if (null == commandTypeMap) {
            commandTypeMap = new HashMap<>();
        }
        commandTypeMap.put(ID, commandType);
    }

    CommandType(String ID) {
        this.ID = ID;
        put(ID, this);
    }

    public String getID() {
        return ID;
    }

    public static CommandType commandTypeForCommandID(String id) {
        return commandTypeMap.get(id);
    }
}
