# Pokédex Mobile (Android)

Projeto desenvolvido para a disciplina de Desenvolvimento Mobile, dividido em duas etapas e organizado em módulos Android no mesmo repositório.

| Módulo | Implementação |
|---|---|
| `etapa1-views` | Android Views com XML, navegação por Intent explícita, ViewBinding, `data class` e dados simulados (*mocks*). |
| `etapa2-compose` | Jetpack Compose, Navigation 3, ViewModel, StateFlow, Repository, coroutines, Room e integração com a PokéAPI. |

## Objetivo e funcionalidades

O aplicativo permite consultar uma lista de Pokémon, visualizar os detalhes de cada um e gerenciar favoritos. Ao favoritar um Pokémon, é possível definir um apelido e adicionar uma anotação. Os favoritos são armazenados localmente e permanecem disponíveis após o fechamento do aplicativo.

- **Etapa 1:** apresenta as telas de lista e detalhes utilizando dados simulados.
- **Etapa 2:** consulta a [PokéAPI](https://pokeapi.co), mantém os dados em cache com Room e permite adicionar, editar e remover favoritos.

## Ambiente de execução

- Android Studio compatível com Android Gradle Plugin 8.13.
- JDK 17 ou superior.
- Android SDK com a plataforma API 36.
- Dispositivo físico ou emulador com Android 8.0 (API 26) ou superior.
- Conexão com a internet necessária para a primeira consulta à PokéAPI na Etapa 2; os dados em cache podem ser utilizados posteriormente.

Para executar o projeto, devem ser executados os dois módulos (`etapa1-views` e `etapa2-compose`). No Android Studio, cada módulo deve ser executado separadamente. Pelo terminal, os comandos são:

```bash
./gradlew :etapa1-views:installDebug
./gradlew :etapa2-compose:installDebug
```

### Testes da Etapa 2

```bash
./gradlew :etapa2-compose:testDebugUnitTest
./gradlew :etapa2-compose:connectedDebugAndroidTest
```

O primeiro comando executa os testes unitários dos ViewModels e das validações. O segundo executa os testes de interface com Compose e requer um dispositivo ou emulador conectado.

O projeto não utiliza arquivos `.env`, credenciais ou chaves de API, pois a PokéAPI é pública.

## Arquitetura da Etapa 2

A implementação separa a interface, o gerenciamento de estado, as regras de acesso aos dados e as fontes de persistência e rede:

```text
UI (Compose) → ViewModel (UiState / StateFlow) → Repository
                                                    ├── Room (cache e favoritos)
                                                    └── Retrofit (PokéAPI)
```

Principais características:

- **Estado da interface:** fluxo unidirecional de dados, com telas conectadas ao ViewModel e componentes sem estado para facilitar os testes.
- **Estados de tela:** carregamento, conteúdo, lista vazia e erro, incluindo opção de nova tentativa e transições animadas.
- **Navegação:** Navigation 3 com rotas serializáveis (`ListRoute`, `DetailRoute(id, nome)` e `FavoritesRoute`), pilha de navegação controlada pelo aplicativo e ViewModel por entrada.
- **Cache local:** Room como fonte principal dos dados exibidos. A consulta à rede ocorre quando o cache está vazio, tem mais de 24 horas ou quando uma atualização é solicitada. Em caso de falha de rede, os dados salvos continuam disponíveis e o usuário recebe um aviso.
- **Operações assíncronas:** coroutines, chamadas de rede suspensas e cancelamento de atualizações anteriores por meio do `viewModelScope`, com tratamento adequado de `CancellationException`.
- **Organização dos dados:** conversão entre DTO da API, Entity do Room, modelo de domínio e modelo de interface (`PokemonItemUi`).
- **Injeção de dependências:** configuração manual pelo `AppContainer`, criado na classe `Application`; nos testes, é utilizado um `FakePokemonRepository`.
- **Validação de favoritos:** apelido obrigatório entre 2 e 20 caracteres, aceitando letras, números, espaços e hífens; anotação limitada a 120 caracteres. A interface apresenta mensagens de validação e confirmação por Snackbar.
- **Acessibilidade:** descrições de conteúdo para imagens e botões, áreas de toque de no mínimo 48 dp, títulos identificados como *heading*, textos dimensionados em `sp` e telas com rolagem para acomodar fontes maiores.

## Bibliotecas utilizadas

### Etapa 1

| Biblioteca | Utilização |
|---|---|
| AndroidX Core KTX, AppCompat e Activity KTX | Componentes básicos do aplicativo e suporte às Activities. |
| Material Components | Tema e componentes visuais. |
| RecyclerView | Exibição da lista de Pokémon. |

### Etapa 2

| Biblioteca | Utilização |
|---|---|
| Jetpack Compose (BOM, UI, Material 3 e ícones core) | Construção da interface declarativa. |
| Navigation 3 (`navigation3-runtime`, `navigation3-ui`) | Rotas e pilha de navegação. |
| Lifecycle (ViewModel Compose, Runtime Compose e ViewModel Navigation3) | Gerenciamento e observação do estado da interface. |
| Room (runtime, ktx e compiler via KSP) | Persistência local, cache e favoritos. |
| Retrofit e OkHttp | Requisições HTTP para a PokéAPI. |
| kotlinx.serialization | Conversão de JSON e serialização das rotas. |
| Kotlin Coroutines | Operações assíncronas e uso de `Flow`. |
| Coil 3 (`coil-compose`, `coil-network-okhttp`) | Carregamento das imagens dos Pokémon. |
| JUnit, kotlinx-coroutines-test e Compose UI Test | Testes unitários e de interface. |

## Organização do projeto

```text
etapa1-views/    telas XML, Activities, adapter e mocks
etapa2-compose/  data/ (remote, local, repository)
                 domain/
                 ui/ (list, detail, favorites, navigation, theme)
```
###
 
 
