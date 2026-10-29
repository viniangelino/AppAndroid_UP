# Gestão de Suprimentos — Trabalho 3 (Persistência com Room)

App Android em **Kotlin + Jetpack Compose** para controle de **clientes** e **fornecedores**.
Evolução do Trabalho 2: as mesmas telas, listas, formulários e navegação, mas agora os dados ficam
salvos de verdade num **banco SQLite local usando Room**. Fechar e abrir o app não apaga nada.

## Como rodar

1. Clone o repositório:
   ```bash
   git clone <(https://github.com/viniangelino/AppAndroid_UP)>
   ```
2. Abra a pasta no **Android Studio Quail (2026.1) ou mais novo** em *File → Open* (a pasta que tem o `settings.gradle.kts`).
3. Se perguntar a JVM do Gradle, escolha **JVM 21**. Espere o Gradle sincronizar.
4. Escolha um emulador ou celular com **Android 8.0 (API 26) ou superior** e clique em **Run ▶**.
5. Entre com **login `admin` / senha `admin`**.

Na primeira vez que o app abre, o banco é criado com 3 clientes e 3 fornecedores de exemplo.
Depois disso, tudo o que for adicionado, editado ou removido fica salvo no banco.

**Teste de persistência:** adicione um cliente → feche o app (tire ele da lista de apps recentes) → abra de novo → o cliente continua lá.

## Tecnologias

Kotlin 2.4 · Jetpack Compose (Material 3) · Navigation Compose 2.7.7 · **Room 2.8.5 + KSP 2.3.12** · Coroutines · AGP 9.3.3 / Gradle 9.5 · minSdk 26

## Estrutura do código

```
app/src/main/java/br/com/gestaosuprimentos/
├── MainActivity.kt
├── data/
│   ├── Modelos.kt           # @Entity Cliente e @Entity Fornecedor (as data classes do Trabalho 2)
│   ├── ClienteDao.kt        # @Dao com @Insert, @Query, @Update, @Delete + buscarPorId, buscarPorCpf
│   ├── FornecedorDao.kt     # @Dao com @Insert, @Query, @Update, @Delete + buscarPorId, buscarPorCnpj, buscarPorRaizCnpj
│   ├── AppDatabase.kt       # @Database, Singleton (companion object + @Volatile + synchronized)
│   └── GestaoViewModel.kt   # só o login (continua em memória, fora do escopo do trabalho)
├── navigation/              # object Rotas + NavHost + NavigationBar
├── ui/telas/                # telas (listas, cadastros, detalhes, menu, login, ajuda)
├── ui/componentes/          # componentes e formulários reaproveitados
└── util/                    # validação de CPF/CNPJ, máscaras, datas, Toast a partir da coroutine
```

---

# Documentação do processo e das decisões

> ✏️ **Antes de entregar:** reescrevam os trechos marcados com ✏️ com as palavras do trio e coloquem
> os prints/vídeos na pasta `docs/prints/`. O enunciado não aceita documentação sem o app funcionando em imagem/vídeo.

## 1. Como estava o projeto no Trabalho 2 e o que mudou

No Trabalho 2 o app já tinha 10 telas navegáveis, duas listas (Clientes e Fornecedores) com cadastro,
edição, remoção e detalhe, mas tudo ficava num `mutableStateListOf` dentro de um ViewModel.
**Ao fechar o app, tudo voltava ao início.**

No Trabalho 3:

| Antes (Trabalho 2) | Agora (Trabalho 3) |
|---|---|
| `data class Cliente` / `Fornecedor` | As mesmas classes com `@Entity` e `@PrimaryKey(autoGenerate = true)` |
| Listas fixas no ViewModel | Cada tela de lista busca no banco com `LaunchedEffect(Unit)` + `dao.buscarTodos()` |
| `vm.adicionarCliente(...)` | `dao.inserir(...)` dentro de `CoroutineScope(Dispatchers.IO).launch` |
| `vm.atualizarCliente(...)` | `dao.atualizar(...)` (edição no detalhe e Checkbox ativo/inativo) |
| `vm.removerCliente(...)` | `dao.deletar(...)` (lixeira, clique longo e botão no detalhe) |
| Detalhe procurava o item na lista em memória | Detalhe busca direto no banco com `dao.buscarPorId(id)` |
| Dados de exemplo criados toda vez que o app abria | Dados de exemplo inseridos **uma única vez**, quando o banco é criado |

✏️ *Print: a mesma tela no Trabalho 2 e no Trabalho 3.*

## 2. Entity, DAO e Database criados — e por quê

**Entities** (tabelas):

