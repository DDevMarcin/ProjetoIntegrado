# Guia de Integração Frontend ↔ Backend

Este documento apresenta como executar o backend do Sistema de Gestão para Artesã, como consumir a API REST e quais formatos de dados devem ser utilizados pelo frontend.

---

# 1. Tecnologias

O backend utiliza:

- Java 21
- Spring Boot 3.3.4
- Spring Web
- Spring Data JPA
- Hibernate
- SQLite
- Maven

O banco de dados utilizado atualmente é o SQLite, através do arquivo:

```text
artesa.db
```

A API REST é executada na porta: 8080

URL base: http://localhost:8080

# 2. Executar

Dentro da raiz do projeto, execute o comando:

```text
mvn spring-boot:run
```

Quando o Spring Boot estiver iniciado, a API estará disponível. 

# 3. Banco de Dados

O Sistema usa o SQLite.

Arquivo do banco:

```text
artesa.db
```
O banco possui atualmente as principais tabelas:

```text
material
produto
produto_material
```

A tabela produto_material representa a associação entre produtos e materiais.

# 4. API de Materiais

A API de materiais possui as seguintes operações:

```text
POST   /materiais
GET    /materiais
GET    /materiais?nome=...&unidadeMedida=...
GET    /materiais/buscar?nome=...
GET    /materiais/{id}
PUT    /materiais/{id}
DELETE /materiais/{id}
```

## 4.1 Cadastrar Material

### Endpoint

```text
POST /materiais
```

### Body

```json
{
  "nome": "Contas de madeira",
  "cor": "Marrom",
  "precoUnidade": 0.50,
  "unidadeMedida": "un",
  "quantidadeEstoque": 100
}
```

### Campos

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `nome` | String | Sim | Nome do material |
| `cor` | String | Sim | Cor do material |
| `precoUnidade` | BigDecimal | Sim | Preço por unidade de medida |
| `unidadeMedida` | String | Sim | Unidade utilizada para controle do material |
| `quantidadeEstoque` | BigDecimal | Não | Quantidade inicial em estoque. Caso não seja informada, o valor padrão é `0` |

### Resposta

O backend retorna o material cadastrado com status:

```text
201 Created
```

Exemplo:

```json
{
  "idMaterial": 1,
  "nome": "Contas de madeira",
  "cor": "Marrom",
  "precoUnidade": 0.50,
  "unidadeMedida": "un",
  "quantidadeEstoque": 100
}
```

# 5. Listar Materiais

### Endpoint

```text
GET /materiais
```

Exemplo:

```bash
curl http://localhost:8080/materiais
```

### Resposta

```json
[
  {
    "idMaterial": 1,
    "nome": "Contas de madeira",
    "cor": "Marrom",
    "precoUnidade": 0.50,
    "unidadeMedida": "un",
    "quantidadeEstoque": 100
  },
  {
    "idMaterial": 2,
    "nome": "Tecido",
    "cor": "Azul",
    "precoUnidade": 25.50,
    "unidadeMedida": "metro",
    "quantidadeEstoque": 10
  }
]
```

Caso não existam materiais cadastrados, o backend retorna uma lista vazia:

```json
[]
```

com status:

```text
200 OK
```

# 6. Buscar Material por ID

### Endpoint

```text
GET /materiais/{id}
```

Exemplo:

```text
GET /materiais/1
```

### Resposta

```json
{
  "idMaterial": 1,
  "nome": "Contas de madeira",
  "cor": "Marrom",
  "precoUnidade": 0.50,
  "unidadeMedida": "un",
  "quantidadeEstoque": 100
}
```

Se o material não existir:

```text
404 Not Found
```

# 6.1 Buscar Material por Nome

### Endpoint

```text
GET /materiais/buscar?nome=madeira
```

Também é possível utilizar o parâmetro na listagem:

```text
GET /materiais?nome=madeira
```

O backend procura materiais cujo nome contenha o texto informado, sem diferenciar letras maiúsculas e minúsculas.

Exemplo de resposta:

```json
[
  {
    "idMaterial": 1,
    "nome": "Contas de madeira",
    "cor": "Marrom",
    "precoUnidade": 0.50,
    "unidadeMedida": "un",
    "quantidadeEstoque": 100
  }
]
```

Caso não haja materiais correspondentes, o retorno é:

```json
[]
```

com status `200 OK`.

# 6.2 Filtrar por Unidade de Medida

Endpoint:

```text
GET /materiais?unidadeMedida=un
```

