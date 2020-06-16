package com.danielnobrega.minhasfinancas.model.service;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.danielnobrega.minhasfinancas.exceptions.ErroAutenticacao;
import com.danielnobrega.minhasfinancas.exceptions.RegraNegocioException;
import com.danielnobrega.minhasfinancas.model.entity.Usuario;
import com.danielnobrega.minhasfinancas.model.repository.UsuarioRepository;
import com.danielnobrega.minhasfinancas.service.impl.UsuarioServiceImpl;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {

	@SpyBean
	UsuarioServiceImpl service;
	
	@MockBean
	UsuarioRepository repository;

	@Test(expected = Test.None.class)
	public void deveValidarEmail() {
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
		service.validarEmail("email@email.com");
	}
	
	@Test(expected = RegraNegocioException.class)
	public void deveLancarExceptionAoValidarEmailQuandoExistirErroCadastrado() {
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		service.validarEmail("email@email.com");
	}
	
	@Test(expected = Test.None.class)
	public void deveAutenticarUsuarioComSucesso() {
		String email = "email@email.com";
		String senha = "senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
		
		Usuario result = service.autenticar(email, senha);
		
		Assertions.assertThat(result).isNotNull();
	}
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty()); 
		
		Throwable exception = Assertions.catchThrowable(() -> service.autenticar("email@email.com", "senha"));
		
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário não encontrado");
	}
	
	@Test
	public void deveLancarErroQuandoSenhaNaoBater() {
		String email = "email@email.com";
		String senha = "senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).id(1l).build();
		
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
		
		Throwable exception = Assertions.catchThrowable(() -> service.autenticar(email, "123"));
		
		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Senha não confere");
		
	}
	
	@Test(expected = Test.None.class)
	public void deveSalvarUsuarioComSucesso() {
		//cenario
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		
		String email = "email@email.com";
		String senha = "senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(senha).nome("nome").id(1l).build();
		
		Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);

		//acao
		service.salvarUsuario(new Usuario());
		
		//verificacao
		Assertions.assertThat(usuario.getId()).isNotNull();
		Assertions.assertThat(usuario.getEmail()).isEqualTo(email);
		Assertions.assertThat(usuario.getSenha()).isEqualTo(senha);
		Assertions.assertThat(usuario.getNome()).isEqualTo("nome");
		Assertions.assertThat(usuario.getId()).isEqualTo(1l);
	}
	
	@Test(expected = RegraNegocioException.class)
	public void deveLancarErroQuandoEmailJaExistirNaBaseDeDados() {
		String email = "email@email.com";
		
		Usuario usuario = Usuario.builder().email(email).build();
		
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);

		service.salvarUsuario(usuario);
		
		Mockito.verify(repository, Mockito.never()).save(usuario);
	}
}
