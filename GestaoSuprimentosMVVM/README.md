# Gestão de Suprimentos — Trabalho 4 (Arquitetura MVVM)

App Android em **Kotlin + Jetpack Compose** para controle de **clientes** e **fornecedores**, com dados salvos
num banco local (**Room**). Neste trabalho o app **não ganhou nenhuma tela nem funcionalidade nova**:
o mesmo código do Trabalho 3 foi reorganizado no padrão **MVVM** (Model — View — ViewModel).

## Como rodar

1. Clone o repositório:
   ```bash
   git clone <https://github.com/viniangelino/AppAndroid_UP>
   ```
2. Abra a pasta no **Android Studio Quail (2026.1) ou mais novo** em *File → Open* (a pasta que tem o `settings.gradle.kts`).
3. Se perguntar a JVM do Gradle, escolha **JVM 21**. Espere o Gradle sincronizar.
4. Escolha um emulador ou celular com **Android 8.0 (API 26) ou superior** e clique em **Run ▶**.
5. Entre com **login `admin` / senha `admin`**.

Na primeira vez que o app abre, o banco é criado com 3 clientes e 3 fornecedores de exemplo.

## Tecnologias

Kotlin 2.4 · Jetpack Compose (Material 3) · Navigation Compose 2.7.7 · Room 2.8.5 + KSP 2.3.12 ·
ViewModel + StateFlow · Coroutines/Flow · AGP 9.3.3 / Gradle 9.5 · minSdk 26

## Camadas e pacotes

```
app/src/main/java/br/com/gestaosuprimentos/
├── MainActivity.kt                 # monta Database -> DAO -> Repository e abre a navegação
├── data/
│   ├── local/                      # MODEL (Room)
│   │   ├── Modelos.kt              #   @Entity Cliente e @Entity Fornecedor
│   │   ├── ClienteDao.kt           #   @Dao — buscarTodos() retorna Flow<List<Cliente>>
│   │   ├── FornecedorDao.kt        #   @Dao — buscarTodos() retorna Flow<List<Fornecedor>>
│   │   └── AppDatabase.kt          #   @Database Singleton
│   └── repository/                 # REPOSITORY (única fonte de verdade)
│       ├── ClienteRepository.kt
│       └── FornecedorRepository.kt
├── viewmodel/                      # VIEWMODEL (estado + regras)
│   ├── ClientesViewModel.kt        #   ClientesUiState        (lista de clientes)
│   ├── ClienteDetalheViewModel.kt  #   ClienteDetalheUiState  (cadastro, detalhe e edição)
│   ├── FornecedoresViewModel.kt    #   FornecedoresUiState    (lista de fornecedores)
│   ├── FornecedorDetalheViewModel.kt # FornecedorDetalheUiState
│   ├── MenuViewModel.kt            #   MenuUiState            (resumo do Menu)
│   ├── AcessoViewModel.kt          #   AcessoUiState          (login, em memória)
│   └── ViewModelFactories.kt       #   ClienteViewModelFactory, FornecedorViewModelFactory, MenuViewModelFactory
├── ui/                             # VIEW (só @Composable)
│   ├── telas/                      #   as telas: leem o uiState e chamam métodos do ViewModel
│   ├── componentes/                #   componentes e formulários reaproveitados
│   └── theme/
├── navigation/                     # object Rotas + NavHost + NavigationBar
└── util/                           # validação de CPF/CNPJ, máscaras e cálculos de data
```

Caminho de uma ação, por exemplo **cadastrar um cliente**:

```
Tela (ClienteCadastroScreen)  →  vm.cadastrar()
ViewModel (ClienteDetalheViewModel)  →  valida o formulário  →  repository.inserir(cliente)
Repository (ClienteRepository)  →  dao.inserir(cliente)
Room grava no banco  →  o Flow de buscarTodos() emite a lista nova  →  ClientesViewModel atualiza o _uiState  →  a lista se redesenha sozinha
```

---

