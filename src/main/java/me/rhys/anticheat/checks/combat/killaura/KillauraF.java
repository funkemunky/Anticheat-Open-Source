package me.rhys.anticheat.checks.combat.killaura;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInformation;
import me.rhys.anticheat.base.event.PacketEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.tinyprotocol.api.Packet;
import me.rhys.anticheat.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.util.Verbose;
import org.bukkit.Bukkit;

@CheckInformation(checkName = "Killaura", checkType = "F", lagBack = false, description = "Check if player attacks while dead", punishmentVL = 5)
public class KillauraF extends Check {

    private double swings, attacks;
    private Verbose threshold = new Verbose();

    /**
     * Hit miss ratio detection, currently trying to find a better way of detecting this.
     */

    @Override
    public void onPacket(PacketEvent event) {
        User user = event.getUser();

        switch (event.getType()) {
            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket attack = new WrappedInUseEntityPacket(event.getPacket(), user.getPlayer());

                if (attack.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {

                    if ((user.getCurrentLocation().getYaw() - user.getLastLocation().getYaw()) > 2.5F) {
                        ++attacks;
                    }

                }
                break;
            }

            case Packet.Client.ARM_ANIMATION: {
                if (user.shouldCancel()
                        || user.getTick() < 60) {
                    threshold.setVerbose(0);
                    return;
                }

                ++swings;

                double ratio = (attacks / swings) * 100;

                if (ratio > 90 && attacks > 10 && swings > 10) {
                    if (threshold.flag(20, 1000L)) {
                  //      flag(user, "Aim is to accurate [H:M]");
                    }
                }
            }
        }
    }
}