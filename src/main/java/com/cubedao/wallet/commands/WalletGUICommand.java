package com.cubedao.wallet.commands;

import com.cubedao.wallet.menus.WalletGUI;
import com.cubedao.wallet.objects.payments.PaymentRequest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WalletGUICommand implements CommandExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        WalletGUI.menu.open((Player) sender);
        return true;
    }

}