# Documentação do processo e das decisões

> ✏️ **Antes de entregar:** reescrevam os trechos marcados com ✏️ com as palavras do trio e coloquem
> prints/vídeos na pasta `docs/prints/`. O enunciado não aceita documentação só em texto.

## 1. Como estava o projeto no Trabalho 3 e o que mudou

No Trabalho 3 o app já salvava no Room, mas **cada tela fazia tudo**: pegava o DAO com
`AppDatabase.getDatabase(context)`, abria `CoroutineScope(Dispatchers.IO).launch`, tratava erro e guardava
o estado em várias variáveis `remember { mutableStateOf(...) }` soltas. Funcionava, mas a tela misturava
desenho, acesso ao banco e regra de negócio.

| Antes (Trabalho 3) | Agora (Trabalho 4) |
|---|---|
| Entity, DAO e Database na pasta `data` | Pasta `data.local` |
| A tela chamava o DAO direto | A tela chama o **ViewModel**, que chama o **Repository**, que chama o DAO |
| `buscarTodos()` era `suspend` e devolvia `List` | `buscarTodos()` devolve **`Flow<List<...>>`**: a lista atualiza sozinha |
| `LaunchedEffect` + `CoroutineScope(Dispatchers.IO)` em cada tela | `viewModelScope.launch` dentro do ViewModel; `Dispatchers.IO` só no Repository |
| Várias variáveis `remember` (lista, carregando, formulário, diálogo...) | Uma `data class ...UiState` por ViewModel |
| Validação (CPF repetido, campos) dentro da tela | Validação dentro do ViewModel |
| `viewModel()` simples (só no login) | `viewModel(factory = ...Factory(repository))` |

✏️ *Print: o mesmo arquivo de tela no Trabalho 3 e no Trabalho 4.*

## 2. Como dividimos os pacotes

- **`data.local`**: o que o Room precisa (`@Entity`, `@Dao`, `@Database`). Não mudou nada além do Flow.
- **`data.repository`**: um Repository por Entity. É a única porta de entrada para os dados.
- **`viewmodel`**: os ViewModels, os UiStates e as Factories. Aqui fica o estado da tela e as regras.
- **`ui`**: só `@Composable`. Nenhuma tela importa DAO, `AppDatabase`, `CoroutineScope` ou `Dispatchers`.

Mantivemos `navigation` e `util` separados: navegação não é tela, e as funções de CPF/CNPJ/datas são usadas
tanto pela View (formatar para mostrar) quanto pelo ViewModel (validar).

## 3. Por que o Repository existe

Antes, se a forma de guardar os dados mudasse (por exemplo, buscar fornecedores de uma API), teríamos que
mexer em todas as telas que usavam o DAO. Agora **só o Repository muda**: ViewModel e tela continuam iguais,
porque só conhecem funções como `buscarTodos()`, `inserir()`, `atualizar()` e `deletar()`.

O Repository também é o lugar que decide **em qual thread** o banco roda (`withContext(Dispatchers.IO)`),
então nem o ViewModel nem a tela precisam se preocupar com isso.

## 4. O que tem em cada UiState e por quê

| UiState | Campos | Por quê |
|---|---|---|
| `ClientesUiState` | `clientes`, `carregando`, `buscaCpf`, `buscaNome`, `mensagem` (+ `filtrados` e `ativos` calculados) | Tudo o que a lista mostra: os itens, o indicador de carregamento, os campos de busca e o Toast |
| `ClienteDetalheUiState` | `cliente`, `carregando`, `editando`, `form`, `salvando`, `confirmandoRemocao`, `idCriado`, `removido`, `mensagem` | Serve ao cadastro e ao detalhe: o item lido do banco, o texto do formulário, se o diálogo de remover está aberto, e os "avisos" para a tela navegar (`idCriado`, `removido`) |
| `FornecedoresUiState` | `fornecedores`, `carregando`, `buscaCnpj`, `buscaRazao`, `paraRemover`, `mensagem` | Igual à lista de clientes, mais o fornecedor do diálogo de remoção (clique longo) |
| `FornecedorDetalheUiState` | `fornecedor`, `mesmaEmpresa`, `carregando`, `editando`, `form`, `salvando`, `confirmandoRemocao`, `idCriado`, `removido`, `mensagem` | Igual ao de cliente, mais a lista de matriz/filiais da mesma empresa |
| `MenuUiState` | `totalClientes`, `clientesAtivos`, `totalFornecedores`, `matrizes` | Os números do resumo. O ViewModel junta os dois Flows com `combine` |
| `AcessoUiState` | `usuarioLogado`, campos e erro do login, campos e erro do "criar acesso" | O login continua em memória, mas também segue o padrão `_uiState`/`uiState` |

