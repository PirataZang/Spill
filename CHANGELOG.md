# Changelog

Todas as mudanças notáveis deste projeto ficam neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto usa [Versionamento Semântico](https://semver.org/lang/pt-BR/).

<!--
INSTRUÇÕES PARA IA (Claude, Copilot, Cursor, ChatGPT…) — leia antes de editar.

Este arquivo é lido pelo DevScope (módulo Releases) na hora de lançar:
  - a seção "## [X.Y.Z] - AAAA-MM-DD" da versão vira a MENSAGEM DA TAG e as
    notas da Release no GitHub — escreva para quem vai ler a página da release;
  - se existe seção para a próxima patch, minor ou major, essa é a versão que
    o DevScope sugere. Então a versão escolhida aqui é a que vai ser lançada.

Como escrever:
  1. Levante o que mudou desde a última release, sem inventar nada:
       git log $(git describe --tags --abbrev=0)..HEAD --oneline
     e o diff quando a mensagem do commit não disser o efeito.
  2. Novidades entram em "## [Unreleased]". Na hora de lançar, renomeie para
     "## [X.Y.Z] - AAAA-MM-DD" e deixe um "## [Unreleased]" vazio acima dela.
  3. Agrupe em ### Added, ### Changed, ### Deprecated, ### Removed, ### Fixed e
     ### Security — nesta ordem, só os que tiverem item.
  4. Um item por mudança que o USUÁRIO percebe, não um por commit. Comece pelo
     efeito ("Login lembra o último e-mail"), não pela implementação
     ("refatora AuthService"). Nome da funcionalidade em negrito.
  5. Escolha a versão pelo maior impacto: algo removido ou incompatível →
     major; funcionalidade nova → minor; só correção ou ajuste pequeno → patch.
  6. Escreva no idioma deste arquivo. Não apague nem reescreva versões já
     lançadas.
-->

## [Unreleased]

### Added

- **Prefere**: sexto jogo de shot. Duas opções por carta; no 3 todo mundo mostra 1 ou 2 dedos e a minoria bebe. 200 cartas nos 3 níveis, com ícone de bifurcação e cor roxa.

### Changed

- **Ícones dos jogos de shot** na tela de configuração agora têm o mesmo tamanho e o mesmo traço, em vez de variar de um jogo para outro.
- A Home passa a dizer **"6 jogos de shot"**.

### Fixed

- A abertura do **leque do logo** na Home abria uma carta por vez, devagar; agora abre em cascata.
- Em telas de 360 dp de largura, o botão **Regras** da Home quebrava letra por letra; agora os botões ficam em uma linha e o texto do topo é que quebra.
