package services.command;

import constants.CommandType;
import org.junit.Assert;
import org.junit.Test;

public class CommandParserTest {

    @Test
    public void validCommands() {
        CommandParser commandParser = new CommandParser();
        for (CommandType commandType: CommandType.values()) {
            Command commandForCommandID = commandParser.getCommandForCommandID(commandType.getID());
            Assert.assertNotEquals(commandForCommandID, null);
        }

        Command invalid_command = commandParser.getCommandForCommandID("INVALID_COMMAND");
        Assert.assertEquals(invalid_command, null);
    }
}
