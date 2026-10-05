# Validação da entrega

Verificação realizada em 05/10/2026:

- JDK Temurin 17.0.17, Maven 3.9.9, macOS ARM64.
- `mvn clean verify`: **BUILD SUCCESS** nos três módulos e no agregador.
- Cinco testes executados, zero falhas, zero erros, zero testes ignorados.
- WAR e EAR abertos e inspecionados: o EAR inclui `pedidos.war`, `application.xml` gerado e `weblogic-application.xml`.
- WAR inclui classes, persistence.xml e descritores proprietários; não inclui o stub WebLogic nem javaee-api.
- Todos os XML do projeto são bem formados. Isso não equivale a validação contra os schemas de cada versão do servidor.
- MTA CLI 8.3.0: conferidos `version`, `analyze --help`, `rules list-sources`, `rules list-targets`, `openrewrite --help`, `openrewrite --list-targets` e `transform openrewrite --help`.

Não executado: análise MTA completa, aplicação de recipes, deploy WebLogic, banco Oracle, broker JMS, transações XA e runtime Quarkus. Os findings documentados são expectativas para comparação, não um relatório produzido.

O ZIP contém fontes, testes, POMs, configuração e documentação. Diretórios `target`, repositório Maven, `.git` e binários gerados não estão incluídos; execute o build para recriá-los.
