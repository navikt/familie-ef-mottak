package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.StringCryptoConverter
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Henvendelse(@Id
                       @GeneratedValue(strategy = GenerationType.IDENTITY)
                       val id: Long,
                       @Convert(converter = StringCryptoConverter::class)
                       val payload: String,
                       @Enumerated(EnumType.STRING)
                       val status: HenvendelseStatus = HenvendelseStatus.UBEHANDLET,
                       val versjon: Int = 1,
                       @Column(name = "opprettet_tidspunkt") val opprettetTidspunkt: LocalDateTime = LocalDateTime.now())
