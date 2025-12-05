# Gerenciador de Contas (Projeto Contas)

Este é um sistema de gerenciamento de contas desenvolvido em Java, utilizando Swing para a interface gráfica e SQLite para persistência de dados. O objetivo principal é facilitar o controle de contas a pagar e receber, permitindo cadastro, visualização, filtragem e manipulação de registros financeiros.

## 🚀 Funcionalidades

- **Cadastro de Contas:** Formulário completo para adição de novas contas com validação de campos obrigatórios.
- **Listagem:** Visualização tabular das contas cadastradas.
- **Busca e Filtros:**
  - Busca por **Nome do Fornecedor**.
  - Busca por **Número da Atividade**.
  - Busca por **Data de Lançamento**.
  - Busca por **Data de Vencimento**.
  - **Novo:** Busca por **Mês e Ano de Vencimento** (ex: todas as contas de Dezembro/25).
- **Gestão de Status:**
  - Lançamento de contas (definir atividade e marcar como lançada).
  - Visualização de detalhes completos com duplo clique.
- **Cálculos:** Soma automática dos valores (NF e Boleto) das contas selecionadas na tabela.
- **Persistência:** Todos os dados são salvos automaticamente em um banco de dados SQLite local (`contas.db`).

## 🛠️ Tecnologias Utilizadas

- **Java:** Linguagem principal (JDK 8+).
- **Swing:** Biblioteca gráfica para construção da interface (GUI).
- **SQLite:** Banco de dados relacional leve e local.
- **JDBC:** Conectividade com o banco de dados.
- **Maven:** Gerenciamento de dependências e build.

## 📦 Estrutura do Projeto

- `src/main/java/org/example/Contas.java`: Classe principal contendo a interface gráfica (`ContaManagerGUI`) e o modelo de dados (`Contas`).
- `src/main/java/org/example/ContaRepositorySQLite.java`: Camada de acesso a dados (DAO/Repository), responsável por todas as operações SQL.
- `src/main/java/org/example/DatabaseConnection.java`: Singleton para gerenciamento da conexão com o banco SQLite.

## ▶️ Como Executar

### Pré-requisitos

- Java JDK instalado.
- Maven instalado (opcional, caso use IDE com suporte nativo).

### Passos

1.  Clone o repositório.
2.  Abra o projeto em sua IDE de preferência (IntelliJ, Eclipse, VS Code).
3.  Certifique-se de que as dependências do Maven foram baixadas (especialmente o driver do SQLite).
4.  Execute a classe `org.example.Contas`.

O arquivo do banco de dados `contas.db` será criado/lido automaticamente na raiz do projeto.