package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.PdfCryptoConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoConverter
import javax.persistence.*

@Entity @Table(name = "SOKNAD")
data class Søknad(@Id
                  @GeneratedValue(strategy = GenerationType.IDENTITY)
                  val id: Long? = null,
                  @Convert(converter = StringValCryptoConverter::class)
                  @Column(name = "soknadJson")
                  val søknadJson: String,
                  @Convert(converter = PdfCryptoConverter::class)
                  @Column(name = "soknadPdf")
                  val søknadPdf: Pdf,
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
