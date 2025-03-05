package com.app.backend.global.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.amqp.core.TopicExchange
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
}