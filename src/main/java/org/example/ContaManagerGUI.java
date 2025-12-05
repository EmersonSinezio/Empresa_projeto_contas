package org.example;

import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContaManagerGUI extends JFrame {
    private final ContaRepositorySQLite contaRepo = new ContaRepositorySQLite();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private List<Contas> listaAtual = new ArrayList<>();
    
    // Formatador para converter do JDateChooser para a String do seu DB (dd-MM-yy)
    private final SimpleDateFormat dateFormatDB = new SimpleDateFormat("dd-MM-yy");

    // Adicione esse campo na classe
    private JComboBox<String> comboMeses;

    public ContaManagerGUI() {
        super("Gerenciador de Contas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750); // Aumentei um pouco a altura
        setLocationRelativeTo(null);

        // --- Configuração da Tabela ---
        String[] cols = new String[]{
                "Fornecedor", "Serviço", "Valor NF", "Valor Boleto",
                "Data Vencimento", "Data Lançada", "Atividade"
        };

        // Modelo modificado para retornar as classes corretas das colunas (para ordenação funcionar)
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Ajuste para ordenar números corretamente (não como texto)
                if (columnIndex == 2 || columnIndex == 3) return Double.class; // Valores
                if (columnIndex == 6) return Integer.class; // Atividade
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(25); // Linhas um pouco mais altas para visual moderno
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // --- HABILITA ORDENAÇÃO POR COLUNA ---
        table.setAutoCreateRowSorter(true);
        
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) onShowInfo(null);
            }
        });

        JScrollPane scroll = new JScrollPane(table);

        // --- Botões com Ícones (Simulados ou Reais) ---
        // Dica: Para ícones reais, coloque as imagens em src/main/resources e use:
        // new ImageIcon(getClass().getResource("/icon_save.png"))
        
        JButton btnAdd = criarBotao("Cadastrar", "➕", new Color(0, 120, 215)); // Azul
        btnAdd.addActionListener(this::onAdd);

        JButton btnLaunch = criarBotao("Lançar", "🚀", new Color(40, 167, 69)); // Verde
        btnLaunch.addActionListener(this::onLaunch);

        JButton btnRemove = criarBotao("Remover", "🗑️", new Color(220, 53, 69)); // Vermelho
        btnRemove.addActionListener(this::onRemove);

        JButton btnRefresh = criarBotao("Listar Tudo", "📄", null);
        btnRefresh.addActionListener(e -> refreshTable(contaRepo.listarTodas()));

        // --- Botões de Busca ---
        JButton btnSearchActivity = new JButton("🔍 Atividade");
        btnSearchActivity.addActionListener(this::onSearchActivity);

        JButton btnSearchMes = new JButton("📅 Mês");
        btnSearchMes.addActionListener(this::onSearchMes);

        // NOVO BOTÃO PERÍODO
        JButton btnSearchPeriodo = new JButton("🗓️ Período");
        btnSearchPeriodo.addActionListener(this::onSearchPeriodo);

        JButton btnSearchNome = new JButton("🔍 Nome");
        btnSearchNome.addActionListener(this::onSearchNome);

        // --- CRIAÇÃO DO FILTRO DE MÊS ---
        comboMeses = new JComboBox<>();
        comboMeses.addItem("Todos"); // Opção padrão
        
        // Carrega os meses do banco
        List<String> mesesDb = contaRepo.listarMesesDisponiveis();
        for (String m : mesesDb) {
            comboMeses.addItem(m);
        }

        // Ação quando trocar o mês no ComboBox
        comboMeses.addActionListener(e -> {
            String selecionado = (String) comboMeses.getSelectedItem();
            if ("Todos".equals(selecionado)) {
                refreshTable(contaRepo.listarTodas());
            } else {
                refreshTable(contaRepo.buscarPorMesReferencia(selecionado));
            }
        });

        // --- Layout (Toolbar Superior) ---
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(btnAdd);
        toolbar.add(btnLaunch);
        toolbar.addSeparator();
        
        // Adiciona o label e o combo na toolbar
        toolbar.add(new JLabel("  Filtrar Mês: ")); 
        toolbar.add(comboMeses);
        
        toolbar.addSeparator();
        toolbar.add(btnRemove);
        toolbar.add(btnRefresh);
        toolbar.addSeparator();
        
        // --- Painel de Filtros (Abaixo da Toolbar) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filtros Rápidos"));
        filterPanel.add(btnSearchActivity);
        filterPanel.add(btnSearchMes);
        filterPanel.add(btnSearchPeriodo); // Adicionado aqui
        filterPanel.add(btnSearchNome);

        // Painel Topo Geral
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(toolbar, BorderLayout.NORTH);
        topContainer.add(filterPanel, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topContainer, BorderLayout.NORTH);
        getContentPane().add(scroll, BorderLayout.CENTER);

        // Barra de Status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel statusLabel = new JLabel("Selecione as contas para ver a soma");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusPanel.add(statusLabel);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateSelectionSum(statusLabel);
        });

        // --- INICIALIZAÇÃO: Carregar Mês Atual ---
        carregarMesAtual();
    }
    
    // --- LÓGICA DE INICIALIZAÇÃO ---
    private void carregarMesAtual() {
        LocalDate hoje = LocalDate.now();
        String mes = String.format("%02d", hoje.getMonthValue());
        String ano = String.format("%02d", hoje.getYear() % 100); // Pega os ultimos 2 digitos do ano (ex: 2025 -> 25)
        
        List<Contas> contasMes = contaRepo.buscarPorMesVencimento(mes, ano);
        refreshTable(contasMes);
        
        // Exibe um titulo na janela ou console para feedback
        this.setTitle("Gerenciador de Contas - Exibindo Mês: " + mes + "/" + ano);
    }
    
    // Método auxiliar para recarregar o combo após cadastrar algo novo
    private void atualizarComboMeses() {
        String atual = (String) comboMeses.getSelectedItem();
        comboMeses.removeAllItems();
        comboMeses.addItem("Todos");
        
        List<String> mesesDb = contaRepo.listarMesesDisponiveis();
        for (String m : mesesDb) {
            comboMeses.addItem(m);
        }
        
        // Tenta manter a seleção anterior
        if (atual != null) {
            comboMeses.setSelectedItem(atual);
        }
    }

    // --- Método Helper para Criar Botões Modernos ---
    private JButton criarBotao(String texto, String emoji, Color corFundo) {
        JButton btn = new JButton(texto);
        // Tenta usar emoji como ícone se não tiver imagem real
        btn.setIcon(new TextIcon(btn, emoji, 16)); 
        
        if (corFundo != null) {
            // Estilo específico do FlatLaf para botões coloridos
            // Isso deixa o botão arredondado e com a cor escolhida
            btn.setBackground(corFundo);
            btn.setForeground(Color.WHITE);
        }
        return btn;
    }

    // --- Formulário Moderno de Cadastro ---
    private void onAdd(ActionEvent e) {
        int proximoId = contaRepo.buscarUltimoId() + 1;

        JTextField txtId = new JTextField(String.valueOf(proximoId));
        txtId.setEditable(false);
        
        JTextField txtFornecedor = new JTextField();
        JTextField txtServico = new JTextField();
        JTextField txtCnpjServico = new JTextField();
        JTextField txtUnidade = new JTextField();
        JTextField txtValorNF = new JTextField();
        JTextField txtValorBoleto = new JTextField();
        JTextField txtCentroCusto = new JTextField();
        
        // SUBSTITUIÇÃO: JDateChooser ao invés de JTextField
        JDateChooser dateVencimento = new JDateChooser();
        dateVencimento.setDateFormatString("dd/MM/yyyy");
        
        // Opcionais
        JDateChooser dateFaturamento = new JDateChooser();
        dateFaturamento.setDateFormatString("dd/MM/yyyy");
        
        JDateChooser dateLancamento = new JDateChooser();
        dateLancamento.setDateFormatString("dd/MM/yyyy");
        
        JTextField txtAtividade = new JTextField();
        JTextField txtContaContabil = new JTextField();

        // Painel com Layout Alinhado
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(500, 450));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Helper para adicionar linhas ao form
        addFormRow(panel, gbc, 0, "ID:", txtId, false);
        addFormRow(panel, gbc, 1, "Fornecedor *:", txtFornecedor, true);
        addFormRow(panel, gbc, 2, "Serviço *:", txtServico, true);
        addFormRow(panel, gbc, 3, "CNPJ Filial *:", txtCnpjServico, true);
        addFormRow(panel, gbc, 4, "Unidade *:", txtUnidade, true);
        addFormRow(panel, gbc, 5, "Valor NF *:", txtValorNF, true);
        addFormRow(panel, gbc, 6, "Valor Boleto *:", txtValorBoleto, true);
        addFormRow(panel, gbc, 7, "Vencimento *:", dateVencimento, true); // Calendário
        addFormRow(panel, gbc, 8, "Centro Custo *:", txtCentroCusto, true);
        
        // Divisória visual
        JSeparator sep = new JSeparator();
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        panel.add(sep, gbc);
        
        addFormRow(panel, gbc, 10, "Faturamento:", dateFaturamento, false);
        addFormRow(panel, gbc, 11, "Lançamento:", dateLancamento, false);
        addFormRow(panel, gbc, 12, "Atividade:", txtAtividade, false);
        addFormRow(panel, gbc, 13, "Conta Contábil:", txtContaContabil, false);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Nova Conta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // --- VALIDAÇÃO VISUAL ---
            boolean temErro = false;
            temErro |= validarCampo(txtFornecedor);
            temErro |= validarCampo(txtServico);
            temErro |= validarCampo(txtCnpjServico);
            temErro |= validarCampo(txtUnidade);
            temErro |= validarCampo(txtValorNF);
            temErro |= validarCampo(txtValorBoleto);
            temErro |= validarCampo(txtCentroCusto);
            temErro |= validarCalendario(dateVencimento);

            if (temErro) {
                JOptionPane.showMessageDialog(this, "Preencha os campos destacados em vermelho.", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Contas novaConta = new Contas();
                novaConta.setFornecedor(txtFornecedor.getText().trim());
                novaConta.setServicoProduto(txtServico.getText().trim());
                novaConta.setCnpjFilial(txtCnpjServico.getText().trim());
                novaConta.setUnidadeGd(txtUnidade.getText().trim());
                
                novaConta.setValorNF(Double.parseDouble(txtValorNF.getText().replace(",", ".")));
                novaConta.setValorBoleto(Double.parseDouble(txtValorBoleto.getText().replace(",", ".")));
                
                // Pegando a data do componente e formatando para String (dd-MM-yy)
                if(dateVencimento.getDate() != null)
                    novaConta.setVencimento(dateFormatDB.format(dateVencimento.getDate()));
                
                if(dateFaturamento.getDate() != null)
                    novaConta.setDataFaturamento(dateFormatDB.format(dateFaturamento.getDate()));
                
                if(dateLancamento.getDate() != null)
                    novaConta.setDataLancamento(dateFormatDB.format(dateLancamento.getDate()));
                
                String ativStr = txtAtividade.getText().trim();
                novaConta.setAtividade(ativStr.isEmpty() ? 0 : Integer.parseInt(ativStr));
                
                novaConta.setCentroCusto(txtCentroCusto.getText().trim());
                novaConta.setContaContabil(txtContaContabil.getText().trim());
                
                contaRepo.adicionarConta(novaConta);
                atualizarComboMeses(); // Atualiza o combo após adicionar
                
                JOptionPane.showMessageDialog(this, "Conta cadastrada com sucesso!");
                refreshTable(contaRepo.listarTodas());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro nos dados: " + ex.getMessage());
            }
        }
    }

    // --- Métodos de Validação Visual (FlatLaf Feature) ---
    
    private boolean validarCampo(JTextField campo) {
        if (campo.getText().trim().isEmpty()) {
            // FlatLaf outline error - deixa a borda vermelha
            campo.putClientProperty("JComponent.outline", "error");
            return true; // Tem erro
        } else {
            campo.putClientProperty("JComponent.outline", null);
            return false; // Sem erro
        }
    }

    private boolean validarCalendario(JDateChooser chooser) {
        if (chooser.getDate() == null) {
            chooser.putClientProperty("JComponent.outline", "error"); // Tenta pintar o chooser
            chooser.getDateEditor().getUiComponent().putClientProperty("JComponent.outline", "error"); // Pinta o campo interno
            return true;
        } else {
            chooser.getDateEditor().getUiComponent().putClientProperty("JComponent.outline", null);
            return false;
        }
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp, boolean obrigatorio) {
        gbc.gridx = 0; 
        gbc.gridy = row; 
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        if (obrigatorio) lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        panel.add(lbl, gbc);

        gbc.gridx = 1; 
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }
    
    // --- Lógica Existente (Mantida) ---
    private String formatarDataUI(String data) {
        if (data == null || data.trim().isEmpty()) return "";
        return data.replace("-", "/");
    }

    private String formatarValor(double valor){
        java.text.NumberFormat formato = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt","BR"));
        return formato.format(valor);
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void refreshTable(List<Contas> contas) {
        this.listaAtual = contas;
        tableModel.setRowCount(0);
        for (Contas c : contas) {
            // IMPORTANTE: Agora passamos os objetos REAIS (Double, Integer) para a tabela
            // para que a ordenação funcione corretamente. O renderizador padrão cuidará de exibir.
            // Mas para manter a formatação R$ bonita, podemos formatar na hora de exibir ou criar um CellRenderer.
            // Para simplificar e manter a ordenação, vamos passar o valor numérico e deixar a tabela exibir.
            // SE quiser formatar E ordenar, precisa de um CellRenderer customizado.
            // Vamos manter simples: Passamos o valor formatado (String) para colunas de texto,
            // e valor numérico (Double) para colunas de valor.
            
            // Mas espere, se eu passar Double, ele vai mostrar "1000.0". O usuário quer "R$ 1.000,00".
            // O jeito certo é passar Double e setar um Renderer.
            // Como o usuário pediu "ordenar como planilha", vou priorizar a ordenação correta.
            // Vou passar Double e a tabela vai mostrar o número.
            
            Object[] row = new Object[] {
                    safe(c.getFornecedor()),
                    safe(c.getServicoProduto()),
                    c.getValorNF(),      // Passando Double puro
                    c.getValorBoleto(),  // Passando Double puro
                    formatarDataUI(c.getVencimento()),
                    formatarDataUI(c.getDataLancamento()),
                    c.getAtividade()     // Passando Integer puro
            };
            tableModel.addRow(row);
        }
    }

    private void onShowInfo(ActionEvent e) {
        int sel = table.getSelectedRow();
        if (sel == -1) return;
        
        // Conversão de índice da View (ordenada) para Model (dados originais)
        int modelRow = table.convertRowIndexToModel(sel);
        
        if (modelRow >= 0 && modelRow < listaAtual.size()) {
            mostrarDetalhesConta(listaAtual.get(modelRow));
        }
    }

    private void mostrarDetalhesConta(Contas conta) {
        JDialog dialog = new JDialog(this, "Detalhes Completos da Conta", true);
        dialog.setSize(450, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createInfoLabel("ID (Banco):", String.valueOf(conta.getId())));
        panel.add(createInfoLabel("Fornecedor:", conta.getFornecedor()));
        panel.add(createInfoLabel("Serviço:", conta.getServicoProduto()));
        panel.add(createInfoLabel("CNPJ Filial:", conta.getCnpjFilial()));
        panel.add(createInfoLabel("Unidade:", conta.getUnidadeGd()));
        panel.add(createInfoLabel("Valor NF:", formatarValor(conta.getValorNF())));
        panel.add(createInfoLabel("Valor Boleto:", formatarValor(conta.getValorBoleto())));
        
        panel.add(createInfoLabel("Data Faturamento:", formatarDataUI(conta.getDataFaturamento())));
        panel.add(createInfoLabel("Data Vencimento:", formatarDataUI(conta.getVencimento())));
        panel.add(createInfoLabel("Data Lançada:", formatarDataUI(conta.getDataLancamento())));
        
        panel.add(createInfoLabel("Atividade:", String.valueOf(conta.getAtividade())));
        
        panel.add(createInfoLabel("Lançada:", conta.isLancada() ? "Sim" : "Não"));
        panel.add(createInfoLabel("Vencida:", conta.isVencida() ? "Sim" : "Não"));
        
        panel.add(createInfoLabel("Centro de Custo:", conta.getCentroCusto()));
        panel.add(createInfoLabel("Conta Contábil:", conta.getContaContabil()));

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(evt -> dialog.dispose());

        // --- BOTÃO RESTAURADO ---
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

    private void updateSelectionSum(JLabel statusLabel) {
        double totalNF = 0.0;
        double totalBoleto = 0.0;
        int selectedCount = table.getSelectedRowCount();
        if (selectedCount > 0) {
            int[] selectedRows = table.getSelectedRows();
            for (int viewRow : selectedRows) {
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

    private void onRemove(ActionEvent e) {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Selecione para remover.");
            return;
        }
        
        // Conversão de índice View -> Model
        int modelRow = table.convertRowIndexToModel(sel);
        Contas conta = listaAtual.get(modelRow);
        
        int resp = JOptionPane.showConfirmDialog(this, "Remover " + conta.getFornecedor() + "?");
        if (resp == JOptionPane.YES_OPTION) {
            contaRepo.removerConta(conta.getId());
            refreshTable(contaRepo.listarTodas());
        }
    }

    private void onLaunch(ActionEvent e) {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Selecione para lançar.");
            return;
        }
        
        // Conversão de índice View -> Model
        int modelRow = table.convertRowIndexToModel(sel);
        Contas conta = listaAtual.get(modelRow);
        
        // --- FORMULÁRIO DE LANÇAMENTO ---
        JTextField txtAtividade = new JTextField(String.valueOf(conta.getAtividade() != 0 ? conta.getAtividade() : ""));
        JTextField txtValorBoleto = new JTextField(String.valueOf(conta.getValorBoleto()).replace(".", ","));
        
        JDateChooser dateFaturamento = new JDateChooser();
        dateFaturamento.setDateFormatString("dd/MM/yyyy");
        // Tenta preencher se já existir
        try {
            if (conta.getDataFaturamento() != null && !conta.getDataFaturamento().isEmpty()) {
                dateFaturamento.setDate(dateFormatDB.parse(conta.getDataFaturamento()));
            }
        } catch (Exception ignored) {}

        JDateChooser dateLancamento = new JDateChooser();
        dateLancamento.setDateFormatString("dd/MM/yyyy");
        // Preenche com data atual por padrão
        dateLancamento.setDate(new Date());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setPreferredSize(new Dimension(400, 200));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addFormRow(panel, gbc, 0, "Atividade:", txtAtividade, true);
        addFormRow(panel, gbc, 1, "Valor Boleto:", txtValorBoleto, true);
        addFormRow(panel, gbc, 2, "Data Faturamento:", dateFaturamento, false);
        addFormRow(panel, gbc, 3, "Data Lançamento:", dateLancamento, true);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Lançar Conta", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Validação básica
                if (txtAtividade.getText().trim().isEmpty()) throw new Exception("Atividade obrigatória.");
                
                conta.setAtividade(Integer.parseInt(txtAtividade.getText().trim()));
                conta.setValorBoleto(Double.parseDouble(txtValorBoleto.getText().replace(",", ".")));
                
                if (dateFaturamento.getDate() != null) {
                    conta.setDataFaturamento(dateFormatDB.format(dateFaturamento.getDate()));
                }
                
                // Se usuário não escolheu data, usa a data atual (que já vem setada no componente, mas garantindo)
                Date dataLanc = dateLancamento.getDate();
                if (dataLanc == null) dataLanc = new Date();
                conta.setDataLancamento(dateFormatDB.format(dataLanc));

                conta.setLancada(true);
                contaRepo.atualizarConta(conta);
                refreshTable(contaRepo.listarTodas());
                JOptionPane.showMessageDialog(this, "Conta lançada com sucesso!");
                
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao lançar: " + ex.getMessage());
            }
        }
    }

    // --- Métodos de Busca (Simplificados para manter o exemplo conciso) ---
    private void onSearchActivity(ActionEvent e) {
        String s = JOptionPane.showInputDialog("Número Atividade:");
        if (s != null) refreshTable(contaRepo.buscarPorAtividade(Integer.parseInt(s)));
    }
    private void onSearchNome(ActionEvent e) {
        String s = JOptionPane.showInputDialog("Nome:");
        if (s != null) refreshTable(contaRepo.buscarPorNome(s));
    }
    private void onSearchMes(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        // Pega data atual para sugerir
        LocalDate hoje = LocalDate.now();
        JTextField txtMes = new JTextField(String.format("%02d", hoje.getMonthValue()));
        JTextField txtAno = new JTextField(String.format("%02d", hoje.getYear() % 100));

        panel.add(new JLabel("Mês (1-12):")); panel.add(txtMes);
        panel.add(new JLabel("Ano (Ex: 25):")); panel.add(txtAno);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Filtrar por Mês", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String mes = txtMes.getText().trim();
            String ano = txtAno.getText().trim();
            
            // Garante formato 2 digitos
            if(mes.length() == 1) mes = "0" + mes;
            
            if (!mes.isEmpty() && !ano.isEmpty()) {
                refreshTable(contaRepo.buscarPorMesVencimento(mes, ano));
                this.setTitle("Gerenciador de Contas - Filtro Mês: " + mes + "/" + ano);
            }
        }
    }

    // NOVO: Busca por Período de Vencimento
    private void onSearchPeriodo(ActionEvent e) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        JDateChooser dataInicio = new JDateChooser();
        JDateChooser dataFim = new JDateChooser();
        
        // Define formato visual
        dataInicio.setDateFormatString("dd/MM/yyyy");
        dataFim.setDateFormatString("dd/MM/yyyy");
        
        // Define data atual como sugestão
        dataInicio.setDate(new Date());
        dataFim.setDate(new Date());

        panel.add(new JLabel("Data Início:")); panel.add(dataInicio);
        panel.add(new JLabel("Data Fim:")); panel.add(dataFim);

        int result = JOptionPane.showConfirmDialog(this, panel, 
                "Filtrar por DATA DE VENCIMENTO", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Date inicio = dataInicio.getDate();
            Date fim = dataFim.getDate();

            if (inicio != null && fim != null) {
                if (inicio.after(fim)) {
                    JOptionPane.showMessageDialog(this, "A data de início não pode ser maior que a data fim.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                List<Contas> filtradas = contaRepo.buscarPorPeriodoVencimento(inicio, fim);
                
                refreshTable(filtradas);
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                this.setTitle("Gerenciador - Filtrando por VENCIMENTO: " + sdf.format(inicio) + " até " + sdf.format(fim));
                
            } else {
                JOptionPane.showMessageDialog(this, "Selecione ambas as datas.");
            }
        }
    }

    // --- Main ---
    public static void main(String[] args) {
        // INSTALAÇÃO DO TEMA MODERNO (FlatLaf)
        try {
            FlatLightLaf.setup();
        } catch (Exception ex) {
            System.err.println("Falha ao carregar FlatLaf. Usando padrão.");
        }

        SwingUtilities.invokeLater(() -> new ContaManagerGUI().setVisible(true));
    }

    // --- Classe Interna para simular ícone com Texto/Emoji ---
    private static class TextIcon implements Icon {
        private final String text;
        private final int width;
        private final int height;
        private final Component component;

        public TextIcon(Component component, String text, int size) {
            this.component = component;
            this.text = text;
            this.width = size;
            this.height = size;
        }
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, height));
            g2.drawString(text, x, y + height - 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
    }
}
