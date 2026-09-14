package br.com.zg.desafio

import spock.lang.Unroll

import java.math.RoundingMode
import java.time.format.DateTimeParseException

/**
 * SUA ENTREGA.
 *
 * Renomeie a classe (e o arquivo) para MinhaSuite<SeuNome>Spec.
 * Exemplo: MinhaSuiteJoanaSpec.groovy
 *
 * Regras:
 *  - use somente o contrato público: calculadora.calcular(...)
 *  - nunca instancie uma implementação diretamente — use o campo `calculadora`
 *  - todo bloco `then:` / `expect:` precisa de ao menos uma asserção real
 *  - a suíte inteira precisa passar contra a referência (rode ./verificar.sh)
 *
 * Os três testes abaixo existem só para mostrar a sintaxe do Spock.
 * Eles não valem ponto. Pode apagá-los.
 */

class MinhaSuiteHenriqueSpec extends DesafioSpec {
    // ---- bloco when/then ----
    def "repassa o valor de tabela quando não há adicional, desconto nem glosa"() {
        when:
        def resultado = calculadora.calcular(solicitacao())

        then:
        resultado == new BigDecimal('1000.00')
    }

    // ---- bloco where: uma tabela de casos, um teste por linha ----
    //R4
    @Unroll
    def "glosas de #descricao reduzem o repasse para #esperado"() {
        expect:
        calculadora.calcular(solicitacao(glosas: glosas)) == new BigDecimal(esperado)

        where:
        descricao     | glosas                            || esperado
        'nenhuma'     | []                                || '1000.00'
        'uma glosa'   | [glosa('250.00')]                 || '750.00'
        'duas glosas' | [glosa('250.00'), glosa('50.00')] || '700.00'
    }

    // ---- exceções ----
    def "convênio em branco é entrada inválida"() {
        when:
        calculadora.calcular(solicitacao(convenio: '   '))

        then:
        thrown(DadosInvalidosException)
    }

    // ==================================================================
    // A partir daqui é com você.
    // ==================================================================


    @Unroll
    def "R2: uma internacao de 24h+ (#tempo) soma ao subtotal 15% do valor de tabela"(BigDecimal valor) {
        when:
        BigDecimal esperado = valor + valor * 0.15
        esperado = esperado.setScale(2, RoundingMode.HALF_EVEN)

        then:
        calculadora.calcular(solicitacao(valorTabela: valor, entrada: entrada, saida: saida)) == esperado


        where:
        tempo                 | entrada               | saida                 | valor
        '24 horas'            | '2026-05-04T08:00:00' | '2026-05-05T08:00:00' | 100032.432
        '24 horas'            | '2026-05-04T08:00:00' | '2026-05-05T08:00:00' | 1012342.332
        '48 horas'            | '2026-05-04T08:00:00' | '2026-05-06T08:00:00' | 1234.5432
        '48 horas'            | '2026-05-04T08:00:00' | '2026-05-06T08:00:00' | 1261729334.512432

        '24 horas e um pouco' | '2026-05-04T08:00:00' | '2026-05-05T08:00:01' | 12325325.123
        '1 ano'               | '2026-05-04T08:00:00' | '2027-05-04T08:00:00' | 0
    }
    @Unroll
    def "R2: uma internação de menos de 24h (#tempo) nao gera taxas extras"(BigDecimal valor) {
        when:
        BigDecimal esperado = valor.setScale(2, RoundingMode.HALF_EVEN)

        then:
        calculadora.calcular(solicitacao(valorTabela: valor, entrada: entrada, saida: saida)) == esperado

        where:
        tempo         | entrada               | saida                 | valor
        '23h 59m 59s' | '2026-05-04T08:00:00' | '2026-05-05T07:59:59' | 100032.432
        '23h 59m 59s' | '2026-05-04T08:00:00' | '2026-05-05T07:59:59' | 11243.1234
        '1h'          | '2026-05-04T08:00:00' | '2026-05-04T09:00:00' | 141233.1234
        '1s'          | '2026-05-04T08:00:00' | '2026-05-04T08:00:01' | 1733.1234
        'instantaneo' | '2026-05-04T08:00:00' | '2026-05-04T08:00:00' | 112412312233.12342143
    }


