package no.nav.familie.ef.mottak.config

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cloud.vault.config.databases.VaultDatabaseProperties
import org.springframework.context.annotation.Configuration
import org.springframework.vault.core.lease.SecretLeaseContainer
import org.springframework.vault.core.lease.domain.RequestedSecret.rotating
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent

@Configuration
@ConditionalOnProperty(name = ["spring.cloud.vault.enabled"])
class VaultHikariConfig(private val container: SecretLeaseContainer,
                        private val hikariDataSource: HikariDataSource,
                        private val props: VaultDatabaseProperties) : InitializingBean {

    override fun afterPropertiesSet() {
        val secret = rotating(props.getBackend() + "/creds/" + props.getRole())
        container.addLeaseListener { leaseEvent ->
            if (leaseEvent.getSource() === secret && leaseEvent is SecretLeaseCreatedEvent) {
                LOGGER.info("Rotating creds for path: " + leaseEvent.getSource().getPath())
                val slce = leaseEvent
                val username = slce.getSecrets().get("username").toString()
                val password = slce.getSecrets().get("password").toString()
                hikariDataSource.username = username
                hikariDataSource.password = password
                hikariDataSource.hikariConfigMXBean.setUsername(username)
                hikariDataSource.hikariConfigMXBean.setPassword(password)
                hikariDataSource.hikariPoolMXBean.softEvictConnections()
            }
        }
        container.addRequestedSecret(secret)
    }

    override fun toString(): String {
        return javaClass.simpleName + " [container=" +
               container + ", hikariDataSource=" +
               hikariDataSource + ", props=" +
               props + "]"
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(VaultHikariConfig::class.java)
    }
}
