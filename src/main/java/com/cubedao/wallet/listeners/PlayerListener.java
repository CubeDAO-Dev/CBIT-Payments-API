package com.cubedao.wallet.listeners;

import com.cubedao.wallet.CubeDAO;
import com.cubedao.wallet.event.PlayerWalletReadyEvent;
import com.cubedao.wallet.objects.NFTPlayer;
import com.cubedao.wallet.qrmaps.QRMapManager;
import com.cubedao.wallet.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private CubeDAO plugin;

    public PlayerListener() {
        this.plugin = CubeDAO.getInstance();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        new NFTPlayer(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        new PlayerWalletReadyEvent(event.getPlayer())
                .callEvent();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!NFTPlayer.getByUUID(p.getUniqueId()).isLinked()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!p.isOnline()) return;
                p.sendMessage(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getNoLinkedWallet()));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            }, 20L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        NFTPlayer.remove(event.getPlayer().getUniqueId());
        restoreItemReplacedWithMap(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            restoreItemReplacedWithMap(event.getPlayer());
        }
    }

    private void restoreItemReplacedWithMap(Player player) {
        ItemStack previousItem = QRMapManager.playerPreviousItem.get(player.getUniqueId());
        if (previousItem != null) {
            player.getInventory().setItem(0, previousItem);
            QRMapManager.playerPreviousItem.remove(player.getUniqueId());
        }
    }

}
