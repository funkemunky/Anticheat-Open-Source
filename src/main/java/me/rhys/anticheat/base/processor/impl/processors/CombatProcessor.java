package me.rhys.anticheat.base.processor.impl.processors;

import lombok.Getter;
import me.rhys.anticheat.Anticheat;
import me.rhys.anticheat.base.event.PacketEvent;
import me.rhys.anticheat.base.processor.api.Processor;
import me.rhys.anticheat.base.processor.api.ProcessorInformation;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.tinyprotocol.api.Packet;
import me.rhys.anticheat.tinyprotocol.api.TinyProtocolHandler;
import me.rhys.anticheat.tinyprotocol.packet.out.WrappedOutTransaction;
import me.rhys.anticheat.tinyprotocol.packet.out.WrappedOutVelocityPacket;
import me.rhys.anticheat.util.EventTimer;
import org.bukkit.util.Vector;

@ProcessorInformation(name = "Combat")
@Getter
public class CombatProcessor extends Processor {

    private EventTimer preVelocityTimer;

    private double velocityH, velocityV;

    private Vector velocity = new Vector();

    private int velocityTicks, velocityNoTransTicks;

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {

            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {
                velocityTicks++;
                velocityNoTransTicks++;
                break;
            }

            case Packet.Server.ENTITY_VELOCITY: {
                WrappedOutVelocityPacket wrappedOutVelocityPacket = new WrappedOutVelocityPacket(event.getPacket(),
                        event.getUser().getPlayer());

                if (wrappedOutVelocityPacket.getId() == event.getUser().getPlayer().getEntityId()) {

                    this.preVelocityTimer.reset();

                    user.getActionProcessor().add(ActionProcessor.Actions.VELOCITY);

                    velocity = new Vector(wrappedOutVelocityPacket.getX(), wrappedOutVelocityPacket.getY(),
                            wrappedOutVelocityPacket.getZ());

                    velocityNoTransTicks = 0;

                    WrappedOutTransaction transaction = new WrappedOutTransaction(0, (short) (Anticheat.getInstance().getKeepaliveHandler().getTime() - 1), false);

                    TinyProtocolHandler.sendPacket(user.getPlayer(), transaction.getObject());

                }
                break;
            }

            case Packet.Server.TRANSACTION: {
                WrappedOutTransaction transaction = new WrappedOutTransaction(event.getPacket(),
                        event.getUser().getPlayer());

                if (transaction.getAction() == ((short) Anticheat.getInstance().getKeepaliveHandler().getTime() - 1)) {
                    velocityH = (int) Math.hypot(velocity.getX(), velocity.getZ());
                    velocityV = Math.pow(velocity.getY() + 2.0, 2.0) * 5.0;
                    velocityTicks = 0;
                }
                break;
            }
        }
    }

    @Override
    public void setupTimers(User user) {
        this.preVelocityTimer = new EventTimer(20, user);
    }
}
