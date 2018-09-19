package ru.kuchanov.scpreaderapi

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import ru.kuchanov.scpreaderapi.network.ApiClient
import ru.kuchanov.scpreaderapi.network.ModelConverter
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.core.task.TaskExecutor



@SpringBootApplication
@EnableScheduling
@EnableAsync
class Application : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(Application::class.java)
    }

    @Bean
    fun logger() = LoggerFactory.getLogger("application")

//    @Bean
//    fun apiClient() = ApiClient()

    @Bean
    fun modelConverter() = ModelConverter()

    @Bean(name = ["processExecutor"])
    fun workExecutor(): TaskExecutor {
        val threadPoolTaskExecutor = ThreadPoolTaskExecutor()
        threadPoolTaskExecutor.threadNamePrefix = "Async-"
        threadPoolTaskExecutor.corePoolSize = 3
        threadPoolTaskExecutor.maxPoolSize = 3
        threadPoolTaskExecutor.setQueueCapacity(600)
        threadPoolTaskExecutor.afterPropertiesSet()
//        logger.info("ThreadPoolTaskExecutor set")
        return threadPoolTaskExecutor
    }

}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
