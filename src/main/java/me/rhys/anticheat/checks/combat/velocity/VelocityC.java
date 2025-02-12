package me.rhys.anticheat.checks.combat.velocity;

import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInformation;
import me.rhys.anticheat.base.event.PacketEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.tinyprotocol.api.Packet;

@CheckInformation(checkName = "Velocity",  checkType = "C", lagBack = false, description = "99% Vertical Velocity [3 Tick]")
public class VelocityC extends Check {

    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {
            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {
                User user = event.getUser();

                if (user.getLastFallDamageTimer().hasNotPassed(20)
                        || user.getVehicleTicks() > 0
                        || user.shouldCancel()) {
                    threshold = 0;
                    return;
                }

                double deltaY = user.getMovementProcessor().getDeltaY();

                double velocity = user.getCombatProcessor().getVelocity().getY();

                if (user.getCombatProcessor().getVelocityTicks() == 3) {

                    double prediction = velocity;

                    prediction -= 0.08D;

                    prediction *= 0.98D;

                    double ratio = deltaY / prediction;

                    if (ratio <= 0.99 && ratio > 0.0) {
                        if (threshold++ > 2) {
                            flag(user, "Vertical Knockback: "+ratio);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.0626f);
                    }
                }

                break;
            }
        }
    }
}
