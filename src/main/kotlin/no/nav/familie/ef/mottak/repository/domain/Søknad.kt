package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.StringCryptoConverter
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity @Table(name = "SOKNAD")
data class Søknad(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        @Convert(converter = StringCryptoConverter::class)
        val payload: String,
        @Column(name = "journalpost_id")
        val journalpostId: String? = null,
        val saksnummer: String? = null,
        val fnr: String,
        @Column(name = "ny_saksbehandling")
        val nySaksbehandling: Boolean = false,
        @OneToMany(mappedBy = "soknad",
                   fetch = FetchType.EAGER,
                   cascade = [CascadeType.ALL],
                   orphanRemoval = true) @OrderBy("id asc")
        val vedlegg: List<Vedlegg> = ArrayList()
)