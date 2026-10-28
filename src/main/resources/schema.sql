-- Sistema de Gestão para Artesã
-- Schema escrito manualmente (em vez de deixar o Hibernate gerar sozinho)
-- justamente para controlar a ordem e a legibilidade das colunas.
-- CREATE TABLE IF NOT EXISTS: seguro rodar em todo startup, não apaga dados.

CREATE TABLE IF NOT EXISTS material (
                                        id_material     INTEGER PRIMARY KEY AUTOINCREMENT,
                                        nome            VARCHAR(255)    NOT NULL,
    unidade_medida  VARCHAR(10)     NOT NULL,
    preco_unidade   NUMERIC(10,2)   NOT NULL
    );

CREATE TABLE IF NOT EXISTS produto (
                                       id_produto            INTEGER PRIMARY KEY AUTOINCREMENT,
                                       codigo                VARCHAR(50)     NOT NULL UNIQUE,
    nome                  VARCHAR(255)    NOT NULL,
    descricao             VARCHAR(255),
    tipo                  VARCHAR(255)    NOT NULL CHECK (tipo IN ('PRE_PRONTO', 'PERSONALIZADO')),
    valor                 NUMERIC(10,2)   NOT NULL,
    valor_manual          BOOLEAN         NOT NULL DEFAULT 0,
    quantidade_estoque    INTEGER,
    prazo_producao_dias   INTEGER,
    observacoes           VARCHAR(500)
    );

CREATE TABLE IF NOT EXISTS produto_material (
                                                id_produto            BIGINT          NOT NULL,
                                                id_material           BIGINT          NOT NULL,
                                                quantidade_utilizada  NUMERIC(10,2)   NOT NULL,
    PRIMARY KEY (id_produto, id_material),
    FOREIGN KEY (id_produto)  REFERENCES produto(id_produto)   ON DELETE CASCADE,
    FOREIGN KEY (id_material) REFERENCES material(id_material)
    );