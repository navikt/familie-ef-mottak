package no.nav.familie.ef.mottak.repository.domain

import javax.persistence.*

@Entity
@Table
data class Vedlegg(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
//        @ManyToOne @JoinColumn(name = "soknad_id")
//        val soknad: Søknad,
        val data: ByteArray,
        val filnavn: String

)