    @Unroll
    def "R3: #convenio é parceiro. Subtraia do subtotal 10% do subtotal corrente"(BigDecimal valor) {
        when:
        BigDecimal esperado = valor - valor * 0.10
        esperado = esperado.setScale(2, RoundingMode.HALF_EVEN)

        then:
        calculadora.calcular(solicitacao(valorTabela: valor, convenio: convenio)) == esperado

// @formatter:off
        where:
        convenio               | valor
        "saude total"          | 1000
        "vida plena"           | 213459.817269
        "bem viver"            | 1.18726398
        "saude total"          | 0.124708

        //maiusuclas
        "Bem Viver"             | 1351427639
        "Saude Total"           | 1000.3333333
        "BEM VIVER"             | 67.420
        "VIDA PLENA"            | 123.1267498124
        "viDa PLEnA"            | 321

        //acento
        "saúde total"           | 123.231647
        "sâúdè tótàl"           | 444.123
        "s̗̀́̚ā̛̘̀û̘̖̈d̙́̆̚e̛̛̅̄ ̙̃̀̚ẗ̛̙̅ó̘̖̀t̗̗̀̀à̙̖́l̘̀̄̚"   | 30   //https://henriqf.github.io/pwa-formatador/
        "ḃ̘ê̛ṁ̙ ̙̇v̛̄ï̚v̙̈ė̚r̙̂"           | 10000.21346
        "v̘̗̖̘̖̘̘̗̗̖̗́̅̀́́̃̇̈̅̇̄̇̚í̛̙̖̗̙̗̗̖̗̖̆́̇̂̀̂̀̈́̇̈̚̚d̛̘̙̗̖̖̖̘̗̖̃̆̇̃̂̀̃̀̈̈̀̈̚̚ẫ̖̗̖̘̘̙̖́̄̀̈̀̂̆̂̈̇̚̚̚̚̚ ̛̛̘̖̗̖̙̘̗̙̙́̂̈̈̄̇̆̃̅̆̃̄̚p̛̛̛̘̗̗̘̙̙̖̖̃̈́̈̀̇̇̂̈̂̇̈̚l̗̘̗̖̖̗̘̘̖̘̖̈̀̈̇́̅̄̅̃̇̇̆̚è̙̗̗̙̖̘̙̖̗̖̙̖̃̆̆̃́̇̇̃̈̀̄n̛̘̘̗̙̗̘̙̘̄̅̈́̃̂́̂̈́̀̂̚̚̚â̘̙̗̙̙̗̙̘̗̖̄̇̅̅̄̈́̇̄̈̇̚̚" | 33

        //espaco branco
        "  saude total  "      | 15423.1794654
        "bem viver\t\n\r   "   | 1000.36298143124
        "\n\r\t\fvida plena"   | 1000.223
        "    vida plena    "   | 32125478901239786452123

        //tudo
        " \r\n Sâúdè tótàL\t "                 | 4217356.432158
        "\t \t \n \rS̛̘̃̇̆̚ā̛̙̂̈̚Ư̗̗̇̆̃d̘̗̙̆̆̀ē̛̖̆̇̚ ̛̘̗̄̄́T̙̙̗̀̅̇Ờ̗̖̄̇t̛̙̘̂̂̅A̙̘̙̅̆̀L̗̗̄̈́̚  \t"  | 1234123.326918
// @formatter:on
    }
    @Unroll
    def "R3: #convenio não é parceiro; Sem descontos."(BigDecimal valor) {
        when:
        BigDecimal esperado = valor.setScale(2, RoundingMode.HALF_EVEN)

        then:
        calculadora.calcular(solicitacao(valorTabela: valor, convenio: convenio)) == esperado

        where:
        convenio       | valor
        "aura"         | 1000
        "ghfsadijf"    | 213569.123
        "    ç    "    | 213.4112438
        "saude  total" | 21378.196283
        "vidaplena"    | 125463.634523
        "  BEMVIVER"   | 12435789.21583
    }


    @Unroll
    def "R4 (EXTENSÃO): subtraido do total a soma de #descricao"() {
        when:
        BigDecimal resultado = calculadora.calcular(solicitacao(valorTabela: valor, glosas: glosas))

        then:
        resultado == esperado.setScale(2, RoundingMode.HALF_EVEN)

        where:
        descricao           | valor    | esperado | glosas
        "uma golsa de mil"  | 1000.00  | 0.00     | [glosa("1000")]
        "uma golsa de mil"  | 2000.00  | 1000.00  | [glosa("1000")]

        "mil glosas de um"  | 1000.00  | 0.00     | [glosa("1")] * 1000
        "mil glosas de um"  | 2000.00  | 1000.00  | [glosa("1")] * 1000

        "glosas de 0.01"    | 100.00   | 0.00     | [glosa("0.01")] * 10000
        "glosas de 0.01"    | 200.00   | 100.00   | [glosa("0.01")] * 10000

        "glosas nulas de 0" | 56234.12 | 56234.12 | [glosa("0")] * 20

        "sem glosas"        | 1213.12  | 1213.12  | []
    }


    @Unroll
    def "R5: se o resultado for negativo, passa a ser 0: (#descricao)"() {
        expect:
        calculadora.calcular(solicitacao(parametros)) == BigDecimal.ZERO

        where:
        descricao                             | parametros
        'glosa de 1001'                       | [glosas: [glosa('1001.00')]]
        'glosa milhonaria'                    | [glosas: [glosa('1000000.00')]]

        "valor inicial 0, glosa alta"         | [valorTabela: 0, glosas: [glosa("23123.2")]]
        "valor inicial 67 glosas 67, 0.00001" | [valorTabela: 67, glosas: [glosa("67"), glosa("0.00001")]]
        "glosa com desconto de parceiro"      | [convenio: "saude total", glosas: [glosa("900.000001")]]
    }


