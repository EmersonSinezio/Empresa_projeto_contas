package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ContaRepositorySQLite {
    private static final DateTimeFormatter TARGET = DateTimeFormatter.ofPattern("dd-MM-yy");
    private final DatabaseConnection dbConnection;

    public ContaRepositorySQLite() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // --- MÉTODOS DE LEITURA E ESCRITA ---

    public List<Contas> listarTodas() {
        return buscarComWhere(null, null);
    }

    public int buscarUltimoId() {
        String sql = "SELECT MAX(id) FROM contas";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void adicionarConta(Contas conta) {
        String sql = "INSERT INTO contas (" +
                "cnpjServico, cnpjFilial, unidade, contaContabil, fornecedor, servico, " +
                "valorNF, centroCusto, valorBoleto, dataFaturamento, dataLancada, " +
                "dataVencimento, lancada, vencida, atividade" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            setContaParameters(pstmt, conta);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void atualizarConta(Contas conta) {
        String sql = "UPDATE contas SET " +
                "cnpjServico=?, cnpjFilial=?, unidade=?, contaContabil=?, fornecedor=?, servico=?, " +
                "valorNF=?, centroCusto=?, valorBoleto=?, dataFaturamento=?, dataLancada=?, " +
                "dataVencimento=?, lancada=?, vencida=?, atividade=? " +
                "WHERE id=?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            setContaParameters(pstmt, conta);
            pstmt.setInt(16, conta.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removerConta(int id) {
        String sql = "DELETE FROM contas WHERE id = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTODOS DE BUSCA ---

    public List<Contas> buscarPorAtividade(int atividade) {
        return buscarComWhere("atividade = ?", new String[]{String.valueOf(atividade)});
    }

    public List<Contas> buscarPorNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            return listarTodas();
        }
        return buscarComWhere("LOWER(fornecedor) LIKE ?", new String[]{"%" + nome.toLowerCase() + "%"});
    }

    public List<Contas> buscarPorDataLancada(String dataLancadaRaw) {
        String target = normalizeToDDMMyy(dataLancadaRaw);
        if (target == null) return new ArrayList<>();
        return buscarComWhere("dataLancada = ?", new String[]{target});
    }

    public List<Contas> buscarPorDataVencimento(String dataVencimentoRaw) {
        String target = normalizeToDDMMyy(dataVencimentoRaw);
        if (target == null) return new ArrayList<>();
        return buscarComWhere("dataVencimento = ?", new String[]{target});
    }

    public List<Contas> buscarPorMesVencimento(String mes, String ano) {
        // Garante que o ano tenha 2 dígitos (ex: 2025 vira 25)
        if (ano.length() == 4) {
            ano = ano.substring(2);
        }
        // Garante que o mês tenha 2 dígitos (ex: 1 vira 01)
        if (mes.length() == 1) {
            mes = "0" + mes;
        }

        // O padrão no banco é dd-MM-yy. 
        // O LIKE '%-MM-yy' busca qualquer dia daquele mês e ano.
        String pattern = "%-" + mes + "-" + ano;
        
        return buscarComWhere("dataVencimento LIKE ?", new String[]{pattern});
    }

    // --- MÉTODOS AUXILIARES ---

    private List<Contas> buscarComWhere(String whereClause, String[] parameters) {
        List<Contas> contas = new ArrayList<>();
        String sql = "SELECT * FROM contas";
        if (whereClause != null) {
            sql += " WHERE " + whereClause;
        }

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    pstmt.setString(i + 1, parameters[i]);
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                contas.add(resultSetToConta(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contas;
    }

    private Contas resultSetToConta(ResultSet rs) throws SQLException {
        Contas c = new Contas();
        c.setId(rs.getInt("id"));
        c.setCnpjServico(rs.getString("cnpjServico"));
        c.setCnpjFilial(rs.getString("cnpjFilial"));
        c.setUnidade(rs.getString("unidade"));
        c.setContaContabil(rs.getString("contaContabil"));
        c.setFornecedor(rs.getString("fornecedor"));
        c.setServico(rs.getString("servico"));
        c.setValorNF(rs.getDouble("valorNF"));
        c.setCentroCusto(rs.getString("centroCusto"));
        c.setValorBoleto(rs.getDouble("valorBoleto"));
        c.setDataFaturamento(rs.getString("dataFaturamento"));
        c.setDataLancada(rs.getString("dataLancada"));
        c.setDataVencimento(rs.getString("dataVencimento"));
        c.setLancada(rs.getInt("lancada") == 1);
        c.setVencida(rs.getInt("vencida") == 1);
        c.setAtividade(rs.getInt("atividade"));
        return c;
    }

    private void setContaParameters(PreparedStatement pstmt, Contas c) throws SQLException {
        pstmt.setString(1, c.getCnpjServico());
        pstmt.setString(2, c.getCnpjFilial());
        pstmt.setString(3, c.getUnidade());
        pstmt.setString(4, c.getContaContabil());
        pstmt.setString(5, c.getFornecedor());
        pstmt.setString(6, c.getServico());
        pstmt.setDouble(7, c.getValorNF());
        pstmt.setString(8, c.getCentroCusto());
        pstmt.setDouble(9, c.getValorBoleto());
        pstmt.setString(10, c.getDataFaturamento());
        pstmt.setString(11, c.getDataLancada());
        pstmt.setString(12, c.getDataVencimento());
        pstmt.setInt(13, c.isLancada() ? 1 : 0);
        pstmt.setInt(14, c.isVencida() ? 1 : 0);
        pstmt.setInt(15, c.getAtividade());
    }

    public static String normalizeToDDMMyy(String input) {
        if (input == null) return null;
        String s = input.trim();
        if (s.isEmpty()) return null;

        s = s.replace('.', '-').replace('/', '-');
        try {
            LocalDate d = LocalDate.parse(s);
            return d.format(TARGET);
        } catch (DateTimeParseException ignored) {}

        String[] patterns = new String[]{"d-M-uuuu", "d-M-uu", "dd-MM-uuuu", "dd-MM-uu", "uuuu-MM-dd"};
        for (String p : patterns) {
            try {
                DateTimeFormatter f = DateTimeFormatter.ofPattern(p);
                LocalDate d = LocalDate.parse(s, f);
                return d.format(TARGET);
            } catch (DateTimeParseException ignored) {}
        }
        return s;
    }
}