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

- **Dates** e **Cantadas**: dois novos baralhos de conversa. O laranja traz 50 perguntas para um encontro, das leves às que mais instigam; o rosa traz 50 cantadas, diretas, despretensiosas e bem-humoradas, para a roda dar nota ou para chamar alguém para sair. Em português e inglês.
- **Mais cartas de conversa**: os cinco baralhos de antes (Dia a Dia, E Se…, Conquistas, Romance e Cringe) passam de 20 para 50 cartas. Com Dates e Cantadas, a conversa tem 7 baralhos e 350 cartas.
- **Prefere**: sexto jogo de shot. Duas opções por carta; no 3 todo mundo mostra 1 ou 2 dedos e a minoria bebe. 200 cartas nos 3 níveis, com ícone de bifurcação e cor roxa.

### Changed

- As **cartinhas do seletor de cor** (e da tela de regras) seguem o visual da logo nova: só a moldura, sem a elipse.
- No card do modo **Aleatório**, as bolinhas de cor não mostram mais o rosa, já que as Cantadas ficam de fora.
- O modo **Aleatório** não mistura mais as **Cantadas**; elas só entram no modo Temático.
- Na tela inicial, a **fila de cores** do modo Temático agora desliza para os lados, já que as sete cores não cabem numa linha.
- **Logo e ícone do app** renovados: as cartas do leque ficam só com a moldura e o ponto, e a carta preta guarda o balão de conversa.
- **Ícones dos jogos de shot** na tela de configuração agora têm o mesmo tamanho e o mesmo traço, em vez de variar de um jogo para outro.
- A Home passa a dizer **"6 jogos de shot"**.

### Fixed

- Na configuração dos jogos de shot, os **cards dos jogos** tinham alturas diferentes (o texto de um quebrava e o do vizinho não); agora todos ficam do mesmo tamanho.
- A abertura do **leque do logo** na Home abria uma carta por vez, devagar; agora abre em cascata.
- Em telas de 360 dp de largura, o botão **Regras** da Home quebrava letra por letra; agora os botões ficam em uma linha e o texto do topo é que quebra.
