package com.paystream.inventory.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {
        // 첫번째 인자: 재시도 간격(ms), 두번째 인자: 최대 재시도 횟수
        FixedBackOff backOff = new FixedBackOff(1000L, 2); // 3번 실행
        DefaultErrorHandler handler = new DefaultErrorHandler(backOff);

        // 로그 출력 옵션 (선택사항: 재시도 마다 로그를 남길지 설정)
        handler.setLogLevel(KafkaException.Level.WARN);

        return handler;
    }

    /**
     * Kafka 메시지 리스너 컨테이너를 생성하는 팩토리 빈입니다. @KafkaListener 어노테이션이 붙은 메서드들을 관리하며, 다중 스레드(Concurrency)
     * 환경에서 메시지를 처리할 수 있도록 컨테이너를 생성합니다.
     */
    @Bean
    ConcurrentKafkaListenerContainerFactory<Integer, String>
            concurrentKafkaListenerContainerFactory(
                    ConsumerFactory<Integer, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Integer, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    /** JSON 문자열을 Java 객체로 변환해주는 컨버터입니다. Jackson의 JavaTimeModule을 등록하여 LocalDate 파싱 문제를 해결합니다. */
    @Bean
    public RecordMessageConverter converter() {
        ObjectMapper mapper = new ObjectMapper();
        // LocalDate, LocalDateTime 처리를 위한 모듈 등록
        mapper.registerModule(new JavaTimeModule());
        // 날짜를 숫자 배열이 아닌 ISO-8601 문자열(2026-01-18)로 처리
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new StringJsonMessageConverter(mapper);
    }

    /**
     * 애플리케이션에서 실제로 메시지를 보낼 때 사용하는 고수준 추상화 템플릿입니다. ProducerFactory를 사용하여 메시지 전송 로직을 간편하게 처리할 수 있도록
     * 돕습니다.
     */
    @Bean
    public KafkaTemplate<Integer, String> kafkaTemplate(
            ProducerFactory<Integer, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
