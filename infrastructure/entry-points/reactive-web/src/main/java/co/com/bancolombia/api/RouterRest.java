package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.CreateLoanApplicationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/solicitudes",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    consumes = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "create",
                    operation = @Operation(
                            operationId = "create",
                            summary = "Crear solicitud",
                            description = "Registra un nueva solicitud de prestamo en el sistema",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = CreateLoanApplicationRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Solicitud creada"),
                                    @ApiResponse(responseCode = "400", description = "Datos invalidos")
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitudes",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listPendingApplications",
                    operation = @Operation(
                            operationId = "listPendingApplications",
                            summary = "Listar solicitudes para revisión del asesor",
                            description = "Devuelve una lista paginada y filtrable de solicitudes en estados PENDING, REJECTED o MANUAL_REVIEW",
                            parameters = {
                                    @Parameter(name = "page", in = ParameterIn.QUERY, example = "0"),
                                    @Parameter(name = "size", in = ParameterIn.QUERY, example = "20"),
                                    @Parameter(name = "loanType", in = ParameterIn.QUERY, description = "Código de tipo de préstamo"),
                                    @Parameter(name = "documentNumber", in = ParameterIn.QUERY, description = "Documento del solicitante")
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "OK"),
                                    @ApiResponse(responseCode = "401", description = "No autenticado"),
                                    @ApiResponse(responseCode = "403", description = "Acceso denegado")
                            },
                            security = @SecurityRequirement(name = "bearerAuth")
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitudes"), handler::create)
                .andRoute(GET("/api/v1/solicitudes"), handler::listPendingApplications);
    }
}
