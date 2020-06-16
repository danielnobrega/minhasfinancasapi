package com.danielnobrega.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danielnobrega.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
