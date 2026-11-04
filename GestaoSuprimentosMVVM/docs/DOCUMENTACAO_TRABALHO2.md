# Documentação do processo e das decisões — Trabalho 2 (MAF)

> ⚠️ **Antes de entregar:** troque todos os trechos marcados com ✏️ pelo que realmente aconteceu no grupo
> e coloque os prints na pasta `docs/prints/` com os nomes indicados. O enunciado não aceita a documentação sem imagens.

---

## 1. Como estava o projeto no Trabalho 1 e o que mudou

✏️ *Descrevam com as palavras de vocês. Sugestão de ponto de partida:*

No Trabalho 1 fizemos o protótipo no Figma (login, menu, cadastro de cliente, busca de cliente, novo fornecedor e busca de fornecedor)
e programamos 3 telas estáticas: ✏️ *quais foram?*. Os botões existiam, mas não trocavam de tela, e os dados eram fixos.

No Trabalho 2:

- As telas viraram um app navegável de verdade com `NavHost` e `NavigationBar`.
- As telas de "busca" do Figma (Cliente / Fornecedor, com CPF/CNPJ e nome) viraram **listas com filtro**: os mesmos campos agora filtram a lista em tempo real.
- As telas de "cadastro" do Figma salvam de verdade na lista (`mutableStateListOf`).
- O botão **AJUDA** do rodapé do protótipo virou um item da barra inferior.
- O ícone de "hambúrguer" do protótipo virou o ícone da `TopAppBar` que leva ao Menu.
- Do menu do Figma, mantivemos só as opções que funcionam (Vendas, Estoque etc. ficam para depois), porque o enunciado proíbe botão sem ação.

| Trabalho 1 | Trabalho 2 |
|---|---|
| ![T1](docs/prints/t1-tela.png) | ![T2](docs/prints/t2-tela.png) |

---

## 2. As telas novas: o que cada uma faz e por que escolhemos

| Tela | Por que existe | Print |
|---|---|---|
| **Lista de clientes** | Ver todos os clientes, filtrar por CPF/nome, marcar ativo/inativo (Checkbox) e remover (lixeira) | ![](docs/prints/04-clientes.png) |
| **Cadastrar cliente** | Adicionar cliente com validação de CPF (dígito verificador) e máscaras | ![](docs/prints/05-cadastro-cliente.png) |
| **Detalhe do cliente** | Mostrar o cliente certo (pelo id da rota) + dados calculados + edição | ![](docs/prints/06-detalhe-cliente.png) |
| **Lista de fornecedores** | Ver/filtrar fornecedores; clique longo (`combinedClickable`) para remover | ![](docs/prints/07-fornecedores.png) |
| **Novo fornecedor** | Cadastro com CNPJ, categoria (chips), prazo numérico e observações em várias linhas | ![](docs/prints/08-cadastro-fornecedor.png) |
| **Detalhe do fornecedor** | Dados + cálculos + outros estabelecimentos da mesma empresa | ![](docs/prints/09-detalhe-fornecedor.png) |
| **Criar novo acesso** | O protótipo já tinha o botão "Criar novo acesso" no login; agora ele funciona | ![](docs/prints/02-criar-acesso.png) |
| **Ajuda** | O botão "Ajuda" aparecia em todas as telas do protótipo | ![](docs/prints/10-ajuda.png) |

As duas *data classes* são **Cliente** e **Fornecedor**, que são os dois cadastros centrais de um sistema de gestão de suprimentos
(quem compra da empresa e de quem a empresa compra).

---

## 3. Decisões de organização do código

