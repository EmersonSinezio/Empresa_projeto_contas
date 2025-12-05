package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;




public class Contas {
    private Integer id;
    private String cnpjServico;
    private String cnpjFilial;
    private String unidade;
    private String contaContabil;
    private String fornecedor;
    private String servico;
    private double valorNF;
    private String centroCusto; // No DB é TEXT
    private double valorBoleto;
    private String dataFaturamento;
    private String dataLancada;
    private String dataVencimento;
    private boolean lancada;
    private boolean vencida;
    private int atividade;

    // Campo PN existe no seu JSON, mas NÃO no arquivo .db enviado.
    // Mantive aqui para o objeto, mas ele não será persistido sem alterar a tabela.
    private String PN;

    public Contas() {}

    // Construtor completo
    public Contas(String cnpjServico, String cnpjFilial, String unidade, String contaContabil,
                  String fornecedor, String servico, double valorNF, String centroCusto,
                  double valorBoleto, String dataFaturamento, String dataLancada,
                  String dataVencimento, String pn) {
        this.cnpjServico = cnpjServico;
        this.cnpjFilial = cnpjFilial;
        this.unidade = unidade;
        this.contaContabil = contaContabil;
        this.fornecedor = fornecedor;
        this.servico = servico;
        this.valorNF = valorNF;
        this.centroCusto = centroCusto;
        this.valorBoleto = valorBoleto;
        this.dataFaturamento = dataFaturamento;
        this.dataLancada = dataLancada;
        this.dataVencimento = dataVencimento;
        this.PN = pn;
        this.lancada = false;
        this.vencida = false;
    }

    // Construtor simplificado usado na GUI
    public Contas(String fornecedor, String servico, double valorNF, double valorBoleto, 
                  String dataFaturamento, String dataVencimento, String centroCusto) {
        this.fornecedor = fornecedor;
        this.servico = servico;
        this.valorNF = valorNF;
        this.valorBoleto = valorBoleto;
        this.dataFaturamento = dataFaturamento;
        this.dataVencimento = dataVencimento;
        this.centroCusto = centroCusto;
        this.lancada = false;
        this.vencida = false;
    }

    // --- Getters e Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCnpjServico() { return cnpjServico; }
    public void setCnpjServico(String cnpjServico) { this.cnpjServico = cnpjServico; }

    public String getCnpjFilial() { return cnpjFilial; }
    public void setCnpjFilial(String cnpjFilial) { this.cnpjFilial = cnpjFilial; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public String getContaContabil() { return contaContabil; }
    public void setContaContabil(String contaContabil) { this.contaContabil = contaContabil; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }

    public double getValorNF() { return valorNF; }
    public void setValorNF(double valorNF) { this.valorNF = valorNF; }

    public String getCentroCusto() { return centroCusto; }
    public void setCentroCusto(String centroCusto) { this.centroCusto = centroCusto; }

    public double getValorBoleto() { return valorBoleto; }
    public void setValorBoleto(double valorBoleto) { this.valorBoleto = valorBoleto; }

    public String getDataFaturamento() { return dataFaturamento; }
    public void setDataFaturamento(String dataFaturamento) { this.dataFaturamento = dataFaturamento; }

    public String getDataLancada() { return dataLancada; }
    public void setDataLancada(String dataLancada) { this.dataLancada = dataLancada; }

    public String getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(String dataVencimento) { this.dataVencimento = dataVencimento; }

    public boolean isLancada() { return lancada; }
    public void setLancada(boolean lancada) { this.lancada = lancada; }

    public boolean isVencida() { return vencida; }
    public void setVencida(boolean vencida) { this.vencida = vencida; }

    public int getAtividade() { return atividade; }
    public void setAtividade(int atividade) { this.atividade = atividade; }

    public String getPN() { return PN; }
    public void setPN(String PN) { this.PN = PN; }

    public void lancarConta(int atividade, String dataLancada) {
        this.atividade = atividade;
        this.dataLancada = dataLancada;
        this.lancada = true;
    }
}

/* ===================== ContaRepository (Mês-aware) ===================== */


/* ===================== ContaManagerGUI (Swing) ===================== */
class ContaManagerGUI extends JFrame {
    private final ContaRepositorySQLite contaRepo = new ContaRepositorySQLite();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Contas> listaAtual = new ArrayList<>(); 