Também é possível combinar filtros:

```text
GET /materiais?nome=contas&unidadeMedida=un
```

# 7. Validações de Material

O backend valida os seguintes campos antes de persistir o material:

```text
Nome
- Obrigatório.
- Não pode ser nulo ou em branco.
- Tamanho máximo de 255 caracteres.

Cor
- Obrigatória.
- Não pode ser nula ou em branco.
- Tamanho máximo de 50 caracteres.

Preço
- Obrigatório.
- Não pode ser nulo.
- Não pode ser negativo.

Unidade de medida
- Obrigatória.
- Não pode ser nula ou em branco.
- Tamanho máximo de 10 caracteres.

Quantidade em estoque
- Caso não seja informada no cadastro, assume o valor 0.
```

# 7.1 Atualizar Material

### Endpoint

```text
PUT /materiais/{id}
```

Exemplo:

```text
PUT /materiais/1
```

### Body

```json
{
  "nome": "Contas de madeira nobre",
  "cor": "Marrom escuro",
  "precoUnidade": 0.75,
  "unidadeMedida": "un"
}
```

### Resposta

Retorna o material atualizado com status:

```text
200 OK
```

Se o ID não existir:

```text
404 Not Found
```

# 7.2 Excluir Material

### Endpoint

```text
DELETE /materiais/{id}
```

Exemplo:

```text
DELETE /materiais/1
```

Se excluído com sucesso:

```text
204 No Content
```

Se o material não existir:

```text
404 Not Found
```

Se o material estiver vinculado a um ou mais produtos por meio de `produto_material`, a exclusão é impedida para preservar a integridade dos dados.
# 8. API de Produtos

A API de produtos possui atualmente:

```text
POST   /produtos
GET    /produtos
GET    /produtos/{id}
GET    /produtos/buscar?nome=...
GET    /produtos/total-estoque
PUT    /produtos/{id}
DELETE /produtos/{id}
```

# 9.Cadastrar Produto

Endpoint

```text
    POST /produtos
```

O cadastro utiliza o seguinte formato:

    {
    "codigo": "001",
    "nome": "Terço dos homens",
    "descricao": "Terço artesanal...",
    "tipo": "PRE_PRONTO",
    "quantidadeEstoque": 10,
    "prazoProducaoDias": null,
    "observacoes": null,
    "calcularManualmente": false,
    "valorManual": null,
    "materiais": [
    {
    "idMaterial": 1,
    "quantidadeUtilizada": 2
    }
    ]
    }

# 10. Campos do cadastro de Produto

O objeto enviado para:

POST /produtos

possui os seguintes campos:

```text
Campo	Tipo	Descrição

codigo	String	Código único do produto
nome	String	Nome do produto
descricao	String	Descrição do produto
tipo	String	PRE_PRONTO ou PERSONALIZADO
quantidadeEstoque	Integer	Quantidade em estoque
prazoProducaoDias	Integer	Prazo de produção
observacoes	String	Observações do produto
calcularManualmente	Boolean	Define se o valor será informado manualmente
valorManual	BigDecimal	Valor informado manualmente
materiais	Array	Materiais utilizados no produto
```
# 11. Materiais utilizados no Produto

Cada item dentro de:

"materiais": []

possui:

    {
    "idMaterial": 1,
    "quantidadeUtilizada": 2
    }

Onde:

idMaterial identifica o material cadastrado;
quantidadeUtilizada representa quanto desse material é utilizado no produto.

O backend busca o material pelo ID e calcula o valor com base no preço cadastrado.

# 12. Exemplo de Produto Pré-Pronto

    {
    "codigo": "001",
    "nome": "Terço de madeira",
    "descricao": "Terço artesanal feito com madeira",
    "tipo": "PRE_PRONTO",
    "quantidadeEstoque": 10,
    "prazoProducaoDias": null,
    "observacoes": null,
    "calcularManualmente": false,
    "valorManual": null,
    "materiais": [
    {
    "idMaterial": 1,
    "quantidadeUtilizada": 20
    }
    ]
    }

Nesse caso:

tipo = PRE_PRONTO

e o backend utiliza:

quantidadeEstoque

# 13. Exemplo de Produto Personalizado
    {
    "codigo": "002",
    "nome": "Terço personalizado azul",
    "descricao": "Terço artesanal personalizada...",
    "tipo": "PERSONALIZADO",
    "quantidadeEstoque": null,
    "prazoProducaoDias": 7,
    "observacoes": "Produção sob demanda",
    "calcularManualmente": false,
    "valorManual": null,
    "materiais": [
    {
    "idMaterial": 1,
    "quantidadeUtilizada": 50
    }
    ]
    }

