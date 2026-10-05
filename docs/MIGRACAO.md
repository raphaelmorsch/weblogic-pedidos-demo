# Roteiro de transformação

1. **Baseline:** compilar, guardar o ZIP original, analisar fontes e registrar filtros/regras/versão do MTA. Opcionalmente implantar em WebLogic para testes de integração.
2. **Java/Jakarta:** testar recipes em cópias independentes. Rever imports, POM, XML e a manutenção de `javax.naming` e `javax.sql`. Troca de namespace não substitui o servidor.
3. **Novo runtime:** selecionar uma versão suportada de Quarkus 3 e JDK compatível, introduzir BOM/plugin e remover EAR, API Java EE agregada e stubs. Migrar endpoint para Jakarta REST, ou avaliar suporte a Servlet.
4. **EJB:** converter serviços para CDI e definir fronteiras `@Transactional`. Revisar propagação, rollback, interceptação, concorrência e qualquer semântica de EJB; uma simples troca de anotações não garante equivalência.
5. **Persistência:** configurar datasource e Hibernate ORM; preservar nomes de tabela/colunas/sequência. Avaliar provider-specific properties, migrações do schema e transações.
6. **Mensageria:** selecionar broker/cliente/extensão suportados e reconstruir consumo, retry, DLQ, idempotência e shutdown. Decidir conscientemente entre XA suportado pelo stack ou outbox; não assumir atomicidade com um send comum.
7. **Operação:** externalizar configuração, remover T3 e dependência de filesystem local, substituir timer/cache e definir sessão, autenticação, observabilidade e health checks.
8. **Aceitação:** build, contrato HTTP, persistência, rollback, falha do broker, reentrega sem efeitos duplicados, restart e execução em duas réplicas. Só depois reanalisar e comparar findings.

O projeto não contém uma implementação Quarkus paralela: ele é a entrada legada do exercício. As recipes empacotadas de propriedades Spring Boot não constituem um conversor WebLogic → Quarkus.