- `clientes` → `id`, `nome`, `cpf`, `telefone`, `nascimento`, `ativo`, `dataCadastro`
- `fornecedores` → `id`, `razaoSocial`, `cnpj`, `categoria`, `prazoEntregaDias`, `observacoes`, `dataCadastro`

Mantivemos exatamente os campos do Trabalho 2 para não quebrar as telas. Decisões:

- **CPF, CNPJ, telefone e nascimento só com números** (a máscara é só visual). Assim a busca e a comparação no banco funcionam sem se preocupar com pontos e traços.
- **`dataCadastro` virou `Long`**: o Room não sabe guardar `LocalDate`. Guardamos o dia como número (`LocalDate.toEpochDay()`) e convertemos de volta na hora de mostrar.
- **`ativo` é `Boolean`**: o Room guarda como 0/1.

**DAOs:** `ClienteDao` e `FornecedorDao`, cada um com `@Insert`, `@Query` (buscar todos), `@Update` e `@Delete`, todos `suspend fun`.

**Database:** `AppDatabase` (`abstract class ... : RoomDatabase()`), `version = 1`, com Singleton
(`companion object` + `@Volatile` + `synchronized`) para o app inteiro usar a mesma conexão.

✏️ *Expliquem com as palavras de vocês por que escolheram esses campos.*

## 3. Queries além dos 4 métodos básicos

| Query | Onde é usada | Por quê |
|---|---|---|
| `buscarPorId(id)` (nos dois DAOs) | Telas de detalhe | O detalhe recebe só o **id** pela rota e busca o item no banco (seção 3.3 do enunciado) |
| `buscarPorCpf(cpf)` | Cadastro e edição de cliente | Não deixar dois clientes com o mesmo CPF |
| `buscarPorCnpj(cnpj)` | Cadastro e edição de fornecedor | Não deixar CNPJ repetido |
| `buscarPorRaizCnpj(raiz)` | Detalhe do fornecedor | `WHERE cnpj LIKE :raiz \|\| '%'` traz matriz e filiais da mesma empresa (8 primeiros dígitos iguais) |

## 4. Dificuldade em migrar de `mutableStateListOf` para Room

✏️ *Contem com as palavras de vocês. O que aconteceu de verdade neste projeto:*

- **Versões do Android Studio:** o projeto não sincronizava no Android Studio Quail 3 com o Gradle 8.9 ("Could not move temporary workspace"). Resolvemos atualizando para AGP 9.3.3 + Gradle 9.5. No AGP 9 o Kotlin já vem embutido, então tiramos o plugin `org.jetbrains.kotlin.android` do módulo `app`, e usamos o KSP 2.3.12, que é compatível com isso.
- **`LocalDate` no Room:** dava erro porque o Room não sabe salvar esse tipo. Trocamos por `Long`.
- **Toast e navegação dentro do `Dispatchers.IO`:** a coroutine do banco roda fora da thread principal, e Toast/navegação só podem ser feitos na thread principal. Tudo que mexe na tela fica dentro de `withContext(Dispatchers.Main) { ... }`.
- **Detalhe que antes recebia o item pronto** agora precisa esperar o banco responder: criamos um estado "carregando" e um indicador de progresso.
- **Validação de CPF/CNPJ repetido** antes olhava a lista em memória; agora é uma consulta no banco feita antes do `inserir`.

## 5. Teste de persistência

1. Adicionar um cliente → ✏️ *print*
2. Fechar o app completamente (tirar dos recentes) → ✏️ *print*
3. Abrir de novo → o cliente continua na lista → ✏️ *print ou vídeo*

✏️ *Coloquem aqui o link do vídeo curto mostrando o teste.*

## 6. Integrantes e dificuldades de cada um

| Integrante | O que fez | Dificuldade e como resolveu |
|---|---|---|
| ✏️ Nome 1 | ✏️ | ✏️ |
| ✏️ Nome 2 | ✏️ | ✏️ |
| ✏️ Nome 3 | ✏️ | ✏️ |

---

## Roteiro da apresentação (2 min)

1. Abrir o app → login `admin` / `admin`.
2. **Adicionar** um cliente (ex.: CPF `935.411.347-80`) → abre o detalhe já buscado do banco.
3. **Editar** o cliente pelo lápis → salvar.
4. **Remover** outro cliente pela lixeira (ou um fornecedor segurando o card).
5. **Fechar o app de verdade** (tirar dos recentes) → abrir de novo → mostrar que o cliente novo e a edição continuam lá e o removido não voltou.

> CNPJ válido extra para teste: `11.444.777/0003-23` (aparece como filial da Madeireira Araucária).

A documentação do Trabalho 2 está em [`docs/DOCUMENTACAO_TRABALHO2.md`](docs/DOCUMENTACAO_TRABALHO2.md).
