package no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.repository

import no.nav.familie.ef.mottak.IntegrasjonSpringRunnerTest
import no.nav.familie.ef.mottak.no.nav.familie.ef.mottak.util.søknad
import no.nav.familie.ef.mottak.repository.SøknadRepository
import no.nav.familie.ef.mottak.repository.VedleggRepository
import no.nav.familie.ef.mottak.repository.domain.EncryptedFile
import no.nav.familie.ef.mottak.repository.domain.Vedlegg
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionSystemException
import java.util.UUID

internal class VedleggRepositoryTest : IntegrasjonSpringRunnerTest() {

    @Autowired lateinit var søknadRepository: SøknadRepository

    @Autowired lateinit var vedleggRepository: VedleggRepository

    @Test
    internal fun `findBySøknadId returnerer vedlegg`() {
        val søknadId = søknadRepository.save(søknad()).id
        vedleggRepository.save(Vedlegg(UUID.randomUUID(), søknadId, "navn", "tittel1", EncryptedFile(byteArrayOf(12))))

        assertThat(vedleggRepository.findBySøknadId(søknadId)).hasSize(1)
        assertThat(vedleggRepository.findBySøknadId("finnes ikke")).isEmpty()
    }

    @Test
    internal fun `findTitlerBySøknadId returnerer titler`() {
        val søknadId = søknadRepository.save(søknad()).id
        vedleggRepository.save(Vedlegg(UUID.randomUUID(), søknadId, "navn", "tittel1", EncryptedFile(byteArrayOf(12))))

        val vedlegg = vedleggRepository.findTitlerBySøknadId(søknadId)

        assertThat(vedlegg).hasSize(1)
    }

    @Test
    internal fun `det skal ikke være mulig å oppdatere et vedlegg med ny søknadId`() {
        val vedleggId = UUID.randomUUID()
        val søknadId = søknadRepository.save(søknad()).id
        vedleggRepository.save(Vedlegg(vedleggId, søknadId, "navn", "tittel1", EncryptedFile(byteArrayOf(12))))

        val søknad2Id = søknadRepository.save(søknad()).id
        assertThat(catchThrowable {
            vedleggRepository.save(Vedlegg(vedleggId, søknad2Id, "navn", "tittel1", EncryptedFile(byteArrayOf(12))))
        }).isInstanceOf(TransactionSystemException::class.java)
                .hasRootCauseMessage("Det går ikke å oppdatere vedlegg")

        assertThat(catchThrowable {
            vedleggRepository.saveAll(listOf(Vedlegg(vedleggId, søknad2Id, "navn", "tittel1", EncryptedFile(byteArrayOf(12)))))
        }).isInstanceOf(TransactionSystemException::class.java)
                .hasRootCauseMessage("Det går ikke å oppdatere vedlegg")
    }
}
