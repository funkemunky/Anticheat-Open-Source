package me.rhys.anticheat.base.command;


import lombok.Getter;
import me.rhys.anticheat.base.command.commands.MainCommand;
import me.rhys.anticheat.util.command.CommandUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CommandManager {
    private final List<Command> commandList = new ArrayList<>();

    public CommandManager() {
        addCommand(new Command(new MainCommand("ac"), "ac", null, "Main command.",
                true));

        addCommand(new Command(new MainCommand("ac"), "ac alerts", "/ac alerts",
                "Toggle on, and off alerts.", true));

        addCommand(new Command(new MainCommand("ac"), "ac check", "/ac check [check&type]",
                "Toggle on, and off detections.", true));
    }

    private void addCommand(Command... commands) {
        for (Command command : commands) {
            commandList.add(command);
            if (command.isEnabled()) CommandUtils.registerCommand(command);
        }
    }

    public void removeCommand() {
        commandList.forEach(CommandUtils::unRegisterBukkitCommand);
    }
}

