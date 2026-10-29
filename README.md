# Spill

Um jogo de conversa para duas ou mais pessoas, feito como app **Android nativo** (Kotlin + Jetpack Compose).

Você puxa uma carta, responde em voz alta e o grupo decide quem mandou melhor. Ninguém ganha de verdade: a ideia é falar sobre coisas que você nunca puxaria sozinho, em inglês ou em português.

<p align="center">
  <img src="docs/demo.gif" width="270" alt="Demonstração do Spill">
</p>

> Vídeo em melhor qualidade: [docs/demo.mp4](docs/demo.mp4)

## Telas

<p align="center">
  <img src="docs/screenshots/home.png" width="190" alt="Início">
  <img src="docs/screenshots/themed.png" width="190" alt="Modo temático">
  <img src="docs/screenshots/card.png" width="190" alt="Carta">
  <img src="docs/screenshots/rules.png" width="190" alt="Regras">
  <img src="docs/screenshots/pt.png" width="190" alt="Português">
</p>

## Como se joga

1. **Escolha um modo.** *Aleatório* mistura todos os baralhos. *Temático* fica numa cor só, e a rodada inteira tem o mesmo clima.
2. **Leia a carta em voz alta** e fale por mais ou menos um minuto.
3. **Continue a conversa.** Todo mundo pode fazer perguntas ou entrar com a própria história.
4. **Próxima carta.** Arraste a carta para qualquer lado da tela (ou toque em *Próxima*).
5. **Pontos, se quiserem.** Depois de cada resposta, o grupo dá um ponto para a mais criativa.

| Cor | Baralho | Clima |
|---|---|---|
| Verde | Dia a Dia | Escola, trabalho, faculdade, histórias da vida |
| Azul | E Se… | Situações aleatórias: apocalipse, outros planetas, superpoderes |
| Amarelo | Conquistas | Vitórias, sonhos e lugares que você quer conhecer |
| Vermelho | Romance | Amor, primeiros encontros, crushes |
| Preto | Cringe | Momentos vergonhosos, micos e comédia pura |

## Como adicionar perguntas

Todo o conteúdo vive em [`app/src/main/assets/catalog`](app/src/main/assets/catalog). **Adicionar uma pergunta não exige mexer em código Kotlin.**

```
catalog/
├── index.json          # idiomas e ordem dos baralhos
├── i18n/en.json        # textos da interface, um arquivo por idioma
├── i18n/pt.json
└── decks/green.json    # cor, nome/descrição por idioma e as cartas
```

**Nova carta:** acrescente um objeto ao array `cards` do baralho. O número da carta é a posição no array.

```json
{
  "kind": "Movie",
  "by": "Opcional: artista, para o tipo Song",
  "en": { "title": "Ratatouille", "prompt": "What dish can you cook well?" },
  "pt": { "title": "Ratatouille", "prompt": "Que prato você sabe fazer bem?" }
}
```

`kind` é uma chave traduzida em `i18n/*.json` (`Movie`, `Series`, `Song`, `Topic`). Para um tipo novo, acrescente a chave em todos os idiomas.

**Novo baralho:** crie `decks/<id>.json` (copie um existente), liste o `<id>` em `index.json` e pronto. A tela inicial, o modo temático e a tela de regras o descobrem sozinhos.

**Novo idioma:** crie `i18n/<codigo>.json`, declare o idioma em `index.json` e traduza `meta` e cada carta nos baralhos.

O teste `CatalogTest` roda no CI e **falha o build** se faltar tradução de qualquer carta, baralho ou tipo, então nada incompleto chega ao jogador.

## Modo Drink +18

Um segundo jogo, para maiores de 18, com visual próprio (fundo escuro, uma cor e um ícone por jogo). Passa por uma tela de confirmação de idade antes de abrir.

| Jogo | Cor | Como funciona |
|---|---|---|
| **Desafio** | preto, garrafa | Responde ou bebe. Cada carta traz também um "Para todos". |
| **Eu nunca** | vinho, copo de shot | Quem já fez, bebe. |
| **Faz ou bebe** | verde, balança | Faz a ação ou bebe. A roda decide se valeu. |
| **Paranoia** | azul, espiral | A pergunta fica escondida: quem está com o celular segura a carta para ler e responde só com um nome. O citado bebe para saber a pergunta (ou ninguém bebe e passa). |
| **Mais provável** | amarelo, caveira-interrogação | "Quem é mais provável de…": no 3 todo mundo aponta e o mais apontado bebe. |

- **200 cartas por jogo**, em **3 níveis**: 50 de nível 1 (Mico), 100 de nível 2 (Picante) e 50 de nível 3 (Pesado). Cada caveira vale um shot.
- **Regras na tela:** o botão **?** ao lado do +18 (na configuração e em cada jogo) abre o *Como jogar* do jogo escolhido, com passo a passo e a escala de shots.
- **Monte a rodada:** escolha um jogo (ou o *Misturado*, que alterna Desafio e Eu nunca), quais níveis entram, **Escalada** (começa no mico e esquenta até o pesado) e **Modo leve** (gole em vez de shot).

As cartas estão em [`drink.json`](app/src/main/assets/catalog/drink.json), um array por jogo (`dare`, `never`, `doit`, `paranoia`, `likely`). Para acrescentar uma, basta um objeto com `level` de 1 a 3 e `main` (o texto); só o Desafio também leva `all` (o "Para todos"). O `DrinkTest` confere formato, contagens, duplicatas e os textos de interface, e falha o build se algo sair do padrão. O conteúdo do Modo Drink existe só em português.

## Rodando

Requer JDK 21 e Android SDK (compileSdk 37).

```bash
./gradlew testDebugUnitTest   # valida o catálogo
./gradlew assembleRelease     # app/build/outputs/apk/release/app-release.apk
./gradlew installDebug        # instala no aparelho/emulador conectado
```

## Build automático (GitHub Actions)

[`.github/workflows/build.yml`](.github/workflows/build.yml) roda em cada push, PR e tag `v*`:

- roda os testes e gera o **`Spill.apk`**, disponível como artefato da execução (`Spill-Android`);
- se os secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` e `KEY_PASSWORD` existirem, o APK é assinado com a sua chave; sem eles, usa a chave de debug (instala igual, mas não serve para a Play Store). Em tags `v*`, o APK vai anexado à Release.

## Estrutura

```
app/src/main/java/app/spill/
├── Catalog.kt        # carrega e valida o catálogo JSON
├── MainActivity.kt   # ViewModel (estado da rodada, idioma), raiz e brilho ambiente
├── Home.kt           # logo em leque, modos e seleção de cor
├── Game.kt           # baralho empilhado (DeckStage), arrastar/jogar a carta, fim do baralho
├── Rules.kt          # diálogo de regras
├── Drink.kt          # catálogo e montagem da rodada do Modo Drink
├── DrinkUi.kt        # confirmação +18, configuração, cartas e a tela do Paranoia
├── DrinkArt.kt       # garrafa, copo, balança, espiral e caveira
├── Ui.kt, Theme.kt   # botões, tipografia e o ornamento das cartas
```

## Licença

[MIT](LICENSE)
