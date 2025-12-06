package org.example.repository;

import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:contas.db";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL);
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTableIfNotExists() {
        // Estrutura atualizada conforme seu pedido
        String sql = "CREATE TABLE IF NOT EXISTS contas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "mes_referencia TEXT," +
                "conta_contabil TEXT," +
                "percentual_2024 TEXT," +
                "valor_reajuste_2024 REAL," +
                "unidade_gd TEXT," +
                "cnpj_filial TEXT," +
                "fornecedor TEXT," +
                "conta TEXT," + // Campo novo 'conta'
                "servico_produto TEXT," +
                "valor_nf REAL," +
                "valor_boleto REAL," +
                "vencimento TEXT," +
                "centro_custo TEXT," +
                "data_faturamento TEXT," +
                "data_lancamento TEXT," +
                "lancada INTEGER DEFAULT 0," +
                "vencida INTEGER DEFAULT 0," +
                "atividade INTEGER DEFAULT 0," +
                "PN TEXT" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}