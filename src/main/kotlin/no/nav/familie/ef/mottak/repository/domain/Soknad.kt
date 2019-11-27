package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.FileCryptoConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoConverter
import javax.persistence.*

@Entity @Table(name = "SOKNAD")
data class Soknad(@Id
                  @GeneratedValue(strategy = GenerationType.IDENTITY)
                  val id: Long? = null,
                  @Convert(converter = StringValCryptoConverter::class)
                  @Column(name = "soknad_json")
                  val søknadJson: String,
                  @Convert(converter = FileCryptoConverter::class)
                  @Column(name = "soknad_pdf")
                  val søknadPdf: Fil,
                  @Column(name = "journalpost_id")
                  val journalpostId: String? = null,
                  val saksnummer: String? = null,
                  val fnr: String,
                  @Column(name = "ny_saksbehandling")
                  val nySaksbehandling: Boolean = false,
                  @OneToMany(fetch = FetchType.EAGER,
                             cascade = [CascadeType.ALL],
                             orphanRemoval = true)
                  @OrderBy("id asc")
                  val vedlegg: List<Vedlegg> = ArrayList()
)
