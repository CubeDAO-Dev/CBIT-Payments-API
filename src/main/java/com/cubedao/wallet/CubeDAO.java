package com.cubedao.wallet;

import com.cubedao.wallet.commands.WalletGUICommand;
import com.cubedao.wallet.config.Config;
import com.cubedao.wallet.config.LangConfig;
import com.cubedao.wallet.contracts.cubedao.Players;
import com.cubedao.wallet.contracts.cubedao.CBIT;
import com.cubedao.wallet.handlers.TimeoutHandler;
import com.cubedao.wallet.listeners.PlayerListener;
import com.cubedao.wallet.menus.WalletGUI;
import com.cubedao.wallet.objects.NFTPlayer;
import com.cubedao.wallet.objects.Wallet;
import com.cubedao.wallet.rpcs.Ethereum;
import com.cubedao.wallet.rpcs.Polygon;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.h2.value.CaseInsensitiveConcurrentMap;

import java.util.Map;

public class CubeDAO extends JavaPlugin {
    private static CubeDAO plugin;

    @Getter private Config nftConfig;

    @Getter private LangConfig langConfig;

    //Contracts
    @Getter private Players players;
    @Getter private CBIT cbit;

    //RPCs
    @Getter private Polygon polygonRPC;
    @Getter private Ethereum ethereumRPC;

    private final Map<String, Wallet> wallets = new CaseInsensitiveConcurrentMap<>();

    public void onEnable() {
        plugin = this;

        (nftConfig = new Config()).registerConfig();
        (langConfig = new LangConfig()).registerConfig();

        polygonRPC = new Polygon();
        ethereumRPC = new Ethereum();

        players = new Players();
        cbit = new CBIT();

        WalletGUI.setup();

        for (Player p : Bukkit.getOnlinePlayers()) {
            new NFTPlayer(p.getUniqueId());
        }

        new TimeoutHandler().handleTimeouts();

        registerEvents();
        registerCommands();

        getServer().getConsoleSender().sendMessage("CubeDAO CBIT API has been enabled");
    }

    public void onDisable() {
        plugin = null;
        getServer().getConsoleSender().sendMessage("CubeDAO CBIT API has been disabled");
    }

    public void registerEvents() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new PlayerListener(), this);
    }

    public void registerCommands() {
        getCommand("wallet").setExecutor(new WalletGUICommand());
    }

    public void addWallet(Wallet wallet) {
        wallets.put(wallet.getAddress(), wallet);
    }

    public Wallet getWallet(String address) {
        return wallets.get(address);
    }

    public void removeWallet(Wallet wallet) {
        wallets.remove(wallet.getAddress(), wallet);
    }

    public static CubeDAO getInstance() {
        return plugin;
    }
}
