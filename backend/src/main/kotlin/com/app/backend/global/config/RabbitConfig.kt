package com.app.backend.global.config

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter
import org.springframework.amqp.support.converter.MessageConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig(
	@Value("\${rabbitmq.queue.name}") private val queueName: String,
	@Value("\${rabbitmq.exchange.name}") private val exchange: String,
	@Value("\${rabbitmq.routing.key}") private val routingKey: String
) {

	@Bean
	fun queue(): Queue = Queue(queueName, true)

	@Bean
	fun exchange(): TopicExchange = TopicExchange(exchange)

	@Bean
	fun binding(queue: Queue, exchange: TopicExchange): Binding =
		BindingBuilder.bind(queue).to(exchange).with(routingKey)

	@Bean
	fun rabbitTemplate(connectionFactory: ConnectionFactory): RabbitTemplate {
		return RabbitTemplate(connectionFactory).apply {
			messageConverter = jackson2JsonMessageConverter()
			setExchange(exchange)
		}
	}

	@Bean
	fun jackson2JsonMessageConverter(): MessageConverter = Jackson2JsonMessageConverter()

	@Bean
	fun rabbitListenerContainerFactory(connectionFactory: ConnectionFactory): SimpleRabbitListenerContainerFactory {
		return SimpleRabbitListenerContainerFactory().apply {
			setConnectionFactory(connectionFactory)

			// 동시 Consumer 수 조정 (최소 3개, 최대 10개)
			setConcurrentConsumers(3)		// 디폴트 값 -> 1
			setMaxConcurrentConsumers(20)	// 디폴트 값 -> null 설정하지 않으면 제한 없음.

			// 메시지 미리 가져오는 개수 (한 번에 5개씩만 가져오기)
			setPrefetchCount(5)				// 디폴트 값 -> 1

			// 메시지 변환 설정
			setMessageConverter(jackson2JsonMessageConverter())

			// ACK 모드 설정 (AUTO or MANUAL)
			setAcknowledgeMode(AcknowledgeMode.AUTO)	// 디폴트 값
		}
	}
}