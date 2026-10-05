# Pedidos legado — laboratório WebLogic → Quarkus

Aplicação fictícia Java EE 7 / Java 8, inspirada em WebLogic 12.2.1.x, preparada para **análise no Red Hat MTA 8.3** e experimentação com OpenRewrite. Projeto Maven multi-módulo com WAR dentro de EAR. Não é uma aplicação Quarkus pronta: o objetivo é expor dependências de servidor e trabalho de migração real.

## Início rápido

Requisitos de build: **JDK 17 e Maven 3.9.x**, acesso ao Maven Central (ou mirror configurado). O compilador gera bytecode Java 8 com `release=8`; as ferramentas de build são recentes para funcionar no JDK 17. Não precisa de WebLogic, Oracle ou broker para compilar.

```bash
cd weblogic-pedidos-demo
mvn clean verify
```

Artefatos: `pedidos-web/target/pedidos.war` e `pedidos-ear/target/pedidos-legado.ear`.
O build executa cinco testes de validação de negócio. Eles não simulam EJB, JPA, JMS ou XA.
Consulte `docs/VALIDACAO.md` para o que foi efetivamente verificado na geração do ZIP.

## Cenário e estrutura

Um portal recebe `cliente` e `valor` por Servlet. Um EJB transacional grava um pedido via JPA e publica uma mensagem JMS. Um MDB escreve uma linha em arquivo local. A sessão HTTP guarda o último pedido. Há exemplos adicionais de relatório JDBC/JNDI e acesso remoto por T3, analisáveis mas não acionados pelo endpoint.

| Módulo/arquivo | Finalidade |
| --- | --- |
| `weblogic-api-stubs` | Assinatura mínima autoral de `weblogic.logging.NonCatalogLogger`, somente compilação |
| `pedidos-web` | Servlet, EJB local, entidade JPA, MDB, JMS e exemplos problemáticos |
| `pedidos-ear` | Empacotamento EAR; `application.xml` gerado pelo Maven |
| `infra` | DDL Oracle e inventário ilustrativo do domínio |
| `docs/FINDINGS.md` | Mapa de evidências e hipóteses de findings |
| `docs/MIGRACAO.md` | Etapas e critérios para chegar ao Quarkus |

O stub tem escopo **provided** e não entra no WAR/EAR. Não há JAR Oracle redistribuído. O método do stub falha explicitamente se usado como implementação; no servidor, a API real deve existir. A presença do módulo de stubs pode adicionar ruído à análise: priorize os usos em `pedidos-web`.

## Executar no WebLogic (opcional)

O build não provisiona servidor nem recursos. Para experimentar o fluxo em um domínio de laboratório compatível:

1. Crie o schema com `infra/oracle-schema.sql` e configure o datasource JTA/XA `jdbc/PedidosDS`, com driver Oracle compatível.
2. Provisione servidor JMS, store, módulo/subdeployment, fila `jms/PedidosQueue` e connection factory `jms/PedidosConnectionFactory` com participação XA; faça o targeting para o servidor/cluster da aplicação.
3. Disponibilize EclipseLink/JPA 2.1 conforme a distribuição do servidor. O `persistence.xml` seleciona explicitamente esse provider.
4. Crie `/var/legacy/pedidos` no host do servidor e conceda escrita ao processo do WebLogic. É um requisito legado deliberado.
5. Implante `pedidos-legado.ear` pelo procedimento normal do seu domínio.

```bash
curl http://localhost:7001/pedidos/api/pedidos
curl -i -X POST http://localhost:7001/pedidos/api/pedidos \
  --data-urlencode 'cliente=Cliente Demo' --data-urlencode 'valor=42.50'
```

Resultado pretendido: HTTP 201 com o ID, linha em `PEDIDO` e mensagem consumida em `audit.log`. Deploy e transações distribuídas precisam ser validados em servidor real. O exemplo não tem autenticação e serve apenas ao laboratório. Endereços `.invalid` são marcadores; não há credenciais reais.

## Análise com MTA 8.3

A sintaxe abaixo foi conferida no executável local **8.3.0**, SHA `ee82eac978278b7ba6136ba96a8f21316baaf744`. Requisitos do provider Java, imagens e execução local/container dependem da instalação. Em ambientes com containers, inicialize o runtime e autentique no registry quando necessário.

Na raiz do projeto:

```bash
mta-cli version
mta-cli analyze --help
mta-cli rules list-sources
mta-cli rules list-targets

# Visão inicial: Quarkus e preparação para containers, sem filtro de source.
# Evita excluir regras genéricas/Java EE por filtrar somente WebLogic.
mta-cli analyze --input "$PWD" --output "$PWD/../report-quarkus" \
  --mode source-only --target quarkus --target cloud-readiness

# Java EE -> Jakarta e baseline Java 17, em relatório separado.
mta-cli analyze --input "$PWD" --output "$PWD/../report-jakarta" \
  --mode source-only --target jakarta-ee9 --target openjdk17

# Diagnóstico de dependências WebLogic em rota EAP, como comparação.
# Este relatório NÃO prova compatibilidade com Quarkus.
mta-cli analyze --input "$PWD" --output "$PWD/../report-weblogic-eap" \
  --mode source-only --source weblogic --target eap8
```

Os IDs `weblogic`, `quarkus`, `quarkus3`, `cloud-readiness`, `jakarta-ee9`, `openjdk17` e `eap8` foram listados pelo CLI local. `quarkus3` é útil também para regras de atualização de aplicações já Quarkus; não significa conversão automática de EJB. Escolha filtros conforme os rótulos reais das regras. Use um diretório de saída novo por execução ou acrescente `--overwrite` somente para substituir um relatório anterior.