Nesse caso:

tipo = PERSONALIZADO

e o backend utiliza:

```text
prazoProducaoDias
observacoes
```

# 14. Produto Personalizado com Valor Manual

Também é possível cadastrar um produto personalizado informando diretamente o valor.

Exemplo:
    
    {
        "codigo": "003",
        "nome": "Rosario personalizada",
        "descricao": "Rosario personalizado com contas vermelhas e medalha de prata",
        "tipo": "PERSONALIZADO",
        "quantidadeEstoque": null,
        "prazoProducaoDias": 10,
        "observacoes": "Produção sob encomenda",
        "calcularManualmente": true,
        "valorManual": 150.00,
        "materiais": []
    }

Nesse caso, o backend utiliza:

valorManual = 150.00

como valor do produto.

# 15. Regras importantes do cadastro

O backend exige:

```text
código do produto;
nome do produto;
tipo do produto.
```

Quando o valor for informado manualmente:

```text
calcularManualmente = true
```

é necessário informar:

```text
valorManual > 0
```

Quando o valor não for informado manualmente:

```text
calcularManualmente = false
```

é necessário informar pelo menos um material.

A quantidade utilizada de cada material também deve ser maior que zero.

# 16. Resposta do cadastro de Produto

Quando o produto é cadastrado com sucesso, o backend retorna:

```text
201 Created
```

Exemplo:

    {
        "idProduto": 1,
        "codigo": "001",
        "nome": "Terço dos homens",
        "descricao": "terço atersanal com 50 contas...",
        "tipo": "PRE_PRONTO",
        "valor": 20.00,
        "valorManual": false,
        "quantidadeEstoque": 10,
        "prazoProducaoDias": null,
        "observacoes": null,
        "materiaisUtilizados": [
        {
        "material": {
        "idMaterial": 1,
        "nome": "Contas de madeira",
        "precoUnidade": 0.50,
        "unidadeMedida": "un"
        },
        "quantidadeUtilizada": 50
        }
        ]
    }

# 17. Listar Produtos

Endpoint

```text
GET /produtos
```

Exemplo:

```text
curl http://localhost:8080/produtos
```

Retorna uma lista de produtos cadastrados.

# 18. Buscar Produto por ID

Endpoint

```text
GET /produtos/{id}
```

Exemplo:

```text
GET /produtos/1
```

Se o produto existir, retorna:

```text
200 OK
```

Se não existir:

```text
404 Not Found
```

# 19. Buscar Produto por Nome

Endpoint
```text
GET /produtos/buscar?nome=terço
```

O backend procura produtos cujo nome contenha o texto informado.

Exemplo:

```text
GET /produtos/buscar?nome=Bolsa
```

Pode retornar:

    [
        {
        "idProduto": 1,
        "codigo": "001",
        "nome": "Terço dos homens",
        "descricao": "Terço atersanal, 1 medalha...",
        "tipo": "PRE_PRONTO",
        "valor": 50.00,
        "valorManual": false,
        "quantidadeEstoque": 10,
        "prazoProducaoDias": null,
        "observacoes": null,
        "materiaisUtilizados": []
        }
    ]

A busca não precisa ser uma correspondência exata.

# 20. Consultar Total em Estoque

Endpoint

```text
GET /produtos/total-estoque
```

Exemplo:

```text
curl http://localhost:8080/produtos/total-estoque
```

Resposta:

    {
    "total": 25
    }

O valor representa a soma das quantidades de estoque dos produtos que possuem quantidade de estoque informada.

# 21. Atualizar Produto
Endpoint

```text
PUT /produtos/{id}
```
Exemplo:

PUT /produtos/1

Body:
    
    {
    "codigo": "001-ALTERADO",
    "nome": "Terço dos homens alterado",
    "descricao": "Descrição atualizada",
    "quantidadeEstoque": 20,
    "prazoProducaoDias": null,
    "observacoes": null
    }

Resposta esperada:

Retornar o produto atualizado e 
```text
200 OK
```


**Importante:** o `valor` e `materiaisUtilizados` aparecem na 
resposta porque o backend devolve o objeto `Produto` atualizado, mas o
`PUT` **não modifica esses dois campos**.

# 22. Excluir produto

Endpoint

```text
DELETE /produtos/{id}
```

Exemplo:

```text
DELETE /produtos/1
```

