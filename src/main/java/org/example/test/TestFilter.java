package org.example.test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.example.model.Contas;
import org.example.repository.ContaRepositorySQLite;

public class TestFilter {
    private static final DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("dd-MM-yy");

    public static void main(String[] args) {
        // Mock data
        List<Contas> todas = new ArrayList<>();
        
        Contas c1 = new Contas();
        c1.setDataLancamento("03-12-25"); // Should be IN
        c1.setFornecedor("Conta Dentro");
        todas.add(c1);
        
        Contas c2 = new Contas();
        c2.setDataLancamento("20-12-25"); // Should be OUT
        c2.setFornecedor("Conta Fora");
        todas.add(c2);
        
        // Filter range: 01/12/2025 to 05/12/2025
        LocalDate ldInicio = LocalDate.of(2025, 12, 1);
        LocalDate ldFim = LocalDate.of(2025, 12, 5);
        
        System.out.println("Filtro: " + ldInicio + " ate " + ldFim);

        List<Contas> filtradas = todas.stream()
                .filter(c -> {
                    String v = c.getDataLancamento();
                    if (v == null || v.isEmpty()) return false;
                    try {
                        LocalDate dataLanc = LocalDate.parse(v, DB_FMT);
                        System.out.println("Checking " + v + " -> " + dataLanc);
                        boolean inRange = !dataLanc.isBefore(ldInicio) && !dataLanc.isAfter(ldFim);
                        System.out.println("  In range? " + inRange);
                        return inRange;
                    } catch (Exception e) {
                        System.out.println("  Error parsing " + v);
                        return false;
                    }
                })
                .collect(Collectors.toList());
        
        System.out.println("Resultados:");
        for(Contas c : filtradas) {
            System.out.println(" - " + c.getFornecedor() + " (" + c.getDataLancamento() + ")");
        }
    }
}
