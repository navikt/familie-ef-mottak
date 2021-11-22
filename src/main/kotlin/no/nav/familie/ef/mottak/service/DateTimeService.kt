package no.nav.familie.ef.mottak.service

import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DateTimeService {

    fun now(): LocalDateTime = LocalDateTime.now()

}