Se foi excluido com sucesso:

```text
204 No Content
```

# 23. API de Controle de Estoque

A API de Controle de Estoque permite consultar e gerenciar os produtos sob controle de estoque físico (`tipo = PRE_PRONTO`). Produtos do tipo `PERSONALIZADO` não possuem estoque (são produzidos sob encomenda) e, portanto, não constam neste recurso.

As operações disponíveis são:

```text
GET /estoque
GET /estoque?nome=...
GET /estoque/buscar?nome=...
GET /estoque/{id}
GET /estoque/total
```

> **Nota de compatibilidade:** As rotas `/produtos/estoque` e `/estoque/produtos` também respondem com o mesmo comportamento.

## 23.1 Listar Produtos no Estoque

Endpoint:

```text
GET /estoque
```

Exemplo:

```text
curl http://localhost:8080/estoque
```

Retorna a lista de produtos pré-prontos com suas respectivas quantidades em estoque e detalhes.

Caso não haja produtos no estoque, retorna lista vazia (`[]`) com status `200 OK`.

## 23.2 Buscar Produto no Estoque por Nome

Endpoint via parâmetro:

```text
GET /estoque?nome=madeira
```

Endpoint via rota dedicada:

```text
GET /estoque/buscar?nome=madeira
```

O backend pesquisa produtos pré-prontos cujo nome contenha o termo informado (insensível a maiúsculas/minúsculas).

Se o termo for vazio ou em branco, retorna todos os produtos do estoque. Se não houver correspondência, retorna `[]` com status `200 OK`.

## 23.3 Buscar Produto no Estoque por ID

Endpoint:

```text
GET /estoque/{id}
```

Exemplo:

```text
GET /estoque/1
```

- Se o produto existir e pertencer ao controle de estoque (`tipo = PRE_PRONTO`), retorna `200 OK` com os dados do produto.
- Se o produto não existir ou pertencer à categoria personalizada (fora do contexto de estoque), retorna:

```text
404 Not Found
```

## 23.4 Consultar Total em Estoque

Endpoint:

```text
GET /estoque/total
```

(Ou `GET /estoque/total-estoque`)

Resposta:

```json
{
  "total": 25
}
```

## 24. Movimentações financeiras

A API de movimentações financeiras permite consultar receitas e despesas cadastradas, aplicar filtros e obter o total de receitas em determinado período.

**URL base:** `http://localhost:8080`

### 24.1. Listar movimentações financeiras

**Endpoint:** `GET /movimentacoes-financeiras`

Retorna todas as movimentações financeiras cadastradas quando nenhum filtro é informado. Também permite filtrar os resultados.

**Parâmetros opcionais:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `tipo` | String | Tipo da movimentação: `RECEITA` ou `DESPESA`. |
| `situacao` | String | Situação: `PENDENTE`, `PAGO` ou `CANCELADO`. |
| `idCategoria` | Long | Identificador da categoria. |
| `idCategoriaDespesa` | Long | Alias para `idCategoria`. |
| `categoria` | String | Nome da categoria. |
| `dataInicio` | LocalDate | Data inicial do período (`AAAA-MM-DD`). |
| `dataInicial` | LocalDate | Alias para `dataInicio`. |
| `dataFim` | LocalDate | Data final do período (`AAAA-MM-DD`). |
| `dataFinal` | LocalDate | Alias para `dataFim`. |
| `descricao` | String | Texto para filtrar pela descrição. |

**Exemplos de requisição:**

Listar todas as movimentações:

```http
GET http://localhost:8080/movimentacoes-financeiras
```

Listar somente receitas:

```http
GET http://localhost:8080/movimentacoes-financeiras?tipo=RECEITA
```

Listar despesas pagas:

```http
GET http://localhost:8080/movimentacoes-financeiras?tipo=DESPESA&situacao=PAGO
```

Consultar movimentações em um período:

```http
GET http://localhost:8080/movimentacoes-financeiras?dataInicio=2026-10-01&dataFim=2026-10-31
```

Combinar filtros:

```http
GET http://localhost:8080/movimentacoes-financeiras?tipo=DESPESA&situacao=PENDENTE&dataInicio=2026-10-01&dataFim=2026-10-31
```

**Resposta:** `200 OK`

Retorna uma lista de objetos `MovimentacaoFinanceira`, contendo os dados das movimentações encontradas.

### 24.2. Listar movimentações por período

**Endpoint:** `GET /movimentacoes-financeiras/periodo`

