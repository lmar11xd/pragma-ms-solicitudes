package co.com.bancolombia.config;

import co.com.bancolombia.model.applicant.gateways.ApplicantPort;
import co.com.bancolombia.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.notification.gateways.NotificationPort;
import co.com.bancolombia.model.security.SecurityPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = UseCasesConfig.class)
class UseCasesConfigTest {

    @MockitoBean
    private LoanTypeRepository loanTypeRepository;
    @MockitoBean
    private LoanApplicationRepository loanApplicationRepository;
    @MockitoBean
    private ApplicantPort applicantPort;
    @MockitoBean
    private SecurityPort securityPort;
    @MockitoBean
    private NotificationPort notificationPort;

    @Test
    void testUseCaseBeansExist(ApplicationContext context) {
        String[] beanNames = context.getBeanDefinitionNames();

        boolean useCaseBeanFound = Arrays.stream(beanNames)
                .anyMatch(name -> name.endsWith("UseCase"));

        assertThat(useCaseBeanFound)
                .as("Debe existir al menos un bean que termine en 'UseCase'")
                .isTrue();
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {

        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}