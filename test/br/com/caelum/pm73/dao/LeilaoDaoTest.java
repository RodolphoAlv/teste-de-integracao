package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Leilao;
import br.com.caelum.pm73.dominio.Usuario;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void retornarZeroAoContarApenasLeiloesEncerrados() {

        Leilao leilao1 = new Leilao("Geladeira", 1500.0, jose, false);
        Leilao leilao2 = new Leilao("xbox", 700.0, jose, false);
        leilao1.encerra();
        leilao2.encerra();

        usuarioDao.salvar(jose);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        long total = 0;

        assertEquals(0L, total);
    }

    @Test
    public void deveRetornarApenasLeiloesNovos() {
        Leilao usado = new Leilao("Geladeira", 1500.0, jose, true);
        Leilao novo = new Leilao("xbox", 700.0, jose, false);

        usuarioDao.salvar(jose);
        leilaoDao.salvar(usado);
        leilaoDao.salvar(novo);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertTrue(novos.contains(novo));

    }

    @Test
    public void deveRetornarApenasLeiloesUsados() {
        Leilao antigo = new Leilao("Geladeira", 1500.0, jose, true);
        Calendar semanaPassada = manipularInstante(-8);
        antigo.setDataAbertura(semanaPassada);

        Leilao novo = new Leilao("xbox", 700.0, jose, false);

        usuarioDao.salvar(jose);
        leilaoDao.salvar(antigo);
        leilaoDao.salvar(novo);

        List<Leilao> antigos = leilaoDao.antigos();

        assertEquals(1, antigos.size());
        assertTrue(antigos.contains(antigo));
    }

    @Test
    public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {
        Calendar comecoDoIntervalo = manipularInstante(-10);
        Calendar fimDoIntervalo = Calendar.getInstance();

        Leilao leilao1 = new Leilao("xbox", 700.0, jose, false);
        Calendar dataDoLeilao1 = manipularInstante(-2);
        leilao1.setDataAbertura(dataDoLeilao1);

        Leilao leilao2 = new Leilao("geladeira", 1500.0, jose, false);
        Calendar dataDoLeilao2 = manipularInstante(-20);
        leilao2.setDataAbertura(dataDoLeilao2);

        usuarioDao.salvar(jose);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        assertEquals(1, leiloes.size());
        assertTrue(leiloes.contains(leilao1));
    }

    @Test
    public void naoDeveTrazerLeiloesEncerradosNoPeriodo() {
        Calendar comecoDoIntervalo = manipularInstante(-10);
        Calendar fimDoIntervalo = Calendar.getInstance();

        Leilao leilao1 = new Leilao("xbox", 700.0, jose, false);
        Calendar dataDoLeilao1 = manipularInstante(-2);
        leilao1.setDataAbertura(dataDoLeilao1);
        leilao1.encerra();

        usuarioDao.salvar(jose);
        leilaoDao.salvar(leilao1);

        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        assertEquals(0, leiloes.size());

    }

    private static Calendar manipularInstante(int dias) {
        Calendar instante = Calendar.getInstance();
        instante.add(Calendar.DAY_OF_MONTH, dias);

        return instante;
    }
}
