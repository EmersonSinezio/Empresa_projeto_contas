package org.example;

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
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTableIfNotExists() {
        // Essa estrutura reflete o arquivo contas.db enviado
        String sql = "CREATE TABLE IF NOT EXISTS contas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "cnpjServico TEXT," +
                "cnpjFilial TEXT," +
                "unidade TEXT," +
                "contaContabil TEXT," +
                "fornecedor TEXT," +
                "servico TEXT," +
                "valorNF REAL," +
                "centroCusto TEXT," +
                "valorBoleto REAL," +
                "dataFaturamento TEXT," +
                "dataLancada TEXT," +
                "dataVencimento TEXT," +
                "lancada INTEGER," + 
                "vencida INTEGER," +
                "atividade INTEGER" +
                ");";
        // Nota: O campo 'PN' do JSON não foi incluído pois não está no arquivo DB original
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}