package no.nav.familie.ef.mottak.repository.domain

import org.springframework.data.annotation.Id

data class Søknad(@Id val id: Long,
                  val soknad_json: String)

