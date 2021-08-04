package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UsuarioDaoTest {

    private Session session;
    private UsuarioDao usuarioDao;

    @Before
    public void init() {
        this.session = new CriadorDeSessao().getSession();
        this.usuarioDao = new UsuarioDao(session);

        this.session.beginTransaction();
    }

    @After
    public void close() {
        this.session.getTransaction().rollback();
        this.session.close();
    }

    @Test
    public void deveEncontrarPeloNomeEEmailMockado() {

        Usuario novoUsuario = new Usuario("João da Silva", "joao@dasilva.com");
        usuarioDao.salvar(novoUsuario);

        Usuario usuario = usuarioDao.porNomeEEmail("João da Silva", "joao@dasilva.com");

        assertEquals("João da Silva", usuario.getNome());
        assertEquals("joao@dasilva.com", usuario.getEmail());
    }

    @Test
    public void deveRetornarNuloSeNaoAcharUsuarioNoBanco() {

        Usuario usuario = usuarioDao.porNomeEEmail("João da Silva", "joao@dasilva.com");
        assertNull(usuario);
    }
}
