package com.cubedao.wallet.rpcs;

import com.cubedao.wallet.CubeDAO;
import lombok.Getter;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

public class Polygon {
    @Getter private Web3j polygonWeb3j;
    @Getter private DefaultGasProvider gasProvider;

    public Polygon() {
        this.polygonWeb3j = Web3j.build(new HttpService(CubeDAO.getInstance().getNftConfig().getPolygonHttpsRpc()));
        this.gasProvider = new DefaultGasProvider();
    }
}
