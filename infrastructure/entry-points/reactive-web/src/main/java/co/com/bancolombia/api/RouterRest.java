package co.com.bancolombia.api;

import co.com.bancolombia.api.dto.*;
import co.com.bancolombia.model.loanapplication.Page;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
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
                            description = "Registra un nueva solicitud de prestamo en el sistema (CUSTOMER)",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = CreateLoanApplicationRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "201",
                                            description = "Solicitud creada",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = LoanApplicationDto.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitudes",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.GET,
                    beanClass = Handler.class,
                    beanMethod = "listApplications",
                    operation = @Operation(
                            operationId = "listApplications",
                            summary = "Listar solicitudes",
                            description = "Devuelve una lista paginada y filtrable de solicitudes (ADVISER)",
                            parameters = {
                                    @Parameter(name = "page", in = ParameterIn.QUERY, example = "0"),
                                    @Parameter(name = "size", in = ParameterIn.QUERY, example = "20"),
                                    @Parameter(name = "statuses", in = ParameterIn.QUERY, description = "Lista de estados separados por coma"),
                                    @Parameter(name = "loanType", in = ParameterIn.QUERY, description = "Código de tipo de préstamo"),
                                    @Parameter(name = "documentNumber", in = ParameterIn.QUERY, description = "Documento del solicitante")
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "OK",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = Page.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
                                    @ApiResponse(responseCode = "403", description = "Acceso denegado", content = @Content)
                            },
                            security = @SecurityRequirement(name = "bearerAuth")
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/solicitud/{id}",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    consumes = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.PUT,
                    beanClass = Handler.class,
                    beanMethod = "changeStatus",
                    operation = @Operation(
                            operationId = "changeStatus",
                            summary = "Actualizar estado de solicitud",
                            description = "Permite a un Asesor cambiar el estado de una solicitud a 'Aprobada' o 'Rechazada'." +
                                    "Al actualizar, se envía un mensaje a SQS para que la Lambda de notificaciones procese " +
                                    "y envíe el correo al solicitante. (ADVISER)",
                            parameters = {
                                    @Parameter(name = "id", in = ParameterIn.PATH, example = "0"),
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = UpdateStatusRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Solicitud actualizada exitosamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = LoanApplicationDto.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Error de validación o petición inválida", content = @Content),
                                    @ApiResponse(responseCode = "403", description = "Acceso denegado. Rol no autorizado", content = @Content),
                                    @ApiResponse(responseCode = "404", description = "Solicitud no encontrada", content = @Content),
                                    @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/calcular-capacidad",
                    produces = {MediaType.APPLICATION_JSON_VALUE},
                    consumes = {MediaType.APPLICATION_JSON_VALUE},
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
                    beanMethod = "calculateCapacityDebt",
                    operation = @Operation(
                            operationId = "calculateCapacityDebt",
                            summary = "Calcular capacidad de endeudamiento",
                            description = "Calcula la capacidad de endeudamiento de un solicitante basado en su salario y deudas actuales. (ALL)",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(
                                            schema = @Schema(implementation = CapacityRequest.class)
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Cálculo realizado exitosamente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    schema = @Schema(implementation = CapacityResponse.class)
                                            )
                                    ),
                                    @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST("/api/v1/solicitudes"), handler::create)
                .andRoute(GET("/api/v1/solicitudes"), handler::listApplications)
                .andRoute(PUT("/api/v1/solicitud/{id}"), handler::changeStatus)
                .andRoute(POST("/api/v1/calcular-capacidad"), handler::calculateCapacityDebt);
    }
}
