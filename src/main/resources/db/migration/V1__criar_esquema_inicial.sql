CREATE TABLE public.tb_produto (
    id UUID NOT NULL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    preco NUMERIC(10,2) NOT NULL,
    estoque INTEGER NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    cor VARCHAR(50),
    material VARCHAR(100),
    ativo BOOLEAN NOT NULL
);

CREATE TABLE public.tb_foto (
    id UUID NOT NULL PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    principal BOOLEAN NOT NULL,
    produto_id UUID NOT NULL,
    CONSTRAINT fk_foto_produto
        FOREIGN KEY (produto_id)
        REFERENCES public.tb_produto(id),

    data_cadastro TIMESTAMP(6) NOT NULL
);