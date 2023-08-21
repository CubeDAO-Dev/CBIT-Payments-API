package com.cubedao.wallet.menus;

import com.cubedao.wallet.CubeDAO;
import ninja.amp.ampmenus.menus.ItemMenu;

public class WalletGUI {
    public static ItemMenu menu;

    public static void setup() {
        menu = new ItemMenu("Cube Wallet", ItemMenu.Size.TWO_LINE, CubeDAO.getInstance());
        menu.setItem(8, new CloseItem());
        menu.setItem(0, new BalanceItem());
    }
}
