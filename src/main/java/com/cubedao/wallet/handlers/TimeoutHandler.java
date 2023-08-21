package com.cubedao.wallet.handlers;

import com.cubedao.wallet.CubeDAO;
import com.cubedao.wallet.objects.payments.PaymentRequest;
import com.cubedao.wallet.objects.payments.PeerToPeerPayment;
import org.bukkit.Bukkit;

public class TimeoutHandler {

    public void handleTimeouts() {
        Bukkit.getScheduler().runTaskTimer(CubeDAO.getInstance(), () -> {
            PaymentRequest.getPaymentRequests().removeIf(paymentRequest -> paymentRequest.getTimeout() < System.currentTimeMillis());
            PeerToPeerPayment.getPeerToPeerPayments().removeIf(peerToPeerPayment -> peerToPeerPayment.getTimeout() < System.currentTimeMillis());
        }, 20L, 20L);
    }

}
