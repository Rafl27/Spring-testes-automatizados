package br.com.alura.forum.service

import br.com.alura.forum.exception.NotFoundException
import br.com.alura.forum.mapper.TopicoFormMapper
import br.com.alura.forum.mapper.TopicoViewMapper
import br.com.alura.forum.model.*
import br.com.alura.forum.repository.TopicoRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*
import javax.persistence.EntityManager

class TopicoServiceTest {

    //Dessa forma é criada uma pagina de tópicos mockados
    val topicos = PageImpl(listOf(TopicoTest.build()))
    val paginacao: Pageable = mockk()

    //Assim, sempre que topicoRepository.findByCursoNome, ele será mockado e retornará a lista de topicos
    val topicoRepository: TopicoRepository = mockk{
        every {findByCursoNome(any(), any())} returns topicos
        every {findAll(paginacao)} returns topicos
    }

    val topicoViewMapper : TopicoViewMapper = mockk {
        //Sempre que um topicoViewMapper.map for chamado, será retornado um pre-build.
        // Como estou trabalhando com o objeto topicoViewMapper posso chamar .map direto
        every {map(any()) } returns TopicoViewTest.build()
    }
    val topicoFormMapper : TopicoFormMapper = mockk()

    val topicoService = TopicoService(topicoRepository, topicoViewMapper, topicoFormMapper)

    //No Kotlin, posso nomear funcoes como texto, utilizando backticks
    @Test
    fun `deve listar topicos a partir do nome do curso`() {
        //Será chamado o método listar, e então acontecerão os testes em 3 métodos exatamente 1 vez.
        topicoService.listar("Kotlin avançado", paginacao)
        verify(exactly = 1) { topicoRepository.findByCursoNome(any(), any())}
        verify(exactly = 1) { topicoViewMapper.map(any()) }
        //Nesse caso findAll deve ser chamado 0 vezes pois o curso a ser buscado existe, logo esse método não será chamado.
        verify(exactly = 0) { topicoRepository.findAll(paginacao) }
    }

    @Test
    fun `deve listar todos os topicos quando o nome do curso for nulos`() {
        topicoService.listar(null , paginacao)
        verify(exactly = 0) { topicoRepository.findByCursoNome(any(), any())}
        verify(exactly = 1 ) { topicoViewMapper.map(any()) }
        verify(exactly = 1) { topicoRepository.findAll(paginacao) }
    }

    @Test
    fun `deve retornar not found exception quando topico nao for achado`() {
        //findById sempre ira retornar nulo
        every {topicoRepository.findById(any())} returns Optional.empty()

        //mesmo passando o id 1 que existe, o metodo ira retornar nulo, logo devera retornar a notFoundException
        var atual = assertThrows<NotFoundException> {
            topicoService.buscarPorId(1)
        }

        assertThat(atual.message).isEqualTo("Topico nao encontrado!")
    }

}