    public ContaManagerGUI() {
        super("Gerenciador de Contas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        String[] cols = new String[]{
            "Fornecedor", "Serviço", "Valor NF", "Valor Boleto", 
            "Data Vencimento", "Data Lançada", "Atividade"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) onShowInfo(null);
            }
        });
        
        JScrollPane scroll = new JScrollPane(table);

        // --- Botões ---
        JButton btnInfo = new JButton("Informações");
        btnInfo.addActionListener(this::onShowInfo);

        JButton btnAdd = new JButton("Cadastrar conta");
        btnAdd.addActionListener(this::onAdd);

        JButton btnLaunch = new JButton("Lançar conta");
        btnLaunch.addActionListener(this::onLaunch);

        JButton btnSearchActivity = new JButton("Buscar por Atividade");
        btnSearchActivity.addActionListener(this::onSearchActivity);

        JButton btnSearchLanc = new JButton("Buscar por Data Lançada");
        btnSearchLanc.addActionListener(this::onSearchDataLancada);

        JButton btnSearchVenc = new JButton("Buscar por Data Vencimento");
        btnSearchVenc.addActionListener(this::onSearchDataVencimento);

        JButton btnSearchNome = new JButton("Buscar por Nome");
        btnSearchNome.addActionListener(this::onSearchNome);
        
        // NOVO BOTÃO
        JButton btnSearchMes = new JButton("Buscar por Mês (Vencimento)");
        btnSearchMes.addActionListener(this::onSearchMes);

        JButton btnRemove = new JButton("Remover conta");
        btnRemove.addActionListener(this::onRemove);

        JButton btnRefresh = new JButton("Listar Todas");
        btnRefresh.addActionListener(e -> refreshTable(contaRepo.listarTodas()));

        JButton btnExit = new JButton("Sair");
        btnExit.addActionListener(e -> System.exit(0));

        // Layout
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(2, 0, 5, 5));

        JPanel buttonRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow1.add(btnSearchNome);
        buttonRow1.add(btnSearchActivity);
        buttonRow1.add(btnSearchLanc);
        buttonRow1.add(btnSearchVenc);
        buttonRow1.add(btnSearchMes);

        JPanel buttonRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow2.add(btnAdd);
        buttonRow2.add(btnLaunch);
        buttonRow2.add(btnRemove);
        buttonRow2.add(btnRefresh);
        buttonRow2.add(btnInfo);
        buttonRow2.add(btnExit);

        buttonPanel.add(buttonRow1);
        buttonPanel.add(buttonRow2);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Selecione as contas para ver a soma");
        statusPanel.add(statusLabel);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateSelectionSum(statusLabel);
        });

        refreshTable(contaRepo.listarTodas());
    }

    // --- Métodos Auxiliares de Formatação ---

    // 2. Transforma 2025-12-05 ou 05-12-25 em 05/12/25
    private String formatarDataUI(String data) {
        if (data == null || data.trim().isEmpty()) return "";
        return data.replace("-", "/");
    }

    private String formatarValor(double valor){
        java.text.NumberFormat formato = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt","BR"));
        return formato.format(valor);
    }
    
    private String safe(String s) { return s == null ? "" : s; }

    // --- Lógica Principal ---

    private void refreshTable(List<Contas> contas) {
        // Atualiza a lista em memória para referência futura (cliques)
        this.listaAtual = contas;
        
        tableModel.setRowCount(0);
        for (Contas c : contas) {
            Object[] row = new Object[] {
                    safe(c.getFornecedor()),
                    safe(c.getServico()),
                    formatarValor(c.getValorNF()),
                    formatarValor(c.getValorBoleto()),
                    formatarDataUI(c.getDataVencimento()), // 3. Aplica formatação visual
                    formatarDataUI(c.getDataLancada()),    // 3. Aplica formatação visual
                    c.getAtividade()
            };
            tableModel.addRow(row);
        }
    }

    private void onShowInfo(ActionEvent e) {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma conta para ver as informações.");
            return;
        }
        
        // Pega o objeto real da lista baseado na linha selecionada
        if (sel >= 0 && sel < listaAtual.size()) {
            Contas conta = listaAtual.get(sel);
            mostrarDetalhesConta(conta);
        }
    }

    private void mostrarDetalhesConta(Contas conta) {
        JDialog dialog = new JDialog(this, "Detalhes Completos da Conta", true);
        dialog.setSize(450, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // AQUI mostramos TUDO, inclusive o que está oculto na tabela
        // E aplicamos a formatação de data com barra (/)
        panel.add(createInfoLabel("ID (Banco):", String.valueOf(conta.getId())));
        panel.add(createInfoLabel("Fornecedor:", conta.getFornecedor()));
        panel.add(createInfoLabel("Serviço:", conta.getServico()));
        panel.add(createInfoLabel("CNPJ Serviço:", conta.getCnpjServico()));
        panel.add(createInfoLabel("Unidade:", conta.getUnidade()));
        panel.add(createInfoLabel("Valor NF:", formatarValor(conta.getValorNF())));
        panel.add(createInfoLabel("Valor Boleto:", formatarValor(conta.getValorBoleto())));
        
        // Datas com /
        panel.add(createInfoLabel("Data Faturamento:", formatarDataUI(conta.getDataFaturamento())));
        panel.add(createInfoLabel("Data Vencimento:", formatarDataUI(conta.getDataVencimento())));
        panel.add(createInfoLabel("Data Lançada:", formatarDataUI(conta.getDataLancada())));
        
        panel.add(createInfoLabel("Atividade:", String.valueOf(conta.getAtividade())));
        
        // Booleans
        panel.add(createInfoLabel("Lançada:", conta.isLancada() ? "Sim" : "Não"));
        panel.add(createInfoLabel("Vencida:", conta.isVencida() ? "Sim" : "Não"));
        
        panel.add(createInfoLabel("Centro de Custo:", conta.getCentroCusto()));
        panel.add(createInfoLabel("Conta Contábil:", conta.getContaContabil()));

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(evt -> dialog.dispose());

        JButton btnCopyCC = new JButton("Copiar Centro Custo");
        btnCopyCC.addActionListener(evt -> {
            if (conta.getCentroCusto() != null) {
                StringSelection stringSelection = new StringSelection(conta.getCentroCusto());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                JOptionPane.showMessageDialog(dialog, "Copiado!");
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(btnFechar);
        bottomPanel.add(btnCopyCC);

        dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createInfoLabel(String titulo, String valor) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        
        JLabel labelValor = new JLabel(valor != null ? valor : "");
        labelValor.setFont(new Font("SansSerif", Font.BOLD, 12));

        panel.add(labelTitulo, BorderLayout.WEST);
        panel.add(labelValor, BorderLayout.CENTER);
        return panel;
    }

    // --- Métodos de Ação (Adaptações simples) ---

    private void updateSelectionSum(JLabel statusLabel) {
        double totalNF = 0.0;
        double totalBoleto = 0.0;
        int selectedCount = table.getSelectedRowCount();

        if (selectedCount > 0) {
            int[] selectedRows = table.getSelectedRows();
            for (int viewRow : selectedRows) {
                // Converte índice da tabela para índice da lista
                int modelRow = table.convertRowIndexToModel(viewRow);
                if (modelRow >= 0 && modelRow < listaAtual.size()) {
                    Contas conta = listaAtual.get(modelRow);
                    totalNF += conta.getValorNF();
                    totalBoleto += conta.getValorBoleto();
                }
            }
            statusLabel.setText(String.format("Selecionadas: %d | Total NF: %s | Total Boleto: %s",
                    selectedCount, formatarValor(totalNF), formatarValor(totalBoleto)));
        } else {
            statusLabel.setText("Selecione as contas para ver os totais");
        }
    }

    private void onAdd(ActionEvent e) {
        // 1. Pega o ID previsto (Visual apenas, o banco garante a unicidade)
        int proximoId = contaRepo.buscarUltimoId() + 1;

        // --- Criação dos Componentes ---
        JTextField txtId = new JTextField(String.valueOf(proximoId));
        txtId.setEditable(false); // O usuário não edita o ID manualmente
        txtId.setBackground(new Color(230, 230, 230)); // Cinza para indicar readonly

        JTextField txtFornecedor = new JTextField();
        JTextField txtServico = new JTextField();
        JTextField txtCnpjServico = new JTextField();
        JTextField txtUnidade = new JTextField();
        JTextField txtValorNF = new JTextField();
        JTextField txtValorBoleto = new JTextField();
        JTextField txtDataVencimento = new JTextField(); // Obrigatório
        JTextField txtCentroCusto = new JTextField(); // Obrigatório

        // Opcionais
        JTextField txtDataFaturamento = new JTextField();
        JTextField txtDataLancamento = new JTextField();
        JTextField txtAtividade = new JTextField();
        JTextField txtContaContabil = new JTextField();

        // --- Montagem do Painel (Formulário) ---
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 colunas, gaps de 10px
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("ID (Automático):")); panel.add(txtId);
        
        // Campos Obrigatórios (Marcados com *)
        panel.add(new JLabel("Fornecedor *:")); panel.add(txtFornecedor);
        panel.add(new JLabel("Serviço *:")); panel.add(txtServico);
        panel.add(new JLabel("CNPJ Serviço *:")); panel.add(txtCnpjServico);
        panel.add(new JLabel("Unidade *:")); panel.add(txtUnidade);
        panel.add(new JLabel("Valor NF (R$) *:")); panel.add(txtValorNF);
        panel.add(new JLabel("Valor Boleto (R$) *:")); panel.add(txtValorBoleto);
        panel.add(new JLabel("Data Vencimento (dd/mm/aa) *:")); panel.add(txtDataVencimento);
        panel.add(new JLabel("Centro de Custo *:")); panel.add(txtCentroCusto);

        // Campos Opcionais
        panel.add(new JLabel("Data Faturamento (dd/mm/aa):")); panel.add(txtDataFaturamento);
        panel.add(new JLabel("Data Lançamento (dd/mm/aa):")); panel.add(txtDataLancamento);
        panel.add(new JLabel("Atividade (Número):")); panel.add(txtAtividade);
        panel.add(new JLabel("Conta Contábil:")); panel.add(txtContaContabil);

        // Adiciona uma rolagem caso a tela seja pequena
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(500, 500));
        scrollPane.setBorder(null);

        // --- Exibir o Dialog ---
        int result = JOptionPane.showConfirmDialog(this, scrollPane, 
                "Cadastrar Nova Conta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // --- Validação dos Campos Obrigatórios ---
            if (txtFornecedor.getText().trim().isEmpty() || 
                txtServico.getText().trim().isEmpty() ||
                txtCnpjServico.getText().trim().isEmpty() ||
                txtUnidade.getText().trim().isEmpty() ||
                txtValorNF.getText().trim().isEmpty() ||
                txtValorBoleto.getText().trim().isEmpty() ||
                txtDataVencimento.getText().trim().isEmpty() ||
                txtCentroCusto.getText().trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(this, 
                    "Por favor, preencha todos os campos obrigatórios (*).", 
                    "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                return; // Interrompe o cadastro
            }

            try {
                // --- Conversão e Criação do Objeto ---
                Contas novaConta = new Contas();
                
                novaConta.setFornecedor(txtFornecedor.getText().trim());
                novaConta.setServico(txtServico.getText().trim());
                novaConta.setCnpjServico(txtCnpjServico.getText().trim());
                novaConta.setUnidade(txtUnidade.getText().trim());
                
                // Tratamento de valores numéricos (troca vírgula por ponto se usuário digitar errado)
                double vNF = Double.parseDouble(txtValorNF.getText().replace(",", ".").trim());
                double vBoleto = Double.parseDouble(txtValorBoleto.getText().replace(",", ".").trim());
                novaConta.setValorNF(vNF);
                novaConta.setValorBoleto(vBoleto);
                
                // Datas (Mantém string simples, mas você pode adicionar normalização se quiser)
                novaConta.setDataVencimento(txtDataVencimento.getText().trim());
                novaConta.setDataFaturamento(txtDataFaturamento.getText().trim()); // Opcional
                novaConta.setDataLancada(txtDataLancamento.getText().trim()); // Opcional
                
                // Inteiros
                String ativStr = txtAtividade.getText().trim();
                novaConta.setAtividade(ativStr.isEmpty() ? 0 : Integer.parseInt(ativStr));
                
                novaConta.setCentroCusto(txtCentroCusto.getText().trim());
                novaConta.setContaContabil(txtContaContabil.getText().trim()); // Opcional
                
                // Padrões (12 e 13)
                novaConta.setLancada(false);
                novaConta.setVencida(false);

                // --- Salvar no Banco ---
                contaRepo.adicionarConta(novaConta);
                
                // Feedback e Atualização
                JOptionPane.showMessageDialog(this, "Conta cadastrada com sucesso!");
                refreshTable(contaRepo.listarTodas());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Erro nos valores numéricos. Verifique Valor NF, Valor Boleto e Atividade.", 
                    "Erro de Formato", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar: " + ex.getMessage());
            }
        }
    }
    
    private void onRemove(ActionEvent e) {
        int sel = table.getSelectedRow();
        if (sel == -1) return;
        
        Contas conta = listaAtual.get(sel);
        int resp = JOptionPane.showConfirmDialog(this, "Remover conta do fornecedor " + conta.getFornecedor() + "?");
        if (resp == JOptionPane.YES_OPTION) {
            contaRepo.removerConta(conta.getId());
            refreshTable(contaRepo.listarTodas());
        }
    }

    private void onLaunch(ActionEvent e) {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma conta.");
            return;
        }
        Contas conta = listaAtual.get(sel);
        
        String atividade = JOptionPane.showInputDialog("Atividade:");
        if(atividade != null) {
            conta.setAtividade(Integer.parseInt(atividade));
            conta.setLancada(true);
            // Salvar no banco
            contaRepo.atualizarConta(conta);
            refreshTable(contaRepo.listarTodas());
        }
    }

    private void onSearchActivity(ActionEvent e) {
        String input = JOptionPane.showInputDialog(this, "Digite o número da atividade:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int atividade = Integer.parseInt(input.trim());
                refreshTable(contaRepo.buscarPorAtividade(atividade));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor, digite um número válido.");
            }
        }
    }

    private void onSearchNome(ActionEvent e) {
        String input = JOptionPane.showInputDialog(this, "Digite parte do nome do fornecedor:");
        if (input != null && !input.trim().isEmpty()) {
            refreshTable(contaRepo.buscarPorNome(input.trim()));
        }
    }

    private void onSearchDataLancada(ActionEvent e) {
        String input = JOptionPane.showInputDialog(this, "Digite a data lançada (ex: 05/12/25):");
        if (input != null && !input.trim().isEmpty()) {
            refreshTable(contaRepo.buscarPorDataLancada(input.trim()));
        }
    }

    private void onSearchDataVencimento(ActionEvent e) {
        String input = JOptionPane.showInputDialog(this, "Digite a data de vencimento (ex: 05/12/25):");
        if (input != null && !input.trim().isEmpty()) {
            refreshTable(contaRepo.buscarPorDataVencimento(input.trim()));
        }
    }

    // NOVO MÉTODO: Filtro por Mês e Ano
    private void onSearchMes(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField txtMes = new JTextField();
        JTextField txtAno = new JTextField("25"); // Sugestão default

        panel.add(new JLabel("Mês (1-12):"));
        panel.add(txtMes);
        panel.add(new JLabel("Ano (2 dígitos, ex: 25):"));
        panel.add(txtAno);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Filtrar por Mês de Vencimento", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String mes = txtMes.getText().trim();
            String ano = txtAno.getText().trim();

            if (!mes.isEmpty() && !ano.isEmpty()) {
                refreshTable(contaRepo.buscarPorMesVencimento(mes, ano));
            } else {
                JOptionPane.showMessageDialog(this, "Preencha mês e ano.");
            }
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new ContaManagerGUI().setVisible(true));
    }
}