# ZetraOnboarding

Plugin de missões iniciais do servidor ZetraMC para Spigot/Paper 1.21.x.

## Conteúdo

- `src/main/java`: código Java recuperado do JAR original com CFR 0.152 e ajustado para compilar.
- `src/main/resources`: `plugin.yml` e configuração padrão incluídos no JAR original.
- `examples/ZetraMC-config.yml`: cópia da configuração encontrada no backup do servidor de 28/07/2026, para referência. Não inclui dados de jogadores.
- `original/ZetraOnboarding_v6.jar`: JAR original preservado, sem alterações.

O código fonte original não estava disponível neste workspace. Esta versão é uma reconstrução a partir do JAR, mantida para permitir manutenção futura. O JAR em `original/` é a referência da versão instalada no backup; um novo build deve ser validado no servidor de teste antes de substituir o plugin em produção.

## Requisitos e compilação

- Java 21
- Maven 3.9+
- Spigot API 1.21.4 e PlaceholderAPI 2.11.6 (resolvidas pelo Maven)

```sh
mvn package


O arquivo compilado fica em `target/ZetraOnboarding-1.0-SNAPSHOT.jar`.

## Uso

Instale o JAR em `plugins/` junto com PlaceholderAPI. Configure `plugins/ZetraOnboarding/config.yml` antes de iniciar o servidor.

As quatro missões padrão são: interagir com o NPC de Jogar (avanço pelo comando administrativo), obter um peitoral de ferro, usar `/g` e entrar em um clã. As recompensas são comandos de console definidos em `missions.m1` a `missions.m4`. O comando `/skip` conclui o tutorial; `/zetraonboarding next <jogador>` avança a primeira missão e requer `zetraonboarding.admin`.

O progresso dos jogadores fica em `plugins/ZetraOnboarding/players.yml`, que não é versionado aqui.
