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

    // Adicione esta constante no topo da classe ou dentro do método
    private static final String[] MESES_ABREV = {
        "JAN", "FEV", "MAR", "ABR", "MAI", "JUN", 
        "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"
    };

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
        
        // Garante que o mês tenha 2 dígitos (ex: "1" vira "01")
        if (mes.length() == 1) mes = "0" + mes;
        
        // Prepara as variações de ano (2 dígitos e 4 dígitos)
        String anoCurto = ano.length() == 4 ? ano.substring(2) : ano; // ex: "25"
        String anoLongo = ano.length() == 2 ? "20" + ano : ano;       // ex: "2025"

        // SQL que busca TODAS as variações possíveis de formatação
        // 1. %-12-25
        // 2. %/12/25
        // 3. %-12-2025
        // 4. %/12/2025
        String sql = "SELECT * FROM contas WHERE " +
                     "vencimento LIKE ? OR " +
                     "vencimento LIKE ? OR " +
                     "vencimento LIKE ? OR " +
                     "vencimento LIKE ?";

        try (PreparedStatement pstmt = dbConnection.getConnection().prepareStatement(sql)) {
            
            // Variação 1: Traço + Ano Curto (ex: 05-12-25)
            pstmt.setString(1, "%-" + mes + "-" + anoCurto);
            
            // Variação 2: Barra + Ano Curto (ex: 05/12/25)
            pstmt.setString(2, "%/" + mes + "/" + anoCurto);
            
            // Variação 3: Traço + Ano Longo (ex: 05-12-2025)
            pstmt.setString(3, "%-" + mes + "-" + anoLongo);
            
            // Variação 4: Barra + Ano Longo (ex: 05/12/2025)
            pstmt.setString(4, "%/" + mes + "/" + anoLongo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(resultSetToConta(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Log para você ver no console o que está acontecendo
        System.out.println("Busca Mes " + mes + "/" + ano + " encontrou " + lista.size() + " contas.");
        
        return lista;
    }

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

    public void replicarContasParaProximoMes(String mesOrigem, String anoOrigem) {
        // Normaliza ano para 4 dígitos para busca por mes_referencia (caso precise)
        String anoBusca = anoOrigem;
        if (anoBusca.length() == 2) anoBusca = "20" + anoBusca;
        
        // Tenta buscar as contas de origem. 
        // Nota: Se suas contas atuais estão salvas como "12/25" ou "DEZ_25", 
        // a busca tem que bater com o que está no banco.
        // O código abaixo tenta buscar por vencimento se não achar por referência.
        
        // 1. Busca as contas do mês de origem
        List<Contas> contasOrigem = buscarPorMesVencimento(mesOrigem, anoOrigem);
        
        // Se a lista vier vazia, pode ser que você esteja tentando buscar pelo mes_referencia antigo (ex: JAN_25)
        if (contasOrigem.isEmpty()) {
             // Tenta construir a string JAN_25 para buscar
             try {
                 int mIdx = Integer.parseInt(mesOrigem) - 1;
                 if (mIdx >= 0 && mIdx < 12) {
                     String refAntiga = MESES_ABREV[mIdx] + "_" + anoOrigem;
                     contasOrigem = buscarPorMesReferencia(refAntiga);
                 }
             } catch (Exception ignored) {}
        }
        
        // --- CORREÇÃO 1: REMOVIDO O FILTRO .filter(Contas::isLancada) ---
        // Agora ele copia TODAS as contas encontradas, não só as pagas.
        
        if (contasOrigem.isEmpty()) {
            throw new RuntimeException("Não foram encontradas contas no mês " + mesOrigem + "/" + anoOrigem + " para copiar.");
        }

        // 2. Calcula qual é o próximo mês e ano
        int m = Integer.parseInt(mesOrigem);
        int a = Integer.parseInt(anoOrigem);
        
        // Incrementa mês
        m++;
        if (m > 12) {
            m = 1;
            a++;
        }
        
        // --- CORREÇÃO 2: FORMATAR COMO JAN_26 ---
        String nomeMes = MESES_ABREV[m - 1]; // Array é 0-based (0 = JAN)
        String proximoAnoStr = String.format("%02d", a); // Garante 2 dígitos (ex: 26)
        
        String novoMesReferencia = nomeMes + "_" + proximoAnoStr; // Gera JAN_26

        // 3. Itera e cria as cópias
        int count = 0;
        for (Contas origem : contasOrigem) {
            Contas nova = new Contas();
            
            // --- CÓPIA DOS DADOS FIXOS ---
            nova.setFornecedor(origem.getFornecedor());
            nova.setServicoProduto(origem.getServicoProduto());
            nova.setConta(origem.getConta());
            nova.setCnpjFilial(origem.getCnpjFilial());
            nova.setUnidadeGd(origem.getUnidadeGd());
            nova.setCentroCusto(origem.getCentroCusto());
            nova.setContaContabil(origem.getContaContabil());
            nova.setPercentual2024(origem.getPercentual2024());
            nova.setValorReajuste2024(origem.getValorReajuste2024());
            
            // Mantém os valores
            nova.setValorNF(origem.getValorNF());
            nova.setValorBoleto(origem.getValorBoleto());

            // --- ATUALIZAÇÃO DE DATAS ---
            nova.setMesReferencia(novoMesReferencia); // Aqui vai o JAN_26
            
            // Calcula nova data de vencimento (Mês + 1)
            String vencOrigem = origem.getVencimento(); // dd-MM-yy
            if (vencOrigem != null && !vencOrigem.isEmpty()) {
                try {
                    // Parse usando LocalDate (formato do banco dd-MM-yy)
                    DateTimeFormatter DB_FMT = DateTimeFormatter.ofPattern("dd-MM-yy");
                    LocalDate dataVenc = LocalDate.parse(vencOrigem, DB_FMT);
                    LocalDate novaDataVenc = dataVenc.plusMonths(1);
                    nova.setVencimento(novaDataVenc.format(DB_FMT));
                } catch (Exception e) {
                    nova.setVencimento(""); 
                }
            }

            // --- ZERAR CAMPOS ---
            nova.setDataFaturamento(null);
            nova.setDataLancamento(null);
            nova.setAtividade(0);
            nova.setLancada(false);
            nova.setVencida(false);
            nova.setPN(""); 

            // Salva a nova conta
            adicionarConta(nova);
            count++;
        }
        
        System.out.println("Copiadas " + count + " contas para " + novoMesReferencia);
    }
}