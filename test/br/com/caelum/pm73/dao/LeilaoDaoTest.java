package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LeilaoDaoTest {

    private Session session;
    private UsuarioDao usuarioDao;
    private LeilaoDao leilaoDao;

    private Usuario maria;
    private Usuario jose;

    @Before
    public void init() {
        this.session = new CriadorDeSessao().getSession();
        this.usuarioDao = new UsuarioDao(session);
        this.leilaoDao = new LeilaoDao(session);

        this.maria = new Usuario("maria", "maria@maria.com");
        this.jose = new Usuario("jose", "jose@jose.com");

        this.session.beginTransaction();
    }

    @After
    public void close() {
        this.session.getTransaction().rollback();
        this.session.close();
    }

    @Test
    public void deveContarLeiloesNaoEncerrados() {

        Leilao ativo = new Leilao("Geladeira", 1500.0, jose, false);
        Leilao encerrado = new Leilao("xbox", 700.0, jose, false);
        encerrado.encerra();

        usuarioDao.salvar(jose);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        long total = leilaoDao.total();

        assertEquals(1L, total);
    }
}
