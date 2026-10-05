# Stub de compilação
Implementação autoral mínima da assinatura pública de `weblogic.logging.NonCatalogLogger`.
Não é biblioteca Oracle, emulador ou runtime WebLogic. `info()` falha explicitamente.
A dependência é `provided`; o JAR **não deve entrar no WAR/EAR**.
O servidor real fornece essa classe. Em migração, remova a dependência e substitua o logger.
A análise do módulo de stubs pode gerar ruído: inspecione os usos em `pedidos-web`.
