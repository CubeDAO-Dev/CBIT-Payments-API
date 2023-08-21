package com.cubedao.wallet.objects;

import com.cubedao.wallet.CubeDAO;
import com.cubedao.wallet.contracts.wrappers.common.ERC20;
import com.cubedao.wallet.contracts.wrappers.common.ERC721;
import com.cubedao.wallet.contracts.wrappers.polygon.PolygonCBITToken;
import com.cubedao.wallet.event.AsyncPlayerPaidFromServerWalletEvent;
import com.cubedao.wallet.objects.payments.PaymentRequest;
import com.cubedao.wallet.objects.payments.PeerToPeerPayment;
import com.cubedao.wallet.qrmaps.LinkUtils;
import com.cubedao.wallet.qrmaps.QRMapManager;
import com.cubedao.wallet.util.ColorUtil;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.json.HTTP;
import org.json.JSONObject;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Wallet {

    @Getter
    private final NFTPlayer owner;
    @Getter
    private final String address;
    @Getter
    @Setter
    private double polygonCBITBalance;
    @Getter
    @Setter
    private double ethereumCBITBalance;
    @Getter
    private static HashMap<String, ERC20> customPolygonTokenWrappers = new HashMap<String, ERC20>();
    @Getter
    private static HashMap<String, Double> customPolygonBalances = new HashMap<>();
    @Getter
    private static HashMap<String, ERC20> customEthereumTokenWrappers = new HashMap<String, ERC20>();
    @Getter
    private static HashMap<String, Double> customEthereumBalances = new HashMap<>();

    public Wallet(UUID uuid, String address) {
        this.owner = NFTPlayer.getByUUID(uuid);
        this.address = address;

        //Get balance initially
        double polygonBalance = 0;
        double ethereumBalance = 0;
        try {
            BigInteger bigIntegerPoly = CubeDAO.getInstance().getCbit().getPolygonBalance(address);
            BigInteger bigIntegerEther = CubeDAO.getInstance().getCbit().getEthereumBalance(address);
            polygonBalance = Convert.fromWei(bigIntegerPoly.toString(), Convert.Unit.ETHER).doubleValue();
            ethereumBalance = Convert.fromWei(bigIntegerEther.toString(), Convert.Unit.ETHER).doubleValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.polygonCBITBalance = polygonBalance;
        this.ethereumCBITBalance = ethereumBalance;

        CubeDAO.getInstance().addWallet(this);
    }

    public Wallet(NFTPlayer owner, String address) {
        this.owner = owner;
        this.address = address;

        //Get balance initially
        double polygonBalance = 0;
        double ethereumBalance = 0;
        try {
            BigInteger bigIntegerPoly = CubeDAO.getInstance().getCbit().getPolygonBalance(address);
            BigInteger bigIntegerEther = CubeDAO.getInstance().getCbit().getEthereumBalance(address);
            polygonBalance = Convert.fromWei(bigIntegerPoly.toString(), Convert.Unit.ETHER).doubleValue();
            ethereumBalance = Convert.fromWei(bigIntegerEther.toString(), Convert.Unit.ETHER).doubleValue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.polygonCBITBalance = polygonBalance;
        this.ethereumCBITBalance = ethereumBalance;

        CubeDAO.getInstance().addWallet(this);
    }

    /**
     * Get the wallet's CBIT balance
     */
    public double getCBITBalance(Network network) {
        if (network == Network.POLYGON) {
            return polygonCBITBalance;
        } else {
            return ethereumCBITBalance;
        }
    }

    /**
     * Refresh the wallet's balance for an arbitrary ERC20 token defined at runtime.
     * This is a blocking call, do not run in main thread.
     */
    public void refreshERC20Balance(Network network, String tokenContract) throws Exception {
        if (network == Network.POLYGON) {
            ERC20 customToken = Wallet.getCustomPolygonTokenWrappers().get(tokenContract);
            if (customToken == null) {
                customToken = ERC20.load(tokenContract, CubeDAO.getInstance().getPolygonRPC().getPolygonWeb3j(),
                        Credentials.create(CubeDAO.getInstance().getNftConfig().getServerPrivateKey()), new DefaultGasProvider());
                Wallet.getCustomPolygonTokenWrappers().put(tokenContract, customToken);
            }
            BigInteger bigInteger = customToken.balanceOf(address).send();
            customPolygonBalances.put(tokenContract,
                    Convert.fromWei(bigInteger.toString(), Convert.Unit.ETHER).doubleValue());

        } else if (network == Network.ETHEREUM) {
            ERC20 customToken = Wallet.getCustomEthereumTokenWrappers().get(tokenContract);
            if (customToken == null) {
                customToken = ERC20.load(tokenContract, CubeDAO.getInstance().getEthereumRPC().getEthereumWeb3j(),
                        Credentials.create(CubeDAO.getInstance().getNftConfig().getServerPrivateKey()), new DefaultGasProvider());
                Wallet.getCustomPolygonTokenWrappers().put(tokenContract, customToken);
            }
            BigInteger bigInteger = customToken.balanceOf(address).send();

            customEthereumBalances.put(tokenContract,
                    Convert.fromWei(bigInteger.toString(), Convert.Unit.ETHER).doubleValue());
        }
    }

    /**
     * Alternative API for NFT fetching that seems to provide better data than Alchemy.
     * Returns NFTs on both Polygon and Ethereum chains.
     */
    public JSONObject getOwnedNFTsByContactWithSimpleHash(String contractAddress) throws URISyntaxException, IOException, InterruptedException {
        String url =
                "https://api.simplehash.com/api/v0/nfts/owners?chains=polygon,ethereum&wallet_addresses=" + address +
                        "&contract_addresses=" + contractAddress;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url)).header("X-API-KEY", "worldql_sk_ssga6syqc1eo5eyn")
                .build();
        return new JSONObject(client.send(request, HttpResponse.BodyHandlers.ofString()).body());
    }

    /**
     * Get a list of all the account's owned NFTs. Does not return metadata.
     * This is a blocking call, do not run in main thread.
     */
    public JSONObject getOwnedNFTs(Network network) throws IOException, InterruptedException {
        String baseURL;
        if (network.equals(Network.ETHEREUM)) {
            baseURL = CubeDAO.getInstance().getNftConfig().getEthereumHttpsRpc();
        } else if (network.equals(Network.POLYGON)) {
            baseURL = CubeDAO.getInstance().getNftConfig().getPolygonHttpsRpc();
        } else {
            return null;
        }
        String url = baseURL + "/getNFTs?owner=" + address + "&withMetadata=false";
        return new JSONObject(HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString()).body());
    }

    /**
     * Get a list of all the account's owned NFTs. Returns metadata.
     */
    public JSONObject getOwnedNFTsFromContract(Network network, String contractAddress) throws IOException, InterruptedException {
        String baseURL;
        if (network.equals(Network.ETHEREUM)) {
            baseURL = CubeDAO.getInstance().getNftConfig().getEthereumHttpsRpc();
        } else if (network.equals(Network.POLYGON)) {
            baseURL = CubeDAO.getInstance().getNftConfig().getPolygonHttpsRpc();
        } else {
            return null;
        }
        String url = baseURL + "/getNFTs?owner=" + address + "&contractAddresses[]=" + contractAddress;
        return new JSONObject(HttpClient.newHttpClient().send(HttpRequest.newBuilder().uri(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString()).body());
    }

    public boolean doesPlayerOwnNFTInCollection(Network network, String contractAddress) {
        ERC721 erc721 = null;
        if (network.equals(Network.ETHEREUM)) {
            erc721 = ERC721.load(
                    contractAddress,
                    CubeDAO.getInstance().getEthereumRPC().getEthereumWeb3j(),
                    Credentials.create(CubeDAO.getInstance().getNftConfig().getServerPrivateKey()),
                    new DefaultGasProvider()
            );
        } else if (network.equals(Network.POLYGON)) {
            erc721 = ERC721.load(
                    contractAddress,
                    CubeDAO.getInstance().getPolygonRPC().getPolygonWeb3j(),
                    Credentials.create(CubeDAO.getInstance().getNftConfig().getServerPrivateKey()),
                    new DefaultGasProvider()
            );
        } else {
            return false;
        }

        try {
            BigInteger balance = erc721.balanceOf(address).send();
            return balance.compareTo(BigInteger.ZERO) > 0; //return true if address owns at least 1 NFT from this contract
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send a request for a CBIT transaction from this wallet
     *
     * @param amount
     * @param network
     * @param reason
     * @param canDuplicate
     * @param payload
     */
    public <T> void requestCBIT(double amount, Network network, String reason, boolean canDuplicate, T payload) throws IOException, InterruptedException {
        CubeDAO cubeDao = CubeDAO.getInstance();

        UUID uuid = owner.getUuid();
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            Uint256 refID = new Uint256(new BigInteger(256, new Random())); //NOTE: This generates a random Uint256 to use as a reference. Don't know if we want to change this or not.
            long timeout = Instant.now().plus(cubeDao.getNftConfig().getLinkTimeout(), ChronoUnit.SECONDS).toEpochMilli();
            new PaymentRequest(uuid, amount, refID, network, reason, timeout, canDuplicate, payload);
            String paymentLink = "https://cubedao.com/pay/?to=" + cubeDao.getNftConfig().getServerWalletAddress() + "&amount=" + amount + "&ref=" + refID.getValue().toString() + "&expires=" + (int) (timeout / 1000) + "&duplicate=" + canDuplicate;


            MapView view = Bukkit.createMap(player.getWorld());
            view.getRenderers().clear();

            QRMapManager renderer = new QRMapManager();
            player.sendMessage(ColorUtil.rgb(MessageFormat.format(CubeDAO.getInstance().getLangConfig().getIncomingRequest(), reason)));
            if (Bukkit.getServer().getPluginManager().getPlugin("Geyser-Spigot") != null && org.geysermc.connector.GeyserConnector.getInstance().getPlayerByUuid(player.getUniqueId()) != null) {
                String shortLink = LinkUtils.shortenURL(paymentLink);
                renderer.load(shortLink);
                // TODO: Better error handling
                view.addRenderer(renderer);
                ItemStack map = new ItemStack(Material.FILLED_MAP);
                MapMeta meta = (MapMeta) map.getItemMeta();

                meta.setMapView(view);
                map.setItemMeta(meta);

                QRMapManager.playerPreviousItem.put(player.getUniqueId(), player.getInventory().getItem(0));
                player.getInventory().setItem(0, map);
                player.getInventory().setHeldItemSlot(0);

                player.sendMessage(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getScanQRCode()));

            } else {
                player.sendMessage(MessageFormat.format(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getPayHere()), paymentLink));
            }
            player.sendMessage(MessageFormat.format(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getPayHere()), paymentLink));
        }
    }

    public void mintERC1155NFT(String contractAddress, Network network, String data, int id) {
        if (!owner.isLinked()) {
            CubeDAO.getInstance().getLogger().warning("Skipped outgoing transaction because wallet was not linked!");
            return;
        }
        if (!network.equals(Network.POLYGON) || !CubeDAO.getInstance().getNftConfig().isUseHotwalletForOutgoingTransactions()) {
            CubeDAO.getInstance().getLogger().warning("Attempted to call Wallet.mintERC1155NFT with unsupported network." +
                    "Only Polygon is supported at the moment when using Hotwallet backend.");
            return;
        }
        Player paidPlayer = Bukkit.getPlayer(owner.getUuid());

        JSONObject json = new JSONObject();
        json.put("network", "Polygon");
        json.put("contract_address", contractAddress);
        json.put("recipient_address", this.getAddress());
        // https://forum.openzeppelin.com/t/erc1155-data-parameter-on-mint-method/4393/4
        // We recommend using a JSON format. The token id will be automatically chosen.
        json.put("data", data);
        json.put("id", id);

        String requestBody = json.toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(CubeDAO.getInstance().getNftConfig().getHotwalletHttpsEndpoint() + "/mint_erc1155"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        try {
            JSONObject response = new JSONObject(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body());
            final String receiptLink = "https://app.economykit.com/hotwallet/transaction/" + response.getInt("outgoing_tx_id");
            Bukkit.getScheduler().runTaskAsynchronously(CubeDAO.getInstance(), () -> {
                if (paidPlayer != null) {
                    paidPlayer.sendMessage(ColorUtil.rgb(
                            MessageFormat.format(CubeDAO.getInstance().getLangConfig().getMinted(), receiptLink)));
                } else {
                    CubeDAO.getInstance().getLogger().info("Sent offline ERC-1155 mint: " + receiptLink);
                }
            });
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    /**
     * Deposit CBIT into this wallet
     *
     * @param amount
     * @param network
     * @param reason
     */
    public void payCBIT(double amount, Network network, String reason) {
        if (!owner.isLinked()) {
            CubeDAO.getInstance().getLogger().warning("Skipped outgoing transaction because wallet was not linked!");
            return;
        }
        if (!network.equals(Network.POLYGON)) {
            CubeDAO.getInstance().getLogger().warning("Attempted to call Wallet.payCBIT with unsupported network. " +
                    "Only Polygon is supported in this plugin at the moment.");
            return;
        }

        BigDecimal sending = Convert.toWei(BigDecimal.valueOf(amount), Convert.Unit.ETHER);
        Player paidPlayer = Bukkit.getPlayer(owner.getUuid());

        if (CubeDAO.getInstance().getNftConfig().isUseHotwalletForOutgoingTransactions()) {
            // TODO: Add support for other outgoing currencies through Hotwallet.
            JSONObject json = new JSONObject();
            json.put("network", "Polygon");
            json.put("token", "POLYGON_CBIT");
            json.put("recipient_address", this.getAddress());
            json.put("amount", sending.toBigInteger());
            String requestBody = json.toString();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(CubeDAO.getInstance().getNftConfig().getHotwalletHttpsEndpoint() + "/send_tokens"))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            try {
                JSONObject response = new JSONObject(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body());
                final String receiptLink = "https://app.economykit.com/hotwallet/transaction/" + response.getInt("outgoing_tx_id");
                Bukkit.getScheduler().runTaskAsynchronously(CubeDAO.getInstance(), () -> {
                    postPaymentEvent(amount, network, reason, paidPlayer, receiptLink);
                });
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                CubeDAO.getInstance().getLogger().info("Sending outgoing transaction using PK to " + paidPlayer + " for " + amount);
                final PolygonCBITToken polygonCBITTokenContract = CubeDAO.getInstance().getCbit().getPolygonCBITTokenContract();
                polygonCBITTokenContract.transfer(this.getAddress(), sending.toBigInteger()).sendAsync().thenAccept((c) -> {
                    final String receiptLink = "https://polygonscan.com/tx/" + c.getTransactionHash();
                    postPaymentEvent(amount, network, reason, paidPlayer, receiptLink);
                }).exceptionally(error -> {
                    CubeDAO.getInstance().getLogger().warning("Caught error in transfer function exceptionally: " + error);
                    return null;
                });
            } catch (Exception e) {
                CubeDAO.getInstance().getLogger().warning("caught error in payCbit:");
                e.printStackTrace();
            }
        }
    }

    private void postPaymentEvent(double amount, Network network, String reason, Player paidPlayer, String receiptLink) {
        if (paidPlayer != null) {
            AsyncPlayerPaidFromServerWalletEvent walletEvent = new AsyncPlayerPaidFromServerWalletEvent(paidPlayer, amount, network, reason, receiptLink);
            walletEvent.callEvent();

            if (walletEvent.isDefaultReceiveMessage()) {
                paidPlayer.sendMessage(ColorUtil.rgb(
                        MessageFormat.format(CubeDAO.getInstance().getLangConfig().getPaid(), reason, receiptLink)));
            }
        } else {
            CubeDAO.getInstance().getLogger().info("Paid offline player: " + receiptLink);
        }
    }

    /**
     * Create a peer to peer payment link for player
     *
     * @param to
     * @param amount
     * @param network
     * @param reason
     * @param payload
     */
    public <T> void createPlayerPayment(NFTPlayer to, double amount, Network network, String reason, T payload) {
        CubeDAO cubeDao = CubeDAO.getInstance();
        if (to != null) {
            Player player = Bukkit.getPlayer(owner.getUuid());
            if (player != null) {
                if (!to.isLinked()) {
                    player.sendMessage(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getPlayerNoLinkedWallet()));
                    return;
                }
                Uint256 refID = new Uint256(new BigInteger(256, new Random()));
                long timeout = Instant.now().plus(cubeDao.getNftConfig().getLinkTimeout(), ChronoUnit.SECONDS).toEpochMilli();
                new PeerToPeerPayment(to, owner, amount, refID, network, reason, timeout, payload);
                String paymentLink = "https://cubedao.com/pay/?to=" + to.getPrimaryWallet().getAddress() + "&amount=" + amount + "&ref=" + refID.getValue().toString() + "&expires=" + (int) (timeout / 1000);
                player.sendMessage(MessageFormat.format(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getPayHere()), paymentLink));
            }
        }
    }

    /**
     * Create a peer to peer payment link for player
     *
     * @param to
     * @param amount
     * @param network
     * @param reason
     */
    public <T> void createPlayerPayment(NFTPlayer to, double amount, Network network, String reason) {
        CubeDAO cubeDao = CubeDAO.getInstance();
        if (to != null) {
            Player player = Bukkit.getPlayer(owner.getUuid());
            if (player != null) {
                if (!to.isLinked()) {
                    player.sendMessage(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getPlayerNoLinkedWallet()));
                    return;
                }
                Uint256 refID = new Uint256(new BigInteger(256, new Random()));
                long timeout = Instant.now().plus(cubeDao.getNftConfig().getLinkTimeout(), ChronoUnit.SECONDS).toEpochMilli();
                new PeerToPeerPayment(to, owner, amount, refID, network, reason, timeout, null);
                String paymentLink = "https://cubedao.com/pay/?to=" + to.getPrimaryWallet().getAddress() + "&amount=" + amount + "&ref=" + refID.getValue().toString() + "&expires=" + (int) (timeout / 1000);
                player.sendMessage(MessageFormat.format(ColorUtil.rgb(CubeDAO.getInstance().getLangConfig().getPayHere()), paymentLink));
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Wallet wallet = (Wallet) object;
        if (!Objects.equals(owner, wallet.owner)) return false;
        return Objects.equals(address, wallet.address);
    }

    @Override
    public int hashCode() {
        int result = owner != null ? owner.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

}