Possui os mesmos filtros do endpoint de listagem, permitindo consultar movimentações por período e combinar os filtros de tipo, situação, categoria e descrição.

**Exemplo:**

```http
GET http://localhost:8080/movimentacoes-financeiras/periodo?dataInicio=2026-10-01&dataFim=2026-10-31&tipo=DESPESA
```

**Resposta:** `200 OK`

Retorna uma lista de movimentações que atendem aos filtros informados.

### 24.3. Buscar movimentações por descrição

**Endpoint:** `GET /movimentacoes-financeiras/buscar`

Busca movimentações utilizando parte ou o conteúdo da descrição.

**Parâmetro:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `descricao` | String | Texto utilizado na busca. |

**Exemplo:**

```http
GET http://localhost:8080/movimentacoes-financeiras/buscar?descricao=Compra
```

**Resposta:** `200 OK`

Retorna uma lista de movimentações encontradas.

### 24.4. Consultar movimentação por ID

**Endpoint:** `GET /movimentacoes-financeiras/{id}`

Busca uma movimentação financeira específica pelo identificador.

**Exemplo:**

```http
GET http://localhost:8080/movimentacoes-financeiras/1
```

**Respostas:**

- `200 OK`: movimentação encontrada.
- `404 Not Found`: não existe movimentação com o identificador informado.
- `400 Bad Request`: identificador inválido.

### 24.5. Consultar total de receitas

**Endpoints:**

- `GET /movimentacoes-financeiras/total-receitas`
- `GET /movimentacoes-financeiras/receitas-total` (alias).

Calcula o total de receitas, permitindo informar um período e uma situação.

**Parâmetros opcionais:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `dataInicio` | LocalDate | Data inicial (`AAAA-MM-DD`). |
| `dataInicial` | LocalDate | Alias para `dataInicio`. |
| `dataFim` | LocalDate | Data final (`AAAA-MM-DD`). |
| `dataFinal` | LocalDate | Alias para `dataFim`. |
| `situacao` | String | `PENDENTE`, `PAGO` ou `CANCELADO`. |

**Exemplo:**

```http
GET http://localhost:8080/movimentacoes-financeiras/total-receitas?dataInicio=2026-10-01&dataFim=2026-10-31&situacao=PAGO
```

**Resposta:** `200 OK`

```json
{
  "total": 1500.00,
  "totalReceitas": 1500.00,
  "tipo": "RECEITA",
  "dataInicio": "2026-10-01",
  "dataFim": "2026-10-31",
  "situacao": "PAGO"
}
```

Os campos `situacao`, `dataInicio` e `dataFim` refletem os filtros informados. Quando a situação não é enviada, o campo `situacao` não é incluído na resposta. As datas podem ser nulas quando não forem informadas.

### 24.6. Códigos de erro

| Código HTTP | Situação |
|---|---|
| `200 OK` | Consulta realizada com sucesso. |
| `400 Bad Request` | Parâmetros inválidos, datas em formato incorreto ou filtros duplicados com valores divergentes. |
| `404 Not Found` | Movimentação não encontrada pelo ID. |

Os erros de validação retornam um objeto JSON com o campo `erro`.

Exemplo:

```json
{
  "erro": "Tipo de movimentação financeira inválido. Valores aceitos: RECEITA, DESPESA."
}
```

**Observação:** os valores dos parâmetros `tipo` e `situacao` devem respeitar os nomes dos enums, em letras maiúsculas. As datas devem utilizar o formato `AAAA-MM-DD`.
### 24.7. Gerar relatório financeiro em PDF

**Endpoint:** `GET /relatorios/financeiros`

Gera um relatório financeiro em formato PDF, considerando o período informado.

**Parâmetros obrigatórios:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `dataInicio` | LocalDate | Data inicial do relatório (`AAAA-MM-DD`). |
| `dataFim` | LocalDate | Data final do relatório (`AAAA-MM-DD`). |

**Exemplo de requisição:**

```http
GET http://localhost:8080/relatorios/financeiros?dataInicio=2026-10-01&dataFim=2026-10-31
```

**Exemplo utilizando cURL:**

```bash
curl -X GET \
"http://localhost:8080/relatorios/financeiros?dataInicio=2026-10-01&dataFim=2026-10-31" \
-H "Accept: application/pdf" \
--output relatorio-financeiro.pdf
```

**Resposta de sucesso:** `200 OK`

Retorna o arquivo PDF diretamente no corpo da resposta.

**Cabeçalhos da resposta:**

