package org.example;

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