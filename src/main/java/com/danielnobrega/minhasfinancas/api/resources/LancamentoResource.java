package com.danielnobrega.minhasfinancas.api.resources;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.danielnobrega.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.danielnobrega.minhasfinancas.api.dto.LancamentoDTO;
import com.danielnobrega.minhasfinancas.enums.StatusLancamento;
import com.danielnobrega.minhasfinancas.enums.TipoLancamento;
import com.danielnobrega.minhasfinancas.exceptions.RegraNegocioException;
import com.danielnobrega.minhasfinancas.model.entity.Lancamento;
import com.danielnobrega.minhasfinancas.model.entity.Usuario;
import com.danielnobrega.minhasfinancas.service.LancamentoService;
import com.danielnobrega.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {

	private final LancamentoService service;

	private final UsuarioService usuarioService;

	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO lancamento ) {

		try {
			Lancamento entidade = service.salvar(converter(lancamento));
			return new ResponseEntity(entidade, HttpStatus.CREATED);
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable("id") Long id,	@RequestBody LancamentoDTO dto ) {

		return service.obterPorId(id).map( entity -> {
			Lancamento lancamento = converter(dto);
			lancamento.setId(entity.getId());
			try {
				service.atualizar(lancamento);
				return ResponseEntity.ok(lancamento);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}

		}).orElseGet( () -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));
	}	
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto) {
		return service.obterPorId(id).map( entity -> {
			StatusLancamento status = StatusLancamento.valueOf(dto.getStatus());
			
			if (status == null) {
				return ResponseEntity.badRequest().body("Não foi possível atualizar o status. Favor enviar um status válido");
			}
			
			entity.setStatus(status);
			
			try {
				service.atualizar(entity);
				return ResponseEntity.ok(entity);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}

		}).orElseGet( () -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));
	}

	@DeleteMapping("{id}")
	public ResponseEntity deletar (@PathVariable("id") Long id) {
		return service.obterPorId(id).map( entity -> {
			try {

				service.deletar(entity);
				return new ResponseEntity(HttpStatus.NO_CONTENT);

			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}

		}).orElseGet( () -> new ResponseEntity("Lançamento não encontrado na base de dados", HttpStatus.BAD_REQUEST));

	}

	@GetMapping
	public ResponseEntity buscar (
			@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam("usuario") Long idUsuario
			) {

		Lancamento filtro = new Lancamento() ;
		filtro.setDescricao(descricao);
		filtro.setMes(mes);
		filtro.setAno(ano);

		Optional <Usuario> usuario = usuarioService.obterPorId(idUsuario);

		if (!usuario.isPresent() ) {
			return ResponseEntity.badRequest().body("Não foi possível realizar a consulta. Usuário não encontrado");
		} else {
			filtro.setUsuario(usuario.get());
		}

		List<Lancamento> lancamentos = service.buscar(filtro);

		return ResponseEntity.ok(lancamentos);
	}

	private Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();

		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());

		Usuario usuario = usuarioService
				.obterPorId(dto.getUsuario())
				.orElseThrow( () -> new RegraNegocioException("Usuário não encontrado para o Id informado"));

		lancamento.setUsuario(usuario);
		
		if (dto.getTipo() != null)
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));

		if (dto.getStatus() != null)
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));

		return lancamento;


	}

}
