package no.nav.familie.ef.mottak.personopplysninger

data class PdlResponse<T>(
    val data: T,
    val errors: List<PdlError>?,
    val extensions: PdlExtensions?,
) {
    fun harFeil(): Boolean = errors != null && errors.isNotEmpty()

    fun harAdvarsel(): Boolean = !extensions?.warnings.isNullOrEmpty()

    fun errorMessages(): String = errors?.joinToString { it -> it.message } ?: ""
}

data class PdlError(
    val message: String,
    val extensions: PdlErrorExtensions?,
)

data class PdlErrorExtensions(
    val code: String?,
) {
    fun notFound() = code == "not_found"
}

data class PdlExtensions(
    val warnings: List<PdlWarning>?,
)

data class PdlWarning(
    val details: Any?,
    val id: String?,
    val message: String?,
    val query: String?,
)

data class PdlIdent(
    val ident: String,
    val historisk: Boolean,
)

data class PdlIdenter(
    val identer: List<PdlIdent>,
) {
    fun gjeldende(): PdlIdent = this.identer.first { !it.historisk }
}

data class PdlHentIdenter(
    val hentIdenter: PdlIdenter?,
)

data class PdlIdentRequest(
    val variables: PdlIdentRequestVariables,
    val query: String,
)
