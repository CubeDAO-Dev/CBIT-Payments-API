package com.cubedao.wallet.menus;

import com.cubedao.wallet.objects.NFTPlayer;
import com.cubedao.wallet.util.ColorUtil;
import com.cubedao.wallet.util.NumberUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class BalanceItem extends MenuItem {
    private static final String DISPLAY_NAME = ColorUtil.rgb("<SOLID:0078BF>&l$CBIT Balance");
    private static final ItemStack ICON = new ItemStack(Material.DIAMOND);

    public BalanceItem() {
        super(DISPLAY_NAME, ICON);
    }

    @Override
    public void onItemClick(ItemClickEvent event) {

    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        ItemStack finalIcon = super.getFinalIcon(player);
        try {
            double balance = NFTPlayer.getByUUID(player.getUniqueId()).getPrimaryWallet().getPolygonCBITBalance();
            ItemMeta meta = finalIcon.getItemMeta();
            List<String> lore = Arrays.asList(ColorUtil.rgb("<SOLID:0078BF>" + NumberUtil.round(balance, 3) + " $CBIT"));
            meta.setLore(lore);
            finalIcon.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalIcon;
    }
}
