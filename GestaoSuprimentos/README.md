# Gestão de Suprimentos — Trabalho 2 (MAF)

App Android em **Kotlin + Jetpack Compose** para controle de **clientes** e **fornecedores** de uma empresa.
Continuação do Trabalho 1: as telas do protótipo no Figma agora estão funcionais, com navegação real,
listas dinâmicas, cadastro, remoção, edição e telas de detalhe.

> Documentação do processo e das decisões: [`DOCUMENTACAO.md`](DOCUMENTACAO.md)

## Como rodar

1. Clone o repositório:
   ```bash
   git clone <URL-DO-REPOSITORIO>
   ```
2. Abra a pasta no **Android Studio** (Quail 2026.1 ou mais novo) em *File → Open*.
3. Espere o Gradle sincronizar (ele baixa o Gradle 9.5 e as dependências sozinho).
4. Escolha um emulador ou celular com **Android 8.0 (API 26) ou superior** e clique em **Run ▶**.
5. Entre com **login `admin` / senha `admin`** (ou crie um acesso em *Criar novo acesso*).

O app já abre com alguns clientes e fornecedores de exemplo para facilitar a apresentação.

## Telas (10)

| # | Tela | O que faz |
|---|------|-----------|
| 1 | Login | Entra no app validando login/senha |
| 2 | Criar novo acesso | Cadastra um novo login (com confirmação de senha) |
| 3 | Menu | Resumo com números das duas listas + atalhos para todas as áreas |
| 4 | Clientes (lista) | `LazyColumn` + `Card`, filtro por CPF/nome, Checkbox ativo/inativo, remover |
| 5 | Cadastrar cliente | Formulário com CPF, telefone e data com máscara |
| 6 | Detalhe do cliente | Dados + informações calculadas + edição no próprio detalhe |
| 7 | Fornecedores (lista) | `LazyColumn` + `Card`, filtro por CNPJ/razão social, clique longo para remover |
| 8 | Novo fornecedor | Formulário com CNPJ, categoria (chips), prazo numérico e observações em várias linhas |
| 9 | Detalhe do fornecedor | Dados + cálculos + estabelecimentos da mesma empresa (navegação secundária) + edição |
| 10 | Ajuda | Perguntas frequentes que abrem e fecham |

Barra inferior (`NavigationBar`): **Menu · Clientes · Fornecedores · Ajuda**.

## Estrutura do código

```
app/src/main/java/br/com/gestaosuprimentos/
├── MainActivity.kt
├── data/
│   ├── Modelos.kt           # data classes Cliente, Fornecedor, Usuario
│   └── GestaoViewModel.kt   # listas reativas (mutableStateListOf) + regras de validação
├── navigation/
│   ├── Rotas.kt             # object Rotas com as rotas em const val
│   └── AppNavigation.kt     # NavHost central + NavigationBar
├── ui/
│   ├── theme/Theme.kt       # cores do Figma
│   ├── componentes/         # componentes reutilizáveis e formulários
│   └── telas/               # uma tela (ou grupo de telas) por arquivo
└── util/
    ├── Validadores.kt       # CPF/CNPJ, formatação e cálculos de data
    └── Mascara.kt           # VisualTransformation para CPF, CNPJ, telefone e data
```

## Tecnologias

Kotlin 2.0 · Jetpack Compose (Material 3) · Navigation Compose 2.7.7 · ViewModel · minSdk 26.

Os dados ficam **apenas em memória** (`mutableStateListOf`), como pede o enunciado — ao fechar o app, voltam os dados de exemplo.

## Integrantes

- Vinicius Vicente Angelino
- Marcos Danrlei de Araujo
