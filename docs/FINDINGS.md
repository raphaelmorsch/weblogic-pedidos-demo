# Evidências para comparar com o relatório

São **hipóteses de findings**, não resultados de uma análise executada. IDs, severidades e esforço dependem das regras instaladas e dos filtros. Nem todo antipadrão possui uma regra padrão; ausência de finding não significa portabilidade.

| Evidência no projeto | Problema esperado / ponto de revisão | Direção para Quarkus |
| --- | --- | --- |
| `PedidoServlet.java`, `WEB-INF/web.xml` | Servlet `javax` e deployment descriptor antigo | Jakarta REST ou servlet Jakarta com extensão compatível |
| `PedidoService.java` | `@Stateless`, `@EJB`, CMT e contexto EJB | CDI, `@Transactional` e revisão da semântica transacional |
| `PedidoListener.java` | MDB, activation config, destino JMS do domínio | Consumer JMS ou messaging com tratamento explícito de falhas |
| `Pedido.java`, `META-INF/persistence.xml` | JPA `javax`, JTA datasource, EclipseLink e Oracle | Hibernate ORM; revisar sequência, schema e configuração |
| `LegacyReportDao.java` | Lookup `java:comp/env/jdbc/PedidosDS` | Datasource configurado e injetado |
| `LegacyRemoteLookup.java` | Factory WebLogic, protocolo T3 e hostname fixo | Remover integração proprietária ou isolar adaptador |
| `PedidoService.java` | `weblogic.logging.NonCatalogLogger` | Logging suportado no destino |
| `weblogic.xml`, `weblogic-ejb-jar.xml`, `weblogic-application.xml` | Descritores e recursos proprietários | Reexpressar configuração e remover dependência de domínio |
| `LegacyAudit.java` | Escrita em arquivo com caminho absoluto | Storage externo ou evento; política de persistência |
| `LegacyWarmup.java` | `Timer`, thread não gerenciada, cache estático ilimitado | Scheduler gerenciado e cache limitado/compartilhado |
| `PedidoServlet.java`, `weblogic.xml` | Sessão HTTP e replicação do servidor | Rever necessidade de estado entre requisições |
| `pedidos-ear/pom.xml` | EAR e lifecycle de application server | Artefato e runtime Quarkus |

Problemas que exigem revisão humana, independentemente de findings:

- Atomicidade banco + JMS depende de XA real, recovery e configuração do domínio.
- Auditoria em arquivo não participa da transação: reentrega do MDB pode duplicar efeitos.
- A aplicação não implementa idempotência, outbox, retry controlado ou DLQ própria.
- Cache cresce indefinidamente; sessão e filesystem dificultam múltiplas réplicas.
- Sem autenticação: endpoint é apenas demonstração local.

Analise também com `--target cloud-readiness`; um relatório somente `quarkus` pode cobrir poucos desses pontos. Regras de WebLogic direcionadas ao EAP ajudam a inventariar APIs proprietárias, mas sua recomendação de substituição pode não se aplicar ao Quarkus.