- **Rotas em um `object Rotas`** com `const val`, e funções `clienteDetalhe(id)` / `fornecedorDetalhe(id)` para montar a rota com o id. Assim ninguém digita string de rota solta pelo código.
- **Um único `NavHost` em `AppNavigation.kt`**, do jeito visto em aula: `rememberNavController()` e cada tela recebendo o `navController` (`ClientesListaScreen(navController, vm)`), que ela usa para chamar `navigate(...)` e `popBackStack()`. Por fora tem um `Scaffold` que mostra a `NavigationBar` em todas as telas, menos Login e Criar acesso.
- **Troca de aba sem empilhar telas:** a barra inferior navega com `popUpTo(MENU) { saveState = true }`, `launchSingleTop` e `restoreState`. Assim, voltar de uma aba não percorre um histórico enorme.
- **A aba certa fica acesa nas telas internas:** o cadastro e o detalhe de cliente deixam a aba "Clientes" selecionada (função `areaDaRota`).
- **As listas ficam no `GestaoViewModel`** (um só, criado no `AppNavigation` e passado às telas). Assim todas as telas enxergam os mesmos dados e a lista não se perde ao trocar de tela.
- **O detalhe recebe só o id** pela rota (`cliente_detalhe/{clienteId}`), lido com `backStackEntry.arguments?.getString("clienteId")` como no exemplo de "Passando dados entre telas". A tela converte para número e busca o item no ViewModel. Assim o detalhe mostra sempre a versão mais atual, inclusive depois de editar.
- **`Toast`** (aula de Mudar Tela) confirma cadastro, edição e remoção.
- **Os formulários são componentes reaproveitados** (`FormularioCliente` / `FormularioFornecedor`): o mesmo formulário aparece na tela de cadastro e na edição dentro do detalhe.
- **CPF, CNPJ, telefone e data ficam salvos só com números**, e uma `VisualTransformation` (`Mascara.kt`) desenha os pontos, barras e traços na tela.
- **A validação fica no ViewModel** (`validarCliente` / `validarFornecedor`) e devolve um mapa campo → mensagem, mostrado embaixo de cada campo.

---

## 4. A complexidade extra nas telas de detalhe

**Detalhe do fornecedor** (a principal):

1. **Informações calculadas**:
   - Valida o CNPJ pelo dígito verificador.
   - Descobre se o estabelecimento é **matriz ou filial** (pelos dígitos 9–12 do CNPJ).
   - Calcula a **data prevista de entrega** se o pedido for feito hoje, somando o prazo em **dias úteis** (pula sábado e domingo).
2. **Cruzamento de dados da lista**: mostra os **outros estabelecimentos da mesma empresa** (mesma raiz de CNPJ) e qual deles entrega mais rápido.
3. **Navegação secundária**: tocar em um desses estabelecimentos abre o detalhe dele. É possível ir da matriz para a filial e voltar com a seta.
4. **Edição no próprio detalhe** (lápis na `TopAppBar`).

**Detalhe do cliente:**

- Valida o CPF.
- Calcula a **idade**, os **dias até o próximo aniversário** e **há quantos dias é cliente**.
- Permite editar e ativar/desativar.

✏️ *Por que escolhemos essas:* para um comprador, saber se o fornecedor é matriz ou filial, quando o pedido chega
e qual unidade da mesma empresa entrega mais rápido é uma decisão real do dia a dia de suprimentos.

![](docs/prints/09-detalhe-fornecedor.png) ![](docs/prints/09b-mesma-empresa.png) ![](docs/prints/09c-editar.png)

---

## 5. Dificuldades e como resolvemos

✏️ *Contem o que realmente aconteceu. Exemplos de coisas que costumam travar:*

- *Passar o id pela rota: ele chega como `String` em `getString(...)` e precisa virar `Int` (`toIntOrNull()`) para buscar o item.*
- *Máscara de CPF/CNPJ: a primeira tentativa salvava os pontos junto e quebrava a validação; separamos "o que é salvo" (só números) de "o que aparece" (VisualTransformation).*
- *A barra inferior empilhando telas repetidas, resolvido com `launchSingleTop` + `popUpTo`.*

---

## 6. Divisão do trabalho

| Integrante | Responsável por |
|---|---|
| ✏️ Nome 1 | ✏️ ex.: navegação, Menu, Login/Criar acesso |
| ✏️ Nome 2 | ✏️ ex.: Clientes (lista, cadastro, detalhe) |
| ✏️ Nome 3 | ✏️ ex.: Fornecedores (lista, cadastro, detalhe) |

---

## Roteiro da apresentação (2 min)

1. Login com `admin` / `admin` → Menu (mostrar os números do resumo).
2. Aba **Clientes** → filtrar por nome → **cadastrar** um cliente (mostrar erro de CPF inválido, depois um válido).
3. Abrir o detalhe → mostrar idade/aniversário → **editar** → **desmarcar o Checkbox** na lista → **remover** pela lixeira.
4. Aba **Fornecedores** → abrir a *Madeireira Araucária* → previsão de entrega → tocar na **filial** (navegação secundária) → voltar.
5. Cadastrar um fornecedor → **segurar** o card para remover.
6. Navegar pela barra inferior e mostrar a **Ajuda**.

> CPFs válidos para testar: `529.982.247-25`, `111.444.777-35` (já usados), `935.411.347-80`.
> CNPJs válidos: `11.222.333/0001-81` (já usado), `11.444.777/0003-23`.
