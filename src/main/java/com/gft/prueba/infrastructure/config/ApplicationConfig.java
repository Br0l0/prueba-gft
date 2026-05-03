package com.gft.prueba.infrastructure.config;

import com.gft.prueba.application.port.in.GetApplicablePriceUseCase;
import com.gft.prueba.application.port.out.LoadPricesPort;
import com.gft.prueba.application.usecase.GetApplicablePriceUseCaseImpl;
import com.gft.prueba.infrastructure.adapter.out.persistence.PriceRepositoryAdapter;
import com.gft.prueba.infrastructure.adapter.out.persistence.SpringDataPriceRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class ApplicationConfig {

    @Bean
    public LoadPricesPort loadPricesPort(SpringDataPriceRepository repository) {
        return new PriceRepositoryAdapter(repository);
    }

    @Bean
    public GetApplicablePriceUseCase getApplicablePriceUseCase(LoadPricesPort loadPricesPort) {
        return new GetApplicablePriceUseCaseImpl(loadPricesPort);
    }
}
