package br.com.zg.desafio.impl

import br.com.zg.desafio.CalculadoraRepasse
import br.com.zg.desafio.DadosInvalidosException
import br.com.zg.desafio.modelo.Glosa
import br.com.zg.desafio.modelo.SolicitacaoRepasse

import java.math.RoundingMode
import java.text.Normalizer
import java.time.Duration

/**
 * Implementação de referência da especificação (ver README).
 *
 * É contra esta classe que você desenvolve, e é aqui que sua suíte
 * precisa estar 100% verde para a entrega ser válida.
 */
class Referencia implements CalculadoraRepasse {

    private static final BigDecimal TAXA_ADICIONAL = new BigDecimal('0.15')
    private static final BigDecimal TAXA_DESCONTO  = new BigDecimal('0.10')
    private static final Set<String> PARCEIROS = ['saude total', 'vida plena', 'bem viver'] as Set

    /*
    Lista de erros que os testes perderam?:

    R3 antes de R2
    TESTAR Para modo HALF EVEN
    se entrada for depois de saida
    se valor de tabela for ausente (mensagens de erros corretas)
    * */

    @Override
    BigDecimal calcular(SolicitacaoRepasse solicitacao) {

        // --- validação de entrada ---
        if (solicitacao == null) {
            throw new DadosInvalidosException('solicitacao nula')
        }
        if (solicitacao.procedimento?.valorTabela == null) {
            throw new DadosInvalidosException('valorTabela ausente')
        }
        if (solicitacao.procedimento.valorTabela < BigDecimal.ZERO) {
            throw new DadosInvalidosException('valorTabela negativo')
        }
        if (solicitacao.internacao?.entrada == null || solicitacao.internacao?.saida == null) {
            throw new DadosInvalidosException('internacao incompleta')
        }
        if (solicitacao.internacao.saida.isBefore(solicitacao.internacao.entrada)) {
            throw new DadosInvalidosException('saida antes da entrada')
        }
        if (!solicitacao.convenio?.trim()) {
            throw new DadosInvalidosException('convenio ausente')
        }
        solicitacao.glosas?.each { Glosa g ->
            if (g?.valor == null) throw new DadosInvalidosException('glosa sem valor')
            if (g.valor < BigDecimal.ZERO) throw new DadosInvalidosException('glosa negativa')
        }

        // --- R1 ---
        BigDecimal tabela = solicitacao.procedimento.valorTabela
        BigDecimal subtotal = tabela

        // --- R2 ---
        long horas = Duration.between(solicitacao.internacao.entrada, solicitacao.internacao.saida).toHours()
        if (horas >= 24) {
            subtotal = subtotal + tabela * TAXA_ADICIONAL
        }

        // --- R3 ---
        String convenio = Normalizer
                .normalize(solicitacao.convenio.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll(/\p{M}/, '')
        if (convenio in PARCEIROS) {
            subtotal = subtotal - subtotal * TAXA_DESCONTO
        }

        // --- R4 ---
        BigDecimal totalGlosas = (solicitacao.glosas ?: [])
                .inject(BigDecimal.ZERO) { BigDecimal acc, Glosa g -> acc + g.valor }
        BigDecimal resultado = subtotal - totalGlosas

        // --- R5 ---
        if (resultado < BigDecimal.ZERO) {
            resultado = BigDecimal.ZERO
        }

        // --- R6 ---
        return resultado.setScale(2, RoundingMode.HALF_EVEN)
    }
}
