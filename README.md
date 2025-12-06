# Gerenciador de Contas (Projeto Contas)

Sistema robusto de gerenciamento financeiro desenvolvido em Java, utilizando Swing com tema moderno (FlatLaf) e arquitetura MVC. O projeto oferece controle total sobre contas a pagar e receber, com recursos avançados de filtragem e exportação.

## 🚀 Funcionalidades Principais

### 📋 Gestão de Contas

- **Cadastro Completo:** Registro detalhado com campos para fornecedor, valores, datas, centro de custo, e mais.
- **Edição e Exclusão:** Gerenciamento fácil de registros existentes.
- **Lançamento Rápido:** Funcionalidade para marcar contas como lançadas e definir atividades.

### 🔍 Filtros Avançados e Dinâmicos

- **Sistema de Chips (Tags):** Filtros ativos são exibidos como etiquetas removíveis, permitindo combinações complexas.
- **Filtro por Período Flexível:** Filtragem por intervalo de datas em três categorias:
  - 📅 Data de Vencimento
  - 💲 Data de Faturamento
  - 🚀 Data de Lançamento
- **Filtro por Mês/Ano:** Seleção rápida de competência (ex: Dezembro/2025).
- **Busca Textual:** Pesquisa por nome de fornecedor.
- **Busca por Atividade:** Filtro específico por número de atividade.

### 📊 Relatórios e Exportação

- **Exportação para Excel (.xlsx):** Gere relatórios profissionais com um clique.
  - Opção de exportar toda a base ou apenas um período específico.
  - Formatação automática de moeda e cabeçalhos.
- **Cálculos em Tempo Real:** Soma automática dos valores (NF e Boleto) das contas listadas/filtradas.

## 🛠️ Arquitetura e Tecnologias

O projeto foi refatorado para seguir o padrão **MVC (Model-View-Controller)**, garantindo organização e manutenibilidade.

### Estrutura de Pacotes (`src/main/java/org/example`)

- **`model`**: Entidades do sistema (`Contas.java`).
- **`view`**: Interface gráfica (`ContaManagerGUI.java`).
- **`repository`**: Acesso a dados e operações SQL (`ContaRepositorySQLite.java`, `DatabaseConnection.java`).
- **`util`**: Utilitários auxiliares (`ExcelExporter.java`).
- **`test`**: Classes de teste (`TestFilter.java`).

### Stack Tecnológico

- **Java 11+**: Linguagem base.
- **Swing + FlatLaf**: Interface gráfica moderna e responsiva.
- **SQLite**: Banco de dados local, leve e sem necessidade de servidor.
- **Apache POI**: Biblioteca para geração de arquivos Excel.
- **JCalendar**: Componentes de calendário para seleção de datas.
- **Maven**: Gerenciamento de dependências e build.

## ▶️ Como Executar

### Pré-requisitos

- Java JDK 11 ou superior.
- Maven.

### Passos

1. Clone o repositório.
2. Na raiz do projeto, execute via terminal ou IDE:

   ```bash
   mvn clean install
   mvn exec:java
   ```

   _Ou execute manualmente a classe principal:_ `org.example.view.ContaManagerGUI`

3. O banco de dados `contas.db` será criado automaticamente na raiz do projeto na primeira execução.

---
