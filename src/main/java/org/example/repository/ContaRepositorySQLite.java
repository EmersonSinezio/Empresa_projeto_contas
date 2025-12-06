package org.example.repository;

import org.example.model.Contas;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ContaRepositorySQLite {
    private final DatabaseConnection dbConnection;

    public ContaRepositorySQLite() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    // --- INSERT ---
    public void adicionarConta(Contas c) {
        // LÓGICA DE PROTEÇÃO: Se não informou mês de referência, extrai do vencimento
        if (c.getMesReferencia() == null || c.getMesReferencia().isEmpty()) {
            String venc = c.getVencimento(); // Esperado: dd-MM-yy ou dd/MM/yyyy
            if (venc != null && venc.length() >= 8) {
                try {
                    String[] partes = venc.replace("/", "-").split("-");
                    if (partes.length == 3) {
                        String mes = partes[1];
                        String ano = partes[2];
                        if (ano.length() == 2) ano = "20" + ano; 
                        c.setMesReferencia(mes + "/" + ano);
                    }
                } catch (Exception ignored) {}
            }
        }

        String sql = "INSERT INTO contas " +
                "(mes_referencia, conta_contabil, percentual_2024, valor_reajuste_2024, " +
                "unidade_gd, cnpj_filial, fornecedor, conta, servico_produto, " +
                "valor_nf, valor_boleto, vencimento, centro_custo, " +
                "data_faturamento, data_lancamento, lancada, vencida, atividade, PN) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, c.getMesReferencia());
            pstmt.setString(2, c.getContaContabil());
            pstmt.setString(3, c.getPercentual2024());
            pstmt.setDouble(4, c.getValorReajuste2024());
            pstmt.setString(5, c.getUnidadeGd());
            pstmt.setString(6, c.getCnpjFilial());
            pstmt.setString(7, c.getFornecedor());
            pstmt.setString(8, c.getConta());
            pstmt.setString(9, c.getServicoProduto());
            pstmt.setDouble(10, c.getValorNF());
            pstmt.setDouble(11, c.getValorBoleto());
            pstmt.setString(12, c.getVencimento());
            pstmt.setString(13, c.getCentroCusto());
            pstmt.setString(14, c.getDataFaturamento());
            pstmt.setString(15, c.getDataLancamento());
            pstmt.setInt(16, c.isLancada() ? 1 : 0);
            pstmt.setInt(17, c.isVencida() ? 1 : 0);
            pstmt.setInt(18, c.getAtividade());
            pstmt.setString(19, c.getPN());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- LEITURA (SELECT) ---
    public List<Contas> listarTodas() {
        List<Contas> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas";
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(resultSetToConta(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Contas resultSetToConta(ResultSet rs) throws SQLException {
        Contas c = new Contas();
        c.setId(rs.getInt("id"));
        c.setMesReferencia(rs.getString("mes_referencia"));
        c.setContaContabil(rs.getString("conta_contabil"));
        c.setPercentual2024(rs.getString("percentual_2024"));
        c.setValorReajuste2024(rs.getDouble("valor_reajuste_2024"));
        c.setUnidadeGd(rs.getString("unidade_gd"));
        c.setCnpjFilial(rs.getString("cnpj_filial"));
        c.setFornecedor(rs.getString("fornecedor"));
        c.setConta(rs.getString("conta"));
        c.setServicoProduto(rs.getString("servico_produto"));
        c.setValorNF(rs.getDouble("valor_nf"));
        c.setValorBoleto(rs.getDouble("valor_boleto"));
        c.setVencimento(rs.getString("vencimento"));
        c.setCentroCusto(rs.getString("centro_custo"));
        c.setDataFaturamento(rs.getString("data_faturamento"));
        c.setDataLancamento(rs.getString("data_lancamento"));
        c.setLancada(rs.getInt("lancada") == 1);
        c.setVencida(rs.getInt("vencida") == 1);
        c.setAtividade(rs.getInt("atividade"));
        c.setPN(rs.getString("PN"));
        return c;
    }

    public int buscarUltimoId() {
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM contas")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void removerConta(int id) {
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement("DELETE FROM contas WHERE id=?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    public void atualizarConta(Contas c) {
        String sql = "UPDATE contas SET atividade=?, valor_boleto=?, data_faturamento=?, data_lancamento=?, lancada=? WHERE id=?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, c.getAtividade());
            pstmt.setDouble(2, c.getValorBoleto());
            pstmt.setString(3, c.getDataFaturamento());
            pstmt.setString(4, c.getDataLancamento());
            pstmt.setInt(5, c.isLancada() ? 1 : 0);
            pstmt.setInt(6, c.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public List<Contas> buscarPorAtividade(int atividade) {
        List<Contas> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas WHERE atividade = ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, atividade);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(resultSetToConta(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Contas> buscarPorNome(String nome) {
        List<Contas> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas WHERE fornecedor LIKE ?";
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%" + nome + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(resultSetToConta(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public List<Contas> buscarPorMesVencimento(String mes, String ano) {
        List<Contas> lista = new ArrayList<>();
        String sql = "SELECT * FROM contas WHERE vencimento LIKE ?"; 
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, "%-" + mes + "-" + ano); 
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(resultSetToConta(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * NOVO MÉTODO: Filtra por período de VENCIMENTO com parser robusto.
     */
    public List<Contas> buscarPorPeriodoVencimento(Date inicio, Date fim) {
        List<Contas> todas = listarTodas();

        LocalDate ldInicio = inicio.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate ldFim = fim.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        return todas.stream()
                .filter(c -> {
                    LocalDate dataVenc = parseDateRobust(c.getVencimento());
                    if (dataVenc == null) return false;
                    return !dataVenc.isBefore(ldInicio) && !dataVenc.isAfter(ldFim);
                })
                .collect(Collectors.toList());
    }

    // Helper para tentar vários formatos de data
    private LocalDate parseDateRobust(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        String[] patterns = {"dd-MM-yy", "dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy"};
        for (String p : patterns) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignored) {}
        }
        return null;
    }

    public List<String> listarMesesDisponiveis() {
        List<String> meses = new ArrayList<>();
        String sql = "SELECT DISTINCT mes_referencia FROM contas WHERE mes_referencia IS NOT NULL AND mes_referencia != '' ORDER BY mes_referencia DESC";
        
        try (Statement stmt = dbConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                meses.add(rs.getString("mes_referencia"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return meses;
    }

    public List<Contas> buscarPorMesReferencia(String mesRef) {
        String sql = "SELECT * FROM contas WHERE mes_referencia = ?";
        List<Contas> lista = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, mesRef);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(resultSetToConta(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Contas> buscarDinamica(String fornecedor, String mes, String ano, 
                                       Date dataInicio, Date dataFim, String tipoData, 
                                       Integer atividade) {
        
        StringBuilder sql = new StringBuilder("SELECT * FROM contas WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // 1. Filtro por Nome
        if (fornecedor != null && !fornecedor.isEmpty()) {
            sql.append(" AND LOWER(fornecedor) LIKE ?");
            params.add("%" + fornecedor.toLowerCase() + "%");
        }

        // 2. Filtro por Mês/Ano (Vencimento Texto - Filtro Rápido)
        if (mes != null && !mes.isEmpty() && ano != null && !ano.isEmpty()) {
            sql.append(" AND vencimento LIKE ?");
            params.add("%-" + mes + "-" + ano);
        }

        // 3. Filtro por Atividade
        if (atividade != null && atividade != 0) {
            sql.append(" AND atividade = ?");
            params.add(atividade);
        }

        List<Contas> resultado = new ArrayList<>();
        
        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(resultSetToConta(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // 4. FILTRO DE PERÍODO NA MEMÓRIA (COM SELEÇÃO DE TIPO)
        if (dataInicio != null && dataFim != null && tipoData != null) {
            LocalDate ldInicio = dataInicio.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate ldFim = dataFim.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            
            return resultado.stream().filter(c -> {
                String dataStr = null;
                if (tipoData.contains("Vencimento")) {
                    dataStr = c.getVencimento();
                } else if (tipoData.contains("Faturamento")) {
                    dataStr = c.getDataFaturamento();
                } else if (tipoData.contains("Lançamento")) {
                    dataStr = c.getDataLancamento();
                }

                LocalDate d = parseDateRobust(dataStr);
                if (d == null) return false;
                return !d.isBefore(ldInicio) && !d.isAfter(ldFim);
            }).collect(Collectors.toList());
        }

        return resultado;
    }
}