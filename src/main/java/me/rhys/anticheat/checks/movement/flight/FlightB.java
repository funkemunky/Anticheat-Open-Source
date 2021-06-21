package me.rhys.anticheat.checks.movement.flight;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInformation;
import me.rhys.anticheat.base.event.PacketEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.tinyprotocol.api.Packet;
import me.rhys.anticheat.util.MathUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent;

@CheckInformation(checkName = "Flight", checkType = "B", description = "Spoof Ground Check (ghost block fly)", canPunish = false)
public class FlightB extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {
            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {
                User user = event.getUser();

                if (this.checkConditions(user)
                        || user.getActionProcessor().getServerPositionTimer().hasNotPassed(3)
                        || user.getLastTeleportTimer().hasNotPassed(20)) {
                    this.threshold = 0;
                    return;
                }

                Location groundLocation = MathUtil.getGroundLocation(user);

                if (!user.getBlockData().onGround && !user.getBlockData().lastOnGround) {
                    if (user.getCurrentLocation().isClientGround() && !user.getMovementProcessor().isServerYGround()) {
                        user.getPlayer().teleport(groundLocation,
                                PlayerTeleportEvent.TeleportCause.PLUGIN);

                        threshold++;

                        if (threshold > 7
                                && !user.getMovementProcessor().getLastBlockPlacePacketTimer().hasNotPassed(20)) {
                            flag(user, "Ghost Block / Ghost Block Fly");
                        }
                    }
                }
            }
        }
    }

    boolean checkConditions(User user) {
        return user.getBlockData().liquidTicks > 0
                || user.getTick() < 60
                || user.shouldCancel()
                || user.getBlockData().climbableTicks > 0;
    }
}