| Cabeçalho | Valor |
|---|---|
| `Content-Type` | `application/pdf` |
| `Content-Disposition` | `attachment; filename="relatorio-financeiro.pdf"` |

O relatório contém:

- Período selecionado.
- Total de receitas.
- Total de despesas.
- Saldo líquido.
- Resumo de despesas por categoria.
- Contas a receber.

**Resposta de erro:** `400 Bad Request`

Quando os parâmetros informados não são válidos, retorna um JSON:

```json
{
  "erro": "Mensagem explicando o problema."
}
```

**Integração com o frontend:**

O frontend deve realizar uma requisição GET, receber a resposta como arquivo binário (Blob) e disponibilizar o PDF para visualização ou download.

Exemplo com JavaScript:

```javascript
async function baixarRelatorio(dataInicio, dataFim) {
    const params = new URLSearchParams({
        dataInicio,
        dataFim
    });

    const response = await fetch(
        `http://localhost:8080/relatorios/financeiros?${params}`,
        {
            headers: {
                Accept: "application/pdf"
            }
        }
    );

    if (!response.ok) {
        const erro = await response.json();
        throw new Error(erro.erro || "Erro ao gerar relatório.");
    }

    const arquivo = await response.blob();
    const url = URL.createObjectURL(arquivo);

    const link = document.createElement("a");
    link.href = url;
    link.download = "relatorio-financeiro.pdf";
    link.click();

    URL.revokeObjectURL(url);
}


