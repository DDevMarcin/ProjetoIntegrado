-- Script de Seed (Carga Inicial de Dados de Exemplo)
-- Sistema de Gestão para Artesã

-- 1. Materiais (com estoque normal, estoque baixo <= 5 e fora de estoque = 0)
INSERT OR IGNORE INTO material (id_material, nome, cor, unidade_medida, preco_unidade, quantidade_estoque) VALUES
(1, 'Contas de Madeira Imbuia 8mm', 'Marrom', 'un', 0.35, 250.00),
(2, 'Contas de Cristal Tcheco 8mm', 'Transparente', 'un', 0.90, 180.00),
(3, 'Pérolas ABS 6mm', 'Branco Perolado', 'un', 0.25, 320.00),
(4, 'Contas de Resina Rosa Bebê 8mm', 'Rosa', 'un', 0.45, 140.00),
(5, 'Contas de Porcelana Azul Royal', 'Azul Royal', 'un', 0.60, 4.00),
(6, 'Fio Encerado Cordone 1mm', 'Marrom Escuro', 'm', 1.20, 45.00),
(7, 'Fio de Seda Rabo de Rato 1mm', 'Branco', 'm', 1.80, 3.00),
(8, 'Crucifixo São Bento Metal Nobre', 'Ouro Velho', 'un', 6.50, 18.00),
(9, 'Crucifixo Cravejado Strass', 'Dourado', 'un', 12.00, 0.00),
(10, 'Entremeio Nossa Senhora Aparecida', 'Prata', 'un', 4.80, 22.00),
(11, 'Entremeio Sagrada Família Resina', 'Colorido', 'un', 5.50, 2.00),
(12, 'Medalha Milagrosa Banho Prata', 'Prata', 'un', 3.20, 0.00);

-- 2. Produtos (PRE_PRONTO para Estoque/Produtos e PERSONALIZADO para Encomendas)
INSERT OR IGNORE INTO produto (id_produto, codigo, nome, descricao, tipo, valor, valor_manual, quantidade_estoque, prazo_producao_dias, observacoes) VALUES
(1, 'TRC-001', 'Terço Tradicional São Bento', 'Terço artesanal em madeira imbuia 8mm com metais em ouro velho e fio encerado de alta resistência.', 'PRE_PRONTO', 45.00, 1, 12, NULL, 'Pronta entrega'),
(2, 'TRC-002', 'Terço Noiva Pérolas e Cristal', 'Terço delicado com pérolas brancas e pai-nossos em cristal tcheco legítimo.', 'PRE_PRONTO', 89.90, 1, 5, NULL, 'Acompanha caixa aveludada'),
(3, 'TRC-003', 'Dezena de Carro N. Sra. Aparecida', 'Dezena para retrovisor com contas em resina e entremeio de Nossa Senhora Aparecida.', 'PRE_PRONTO', 25.00, 1, 20, NULL, NULL),
(4, 'TRC-004', 'Pulseira Terço Infantil Rosa', 'Pulseira terço delicada em contas rosa bebê com fecho ajustável.', 'PRE_PRONTO', 29.90, 1, 8, NULL, NULL),
(5, 'ENC-001', 'Terço Personalizado Primeira Eucaristia', 'Terço personalizado com nome da criança em letras douradas e pérolas ABS.', 'PERSONALIZADO', 95.00, 1, NULL, 7, 'Informar o nome para gravação até 10 letras'),
(6, 'ENC-002', 'Terço de Casamento Luxo Cristal', 'Terço exclusivo sob medida para noivas com crucifixo cravejado e banho dourado.', 'PERSONALIZADO', 165.00, 1, NULL, 12, 'Inclui porta-terço personalizado com iniciais dos noivos'),
(7, 'ENC-003', 'Kit Batizado Padrinhos Sagrada Família', 'Par de dezenas personalizadas em fio de seda branco com entremeio da Sagrada Família.', 'PERSONALIZADO', 110.00, 1, NULL, 5, 'Embalagem presenteável em linho');

