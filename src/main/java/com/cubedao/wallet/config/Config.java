package com.cubedao.wallet.config;

import com.cubedao.wallet.CubeDAO;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

import java.util.logging.Level;

@Getter
public class Config {
    private String polygonHttpsRpc;
    private String ethereumHttpsRpc;
    private String serverWalletAddress;
    private String serverPrivateKey;
    private String hotwalletHttpsEndpoint;

    private String polygonPlayerContract;
    private String polygonCbitContract;
    private String ethereumCbitContract;

    private int linkTimeout; //Link timeout in minutes

    private boolean debug;
    private boolean useHotwalletForOutgoingTransactions;

    public void registerConfig() {
        CubeDAO wallet = CubeDAO.getInstance();
        FileConfiguration config = wallet.getConfig();
        config.options().copyDefaults(true);
        wallet.saveConfig();

        this.polygonHttpsRpc = config.getString("polygon_https_rpc");
        this.ethereumHttpsRpc = config.getString("ethereum_https_rpc");
        this.hotwalletHttpsEndpoint = config.getString("hotwallet_https_endpoint");

        if (this.polygonHttpsRpc.isEmpty() || this.ethereumHttpsRpc.isEmpty()) {
            CubeDAO.getInstance().getLogger().log(Level.SEVERE, "polygon_https_rpc and ethereum_https_rpc are not set! Please " +
                    "set an HTTPS endpoint for an Ethereum and Polygon node. We recommend using QuickNode at the moment as it supports EthFilter which" +
                    " will allow you to get started in five minutes!");
            CubeDAO.getInstance().getLogger().log(Level.SEVERE, "Shutting down server. You must configure polygon_https_rpc and " +
                    "ethereum_https_rpc to use CBIT-Payments-API. Please see docs at " +
                    "https://dev.cubedao.com/payments/cbit-payments-api#configuring-ethereum-and-polygon-rpc-endpoints");
            System.exit(-1);
        }

        String serverWalletPrivateKey = config.getString("server_wallet_private_key");
        if (serverWalletPrivateKey != null && !serverWalletPrivateKey.equals("")) {
            CubeDAO.getInstance().getLogger().warning("A private key has been set in the plugin config! Only install " +
                    "plugins you trust. ");
            this.serverPrivateKey = config.getString("server_wallet_private_key");
            useHotwalletForOutgoingTransactions = false;
        } else {
            this.serverPrivateKey = "0x0000000000000000000000000000000000000000000000000000000000000000";
        }

        if (hotwalletHttpsEndpoint != null && !hotwalletHttpsEndpoint.equals("")) {
            CubeDAO.getInstance().getLogger().info("Hotwallet API for outgoing transactions enabled!");
            useHotwalletForOutgoingTransactions = true;
        }


        String address = config.getString("server_wallet_address");
        if (validateAddress(address, "Server Wallet Address")) {
            this.serverWalletAddress = address;
        }

        String polygonPlayerContract = config.getString("contracts.polygon_player_contract");
        if (validateAddress(polygonPlayerContract, "Polygon Player Contract")) {
            this.polygonPlayerContract = polygonPlayerContract;
        }

        String polygonCbitContract = config.getString("contracts.polygon_cbit_contract");
        if (validateAddress(polygonCbitContract, "Polygon CBIT Contract")) {
            this.polygonCbitContract = polygonCbitContract;
        }

        String ethereumCbitContract = config.getString("contracts.ethereum_cbit_contract");
        if (validateAddress(ethereumCbitContract, "Ethereum CBIT Contract")) {
            this.ethereumCbitContract = ethereumCbitContract;
        }

        this.linkTimeout = config.getInt("link-timeout");
        this.debug = config.getBoolean("debug");
    }

    private boolean validateAddress(String address, String name) {
        if (!WalletUtils.isValidAddress(address) || !Keys.toChecksumAddress(address).equalsIgnoreCase(address)) {
            CubeDAO.getInstance().getLogger().log(Level.WARNING, name + " is an invalid format. Check config.yml.");
            Bukkit.getServer().getPluginManager().disablePlugin(CubeDAO.getInstance());
            return false;
        }

        return true;
    }

}
