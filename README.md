````
src/
└── br/processador/
├── Main.java
├── config/
│    └── ConfigLoader.java
├── db/
│    ├── ConnectionFactory.java
│    ├── Dialect.java
├── service/
│    └── ConsultaExecutor.java
└── util/
└── TextSanitizer.java
````

### Dialect.java — Enum para tipos de banco

- Centraliza dados do driver JDBC e URL para cada banco suportado.

- Facilita a criação da conexão dinâmica.

- Permite adicionar bancos novos no futuro só adicionando mais entradas nesse enum.

### ConnectionFactory.java — Fabrica conexões genéricas

- Carrega o driver JDBC do banco escolhido.

- Cria uma conexão java.sql.Connection usando dados dinâmicos (host, porta, banco, usuário, senha).

- Isola essa lógica para facilitar manutenção e testes.

### TextSanitizer.java — Utilitário para limpar texto

- Recebe uma string, remove acentuação (útil para evitar problemas de encoding).

- Troca tabs e quebras de linha por espaço, garantindo saída limpa.

- Garante que não haja espaços extras desnecessários.

### ConsultaExecutor.java — Executa as consultas e grava resultado

- Recebe a pasta com arquivos .txt.

- Para cada arquivo:

- - Lê a primeira linha (espera um SELECT).

- - Executa a consulta no banco.

- - Grava resultado formatado em um arquivo .resultado.txt.

- Usa try-with-resources para garantir fechamento de streams e conexões.

- Sanitiza texto para saída limpa.

- Exibe mensagens úteis no console para acompanhamento.