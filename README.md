 # Desafio: Seja um sentinela, matando mutantes

Você **não vai escrever código de produção**. Você vai escrever apenas a suíte de testes.

Sua suíte será executada contra **12 implementações** desta mesma especificação: uma correta e onze com defeito. Cada implementação defeituosa que sua suíte reprovar conta um ponto.

Nenhum dos onze defeitos é um erro de digitação. Todos são código que alguém escreveu acreditando estar certo.

---

## O domínio

Cálculo do valor de repasse de um procedimento hospitalar.

```groovy
interface CalculadoraRepasse {
    BigDecimal calcular(SolicitacaoRepasse solicitacao)
}
```

```groovy
class SolicitacaoRepasse {
    Procedimento procedimento   // codigo: String, valorTabela: BigDecimal
    Internacao   internacao     // entrada: ZonedDateTime, saida: ZonedDateTime
    String       convenio
    List<Glosa>  glosas         // motivo: String, valor: BigDecimal
}
```

---

## Especificação

O cálculo acontece **nesta ordem**:

| # | Regra |
|---|---|
| **R1** | O subtotal começa igual a `procedimento.valorTabela`. |
| **R2** | Se a internação durou **24 horas ou mais**, some ao subtotal 15% do valor de tabela. |
| **R3** | Se o convênio é parceiro, subtraia do subtotal 10% **do subtotal corrente** (ou seja, já com o adicional de R2, quando houver). |
| **R4** | Subtraia do subtotal a soma dos valores de todas as glosas. |
| **R5** | Se o resultado for negativo, ele passa a ser zero. |
| **R6** | Retorne o resultado com exatamente **2 casas decimais**, arredondado com **`RoundingMode.HALF_EVEN`**. |

**Convênios parceiros:** `saude total`, `vida plena`, `bem viver`.
A identificação ignora diferença de maiúsculas/minúsculas, acentuação e espaços nas bordas.

**Duração da internação:** tempo absoluto decorrido entre `entrada` e `saida`, truncado para horas inteiras.

**Entradas inválidas** lançam `DadosInvalidosException`:

- `solicitacao` nula
- `valorTabela` nulo ou negativo
- `entrada` ou `saida` nulas, ou `saida` anterior a `entrada`
- `convenio` nulo ou em branco
- qualquer glosa com valor nulo ou negativo

`glosas` nula é tratada como lista vazia — isso **não** é erro.

**Além disso:** `calcular` é uma função pura. Não modifica nada que recebe, e a mesma entrada sempre produz a mesma saída.

Isso é a especificação inteira. Não há regra escondida.

---

## Instruções para solucionar o problema

1. Clone esse repositório ou faça um forque
2. A entrega efetiva será **apenas** o arquivo `MinhaSuite<SeuNome>Spec.groovy`, estendendo `DesafioSpec`. Preste atenção, seu nome deve ser inserido no nome do arquivo, de acordo com a intrução
2. Utilize **somente** o contrato público `calcular(...)`. Nada de reflexão, nada de acessar internals.
3. Não altere o código da calculadora, sua tarefa não é refatorar ele, é apenas elaborar os testes.
3. Todo bloco `then:`/`expect:` precisa conter ao menos uma asserção real. Suíte sem asserção não são válidas.
4. Sua suíte precisa passar **100%** contra a implementação de referência. Uma única falha ali zera a entrega — um teste que acusa código correto é pior que teste nenhum.



---


## Como rodar

Requer JDK 17+ e Gradle 8.x. Se o wrapper não vier no clone, gere com `gradle wrapper`.

```bash
./gradlew test        # desenvolve aqui
./verificar.sh        # sanity check antes de entregar
```

`verificar.sh` roda sua suíte contra a referência (implementação correta que você tem aceso) e falha se qualquer teste ficar vermelho.
A avaliação contra as implementações defeituosas (Mutantes) será rodada pelo intrutor quando for avaliar.

---

## Como entregar

1. Renomeie `MinhaSuiteSpec.groovy` para `MinhaSuite<SeuNome>Spec.groovy` (e a classe junto).
2. Apague os testes de exemplo, ou fique com eles — não faz diferença
3. `./verificar.sh` precisa passar.
4. Faça commit para seu repositório contendo as alterações em `MinhaSuite<SeuNome>Spec.groovy`. Qualquer alteração em `src/main/` ou em `DesafioSpec.groovy` invalida a entrega.
5. Avise o instrutor, assim ele pode validar sua entrega contra os mutantes e te informar o resultado
6. O instrutor te retorna o resultado *"sua suíte matou N de 11"* — sem dizer quais
7. Se mutantes sobreviveram, raciocíne o que pode ter ocorrido e faça mais testes para serem validados

Saber que sobraram sete e não saber quais te obriga a fazer a pergunta certa: *que tipo de coisa eu ainda não estou testando?*

---


## O repositório

```
src/main/groovy/br/com/zg/desafio/
  CalculadoraRepasse.groovy        contrato — a única coisa que você testa
  DadosInvalidosException.groovy
  modelo/                          Procedimento, Internacao, Glosa, SolicitacaoRepasse
  impl/Referencia.groovy           implementação correta da spec

src/test/groovy/br/com/zg/desafio/
  DesafioSpec.groovy               classe base + fixtures (não altere)
  MinhaSuiteSpec.groovy            >>> SUA ENTREGA <<<
```

`DesafioSpec` já te dá `calculadora` pronta e alguns atalhos:

```groovy
solicitacao()                                   // caso padrão: 1000.00, 12h, convênio avulso, sem glosas
solicitacao(valorTabela: '250.00')              // sobrescreve só o que interessa
solicitacao(entrada: '2026-05-04T08:00:00',
            saida:   '2026-05-05T14:00:00')
solicitacao(convenio: 'Vida Plena')
solicitacao(glosas: [glosa('80.00'), glosa('20.00')])
```

Nunca instancie uma implementação diretamente. Use sempre o campo `calculadora` — é ele que a avaliação troca por baixo dos panos.

---
## Uma dica, e só uma

Cada regra acima é uma afirmação sobre um caso. Defeitos raramente vivem dentro de um caso — vivem nas beiradas (edges) entre eles.
