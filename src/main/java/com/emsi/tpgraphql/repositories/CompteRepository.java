package com.emsi.tpgraphql.repositories;

import com.emsi.tpgraphql.entities.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    @Query("SELECT SUM(c.solde) FROM Compte c")
    double sumSoldes();
}
