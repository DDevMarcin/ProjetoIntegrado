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

Retorna a soma das quantidades em estoque de todos os produtos sob controle de estoque.
