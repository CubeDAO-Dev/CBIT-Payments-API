package com.cubedao.wallet.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.web3j.abi.datatypes.generated.Uint256;

/**
 * Holds information for player transaction events
 */
public class PlayerTransactEvent<T> extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private double amount;
    private String reason;
    private Uint256 refID;
    private T payload;

    public PlayerTransactEvent(@NotNull final Player player, final double amount, @NotNull final String reason, @NotNull final Uint256 refID, @NotNull final T payload) {
        super(player);
        this.amount = amount;
        this.reason = reason;
        this.refID = refID;
        this.payload = payload;
    }

    /**
     * Gets the amount of $CBIT from the transaction
     *
     * @return Amount of $CBIT
     */
    @NotNull
    public double getAmount() {
        return amount;
    }

    /**
     * Gets the amount of $CBIT from the transaction
     *
     * @return Amount of $CBIT
     */
    @NotNull
    public double getCBIT() {
        return amount;
    }

    /**
     * Gets the reason for the transaction
     *
     * @return Transaction Reason
     */
    @NotNull
    public String getReason() {
        return reason;
    }

    /**
     * Gets the refid for the transaction
     *
     * @return Payment reference ID
     */
    @NotNull
    public Uint256 getRefID() {
        return refID;
    }

    @NotNull
    public T getPayload() {
        return payload;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }


    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
