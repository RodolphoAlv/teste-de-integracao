package br.com.caelum.pm73.dao;

import br.com.caelum.pm73.dominio.Lance;
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

        Leilao ativo = new LeilaoBuilder()
                .comNome("geladeira")
                .comValor(1500.0)
                .comDono(jose)
                .constroi();

        Leilao encerrado = new LeilaoBuilder()
                .comNome("xbox")
                .comValor(700.0)
                .comDono(jose)
                .encerrado()
                .constroi();

        usuarioDao.salvar(jose);
        leilaoDao.salvar(ativo);
        leilaoDao.salvar(encerrado);

        long total = leilaoDao.total();

        assertEquals(1L, total);
    }

    @Test
    public void retornarZeroAoContarApenasLeiloesEncerrados() {

        Leilao leilao1 = new LeilaoBuilder()
                .comNome("geladeira")
                .comValor(1500.0)
                .comDono(jose)
                .encerrado()
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .comNome("xbox")
                .comValor(700.0)
                .comDono(jose)
                .encerrado()
                .constroi();

        usuarioDao.salvar(jose);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        long total = 0;

        assertEquals(0L, total);
    }

    @Test
    public void deveRetornarApenasLeiloesNovos() {
        Leilao usado = new LeilaoBuilder()
                .comNome("geladeira")
                .comValor(1500.0)
                .comDono(jose)
                .usado()
                .constroi();

        Leilao novo = new LeilaoBuilder()
                .comNome("xbox")
                .comValor(700.0)
                .comDono(jose)
                .constroi();

        usuarioDao.salvar(jose);
        leilaoDao.salvar(usado);
        leilaoDao.salvar(novo);

        List<Leilao> novos = leilaoDao.novos();

        assertEquals(1, novos.size());
        assertTrue(novos.contains(novo));

    }

    @Test
    public void deveRetornarApenasLeiloesUsados() {
        Leilao antigo = new LeilaoBuilder()
                .comNome("geladeira")
                .comValor(1500.0)
                .comDono(jose)
                .usado()
                .diasAtras(7)
                .constroi();

        Leilao novo = new LeilaoBuilder()
                .comNome("xbox")
                .comValor(700.0)
                .comDono(jose)
                .constroi();

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

        Leilao leilao1 = new LeilaoBuilder()
                .comNome("xbox")
                .comValor(700.0)
                .comDono(jose)
                .diasAtras(2)
                .constroi();

        Leilao leilao2 = new LeilaoBuilder()
                .comNome("geladeira")
                .comValor(1500.0)
                .comDono(jose)
                .diasAtras(20)
                .constroi();

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

        Leilao leilao1 = new LeilaoBuilder()
                .comNome("xbox")
                .comValor(700.0)
                .comDono(jose)
                .diasAtras(2)
                .encerrado()
                .constroi();

        usuarioDao.salvar(jose);
        leilaoDao.salvar(leilao1);

        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        assertEquals(0, leiloes.size());

    }

    @Test
    public void deveDevolverLeiloesOndeOUsuarioFezPeloMenosUmLance() {

        Leilao leilao1 = new LeilaoBuilder()
                .comNome("geladeira")
                .comValor(1500.0)
                .comDono(jose)
                .constroi();

        Lance lance1 = new Lance(Calendar.getInstance(), jose, 200.0, leilao1);
        leilao1.adicionaLance(lance1);

        Leilao leilao2 = new LeilaoBuilder()
                .comNome("xbox")
                .comValor(700.0)
                .comDono(jose)
                .constroi();

        Lance lance2 = new Lance(Calendar.getInstance(), jose, 300.0, leilao2);
        leilao2.adicionaLance(lance2);

        usuarioDao.salvar(jose);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);


        List<Leilao> leiloesDeJose = leilaoDao.listaLeiloesDoUsuario(jose);

        assertEquals(2, leiloesDeJose.size());
        assertTrue(leiloesDeJose.contains(leilao1));
        assertTrue(leiloesDeJose.contains(leilao2));
    }

    @Test
    public void listaDeLeiloesDeUmUsuarioNaoTemRepeticao() throws Exception {

        Leilao leilao = new LeilaoBuilder()
                .comDono(maria)
                .constroi();

        Lance lance1 = new Lance(Calendar.getInstance(), jose, 200.0, leilao);
        Lance lance2 = new Lance(Calendar.getInstance(), jose, 200.0, leilao);
        leilao.adicionaLance(lance1);
        leilao.adicionaLance(lance2);

        usuarioDao.salvar(maria);
        usuarioDao.salvar(jose);
        leilaoDao.salvar(leilao);

        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(jose);
        assertEquals(1, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }

    private static Calendar manipularInstante(int dias) {
        Calendar instante = Calendar.getInstance();
        instante.add(Calendar.DAY_OF_MONTH, dias);

        return instante;
    }
}
