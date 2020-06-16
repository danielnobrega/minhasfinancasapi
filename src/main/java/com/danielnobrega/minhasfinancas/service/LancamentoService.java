package com.danielnobrega.minhasfinancas.service;

import java.util.List;

import com.danielnobrega.minhasfinancas.enums.StatusLancamento;
import com.danielnobrega.minhasfinancas.model.entity.Lancamento;

public interface LancamentoService {

	Lancamento salvar(Lancamento lancamento);
	
	Lancamento atualizar(Lancamento lancamento);
	
	void deletar(Lancamento lancamento);
	
	List<Lancamento> buscar( Lancamento lancamento );
	
	void atualizarStatus(Lancamento lancamento, StatusLancamento status);
	
	void validar(Lancamento lancamento);
}
