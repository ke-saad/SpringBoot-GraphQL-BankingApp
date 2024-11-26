package com.emsi.tpgraphql.controllers;

import com.emsi.tpgraphql.entities.Compte;
import com.emsi.tpgraphql.entities.Transaction;
import com.emsi.tpgraphql.entities.TransactionRequest;
import com.emsi.tpgraphql.entities.TypeTransaction;
import com.emsi.tpgraphql.repositories.CompteRepository;
import com.emsi.tpgraphql.repositories.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class AppControllerGraphQL {

    private final CompteRepository compteRepository;
    private final TransactionRepository transactionRepository;

    @MutationMapping
    public Transaction addTransaction(@Argument TransactionRequest transactionRequest) {
        Compte compte = compteRepository.findById(transactionRequest.getCompteId())
                .orElseThrow(() -> new RuntimeException("Compte not found"));

        Transaction transaction = new Transaction();
        transaction.setMontant(transactionRequest.getMontant());
        transaction.setDate(transactionRequest.getDate());
        transaction.setType(transactionRequest.getType());
        transaction.setCompte(compte);

        if (transaction.getType() == TypeTransaction.DEPOT) {
            compte.setSolde(compte.getSolde() + transaction.getMontant());
        } else if (transaction.getType() == TypeTransaction.RETRAIT) {
            compte.setSolde(compte.getSolde() - transaction.getMontant());
        }

        compteRepository.save(compte);
        transactionRepository.save(transaction);
        return transaction;
    }

    @QueryMapping
    public Map<String, Object> transactionStats() {
        long count = transactionRepository.count();
        double sumDepots = transactionRepository.sumByType(TypeTransaction.DEPOT);
        double sumRetraits = transactionRepository.sumByType(TypeTransaction.RETRAIT);

        return Map.of(
                "count", count,
                "sumDepots", sumDepots,
                "sumRetraits", sumRetraits
        );
    }

    @QueryMapping
    public List<Transaction> compteTransactions(@Argument Long id) {
        Compte compte = compteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compte not found"));

        return transactionRepository.findByCompte(compte);
    }

    @QueryMapping
    public List<Compte> allComptes() {
        return compteRepository.findAll();
    }

    @QueryMapping
    public Compte compteById(@Argument Long id) {
        return compteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Compte %s not found", id)));
    }

    @MutationMapping
    public Compte saveCompte(@Argument Compte compte) {
        return compteRepository.save(compte);
    }

    @QueryMapping
    public Map<String, Object> totalSolde() {
        long count = compteRepository.count();
        double sum = compteRepository.sumSoldes();
        double average = count > 0 ? sum / count : 0;

        return Map.of(
                "count", count,
                "sum", sum,
                "average", average
        );
    }

    @QueryMapping
    public List<Transaction> allTransactions() {
        return transactionRepository.findAll();
    }

    @QueryMapping
    public List<Transaction> transactionsByType(@Argument TypeTransaction type) {
        return transactionRepository.findByType(type);
    }

    @QueryMapping
    public List<Transaction> allTransactionsByTypeAndAccountId(@Argument TypeTransaction type, @Argument Long accountId) {
        return transactionRepository.findByTypeAndCompte_Id(type, accountId);
    }
}