Para análise com dependências, execute primeiro `mvn install` e troque `--mode source-only` por `--mode full`. O provider precisa resolver o reactor e as dependências; o repositório Maven dentro de um container pode ser diferente do host. Falha na resolução do stub não indica erro de migração. O primeiro exercício pode permanecer em `source-only`.

## OpenRewrite pelo MTA

No CLI 8.3.0 deste ambiente, estes comandos existem:

```bash
mta-cli openrewrite --help
mta-cli openrewrite --list-targets
```

Lista observada: `jakarta-imports`, `jakarta-xml`, `jakarta-bootstrapping`, `quarkus-properties` e `eap8-xml`. Os três primeiros são candidatos à preparação Jakarta; leia os resultados de cada um separadamente. `quarkus-properties` transforma propriedades **Spring Boot**: este projeto não usa Spring, portanto não é a recipe de conversão deste legado. `eap8-xml` trata Faces/web XML e não é o caminho principal deste exemplo.

Há avisos contraditórios de depreciação neste binário: `openrewrite --help` marca o comando como depreciado, e `transform openrewrite --help` recomenda `openrewrite`. Se necessário, consulte ambos os helps; não troque parâmetros por suposição. Os nomes de `--target` do MTA são aliases empacotados, não nomes completos arbitrários do catálogo OpenRewrite.

### Isolar a experiência, inclusive dryRun

Dado o comportamento de cópia de volta relatado no ambiente anterior, use uma cópia descartável sem `.git`, com destino vazio. Os comandos abaixo não alteram a configuração do Podman nem as imagens existentes.

```bash
# Executar na raiz do projeto ORIGINAL.
LAB="$(mktemp -d "${TMPDIR:-/tmp}/pedidos-rewrite.XXXXXX")"
mkdir "$LAB/app"
tar --exclude='./.git' --exclude='*/target' -cf - . | tar -xf - -C "$LAB/app"
printf 'Cópia de trabalho: %s\n' "$LAB/app"

mta-cli openrewrite --input "$LAB/app" --target jakarta-imports --goal dryRun

# Compare os fontes e POMs antes/depois; procure rewrite.patch em target/.
# O comando interno deve conter rewrite-maven-plugin:dryRun (ou goal dryRun no log).
diff -ru --exclude=.git --exclude=target "$PWD" "$LAB/app"
```

`diff` retorna 1 quando existem diferenças; isso é normal. Um `dryRun` Maven calcula mudanças e pode gerar relatórios/artefatos em `target`, mas não deve aplicar o patch aos fontes. Se o log mostrar `:run` ou `Changes have been made`, trate como execução real na cópia. Um erro de permissão posterior em cópia não desfaz alterações já aplicadas. Não presuma que o ZIP corrige o entrypoint da sua imagem.

Depois de revisar a experiência, para aplicar **na cópia**:

```bash
mta-cli openrewrite --input "$LAB/app" --target jakarta-imports --goal run
mta-cli openrewrite --input "$LAB/app" --target jakarta-xml --goal run
mta-cli openrewrite --input "$LAB/app" --target jakarta-bootstrapping --goal dryRun
mvn -f "$LAB/app/pom.xml" clean verify
```

As receitas Jakarta podem deixar o reactor sem compilar até ajustar dependências e APIs: isso faz parte da avaliação, não é evidência de que o legado era Quarkus. Não renomeie globalmente todo `javax`: `javax.sql` e `javax.naming`, por exemplo, continuam sendo APIs Java SE.

### Alternativa: Maven OpenRewrite diretamente

Útil para separar problemas de receita dos problemas do wrapper/container MTA. Use outra cópia limpa. Exemplo de receita do catálogo público: `org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta`, do artefato `org.openrewrite.recipe:rewrite-migrate-java`. Essa preparação para Jakarta EE 9 não converte EJB/MDB para Quarkus.

```bash
# Escolha e fixe uma versão do artefato compatível com o plugin usado.
# Substitua o marcador antes de executar; não é uma versão publicada.
RECIPE_VERSION='SUBSTITUA_PELA_VERSAO_VALIDADA'
mvn -f "$LAB/app/pom.xml" org.openrewrite.maven:rewrite-maven-plugin:6.49.0:dryRun \
  "-Drewrite.recipeArtifactCoordinates=org.openrewrite.recipe:rewrite-migrate-java:$RECIPE_VERSION" \
  -Drewrite.activeRecipes=org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta
```

A versão do plugin é a exibida na documentação consultada; esse par plugin/recipe não foi executado neste laboratório. Registre o par validado para reproduzir o teste. No MTA, prefira inicialmente os aliases empacotados confirmados por `--list-targets`.

## Referências

- [Guia CLI Red Hat MTA 8.1](https://docs.redhat.com/en/documentation/migration_toolkit_for_applications/8.1/html-single/using_the_migration_toolkit_for_applications_command-line_interface/index) — referência conceitual; os comandos 8.3 acima foram conferidos diretamente no binário local, pois a URL pública 8.3 não pôde ser consultada.
- [OpenRewrite: migrar para Jakarta EE 9](https://docs.openrewrite.org/recipes/java/migrate/jakarta/javaxmigrationtojakarta).
- [Oracle: logging WebLogic](https://docs.oracle.com/en/middleware/standalone/weblogic-server/15.1.1/logsv/writing.html).
- [Quarkus: Hibernate ORM](https://quarkus.io/guides/hibernate-orm/).
- [Quarkus: JMS](https://quarkus.io/version/3.27/guides/jms/).