```
## 25. Receitas

A API de receitas permite cadastrar receitas, consultar os registros existentes, filtrar por período e calcular o total recebido ou previsto.

### 25.1. Cadastrar receita

**Endpoint:** `POST /receitas`

Também aceita os caminhos alternativos:

- `/financeiro/receitas`
- `/movimentacoes-financeiras/receitas`

**Corpo da requisição:**

```json
{
  "descricao": "Venda de produtos",
  "valor": 1500.00,
  "categoria": "Vendas",
  "situacao": "PENDENTE",
  "dataMovimentacao": "2026-10-10",
  "dataRecebimento": "2026-10-10",
  "dataVencimento": "2026-10-20",
  "observacoes": "Venda realizada para cliente",
  "tipo": "RECEITA"
}
```

**Descrição dos campos:**

| Campo | Tipo | Descrição |
|---|---|---|
| `descricao` | String | Descrição da receita. |
| `valor` | BigDecimal | Valor da receita. |
| `categoria` | String | Categoria da receita. |
| `situacao` | String | `PENDENTE`, `PAGO` ou `CANCELADO`. |
| `dataMovimentacao` | LocalDate | Data da movimentação. |
| `dataRecebimento` | LocalDate | Alternativa para informar a data da movimentação. |
| `dataVencimento` | LocalDate | Data de vencimento da receita. |
| `observacoes` | String | Observações adicionais. |
| `tipo` | String | Tipo da movimentação. Quando informado, deve ser `RECEITA`. |

As datas devem utilizar o formato `AAAA-MM-DD`.

**Regras importantes:**

- Os campos `dataMovimentacao` e `dataRecebimento` podem ser utilizados como alternativas.
- Se ambos forem informados, devem possuir a mesma data.
- O tipo da movimentação é definido pelo serviço como receita quando não informado.
- Os demais campos e suas validações devem respeitar as regras do serviço.

**Resposta de sucesso:** `201 Created`

Retorna o objeto da receita cadastrada.

**Resposta de erro:** `400 Bad Request`

Retorna um JSON com o campo `erro` explicando o problema.

### 25.2. Listar receitas

**Endpoint:** `GET /receitas`

Retorna todas as receitas cadastradas.

Também permite filtrar por período.

**Parâmetros opcionais:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `dataInicio` | LocalDate | Data inicial do período (`AAAA-MM-DD`). |
| `dataFim` | LocalDate | Data final do período (`AAAA-MM-DD`). |

**Exemplos:**

Listar todas as receitas:

```http
GET http://localhost:8080/receitas
```

Listar receitas de outubro:

```http
GET http://localhost:8080/receitas?dataInicio=2026-10-01&dataFim=2026-10-31
```

**Resposta:** `200 OK`

Retorna uma lista de objetos de movimentação financeira correspondentes às receitas.

### 25.3. Consultar total de receitas

**Endpoints:**

- `GET /receitas/total`
- `GET /receitas/total-periodo`
- `GET /receitas/periodo/total`

Também existem os caminhos alternativos:

- `/financeiro/receitas/total`
- `/movimentacoes-financeiras/receitas/total`

Calcula o total das receitas, permitindo filtros por período e situação.

**Parâmetros opcionais:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `dataInicio` | LocalDate | Data inicial (`AAAA-MM-DD`). |
| `dataInicial` | LocalDate | Alias para `dataInicio`. |
| `dataFim` | LocalDate | Data final (`AAAA-MM-DD`). |
| `dataFinal` | LocalDate | Alias para `dataFim`. |
| `situacao` | String | `PENDENTE`, `PAGO` ou `CANCELADO`. |

**Exemplo:**

```http
GET http://localhost:8080/receitas/total?dataInicio=2026-10-01&dataFim=2026-10-31&situacao=PAGO
```

**Resposta:** `200 OK`

```json
{
  "total": 1500.00,
  "totalReceitas": 1500.00,
  "tipo": "RECEITA",
  "dataInicio": "2026-10-01",
  "dataFim": "2026-10-31",
  "situacao": "PAGO"
}
```

O campo `situacao` só aparece quando informado. As datas podem ser nulas quando não forem fornecidas.

### 25.4. Consultar receita por ID

**Endpoint:** `GET /receitas/{id}`

Consulta uma receita específica pelo identificador.

**Exemplo:**

```http
GET http://localhost:8080/receitas/1
```

**Respostas:**

- `200 OK`: receita encontrada.
- `404 Not Found`: receita não encontrada.
- `400 Bad Request`: identificador inválido.

### 25.5. Códigos de erro

| Código HTTP | Situação |
|---|---|
| `201 Created` | Receita cadastrada com sucesso. |
| `200 OK` | Consulta realizada com sucesso. |
| `400 Bad Request` | Dados inválidos, datas incompatíveis ou parâmetros incorretos. |
| `404 Not Found` | Receita não encontrada pelo ID. |

Os erros são retornados em JSON:

```json
{
  "erro": "Mensagem explicando o problema."
}
```

**Observação:** o controller apresentado não disponibiliza operações de atualização ou exclusão de receitas.

## 26. Despesas

A API de despesas permite cadastrar despesas, consultar registros, filtrar por categoria e período e consultar uma despesa específica.

### 26.1. Cadastrar despesa

**Endpoint:** `POST /despesas`

Caminhos alternativos:

- `/financeiro/despesas`
- `/movimentacoes-financeiras/despesas`

**Corpo da requisição:**

```json
{
  "descricao": "Compra de matéria-prima",
  "valor": 350.00,
  "categoria": "Insumos",
  "idCategoria": 1,
  "situacao": "PENDENTE",
  "dataMovimentacao": "2026-10-10",
  "dataPagamento": "2026-10-10",
  "dataVencimento": "2026-10-20",
  "observacoes": "Compra para produção",
  "tipo": "DESPESA"
}
```

**Descrição dos campos:**

| Campo | Tipo | Descrição |
|---|---|---|
| `descricao` | String | Descrição da despesa. |
| `valor` | BigDecimal | Valor da despesa. |
| `categoria` | String | Nome da categoria da despesa. |
| `idCategoria` | Long | Identificador da categoria cadastrada. |
| `idCategoriaDespesa` | Long | Alias para `idCategoria`. |
| `situacao` | String | `PENDENTE`, `PAGO` ou `CANCELADO`. |
| `dataMovimentacao` | LocalDate | Data da movimentação. |
| `dataPagamento` | LocalDate | Alternativa para informar a data da movimentação. |
| `dataVencimento` | LocalDate | Data de vencimento da despesa. |
| `observacoes` | String | Observações adicionais. |
| `tipo` | String | Tipo da movimentação. Quando informado, deve ser `DESPESA`. |

As datas devem utilizar o formato `AAAA-MM-DD`.

**Regras importantes:**

- `dataMovimentacao` e `dataPagamento` podem ser utilizados como alternativas.
- Caso ambos sejam informados, devem possuir a mesma data.
- `idCategoria` e `idCategoriaDespesa` também são alternativas. Se ambos forem enviados, devem possuir o mesmo valor.
- Quando um ID de categoria é informado, o backend associa a despesa à categoria correspondente.
- O tipo da movimentação é definido pelo serviço como despesa quando não informado.
- Os demais campos e suas validações devem respeitar as regras do serviço.

**Resposta de sucesso:** `201 Created`

Retorna o objeto da despesa cadastrada.

**Resposta de erro:** `400 Bad Request`

Retorna um JSON com o campo `erro`.

### 26.2. Listar despesas

**Endpoint:** `GET /despesas`

Permite consultar todas as despesas ou aplicar filtros por categoria e período.

**Parâmetros opcionais:**

| Parâmetro | Tipo | Descrição |
|---|---|---|
| `idCategoria` | Long | Identificador da categoria. |
| `categoria` | String | Nome da categoria. |
| `dataInicio` | LocalDate | Data inicial (`AAAA-MM-DD`). |
| `dataFim` | LocalDate | Data final (`AAAA-MM-DD`). |

**Exemplos:**

Listar todas as despesas:

```http
GET http://localhost:8080/despesas
```

Filtrar por categoria:

```http
GET http://localhost:8080/despesas?idCategoria=1
```

Filtrar pelo nome da categoria:

```http
GET http://localhost:8080/despesas?categoria=Insumos
```

Consultar despesas de um período:

```http
GET http://localhost:8080/despesas?dataInicio=2026-10-01&dataFim=2026-10-31
```

Combinar categoria e período:

```http
GET http://localhost:8080/despesas?idCategoria=1&dataInicio=2026-10-01&dataFim=2026-10-31
```

**Resposta:** `200 OK`

Retorna uma lista de objetos de movimentação financeira correspondentes às despesas encontradas.

**Possíveis

## 27. Categorias de despesas

O sistema disponibiliza endpoints para gerenciar as categorias utilizadas na organização das despesas financeiras.

**Rotas alternativas:** todos os endpoints também podem ser acessados pelos prefixos:

- `/categorias-despesas`
- `/financeiro/categorias-despesas`
- `/despesas/categorias`

Para manter a padronização, recomenda-se utilizar `/categorias-despesas` no frontend.

### 27.1. Cadastrar categoria de despesa

**Endpoint:** `POST /categorias-despesas`

**Corpo da requisição:**

```json
{
  "nome": "Insumos",
  "descricao": "Despesas relacionadas à aquisição de matérias-primas."
}
```

**Resposta:** `201 Created`

Retorna a categoria cadastrada.

**Possíveis erros:**
- `400 Bad Request`: dados não informados ou categoria inválida. O corpo da resposta contém a propriedade `erro`.

### 27.2. Listar categorias

**Endpoint:** `GET /categorias-despesas`

Retorna todas as categorias cadastradas.

Também é possível filtrar pelo nome:

`GET /categorias-despesas?nome=Insumos`

O filtro é opcional. Quando informado e não estiver em branco, o backend utiliza a busca por nome.

**Resposta:** `200 OK`

Retorna uma lista de categorias.

### 27.3. Buscar categoria por ID

**Endpoint:** `GET /categorias-despesas/{id}`

Exemplo:

`GET /categorias-despesas/1`

**Respostas:**
- `200 OK`: categoria encontrada.
- `404 Not Found`: categoria não encontrada.
- `400 Bad Request`: identificador inválido.

### 27.4. Atualizar categoria

**Endpoint:** `PUT /categorias-despesas/{id}`

**Corpo da requisição:**

```json
{
  "nome": "Insumos e Matérias-primas",
  "descricao": "Despesas relacionadas à produção."
}
```

**Respostas:**
- `200 OK`: categoria atualizada.
- `404 Not Found`: categoria não encontrada.
- `400 Bad Request`: dados inválidos ou incompatíveis com as regras do backend.

### 27.5. Excluir categoria

**Endpoint:** `DELETE /categorias-despesas/{id}`

Exemplo:

`DELETE /categorias-despesas/1`

**Respostas:**
- `204 No Content`: categoria excluída com sucesso.
- `404 Not Found`: categoria não encontrada.
- `400 Bad Request`: exclusão não permitida pelas regras do sistema ou identificador inválido.

### 27.6. Estrutura dos dados

O DTO utilizado para cadastro e atualização possui os seguintes campos:

| Campo | Tipo | Descrição |
|---|---|---|
| `nome` | String | Nome da categoria |
| `descricao` | String | Descrição da categoria |

**Observação para o frontend:** as categorias cadastradas podem ser consultadas pelo endpoint `GET /categorias-despesas` e utilizadas para preencher os campos de seleção durante o cadastro de despesas. O identificador da categoria pode ser enviado no campo `idCategoria` ou `idCategoriaDespesa` do cadastro de uma despesa, conforme documentado na seção 26.
Retorna a soma das quantidades em estoque de todos os produtos sob controle de estoque.
