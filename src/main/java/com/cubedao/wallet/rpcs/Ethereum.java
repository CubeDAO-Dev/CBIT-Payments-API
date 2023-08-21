package com.cubedao.wallet.rpcs;

import com.cubedao.wallet.CubeDAO;
import lombok.Getter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

public class Ethereum {
    @Getter private Web3j ethereumWeb3j;
    @Getter private DefaultGasProvider gasProvider;

    public Ethereum() {
        this.ethereumWeb3j = Web3j.build(new HttpService(CubeDAO.getInstance().getNftConfig().getEthereumHttpsRpc()));
        this.gasProvider = new DefaultGasProvider();
    }
}