Todos seguem o mesmo padrão:

```kotlin
private val _uiState = MutableStateFlow(ClientesUiState())         // só o ViewModel altera
val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()   // a tela só lê
```

E a tela faz só duas coisas:

```kotlin
val estado by vm.uiState.collectAsStateWithLifecycle()   // 1. observar o estado
vm.removerCliente(cliente)                               // 2. chamar métodos do ViewModel
```

## 5. ViewModelFactory

O Android não sabe criar sozinho um ViewModel que recebe o Repository no construtor. Criamos:

- `ClienteViewModelFactory(repository)`: cria `ClientesViewModel` e `ClienteDetalheViewModel`
- `FornecedorViewModelFactory(repository)`: cria `FornecedoresViewModel` e `FornecedorDetalheViewModel`
- `MenuViewModelFactory(clienteRepository, fornecedorRepository)`: cria `MenuViewModel`

Reaproveitamos uma Factory por Entity porque os dois ViewModels de cada Entity recebem o mesmo Repository.
Na tela: `vm: ClientesViewModel = viewModel(factory = ClienteViewModelFactory(repository))`.

## 6. Alguma funcionalidade quebrou durante a reorganização?

✏️ *Contem o que aconteceu de verdade com o trio. Pontos de atenção que tivemos que tratar no código:*

- **Toast e navegação** não podem ser chamados de dentro do ViewModel (ele não conhece a tela). Solução: o ViewModel
  coloca `mensagem`, `idCriado` ou `removido` no UiState, e a tela reage com `LaunchedEffect`.
- **Atualizar a lista depois de inserir/remover:** no Trabalho 3 a lista era buscada uma vez ao abrir a tela.
  Com o Flow do Room, a lista emite de novo sozinha a cada mudança, então não precisamos mais recarregar "na mão".
- **Formulário:** antes cada campo era um `mutableStateOf` dentro de uma classe da tela. Agora o texto do formulário
  mora no UiState (`ClienteForm` / `FornecedorForm`) e o componente `FormularioCliente` só recebe o valor e avisa mudanças.


## 7. Integrantes e dificuldades

| Integrante | O que fez | Dificuldade e como resolveu |
|---|---|---|
| Vinicius Vicente Angelino |
| Marcos Danrlei de Araujo |

---

## Roteiro da apresentação

1. Rodar o app e mostrar que tudo funciona como antes: adicionar, editar, remover, fechar e reabrir.
2. No Android Studio, mostrar os pacotes **`data.local` → `data.repository` → `viewmodel` → `ui`**.
3. Abrir o `ClienteDao` (Flow), o `ClienteRepository`, o `ClientesViewModel` (`_uiState` / `uiState`) e a `ClienteViewModelFactory`.
4. Abrir a `ClientesListaScreen` e mostrar que ela só tem `collectAsStateWithLifecycle()` e chamadas `vm.xxx()`.

Documentação dos trabalhos anteriores: [`docs/DOCUMENTACAO_TRABALHO3.md`](docs/DOCUMENTACAO_TRABALHO3.md) ·
[`docs/DOCUMENTACAO_TRABALHO2.md`](docs/DOCUMENTACAO_TRABALHO2.md)
