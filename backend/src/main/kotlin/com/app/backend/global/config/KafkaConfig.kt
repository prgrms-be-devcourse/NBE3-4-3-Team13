package com.app.backend.global.config

import com.app.backend.domain.notification.dto.NotificationMessage
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class KafkaConfig {
    // 어드민 - Producer
    @Bean
    fun producerFactory(): ProducerFactory<String, NotificationMessage> {
        // JSON Deserializer 설정
        val jsonDeserializer = JsonDeserializer(
            NotificationMessage::class.java, false
        )
        jsonDeserializer.addTrustedPackages("*") // 모든 패키지 신뢰 설정

        // Producer 속성 설정
        val props = mutableMapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )

        return DefaultKafkaProducerFactory(props)
    }

    // 회원 - Consumer
    // KafkaTemplate 설정 - Producer가 메시지를 보내는데 사용
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, NotificationMessage> {
        return KafkaTemplate(producerFactory())
    }

    @Bean
    fun consumerFactory(): ConsumerFactory<String, NotificationMessage> {
        val props: MutableMap<String, Any> = HashMap()
        // Kafka 서버 주소
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"

        // Consumer 그룹 ID
        props[ConsumerConfig.GROUP_ID_CONFIG] = "notification-group"
        // Deserializer 설정
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        // 오프셋 설정 - 새로운 Consumer가 시작할 때 가장 최근 메시지부터 읽기
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "latest"
        // NotificationMessage 클래스의 패키지를 신뢰하도록 설정
        props[JsonDeserializer.TRUSTED_PACKAGES] = "com.app.backend.domain.notification.dto"
        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    fun kafkaListenerContainerFactory() = ConcurrentKafkaListenerContainerFactory<String, NotificationMessage>().apply {
        consumerFactory = consumerFactory()
    }
}