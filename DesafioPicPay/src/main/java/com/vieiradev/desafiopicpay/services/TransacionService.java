package com.vieiradev.desafiopicpay.services;

import com.vieiradev.desafiopicpay.domain.transaction.Transaction;
import com.vieiradev.desafiopicpay.domain.user.User;
import com.vieiradev.desafiopicpay.dtos.TransactionDTO;
import com.vieiradev.desafiopicpay.repositories.TransationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class TransacionService {

    @Autowired
    private UserService userService;

    @Autowired
    private TransationRepository transationRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private NotificationService notificationService;

    public Transaction createTransaction(TransactionDTO transaction) throws Exception {
        User sender = this.userService.findUserById(transaction.senderId());
        User receiver = this.userService.findUserById(transaction.receiverId());

        userService.validateTransaction(sender, transaction.value());

        Boolean isAuthorize = this.authorizeTransaction(sender, transaction.value());

        if (this.authorizeTransaction(sender, transaction.value())) {
            throw new Exception("Transação não autorizada.");
        }

        Transaction newTransaction = new Transaction();

        newTransaction.setAmount(transaction.value());
        newTransaction.setSender(sender);
        newTransaction.setReceiver(receiver);
        newTransaction.setTimestamp(LocalDateTime.now());

        sender.setBalance(sender.getBalance().subtract(transaction.value()));
        receiver.setBalance(receiver.getBalance().add(transaction.value()));

        this.transationRepository.save(newTransaction);
        this.userService.saveUser(sender);
        this.userService.saveUser(receiver);

        this.notificationService.sendNotification(sender, "Transação realizada com sucesso!");
        this.notificationService.sendNotification(receiver, "Transação recebida com sucesso!");

        return newTransaction;
    }

    public Boolean authorizeTransaction(User sender, BigDecimal value) {
        ResponseEntity<Map> authorizarionResponse = restTemplate.getForEntity("https://util.devi.tools/api/v2/authorize", Map.class);

        if (authorizarionResponse.getStatusCode() == HttpStatus.OK) {
            String message = (String) authorizarionResponse.getBody().get("message");
            return "Autorizado".equalsIgnoreCase(message);
        } else return false;
    }
}
