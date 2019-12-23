package no.nav.familie.ef.mottak.repository.domain

import no.nav.familie.ef.mottak.encryption.FileCryptoConverter
import no.nav.familie.ef.mottak.encryption.StringValCryptoConverter
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
@Table(name = "SOKNAD")
data class Soknad(@Id
                  val id: String = UUID.randomUUID().toString(),
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
                  @JoinColumn(name = "soknad_id",
                              referencedColumnName = "id",
                              nullable = false)
                  @OrderBy("id asc")
                  val vedlegg: List<Vedlegg> = ArrayList()
)