    def "R6: resultado final tem exatamente 2 casas decimais"() {
        when:
        def total = calculadora.calcular(solicitacao(valorTabela: valor))

        then:
        (total * 100) % 1 == 0

        and:
        total.scale() == 2

        where:
        valor << [12, 124592.432141285, 4321489072134.51243218, 183, 41, 321, 5, 6, 45, 0, 1217490174, 0.3214342, 312587324]
    }


    def "(EXTENSÃO) repassa o valor de tabela quando não há adicional, desconto nem glosa"(BigDecimal valor) {
        when:
        def resultado = calculadora.calcular(solicitacao(valorTabela: valor))
        def esperado = valor.setScale(2, RoundingMode.HALF_EVEN)

        then:
        resultado == esperado

        where:
        valor << [124.0123, 32141234.12, 3214.413, 3124.12, 518924, 231048971, 0, 123, 1000, 34259.32, 1845123.12, 84752.00, 456782421647981244.92]
    }


    //conferir dados invalidos
    def "solicitação nula é invalida"() {
        when:
        calculadora.calcular(null)

        then:
        thrown(DadosInvalidosException)
    }

    def "valor tabela nulo ou negativo é invalido"() {
        when:
        calculadora.calcular(solicitacao(valorTabela: valor))

        then:
        def ex = thrown(DadosInvalidosException)
        println ex.message
        //thrown(DadosInvalidosException)

        where:
        valor << [null] + (-1000000..-1).step(10000)
    }

    def "(entrada ou saida nulas) ou (saida antes de entrada) é invalido"() {
        when:
        calculadora.calcular(solicitacao(entrada: entrada, saida: saida))

        then:
        thrown(ex)

        // acho que esse nullpointerexecption nao foi planejado
        where:
        entrada               | saida                 | ex
        null                  | null                  | NullPointerException
        null                  | '2026-05-05T07:59:59' | NullPointerException
        '2026-05-04T08:00:00' | null                  | NullPointerException
        '2026-05-04T08:00:00' | '2026-05-03T08:00:00' | DadosInvalidosException
        '2026-05-04T08:00:00' | '2026-05-04T07:59:59' | DadosInvalidosException
        '2026-05-04T08:00:00' | '2026-05-04T07:00:00' | DadosInvalidosException
    }

    def "tempos extranhos de entrada e saida são invalidos"() {
        when:
        calculadora.calcular(solicitacao(entrada: entrada, saida: saida))

        then:
        thrown(DateTimeParseException)

        where:
        entrada               | saida
        '2026-05-04T08:00:00' | 'valido texto etc'
        'mais valido ainda t' | '2026-05-04T07:00:00'

        'beto carreiro world' | 'propagandas'
        ''                    | ''
        '2026-05-04T67:00:00' | '2026-05-04T67:00:00'

    }

    def "convenio nulo ou branco é invalido"() {
        when:
        calculadora.calcular(solicitacao(convenio: convenio))

        then:
        thrown(DadosInvalidosException)

        where:
        convenio << [null, "", " ", "\t", "\n", "\r"]
    }

    def "glosa de valor nulo ou negativo é inválida"() {
        when:
        calculadora.calcular(solicitacao(glosas: glosa))

        then:
        thrown(DadosInvalidosException)

        where:
        glosa << [[null], [glosa("-1")], [glosa("-2")], [glosa("-12398.32")], [[glosa("-1000.12783")]]]
    }


    //funcao calcular é pura ()
    def "funcao calcular nao modifica as entradas"() {
        when:
        def parametros = parametros_closure()
        def copia = parametros_closure()

        and:
        calculadora.calcular(solicitacao(copia))

        then:
        copia == parametros

        where:
        parametros_closure << [
                { [glosas: [glosa('1001.00')]] },
                { [glosas: [glosa('1000000.00')]] },
                { [valorTabela: 0, glosas: [glosa("23123.2")]] },
                { [valorTabela: 67, glosas: [glosa("67"), glosa("0.00001")]] },
                { [convenio: "saude total", glosas: [glosa("900.000001")]] }
        ]
    }

    def "funcao calcular responde consistentemente"() {
        when:
        def A = parametros_closure()
        def B = parametros_closure()

        then:
        calculadora.calcular(solicitacao(A)) == calculadora.calcular((solicitacao(B)))

        where:
        parametros_closure << [
                { [glosas: [glosa('1001.00')]] },
                { [glosas: [glosa('1000000.00')]] },
                { [valorTabela: 0, glosas: [glosa("23123.2")]] },
                { [valorTabela: 67, glosas: [glosa("67"), glosa("0.00001")]] },
                { [convenio: "saude total", glosas: [glosa("900.000001")]] },
        ] * 2
    }
}
//boa parte desses testes dependem de assumir que outro componente do calculo funciona corretamente...
//nao sei como testar isoladamente já que internamente é tudo sequencial