-- 3. Composição Produto x Material (produto_material)
INSERT OR IGNORE INTO produto_material (id_produto, id_material, quantidade_utilizada) VALUES
(1, 1, 59.00),
(1, 6, 1.50),
(1, 8, 1.00),
(2, 3, 53.00),
(2, 2, 6.00),
(2, 7, 1.50),
(3, 4, 11.00),
(3, 10, 1.00),
(4, 4, 22.00),
(4, 6, 0.50),
(5, 3, 53.00),
(5, 7, 1.50),
(5, 11, 1.00),
(6, 2, 59.00),
(6, 9, 1.00),
(7, 3, 22.00),
(7, 7, 1.00),
(7, 11, 2.00);

-- 4. Histórico de Movimentação de Materiais
INSERT INTO movimentacao_material (id_material, tipo, quantidade, saldo_anterior, saldo_atual, motivo, observacoes, data_hora) VALUES
(1, 'ENTRADA', 300.00, 0.00, 300.00, 'Compra de fornecedor', 'Nota fiscal #4512', '2026-11-01 09:30:00'),
(1, 'SAIDA', 50.00, 300.00, 250.00, 'Produção de terços São Bento', 'Lote semanal', '2026-11-03 14:15:00'),
(2, 'ENTRADA', 190.00, 0.00, 190.00, 'Reposição de estoque', 'Importadora Cristais SP', '2026-11-02 11:00:00'),
(2, 'PERDA', 10.00, 190.00, 180.00, 'Contas trincadas no transporte', 'Pacote 2 veio com avaria', '2026-11-04 16:40:00'),
(9, 'SAIDA', 5.00, 5.00, 0.00, 'Uso em encomendas de noiva', 'Estoque zerado, solicitar reposição', '2026-11-08 10:20:00');

-- 5. Categorias de Despesas
INSERT OR IGNORE INTO categoria_despesa (id_categoria, nome, descricao) VALUES
(1, 'Insumos', 'Compra de matérias-primas, contas, fios, entremeios e crucifixos'),
(2, 'Embalagens', 'Caixas, saquinhos de linho, tags e materiais de envio'),
(3, 'Frete', 'Despesas de envio pelos Correios e transportadoras'),
(4, 'Logística', 'Custos logísticos de transporte e deslocamento'),
(5, 'Custos Fixos', 'Energia, internet, água e despesas recorrentes do ateliê'),
(6, 'Ferramentas e Equipamentos', 'Alicates, organizadores e utensílios de produção'),
(7, 'Marketing e Divulgação', 'Anúncios, fotografia e materiais promocionais'),
(8, 'Impostos e Taxas', 'DAS MEI, taxas de meios de pagamento e tarifas bancárias'),
(9, 'Outros', 'Demais despesas gerais do ateliê');

-- 6. Movimentações Financeiras
INSERT INTO movimentacao_financeira (descricao, valor, tipo, categoria, id_categoria_despesa, situacao, data_movimentacao, data_vencimento, observacoes) VALUES
('Compra de insumos (Madeira, Cristais e Metais)', 285.50, 'DESPESA', 'Insumos', 1, 'PAGO', '2026-11-01', '2026-11-01', 'Pagamento à vista com desconto'),
('Venda de 3 Terços Tradicionais São Bento', 135.00, 'RECEITA', 'Vendas Pronta Entrega', NULL, 'PAGO', '2026-11-04', '2026-11-04', 'Pagamento via PIX'),
('Sinal 50% - Encomenda Terço Casamento Luxo', 82.50, 'RECEITA', 'Encomendas', NULL, 'PAGO', '2026-11-06', '2026-11-06', 'Cliente Mariana'),
('Restante 50% - Encomenda Terço Casamento Luxo', 82.50, 'RECEITA', 'Encomendas', NULL, 'PENDENTE', '2026-11-06', '2026-11-18', 'Receber na entrega'),
('Compra de embalagens e caixas aveludadas', 94.00, 'DESPESA', 'Embalagens', 2, 'PENDENTE', '2026-11-08', '2026-11-15', 'Boleto 7 dias');
